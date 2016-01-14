package com.aluxian.butler.recycler.items;

import com.aluxian.butler.recycler.ConversationItem;

/**
 * Pseudo ConversationItem, used to indicate that the assistant shouldn't say anything
 */
public class EmptyItem implements ConversationItem {

    @Override
    public void bindViewHolder(ViewHolder viewHolder) {}

    @Override
    public String getSpeakable() {
        return null;
    }

    @Override
    public boolean isSilent() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

}
