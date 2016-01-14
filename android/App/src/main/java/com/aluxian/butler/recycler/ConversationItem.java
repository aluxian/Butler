package com.aluxian.butler.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.io.Serializable;

/**
 * Interface for recycler list items
 */
public interface ConversationItem extends Serializable {

    /**
     * Binds the values of the item to their views
     */
    public void bindViewHolder(ViewHolder viewHolder);

    /**
     * @return A string which Butler should say when this item is displayed
     */
    public String getSpeakable();

    /**
     * @return Whether the item should not be spoken
     */
    public boolean isSilent();

    /**
     * @return Whether the item is empty (thus, just a temporary item)
     */
    public boolean isEmpty();

    /**
     * ViewHolder base class
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

    }

}
