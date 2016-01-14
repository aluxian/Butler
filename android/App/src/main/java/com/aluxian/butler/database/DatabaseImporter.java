package com.aluxian.butler.database;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;

import com.activeandroid.ActiveAndroid;
import com.aluxian.butler.MainActivity;
import com.aluxian.butler.R;
import com.aluxian.butler.database.models.AssistantCommand;
import com.aluxian.butler.database.models.ChitChatAnswer;
import com.aluxian.butler.database.models.ChitChatQuestion;
import com.aluxian.butler.database.models.FreebaseQuestionMap;
import com.aluxian.butler.database.xml.XmlAssistant;
import com.aluxian.butler.database.xml.XmlChitChat;
import com.aluxian.butler.database.xml.XmlFreebase;
import com.aluxian.butler.main.MainActivityDelegate;
import com.aluxian.butler.utils.Logger;
import com.aluxian.butler.utils.Preferences;

import org.simpleframework.xml.core.Persister;

import java.util.List;

/**
 * Imports data from XML into the database
 */
public class DatabaseImporter extends MainActivityDelegate {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") private static final Logger LOG = new Logger(DatabaseImporter.class);

    public DatabaseImporter(MainActivity mainActivity) {
        super(mainActivity);
    }

    /**
     * Execute the import data task
     */
    public void importData() {
        new ImportDatabaseDataTask().execute();
    }

    /**
     * AsyncTask to import data from XML files into the database.
     */
    private class ImportDatabaseDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences defaultPreferences = mainActivity.preferences.getDefaultPreferences();

            if (!defaultPreferences.getBoolean(Preferences.DEF_PREF_DATA_LOADED, false)) {
                importDatabaseData(mainActivity.getResources());
                defaultPreferences.edit().putBoolean(Preferences.DEF_PREF_DATA_LOADED, true).apply();
            }

            return null;
        }

    }

    /**
     * Import data into the database.
     */
    private static void importDatabaseData(Resources res) {
        ActiveAndroid.beginTransaction();
        LOG.d("Importing data");

        try {
            // Read FreebaseQuestionMaps
            XmlFreebase xmlFreebase = new Persister().read(XmlFreebase.class, res.openRawResource(R.raw.data_freebase));

            for (XmlFreebase.Map map : xmlFreebase.getMaps()) {
                new FreebaseQuestionMap(
                        map.getType(),
                        map.getPattern(),
                        map.getQuery(),
                        map.getFilter(),
                        map.getMinScore()
                ).save();
            }

            // Read ChitChatQuestions and ChitChatAnswers
            XmlChitChat xmlChitChat = new Persister().read(XmlChitChat.class, res.openRawResource(R.raw.data_chitchat));
            importQuestions(null, xmlChitChat.getQuestions());

            // Read AssistantCommands
            XmlAssistant xmlAssistant = new Persister().read(XmlAssistant.class,
                    res.openRawResource(R.raw.data_assistant));
            importCommands(null, xmlAssistant.getCommands());

            ActiveAndroid.setTransactionSuccessful();
        } catch (Exception e) {
            LOG.e(e);
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    /**
     * Imports the given list of questions into the database. Runs recursively.
     *
     * @param questions List of questions to import
     */
    private static void importQuestions(ChitChatAnswer parentAnswer, List<XmlChitChat.Question> questions) {
        if (questions == null) {
            return;
        }

        for (XmlChitChat.Question question : questions) {
            ChitChatQuestion chitChatQuestion = new ChitChatQuestion(
                    question.getType(),
                    question.getPattern(),
                    parentAnswer);
            chitChatQuestion.save();

            for (XmlChitChat.Answer answer : question.getAnswers()) {
                ChitChatAnswer chitChatAnswer = new ChitChatAnswer(
                        answer.getText(),
                        answer.getScore(),
                        answer.getConditions(),
                        answer.getSentiment(),
                        answer.promptForInput(),
                        chitChatQuestion);

                chitChatAnswer.save();
                importQuestions(chitChatAnswer, answer.getQuestions());
            }
        }
    }

    /**
     * Imports the given list of commands into the database. Runs recursively.
     *
     * @param commands List of commands to import
     */
    private static void importCommands(AssistantCommand parentCommand, List<XmlAssistant.Command> commands) {
        if (commands == null) {
            return;
        }

        for (XmlAssistant.Command command : commands) {
            AssistantCommand assistantCommand = new AssistantCommand(
                    command.getType(),
                    command.getPattern(),
                    command.getFunction(),
                    command.getParameters(),
                    parentCommand);

            assistantCommand.save();
            importCommands(assistantCommand, command.getCommands());
        }
    }

}
