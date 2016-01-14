package com.aluxian.butler.processing.processors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;

import com.aluxian.butler.MainActivity;
import com.aluxian.butler.apis.corenlp.CoreNlpService;
import com.aluxian.butler.database.PatternModel;
import com.aluxian.butler.database.models.AssistantCommand;
import com.aluxian.butler.database.models.GcmMessage;
import com.aluxian.butler.database.pojos.Contact;
import com.aluxian.butler.gcm.AndroidMessage;
import com.aluxian.butler.processing.PatternMatcher;
import com.aluxian.butler.processing.ProcessingHistory;
import com.aluxian.butler.processing.ProcessingUtils;
import com.aluxian.butler.processing.Processor;
import com.aluxian.butler.processing.ProcessorOutput;
import com.aluxian.butler.processing.UserInput;
import com.aluxian.butler.recycler.items.ContactPickerItem;
import com.aluxian.butler.recycler.items.TextItem;
import com.aluxian.butler.utils.LogCatReportException;
import com.aluxian.butler.utils.Logger;
import com.aluxian.butler.utils.Utils;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;

/**
 * Processes user commands
 */
public class CommandProcessor extends Processor {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") private static final Logger LOG = new Logger(CommandProcessor.class);

    public CommandProcessor(MainActivity mainActivity) {
        super(mainActivity);
    }

    @Override
    public ProcessorOutput process(UserInput userInput) {
        LOG.d("Processing input", userInput);

        List<AssistantCommand> defaultQuestions = AssistantCommand.getDefaultQuestions();
        ProcessorOutput lastProcessorOutput = ProcessingHistory.getLastProcessorOutput();

        if (lastProcessorOutput != null) {
            LOG.d("Searching expected questions");

            List<? extends PatternModel> expectedInput = lastProcessorOutput.expectedInput;

            if (expectedInput != null && expectedInput.size() > 0 && expectedInput.get(0) instanceof AssistantCommand) {
                @SuppressWarnings("unchecked")
                ProcessorOutput processorOutput = searchMatch(userInput, (List<AssistantCommand>) expectedInput);

                if (processorOutput != null) {
                    return processorOutput;
                }
            }
        }

        LOG.d("Searching default questions");
        return searchMatch(userInput, defaultQuestions);
    }

    /**
     * Searches the given list for an item with a matching pattern
     *
     * @param userInput         User input
     * @param assistantCommands List of commands to search
     * @return A {link ProcessorOutput} object (if found)
     */
    private ProcessorOutput searchMatch(UserInput userInput, List<AssistantCommand> assistantCommands) {
        if (assistantCommands == null) {
            return null;
        }

        // Search for a ChitChatQuestion with a matching pattern
        return new PatternMatcher<AssistantCommand>(userInput) {
            @Override
            protected ProcessorOutput matchesRegex(AssistantCommand assistantCommand) {
                List<String> parameters = new ArrayList<>();

                if (assistantCommand.parameters != null) {
                    for (String parameter : assistantCommand.parameters) {
                        parameters.add(regexMatcher.replaceAll(parameter));
                    }
                }

                ProcessorOutput processorOutput = doCommand(assistantCommand, parameters);

                if (processorOutput != null) {
                    return processorOutput;
                }

                return null;
            }

            @Override
            protected ProcessorOutput matchesSemgrex(AssistantCommand assistantCommand) {
                List<String> parameters = new ArrayList<>();

                if (assistantCommand.parameters != null) {
                    for (String parameter : assistantCommand.parameters) {
                        parameters.add(ProcessingUtils.interpolateValuesFromSemGraph(parameter, semgrexMatcher));
                    }
                }

                ProcessorOutput processorOutput = doCommand(assistantCommand, parameters);

                if (processorOutput != null) {
                    return processorOutput;
                }

                return null;
            }
        }.match(assistantCommands);
    }

    /**
     * Run the function of the given AssistantCommand with the provided parameters
     *
     * @param assistantCommand AssistantCommand
     * @param parameters       Parameters to use
     * @return A ProcessorOutput if the command is successful
     */
    @SuppressLint("CommitPrefEdits")
    private ProcessorOutput doCommand(AssistantCommand assistantCommand, List<String> parameters) {
        switch (assistantCommand.function) {
            case SAY: {
                String text = parameters.get(0);
                return new ProcessorOutput(new TextItem(text, true));
            }

            case REMEMBER: {
                String key = parameters.get(0).toLowerCase();
                String value = parameters.get(1);

                SharedPreferences.Editor prefsEditor = mainActivity.preferences.getAssistantPreferences().edit();
                String message = "Ok";

                if ("NULL".equals(value)) {
                    prefsEditor.remove(key.toLowerCase());
                } else {
                    prefsEditor.putString(key.toLowerCase(), value);

                    switch (key.toUpperCase()) {
                        case "USER_FIRSTNAME": {
                            // Send it with Crashlytics reports
                            Crashlytics.setUserName(value);

                            // Find the gender
                            String gender = CoreNlpService.api.getGenders(value).get(0).get(0);
                            if (gender != null) {
                                prefsEditor.putBoolean("user_gender_" + gender.toLowerCase(), true);
                            }

                            // Register in the cloud
                            mainActivity.gcmManager.registerInTheCloud();

                            message = "Nice to meet you, " + value;
                            break;
                        }
                    }
                }

                prefsEditor.apply();
                return new ProcessorOutput(new TextItem(message, true));
            }

            case FRIEND_REQUEST: {
                String friendName = parameters.get(0);
                LOG.d("Sending friend request to " + friendName);

                GcmMessage gcmMessage = new GcmMessage(
                        AndroidMessage.RequestType.FRIEND_REQUEST,
                        GcmMessage.MessageDirection.SENT);
                gcmMessage.save();

                mainActivity.gcmManager.send(new AndroidMessage(
                        gcmMessage.getId().toString(),
                        mainActivity.utils.getUserId(),
                        mainActivity.gcmManager.getRegistrationId(),
                        mainActivity.gcmManager.getAuthToken(),
                        gcmMessage.type,
                        null,
                        new AndroidMessage.FriendRequest(
                                mainActivity.preferences.getUserName(),
                                friendName
                        ),
                        null,
                        null,
                        null
                ), gcmMessage);

                return new ProcessorOutput(new TextItem("Ok, I sent " + friendName + " a request", true));
            }

            case FRIEND_REQUEST_RESPOND: {
                String messageId = parameters.get(0);
                String friendName = parameters.get(1);
                boolean status = Boolean.valueOf(parameters.get(2));

                LOG.d("Friend request response", messageId, status);

                mainActivity.gcmManager.send(new AndroidMessage(
                        messageId,
                        mainActivity.utils.getUserId(),
                        mainActivity.gcmManager.getRegistrationId(),
                        mainActivity.gcmManager.getAuthToken(),
                        AndroidMessage.RequestType.FRIEND_REQUEST_RESPONSE,
                        null,
                        null,
                        new AndroidMessage.FriendRequestResponse(friendName, status),
                        null,
                        null
                ), null);

                return new ProcessorOutput(new TextItem("Ok, I'll let them know", true));
            }

            case CALL: {
                final String contactName = Utils.capFirstLetter(parameters.get(0).trim());

                // @source for the regex: http://stackoverflow.com/a/20971688/1133344
                if (contactName.matches("^(?:(?:\\(?(?:00|\\+)([1-4]\\d\\d|[1-9]\\d?)\\)?)?[\\-\\. \\\\/]?)?((?:\\" +
                        "(?\\d+\\)?[\\-\\. \\\\/]?)*)(?:[\\-\\. \\\\/]?(?:#|ext\\.?|extension|x)[\\-\\. \\\\/]?(\\d+)" +
                        ")?$")) {

                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mainActivity.utils.callNumber(contactName);
                        }
                    }, 2500);

                    return new ProcessorOutput(new TextItem("Calling " + contactName, true));
                }

                final List<Contact> contacts = mainActivity.utils.searchContactByName(contactName);

                if (contacts.size() == 0) {
                    return new ProcessorOutput(new TextItem("I could not find " + contactName +
                            " in your address book", true));
                }

                if (contacts.size() == 1) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mainActivity.utils.callNumber(contacts.get(0).number);
                        }
                    }, 2500);

                    return new ProcessorOutput(new TextItem("Calling " + contactName, true));
                }

                return new ProcessorOutput(
                        new TextItem("Which " + contactName + "?", true),
                        new ContactPickerItem(mainActivity, contacts, ContactPickerItem.OnClickAction.CALL));
            }

            case RINGER_MODE: {
                String mode = parameters.get(0);
                AudioManager audioManager = (AudioManager) mainActivity.getSystemService(Context.AUDIO_SERVICE);

                LOG.d("RINGER_MODE()", mode);

                switch (mode.toUpperCase()) {
                    case "NORMAL":
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        break;
                    case "SILENT":
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        break;
                    case "VIBRATE":
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                        break;
                }

                return new ProcessorOutput(new TextItem("Done", true));
            }

            case TEXT: {
                String contactName = Utils.capFirstLetter(parameters.get(0).trim());
                List<Contact> contacts = mainActivity.utils.searchContactByName(contactName);

                if (contacts.size() == 0) {
                    return new ProcessorOutput(new TextItem("I could not find " + contactName +
                            " in your address book", true));
                }

                if (contacts.size() == 1) {
                    Contact contact = contacts.get(0);

                    List<AssistantCommand> expectedInput = assistantCommand.getCommands();
                    expectedInput.get(0).parameters = new String[]{
                            expectedInput.get(0).parameters[0],
                            contact.number
                    };

                    return new ProcessorOutput(
                            expectedInput,
                            true,
                            new TextItem("What would you like to text " + contact.displayName + "?", true));
                }

                return new ProcessorOutput(
                        new TextItem("Which " + contactName + "?", true),
                        new ContactPickerItem(mainActivity, contacts, ContactPickerItem.OnClickAction.TEXT));
            }

            case TEXT_MSG: {
                String message = parameters.get(0).trim();
                String contactNumber = parameters.get(1);

                SmsManager.getDefault().sendTextMessage(contactNumber, null, message, null, null);
                return new ProcessorOutput(new TextItem("Message sent", true));
            }

            case TELL: {
                String friendName = parameters.get(0).trim();
                LOG.d("Tell message to " + friendName);

                List<AssistantCommand> expectedInput = assistantCommand.getCommands();
                expectedInput.get(0).parameters = new String[]{
                        expectedInput.get(0).parameters[0],
                        friendName
                };

                return new ProcessorOutput(
                        expectedInput,
                        true,
                        new TextItem("What would you like to tell " + friendName + "?", true));
            }

            case TELL_MSG: {
                String message = parameters.get(0).trim();
                String friendName = parameters.get(1);

                GcmMessage gcmMessage = new GcmMessage(
                        AndroidMessage.RequestType.TELL_MESSAGE,
                        GcmMessage.MessageDirection.SENT);
                gcmMessage.save();

                mainActivity.gcmManager.send(new AndroidMessage(
                        gcmMessage.getId().toString(),
                        mainActivity.utils.getUserId(),
                        mainActivity.gcmManager.getRegistrationId(),
                        mainActivity.gcmManager.getAuthToken(),
                        gcmMessage.type,
                        null,
                        null,
                        null,
                        new AndroidMessage.TellMessage(
                                mainActivity.preferences.getUserName(),
                                friendName,
                                message
                        ),
                        null
                ), gcmMessage);

                return new ProcessorOutput(new TextItem("Ok, message sent", true));
            }

            case SEND_LOGCAT: {
                LOG.e(new LogCatReportException());
                return new ProcessorOutput(new TextItem("They're on their way", true));
            }

//            case TOGGLE_LOGGING: {
//                boolean enabled = "ON".equals(parameters.get(0).toUpperCase());
//
//                mainActivity.preferences.getDefaultPreferences()
//                        .edit().putBoolean(Preferences.DEF_PREF_LOGGING_ENABLED, enabled).apply();
//
//                return new ProcessorOutput(new TextItem("Logging is now " + (enabled ? "enabled" : "disabled"),
// true));
//            }
        }

        return null;
    }

}
