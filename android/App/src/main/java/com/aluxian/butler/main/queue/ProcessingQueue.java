package com.aluxian.butler.main.queue;

import com.aluxian.butler.MainActivity;
import com.aluxian.butler.utils.Logger;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Base class for item queues
 */
public abstract class ProcessingQueue<T> {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") private static final Logger LOG = new Logger(ProcessingQueue.class);

    /** The activity to queue for */
    protected final MainActivity mainActivity;

    /** List to hold the queued objects */
    protected final Queue<T> queue = new LinkedList<>();

    protected ProcessingQueue(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    /**
     * Queue a new item
     *
     * @param item Item to queue
     */
    public void add(T item) {
        queue.add(item);
        display();
    }

    /**
     * Gradually add items into the adapter
     */
    protected abstract void display();

}
