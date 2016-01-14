package com.aluxian.butler.processing;

import com.aluxian.butler.database.PatternModel;
import com.aluxian.butler.recycler.ConversationItem;

import java.util.Arrays;
import java.util.List;

/**
 * Object that is returned by processors upon successful processing of UserInput
 */
public final class ProcessorOutput {

    /** Output items */
    public final List<ConversationItem> items;

    /** The expectedInput follow-up questions from the user */
    public final List<? extends PatternModel> expectedInput;

    /** Whether the assistant should start listening again after speaking this item */
    public final boolean promptForInput;

    public ProcessorOutput(List<? extends PatternModel> expectedInput, boolean promptForInput,
                           ConversationItem... items) {
        this.items = Arrays.asList(items);
        this.expectedInput = expectedInput;
        this.promptForInput = promptForInput;
    }

    public ProcessorOutput(ConversationItem... items) {
        this(null, false, items);
    }

}
