package com.aluxian.butler.processing.processors;

import com.aluxian.butler.processing.Processor;
import com.aluxian.butler.processing.ProcessorOutput;
import com.aluxian.butler.processing.UserInput;
import com.aluxian.butler.recycler.items.TextItem;

/**
 * The last processor that is run by the {@link com.aluxian.butler.processing.MainProcessor}. It responds with a (kind
 * of) error answer.
 */
public class FallbackProcessor extends Processor {

    public FallbackProcessor() {
        super(null);
    }

    @Override
    public ProcessorOutput process(UserInput userInput) {
        return new ProcessorOutput(new TextItem("I don't know what you mean", true));
    }

}
