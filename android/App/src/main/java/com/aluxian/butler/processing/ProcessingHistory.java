package com.aluxian.butler.processing;

import com.aluxian.butler.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores past UserInput and assistant responses
 */
public class ProcessingHistory {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") private static final Logger LOG = new Logger(ProcessingHistory.class);

    /** Stores previous {@link UserInput}'s */
    public static List<UserInput> pastUserInput = new ArrayList<>();

    /** Stores previous {@link ProcessorOutput}'s */
    public static List<ProcessorOutput> pastProcessorOutput = new ArrayList<>();

//    /**
//     * @return Whether an answer from the assistant is expectedInput for the next UserInput
//     */
//    public static boolean singleAnswerExpected() {
//        if (pastProcessorOutput.size() == 0) {
//            return false;
//        }
//
//        ProcessorOutput lastProcessorOutput = getLastProcessorOutput();
//        boolean expectedInput = lastProcessorOutput.promptForInput &&
//                (lastProcessorOutput.expectedQuestions == null || lastProcessorOutput.expectedQuestions.size() == 0);
//
//        if (expectedInput) {
//            pastProcessorOutput.add(new ProcessorOutput(new EmptyItem()));
//        }
//
//        return expectedInput;
//    }

//    /**
//     * @return The latest item of the {link pastUserInput} list
//     */
//    public static UserInput getLastUserInput() {
//        LOG.d("getLastUserInput()", pastUserInput.size());
//
//        if (pastUserInput.size() == 0) {
//            return null;
//        }
//
//        return pastUserInput.get(pastUserInput.size() - 1);
//    }

    /**
     * @return The latest item of the {link pastProcessorOutput} list
     */
    public static ProcessorOutput getLastProcessorOutput() {
        LOG.d("getLastProcessorOutput()", pastProcessorOutput.size());

        if (pastProcessorOutput.size() == 0) {
            return null;
        }

        return pastProcessorOutput.get(pastProcessorOutput.size() - 1);
    }

}
