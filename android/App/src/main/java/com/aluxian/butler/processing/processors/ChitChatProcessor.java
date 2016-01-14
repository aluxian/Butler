package com.aluxian.butler.processing.processors;

import com.aluxian.butler.MainActivity;
import com.aluxian.butler.database.PatternModel;
import com.aluxian.butler.database.models.ChitChatAnswer;
import com.aluxian.butler.database.models.ChitChatQuestion;
import com.aluxian.butler.database.models.GcmMessage;
import com.aluxian.butler.gcm.AndroidMessage;
import com.aluxian.butler.processing.PatternMatcher;
import com.aluxian.butler.processing.ProcessingHistory;
import com.aluxian.butler.processing.ProcessingUtils;
import com.aluxian.butler.processing.Processor;
import com.aluxian.butler.processing.ProcessorOutput;
import com.aluxian.butler.processing.UserInput;
import com.aluxian.butler.recycler.items.TextItem;
import com.aluxian.butler.utils.Logger;
import com.aluxian.butler.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processor for chit chat input
 */
public class ChitChatProcessor extends Processor {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") private static final Logger LOG = new Logger(ChitChatProcessor.class);

    public ChitChatProcessor(MainActivity mainActivity) {
        super(mainActivity);
    }

    @Override
    public ProcessorOutput process(UserInput userInput) {
        LOG.d("Processing input", userInput);

        List<ChitChatQuestion> defaultQuestions = ChitChatQuestion.getDefaultQuestions();
        ProcessorOutput lastProcessorOutput = ProcessingHistory.getLastProcessorOutput();

        if (lastProcessorOutput != null) {
            LOG.d("Searching expected questions");

            List<? extends PatternModel> expectedInput = lastProcessorOutput.expectedInput;

            if (expectedInput != null && expectedInput.size() > 0 && expectedInput.get(0) instanceof ChitChatQuestion) {
                @SuppressWarnings("unchecked")
                ProcessorOutput processorOutput = searchMatch(userInput, (List<ChitChatQuestion>) expectedInput);

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
     * @param chitChatQuestions List of questions to search
     * @return A {link ProcessorOutput} object (if found)
     */
    private ProcessorOutput searchMatch(UserInput userInput, List<ChitChatQuestion> chitChatQuestions) {
        if (chitChatQuestions == null) {
            return null;
        }

        // Search for a ChitChatQuestion with a matching pattern
        return new PatternMatcher<ChitChatQuestion>(userInput) {
            @Override
            protected ProcessorOutput matchesRegex(ChitChatQuestion chitChatQuestion) {
                LOG.d("Found REGEX match", chitChatQuestion.getPattern());

                ChitChatAnswer answer = findAnswer(userInput, chitChatQuestion);

                if (answer != null) {
                    String preparedAnswer = prepareAnswer(answer.text);
                    preparedAnswer = regexMatcher.replaceAll(preparedAnswer);

                    return new ProcessorOutput(
                            answer.getQuestions(),
                            answer.promptForInput,
                            new TextItem(preparedAnswer, true)
                    );
                }

                return null;
            }

            @Override
            protected ProcessorOutput matchesSemgrex(ChitChatQuestion chitChatQuestion) {
                ChitChatAnswer answer = findAnswer(userInput, chitChatQuestion);

                if (answer != null) {
                    LOG.d("Found SEMGREX match", chitChatQuestion.getPattern());

                    String preparedAnswer = prepareAnswer(answer.text);
                    preparedAnswer = ProcessingUtils.interpolateValuesFromSemGraph(preparedAnswer, semgrexMatcher);

                    return new ProcessorOutput(
                            answer.getQuestions(),
                            answer.promptForInput,
                            new TextItem(preparedAnswer, true)
                    );
                }

                return null;
            }
        }.match(chitChatQuestions);
    }

    /**
     * Randomly choose an answer to the given question
     *
     * @param userInput        User input
     * @param chitChatQuestion The ChitChatQuestion to answer
     * @return The chosen answer
     */
    private ChitChatAnswer findAnswer(UserInput userInput, ChitChatQuestion chitChatQuestion) {
        List<ChitChatAnswer> chitChatAnswers = chitChatQuestion.getAnswers();
        Pattern pattern = Pattern.compile("__\\w+__");

        // Remove ineligible answers
        for (int i = 0; i < chitChatAnswers.size(); i++) {
            ChitChatAnswer chitChatAnswer = chitChatAnswers.get(i);

            // Check conditions
            if (chitChatAnswer.conditions != null) {
                String expression = interpolateConditions(chitChatAnswer.conditions);

                if (!ProcessingUtils.logicalEval(expression)) {
                    LOG.d("Unmet condition", chitChatAnswer.conditions);
                    chitChatAnswers.remove(chitChatAnswer);
                    i--;
                    continue;
                }
            }

            // Check variables
            Matcher matcher = pattern.matcher(chitChatAnswer.text);

            while (matcher.find()) {
                String match = matcher.group();
                match = match.substring(2, match.length() - 2);

                if (!isConditionMet(match)) {
                    LOG.d("Unmet condition", match);
                    chitChatAnswers.remove(chitChatAnswer);
                    i--;
                    break;
                }
            }
        }

        // Remove answers whose sentiment doesn't match
        for (int i = 0; i < chitChatAnswers.size(); i++) {
            String requiredSentiment = chitChatAnswers.get(i).sentiment;

            if (requiredSentiment == null) {
                continue;
            }

            String[] requiredSentiments = requiredSentiment.split("\\s*\\|\\s*");
            String inputSentiment = userInput.getSentiment().toLowerCase();
            boolean atLeastOneMatch = false;

            for (String sentiment : requiredSentiments) {
                if (inputSentiment.equals(sentiment.replace(' ', '_').toLowerCase())) {
                    atLeastOneMatch = true;
                    break;
                }
            }

            if (!atLeastOneMatch) {
                chitChatAnswers.remove(i);
            }
        }

        if (chitChatAnswers.size() == 0) {
            return null;
        }

        // Calculate the total score
        int totalScore = 0;
        for (ChitChatAnswer chitChatAnswer : chitChatAnswers) {
            totalScore += chitChatAnswer.score;
        }

        // Choose an answer randomly
        int rand = new Random().nextInt(totalScore) + 1;
        int iterTotal = 0;

        for (ChitChatAnswer chitChatAnswer : chitChatAnswers) {
            iterTotal += chitChatAnswer.score;

            if (rand <= iterTotal) {
                // TODO: Consider an alternative implementation of event tracking
                // Found an answer for the Hello/Hi/Hey question, now send it to the server for prediction
                if ("{tag:UH;lemma:/(hello|hi|hey)/}".equals(chitChatAnswer.question.getPattern())) {
                    // TODO: Make gcmManager.send() easier to use

                    GcmMessage gcmMessage = new GcmMessage(
                            AndroidMessage.RequestType.TRACK_EVENT,
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
                            null,
                            new AndroidMessage.TrackEvent(
                                    String.valueOf(System.currentTimeMillis()), userInput.toString())
                    ), gcmMessage);
                }

                return chitChatAnswer;
            }
        }

        return null;
    }

    /**
     * Replaces conditions with their truth values
     *
     * @param conditions String with conditions
     * @return Expression string
     */
    private String interpolateConditions(String conditions) {
        Matcher matcher = Pattern.compile("\\w+").matcher(conditions);

        while (matcher.find()) {
            String match = matcher.group();
            conditions = conditions.replaceAll(match, String.valueOf(isConditionMet(match)));
        }

        return conditions;
    }

    /**
     * Makes changes to the answer: <ul> <li>(a|b|..|n) groups are replaced with either a, b, ... or n</li> <li>{@code
     * __varname__}'s are interpolated</li> </ul>
     *
     * @param answer Text answer to change
     * @return A speakable answer
     */
    private String prepareAnswer(String answer) {
        // Replace OR groups
        Matcher groupsMatcher = Pattern.compile("\\(([\\w\\s'!?,.]*\\|)+[\\w\\s'!?,.]+\\)").matcher(answer);

        while (groupsMatcher.find()) {
            String match = groupsMatcher.group();
            String[] options = match.substring(1, match.length() - 1).split("\\|");
            String option = options[new Random().nextInt(options.length)];
            answer = answer.replace(match, option);
        }

        // Interpolate variables
        Matcher varsMatcher = Pattern.compile("__\\w+__").matcher(answer);

        while (varsMatcher.find()) {
            String match = varsMatcher.group();
            String varName = match.substring(2, match.length() - 2);

            answer = answer.replace(match, getVarValue(varName));
        }

        return answer;
    }

    /**
     * Checks if a given condition is met
     *
     * @param condition Condition to check
     * @return Whether it's met or not
     */
    private boolean isConditionMet(String condition) {
        switch (condition.toUpperCase()) {
            // Time
            case "TIME_MORNING": {
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                return hour >= 5 && hour < 12;
            }
            case "TIME_AFTERNOON": {
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                return hour >= 12 && hour < 18;
            }
            case "TIME_EVENING": {
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                return hour >= 18 && hour < 24;
            }
            case "TIME_NIGHT": {
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                return hour >= 21 || hour < 7;
            }
        }

        return mainActivity.preferences.getAssistantPreferences().contains(condition.toLowerCase())
                || getVarValue(condition.toLowerCase()) != null;
    }

    /**
     * @param varName Variable name
     * @return The value of the variable
     */
    private String getVarValue(String varName) {
        switch (varName.toUpperCase()) {
            // Time
            case "TIME_SHORT": {
                return new SimpleDateFormat("h:mm a").format(new Date());
            }
            case "TIME_LONG": {
                return Utils.getLongExplicitTime();
            }
        }

        return mainActivity.preferences.getAssistantPreferences().getString(varName.toLowerCase(), null);
    }

}
