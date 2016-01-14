package com.aluxian.butler.main.queue;

import android.os.Handler;

import com.aluxian.butler.MainActivity;
import com.aluxian.butler.recycler.items.EmptyItem;
import com.aluxian.butler.utils.Logger;
import com.aluxian.butler.utils.Utils;

/**
 * Queue responsible for displaying recycler items
 */
public class ItemQueue extends ProcessingQueue<ItemQueueEntry> {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") private static final Logger LOG = new Logger(ItemQueue.class);

    /** Whether queue processing if paused */
    private boolean paused;

    public ItemQueue(MainActivity mainActivity) {
        super(mainActivity);
    }

    @Override
    public void add(ItemQueueEntry itemQueueEntry) {
        LOG.d("Adding entry", Utils.gson().toJson(itemQueueEntry));

        if (!(itemQueueEntry.item instanceof EmptyItem)) {
            super.add(itemQueueEntry);
        }
    }

    /**
     * Called after an item has been spoken
     */
    public void finishedSpeaking() {
        LOG.d("finishedSpeaking()");

        paused = false;
        display();
    }

    @Override
    protected void display() {
        LOG.d("display()", paused, queue.size());

        if (paused || queue.size() == 0) {
            return;
        }

        ItemQueueEntry itemQueueEntry = queue.poll();
        mainActivity.conversationRecyclerAdapter.addItem(itemQueueEntry.item);
        LOG.d("Processing entry", Utils.gson().toJson(itemQueueEntry));

        if (!itemQueueEntry.item.isSilent()) {
            LOG.d("Speak item");

            paused = true;
            mainActivity.textToSpeech.speakItem(itemQueueEntry.item, itemQueueEntry.listenAfterSpeech);
        } else if (itemQueueEntry.listenAfterSpeech) {
            LOG.d("Start listening");

            mainActivity.speechRecognizer.startListening();
        } else {
            LOG.d("Postpone");

            paused = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LOG.d("Running postponed");

                            paused = false;
                            display();
                        }
                    });
                }
            }, 250);
        }
    }

}
