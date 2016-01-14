package com.aluxian.butler.recycler.items;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aluxian.butler.R;
import com.aluxian.butler.recycler.ConversationItem;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Conversation item that displays a result from a Freebase API search
 */
public class FreebaseItem implements ConversationItem {

    /** The item's title */
    private String title;

    /** The item's description */
    private String description;

    /** The url of the item's image */
    private String imageUrl;

    /** The first sentence of the description, spoken by the assistant */
    private String speakableText;

    public FreebaseItem(String title, String description, String imageUrl, String speakableText) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.speakableText = speakableText;
    }

    @Override
    public void bindViewHolder(ConversationItem.ViewHolder baseViewHolder) {
        ViewHolder viewHolder = (ViewHolder) baseViewHolder;
        viewHolder.titleView.setText(title);
        viewHolder.descriptionView.setText(description);

        // Load the image
        //Picasso.with(viewHolder.context).load(imageUrl).into(viewHolder.imageView);
    }

    @Override
    public String getSpeakable() {
        return speakableText;
    }

    @Override
    public boolean isSilent() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * ViewHolder pattern class
     */
    public static class ViewHolder extends ConversationItem.ViewHolder {

        /** The item's title */
        @InjectView(R.id.title) public TextView titleView;

        /** The item's description */
        @InjectView(R.id.description) public TextView descriptionView;

        /** The image of the item */
        @InjectView(R.id.image) public ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

    }

}
