package com.aluxian.butler.main.queue;

import android.os.Handler;

import com.aluxian.butler.MainActivity;
import com.aluxian.butler.processing.ProcessingHistory;
import com.aluxian.butler.processing.ProcessorOutput;

/**
 * Queue responsible for displaying ProcessorOutput objects
 */
public class ProcessorOutputQueue extends ProcessingQueue<ProcessorOutput> {

    public ProcessorOutputQueue(MainActivity mainActivity) {
        super(mainActivity);
    }

    @Override
    public void add(ProcessorOutput item) {
        super.add(item);
        ProcessingHistory.pastProcessorOutput.add(item);
    }

    @Override
    public void display() {
        // Ensure the UI and the TTS engine have been loaded
        if (!mainActivity.uiReady || !mainActivity.ttsReady) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            display();
                        }
                    });
                }
            }, 50);
            return;
        }

        while (queue.size() > 0) {
            ProcessorOutput processorOutput = queue.poll();

            for (int i = 0; i < processorOutput.items.size(); i++) {
                mainActivity.itemQueue.add(new ItemQueueEntry(
                        processorOutput.items.get(i),
                        processorOutput.promptForInput && i == processorOutput.items.size() - 1,
                        processorOutput.expectedInput
                ));
            }
        }
    }

}
