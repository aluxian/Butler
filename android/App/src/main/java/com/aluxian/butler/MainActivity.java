package com.aluxian.butler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ViewFlipper;

import com.aluxian.butler.database.DatabaseImporter;
import com.aluxian.butler.database.PatternModel;
import com.aluxian.butler.database.enums.FunctionType;
import com.aluxian.butler.database.enums.PatternType;
import com.aluxian.butler.database.models.AssistantCommand;
import com.aluxian.butler.database.models.GcmMessage;
import com.aluxian.butler.gcm.AndroidMessage;
import com.aluxian.butler.gcm.GcmIntentService;
import com.aluxian.butler.gcm.GcmManager;
import com.aluxian.butler.gcm.GcmServerMessage;
import com.aluxian.butler.main.ProcessInputTask;
import com.aluxian.butler.main.SpeechRecognizer;
import com.aluxian.butler.main.TextToSpeech;
import com.aluxian.butler.main.queue.ItemQueue;
import com.aluxian.butler.main.queue.ProcessorOutputQueue;
import com.aluxian.butler.processing.MainProcessor;
import com.aluxian.butler.processing.ProcessorOutput;
import com.aluxian.butler.recycler.ConversationItem;
import com.aluxian.butler.recycler.ConversationRecyclerAdapter;
import com.aluxian.butler.recycler.items.TextItem;
import com.aluxian.butler.utils.Constants;
import com.aluxian.butler.utils.Logger;
import com.aluxian.butler.utils.Preferences;
import com.aluxian.butler.utils.Utils;
import com.crashlytics.android.Crashlytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.newrelic.agent.android.NewRelic;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnLongClick;

import static android.graphics.PorterDuff.Mode;

/**
 * The main activity of the assistant
 */
public class MainActivity extends Activity {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") private static final Logger LOG = new Logger(MainActivity.class);

    /** SavedInstanceBundle: The list of items in the adapter */
    public static final String KEY_ITEMS = "list_items";

    /** SavedInstanceBundle: Whether the assistant is processing input */
    public static final String KEY_IS_PROCESSING = "is_processing";

    /** SavedInstanceBundle: The last input of the user */
    public static final String KEY_USER_INPUT = "user_input";

    /** Intent Extras key for the presentation (magic trick) */
    public static final String KEY_INTERACTIVITY = "interactivity";

    /** Action for the GCM broadcasts intent filter */
    public static final String FILTER_ACTION_GCM = "gcm_broadcast";

    /** Flag to know whether SpeechRecognizer is recognising or not */
    public boolean isListening;

    /** Flag to know whether the TTS engine is speaking or not */
    public boolean isSpeaking;

    /** The view of the microphone shadow (volume level indicator) */
    @InjectView(R.id.microphone_volume) public View micShadowView;

    /** Contains the microphone button and shapes */
    @InjectView(R.id.microphone_container) View mMicButtonContainer;

    /** Microphone button view */
    @InjectView(R.id.microphone_button) Button mMicButtonView;

    /** The microphone shape on the mic button */
    @InjectView(R.id.microphone_shape) View mMicShapeView;

    /** ViewFlipper to switch between the microphone and the speaker shapes */
    @InjectView(R.id.button_shape_flipper) ViewFlipper mButtonShapeFlipper;

    /** The conversation ListView */
    @InjectView(R.id.conversation_recycler) RecyclerView mConversationRecyclerView;

    /** Adapter instance for the list view */
    public ConversationRecyclerAdapter conversationRecyclerAdapter;

    /** An item which serves as a loading indicator */
    public TextItem temporaryConversationItem;

    /** The most recent ProcessorOutput object */
    public ProcessorOutput processorOutput;

    /** Instance of the task which processes user input */
    public ProcessInputTask processInputTask;

    /** Overscroll glow drawable, used to re-color */
    private Drawable mOverscrollGlow;

    /** Overscroll edge drawable, used to re-color */
    private Drawable mOverscrollEdge;

    /** TTS engine instance */
    public TextToSpeech textToSpeech;

    /** SpeechRecognizer instance */
    public SpeechRecognizer speechRecognizer;

    /** MixpanelAPI instance */
    public MixpanelAPI mixpanel;

    /** Queue for ProcessorOutput objects */
    public ProcessorOutputQueue processorOutputQueue;

    /** Queue for items */
    public ItemQueue itemQueue;

    /** SharedPreferences */
    public Preferences preferences;

    /** The MainProcessor of the activity */
    public MainProcessor mainProcessor;

    /** GcmManager instance */
    public GcmManager gcmManager;

    /** Utils object with a context already set */
    public Utils utils;

    /** Flag to know whether the UI has been loaded and items can be added to the list */
    public boolean uiReady;

    /** Flag to know whether the TTS engine is ready and items can be added to the list */
    public boolean ttsReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        // Initialise Utils
        utils = new Utils(this);

        // Crashlytics error reporting
        Crashlytics.start(getApplicationContext());
        Crashlytics.setUserIdentifier(utils.getUserId());

        // Initialise objects
        itemQueue = new ItemQueue(this);
        textToSpeech = new TextToSpeech(this);
        speechRecognizer = new SpeechRecognizer(this);
        processorOutputQueue = new ProcessorOutputQueue(this);
        preferences = new Preferences(this);
        mainProcessor = new MainProcessor(this);
        gcmManager = new GcmManager(this);

        // Set up StrictMode

        if (BuildConfig.STRICT_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .setClassInstanceLimit(MainActivity.class, 100)
                    .detectAll().penaltyLog().build());
        }

        // Hack the overscroll colour
        changeOverscrollColour();

        // Animate the microphone button
        mMicButtonContainer.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMicButtonContainer.animate()
                        .setInterpolator(new OvershootInterpolator())
                        .translationY(0.0f)
                        .translationX(0.0f)
                        .alpha(1.0f)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                uiReady = true;
                            }
                        });
            }
        }, 200);

        // Set the right color on the mic shape
        mMicShapeView.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimaryLight), Mode.MULTIPLY);

        // Set up the conversation recycler
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mConversationRecyclerView.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setStackFromEnd(true);

        conversationRecyclerAdapter = new ConversationRecyclerAdapter(linearLayoutManager);
        mConversationRecyclerView.setAdapter(conversationRecyclerAdapter);
        mConversationRecyclerView.setHasFixedSize(true);

        // Reload previous items
        boolean previousItemsReloaded = false;

        if (savedInstanceState != null) {
            Serializable serializedItems = savedInstanceState.getSerializable(KEY_ITEMS);

            if (serializedItems != null) {
                @SuppressWarnings("unchecked")
                ArrayList<ConversationItem> oldItems = (ArrayList<ConversationItem>) serializedItems;
                conversationRecyclerAdapter.getItemsList().addAll(oldItems);
                previousItemsReloaded = true;
            }

            boolean wasProcessing = savedInstanceState.getBoolean(KEY_IS_PROCESSING);
            String input = savedInstanceState.getString(KEY_USER_INPUT);

            if (wasProcessing && input != null) {
                processInputTask = new ProcessInputTask(this);
                processInputTask.execute(input);
            }
        }

        if (!previousItemsReloaded) {
            if (getIntent().getStringExtra(KEY_INTERACTIVITY) != null) {
                processorOutputQueue.add(new ProcessorOutput(
                        new TextItem("Hey, Alexander", true)));
                processorOutputQueue.add(new ProcessorOutput(
                        new TextItem("Don't forget to rehearse for the presentation", true)));
            } else if (preferences.getDefaultPreferences().getBoolean(Preferences.AST_PREF_INITIAL_SETUP, false)) {
                // Check intent
                String json = getIntent().getStringExtra(GcmIntentService.KEY_SERVER_MESSAGE);

                if (json != null) {
                    handleServerMessage(json);
                } else {
                    // Show a welcoming item
                    processorOutputQueue.add(new ProcessorOutput(new TextItem("What can I help you with?", true)));
                }
            } else {
                // Start intro
                List<String> parameters = new ArrayList<>();
                parameters.add("USER_FIRSTNAME");
                parameters.add("$1");

                List<PatternModel> expectedInput = new ArrayList<>();

                expectedInput.add(new AssistantCommand(
                        PatternType.REGEX,
                        "my name is (.+)",
                        FunctionType.REMEMBER,
                        parameters,
                        null
                ));

                expectedInput.add(new AssistantCommand(
                        PatternType.REGEX,
                        "it's (.+)",
                        FunctionType.REMEMBER,
                        parameters,
                        null
                ));

                expectedInput.add(new AssistantCommand(
                        PatternType.REGEX,
                        "(.+)",
                        FunctionType.REMEMBER,
                        parameters,
                        null
                ));

                processorOutputQueue.add(new ProcessorOutput(
                        expectedInput,
                        true,
                        new TextItem("Hello, there!", true),
                        new TextItem("My name is Butler", true),
                        new TextItem("From now on I am going to be your personal assistant", true),
                        new TextItem("What is your name?", true)
                ));

                preferences.getDefaultPreferences().edit().putBoolean(Preferences.AST_PREF_INITIAL_SETUP, true).apply();
            }

            // Check for Play Services
            if (utils.checkPlayServices()) {
                String registrationId = gcmManager.getRegistrationId();

                if (TextUtils.isEmpty(registrationId)) {
                    gcmManager.registerInBackground();
                }
            } else {
                // Notify the user
                if (conversationRecyclerAdapter.getItemCount() == 0) {
                    processorOutputQueue.add(new ProcessorOutput(new TextItem("I couldn't connect to Google Play " +
                            "Services. Some commands will be unavailable.", true)
                    ));
                }
            }
        }

        // Import database data (in the background)
        new DatabaseImporter(this).importData();

        // Init Mixpanel
        mixpanel = MixpanelAPI.getInstance(getApplicationContext(), Constants.MIXPANEL_TOKEN);
        mixpanel.identify(utils.getUserId());
        mixpanel.getPeople().identify(utils.getUserId());

        try {
            JSONObject props = new JSONObject();
            props.put("Debug", BuildConfig.DEBUG);
            mixpanel.registerSuperProperties(props);
        } catch (JSONException e) {
            LOG.e(e);
        }

        // Do some of the work in the background to prevent blocking the thread
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                // New Relic monitoring
                NewRelic.withApplicationToken(Constants.NEW_RELIC_KEY).start(getApplication());
                return null;
            }
        }.execute();
    }

    /**
     * Change the colour of the overscroll effect. This is not future-proof.
     *
     * @source http://evendanan.net/android/branding/2013/12/09/branding-edge-effect/
     */
    public void changeOverscrollColour() {
        if (mOverscrollGlow == null || mOverscrollEdge == null) {
            final Resources res = getResources();
            mOverscrollGlow = res.getDrawable(res.getIdentifier("overscroll_glow", "drawable", "android"));
            mOverscrollEdge = res.getDrawable(res.getIdentifier("overscroll_edge", "drawable", "android"));
        }

        mOverscrollGlow.setColorFilter(getResources().getColor(R.color.colorPrimaryDark), Mode.SRC_IN);
        mOverscrollEdge.setColorFilter(getResources().getColor(R.color.colorPrimaryDark), Mode.SRC_IN);
    }

    /**
     * Update the UI when speech recognition starts
     */
    public void startedListening() {
        LOG.d("startedListening()");

        textToSpeech.stop();
        isListening = true;

        // Change the microphone's color
        mMicButtonView.setBackgroundResource(R.drawable.mic_listening_transition);
        ((TransitionDrawable) mMicButtonView.getBackground()).startTransition(30);
        mMicShapeView.getBackground().clearColorFilter();
    }

    /**
     * Update the UI when speech recognition stops
     */
    public void stoppedListening() {
        LOG.d("stoppedListening()");
        isListening = false;

        // Change the microphone's color
        mMicShapeView.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimaryLight), Mode.MULTIPLY);
        ((TransitionDrawable) mMicButtonView.getBackground()).reverseTransition(100);

        // Make sure the shadow isn't visible anymore
        if (micShadowView.getScaleX() != 0.67f) {
            micShadowView.animate()
                    .setInterpolator(new DecelerateInterpolator())
                    .setDuration(100)
                    .scaleX(0.67f)
                    .scaleY(0.67f)
                    .start();
        }
    }

    /**
     * Update the UI when speaking starts
     */
    public void startedSpeaking() {
        LOG.d("startedSpeaking()");
        isSpeaking = true;

        // Change the microphone's color
        mMicButtonView.setBackgroundResource(R.drawable.mic_speaking_transition);
        ((TransitionDrawable) mMicButtonView.getBackground()).startTransition(100);
        mButtonShapeFlipper.setDisplayedChild(1);
    }

    /**
     * Update the UI when speaking stops
     */
    public void stoppedSpeaking() {
        LOG.d("stoppedSpeaking()");
        isSpeaking = false;

        // Change the microphone's color
        ((TransitionDrawable) mMicButtonView.getBackground()).reverseTransition(100);
        mButtonShapeFlipper.setDisplayedChild(0);
    }

    /**
     * Handle clicks on the microphone button
     */
    @OnClick(R.id.microphone_button)
    void microphoneButtonOnClick() {
        if (isListening) {
            speechRecognizer.stopListening();
        } else if (isSpeaking) {
            textToSpeech.stop();
        } else {
            if (processInputTask != null && processInputTask.getStatus() != AsyncTask.Status.FINISHED) {
                processInputTask.cancel(true);

                // Remove the temporary item
                //temporaryConversationItem.stopAnimation();
                conversationRecyclerAdapter.removeItem(temporaryConversationItem);
                temporaryConversationItem = null;
            }

            speechRecognizer.startListening();
            startedListening();
        }
    }

    /**
     * Handle long clicks on the microphone button
     */
    @OnLongClick(R.id.microphone_button)
    boolean microphoneButtonLongClick() {
        final EditText editText = new EditText(getApplicationContext());

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Manual input")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String input = editText.getText().toString();
                        if (input.trim().length() == 0) {
                            return;
                        }

                        // Add item
                        conversationRecyclerAdapter.addItem(new TextItem(input, false));

                        // Process input
                        processInputTask = new ProcessInputTask(MainActivity.this);
                        processInputTask.execute(input);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {}
                })
                .create();

        alertDialog.setView(editText, 16, 32, 16, 16);
        alertDialog.show();

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LOG.d("onNewIntent() Extra: " + intent.getExtras());

        if (!Intent.ACTION_MAIN.equals(intent.getAction())) {
            String json = intent.getStringExtra(GcmIntentService.KEY_SERVER_MESSAGE);

            if (json != null) {
                handleServerMessage(json);
            } else {
                // Start listening
                speechRecognizer.startListening();
                startedListening();
            }
        }
    }

    /**
     * Receive GCM messages
     */
    private BroadcastReceiver mGcmBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            preferences.getDefaultPreferences().edit()
                    .putBoolean(Preferences.DEF_PREF_GCM_BROADCAST_RECEIVED, true).apply();

            // Extract the json
            String json = intent.getStringExtra(GcmIntentService.KEY_SERVER_MESSAGE);

            if (json != null) {
                handleServerMessage(json);
            }
        }
    };

    /**
     * Handles received GcmServerMessage objects
     *
     * @param json The JSON of the GcmServerMessage
     */
    private void handleServerMessage(String json) {
        final GcmServerMessage serverMessage = Utils.gson().fromJson(json, GcmServerMessage.class);

        switch (serverMessage.type) {
            case FRIEND_REQUEST: {
                // Params to accept
                List<String> parametersAccept = new ArrayList<>();
                parametersAccept.add(serverMessage.messageId);
                parametersAccept.add(String.valueOf(serverMessage.content));
                parametersAccept.add("true");

                // Params to decline
                List<String> parametersDecline = new ArrayList<>();
                parametersDecline.add(serverMessage.messageId);
                parametersDecline.add(String.valueOf(serverMessage.content));
                parametersDecline.add("false");

                // Create the expected input
                List<PatternModel> expectedInput = new ArrayList<>();

                expectedInput.add(new AssistantCommand(
                        PatternType.REGEX,
                        "yes",
                        FunctionType.FRIEND_REQUEST_RESPOND,
                        parametersAccept,
                        null
                ));

                expectedInput.add(new AssistantCommand(
                        PatternType.REGEX,
                        "no",
                        FunctionType.FRIEND_REQUEST_RESPOND,
                        parametersDecline,
                        null
                ));

                // Queue the ProcessorOutput
                processorOutputQueue.add(new ProcessorOutput(
                        expectedInput,
                        true,
                        new TextItem(serverMessage.content + " has sent you a friend request", true),
                        new TextItem("Do you accept?", true)
                ));

                break;
            }

            case FRIEND_REQUEST_NOT_FOUND: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        GcmMessage gcmMessage = GcmMessage.load(GcmMessage.class,
                                Long.valueOf(serverMessage.messageId));
                        AndroidMessage.FriendRequest friendRequest = Utils.gson()
                                .fromJson(gcmMessage.data, AndroidMessage.FriendRequest.class);
                        final String userName = friendRequest.targetOwnerName;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                processorOutputQueue.add(new ProcessorOutput(
                                        new TextItem("I'm sorry, I couldn't find " + userName, true)
                                ));
                            }
                        });
                    }
                }).start();

                break;
            }

            case FRIEND_REQUEST_RESPONSE: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        GcmMessage gcmMessage = GcmMessage.load(GcmMessage.class,
                                Long.valueOf(serverMessage.messageId));
                        AndroidMessage.FriendRequest friendRequest = Utils.gson()
                                .fromJson(gcmMessage.data, AndroidMessage.FriendRequest.class);

                        final String userName = friendRequest.targetOwnerName;
                        final String status = Boolean.valueOf(String.valueOf(serverMessage.content))
                                ? "accepted" : "declined";

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                processorOutputQueue.add(new ProcessorOutput(
                                        new TextItem(userName + " has " + status + " your friend request", true)
                                ));
                            }
                        });
                    }
                }).start();

                break;
            }

            case TELL_MESSAGE: {
                String[] splitContent = String.valueOf(serverMessage.content).split("\\|");
                String friendName = splitContent[0];
                String message = splitContent[1];

                processorOutputQueue.add(new ProcessorOutput(
                        new TextItem("You have received a message from " + friendName, true),
                        new TextItem(message, true)
                ));

                break;
            }

            case PREDICTION: {
                String message = String.valueOf(serverMessage.content);
                processorOutputQueue.add(new ProcessorOutput(new TextItem(message, true)));

                break;
            }
        }
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mGcmBroadcastReceiver, new IntentFilter(FILTER_ACTION_GCM));
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mGcmBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (isListening || isSpeaking) {
            microphoneButtonOnClick();
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        ArrayList<ConversationItem> items = conversationRecyclerAdapter.getItemsList();

        // Remove the last item if it's just a temporary one
        if (items.size() > 0 && items.get(items.size() - 1).isEmpty()) {
            items.remove(items.size() - 1);
        }

        outState.putSerializable(KEY_ITEMS, items);
        outState.putBoolean(KEY_IS_PROCESSING, processInputTask != null &&
                processInputTask.getStatus() != AsyncTask.Status.FINISHED);
        outState.putString(KEY_USER_INPUT, processInputTask != null ? processInputTask.getInput() : null);
    }

    @Override
    protected void onDestroy() {
        // Flush events
        mixpanel.flush();

        // Shut down the TTS engine
        textToSpeech.shutdown();

        // Notify the Preferences object
        preferences.onDestroy();

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) {
            menu.add("Create Notification");

        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ("Create Notification".equals(item.getTitle())) {
            // Display a notification instead
            Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
            notificationIntent.putExtra(KEY_INTERACTIVITY, "true");

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                    .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("Butler wants to speak with you");

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context
                    .NOTIFICATION_SERVICE);
            notificationManager.notify(20, builder.build());
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}
