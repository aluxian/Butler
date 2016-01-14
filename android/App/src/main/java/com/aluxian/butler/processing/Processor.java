package com.aluxian.butler.processing;

import com.aluxian.butler.MainActivity;
import com.aluxian.butler.main.MainActivityDelegate;

import java.util.concurrent.Callable;

/**
 * Base class for the other processors
 */
public abstract class Processor extends MainActivityDelegate implements Callable<ProcessorOutput> {

    /** The input to be processed */
    private UserInput userInput;

    public void setUserInput(UserInput userInput) {
        this.userInput = userInput;
    }

    public Processor(MainActivity mainActivity) {
        super(mainActivity);
    }

    @Override
    public ProcessorOutput call() throws Exception {
        return process(userInput);
    }

    /**
     * Process the user input and return a conversation list item
     *
     * @param userInput User input
     */
    public abstract ProcessorOutput process(UserInput userInput);

}
