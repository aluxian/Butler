package com.aluxian.butler.main.queue;

import com.activeandroid.Model;
import com.aluxian.butler.recycler.ConversationItem;

import java.util.List;

/**
 * Used in the ItemQueue in MainActivity
 */
public final class ItemQueueEntry {

    public final ConversationItem item;

    public final boolean listenAfterSpeech;

    public final List<? extends Model> expectedInput;

    public ItemQueueEntry(ConversationItem item, boolean listenAfterSpeech, List<? extends Model> expectedInput) {
        this.item = item;
        this.listenAfterSpeech = listenAfterSpeech;
        this.expectedInput = expectedInput;
    }

}
