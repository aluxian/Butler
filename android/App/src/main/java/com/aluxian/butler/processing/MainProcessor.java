package com.aluxian.butler.processing;

import com.aluxian.butler.MainActivity;
import com.aluxian.butler.processing.processors.ChitChatProcessor;
import com.aluxian.butler.processing.processors.CommandProcessor;
import com.aluxian.butler.processing.processors.FallbackProcessor;
import com.aluxian.butler.processing.processors.FreebaseProcessor;
import com.aluxian.butler.utils.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Process user input.
 * <p/>
 * When input is processed, it will pass through all the processors, in parallel, until one of them is successful.
 */
public class MainProcessor {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") private static final Logger LOG = new Logger(MainProcessor.class);

    /** Set of available processors */
    private Set<Processor> processors = new HashSet<>();

    public MainProcessor(MainActivity mainActivity) {
        processors.add(new CommandProcessor(mainActivity));
        processors.add(new FreebaseProcessor(mainActivity));
        processors.add(new ChitChatProcessor(mainActivity));
    }

    /**
     * Process input from the user. All the processors are started in at once and run in parallel. The first non-null
     * output is returned.
     *
     * @param userInput Input to process
     * @return A {@link ProcessorOutput} object
     */
    public ProcessorOutput process(UserInput userInput) {
        ProcessingHistory.pastUserInput.add(userInput);
        ProcessorOutput result = null;

        ExecutorService executorService = Executors.newFixedThreadPool(processors.size());
        Set<Future<ProcessorOutput>> futureSet = new HashSet<>();

        // Run processors
        for (Processor processor : processors) {
            processor.setUserInput(userInput);
            futureSet.add(executorService.submit(processor));
        }

        try {
            // Check for their outputs
            while (true) {
                int doneFutures = 0;

                // Check the output of each future
                for (Future<ProcessorOutput> future : futureSet) {
                    if (future.isDone()) {
                        result = future.get();

                        // Found a valid output
                        if (result != null) {
                            break;
                        }

                        doneFutures++;
                    }
                }

                // Found a valid output
                if (result != null) {
                    break;
                }

                // Check if execution has finished
                if (doneFutures == futureSet.size()) {
                    break;
                }

                // Wait a bit before checking again
                Thread.sleep(50);
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.e(e);
        } finally {
            executorService.shutdown();
        }

        // Default input handler
        if (result == null) {
            result = new FallbackProcessor().process(null);
        }

        return result;
    }

}
