package com.aluxian.butler.recycler.items;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aluxian.butler.R;
import com.aluxian.butler.recycler.ConversationItem;
import com.aluxian.butler.utils.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Conversation item that displays text
 */
public class TextItem implements ConversationItem {

    /** The text displayed in the item */
    private String text;

    /** The text should be aligned to the right if it belongs to the assistant instead of the user */
    private boolean alignRight;

    /** Whether the text of the item should not be spoken */
    private boolean isSilent;

//    /** The dots for the loading animation */
//    private List<View> dots;
//
//    /** The animators used to animate the dots */
//    private ObjectAnimator[] animators = new ObjectAnimator[3];
//
//    /** Whether the animation should be started on viewholder bind */
//    private boolean shouldAnimate;

    public TextItem(String text, boolean alignRight, boolean isSilent) {
        setText(text);
        this.alignRight = alignRight;
        this.isSilent = isSilent;
    }

    public TextItem(String text, boolean alignRight) {
        this(text, alignRight, false);
    }

    @Override
    public void bindViewHolder(ConversationItem.ViewHolder baseViewHolder) {
        ViewHolder viewHolder = (ViewHolder) baseViewHolder;
        viewHolder.textView.setText(text);
        //this.dots = viewHolder.dots;

        // Set alignment
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewHolder.cardView.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, alignRight ? 1 : 0);
        viewHolder.cardView.setLayoutParams(layoutParams);

        /*// Start animation
        if (shouldAnimate) {
            startAnimation();
            shouldAnimate = false;
        }*/
    }

    @Override
    public String getSpeakable() {
        return text;
    }

    @Override
    public boolean isSilent() {
        return isSilent;
    }

    @Override
    public boolean isEmpty() {
        return text.length() == 0 || "...".equals(text);
    }

//    /**
//     * Setter
//     *
//     * @param shouldAnimate New value
//     */
//    public void setShouldAnimate(boolean shouldAnimate) {
//        this.shouldAnimate = shouldAnimate;
//    }
//
//    /**
//     * Starts the loading animation
//     */
//    private void startAnimation() {
//        // Show views
//        for (View dot : dots) {
//            dot.setVisibility(View.VISIBLE);
//        }
//
//        // Animate them
//        for (int i = 0; i < dots.size(); i++) {
//            PropertyValuesHolder translationY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -60.0f);
//            animators[i] = ObjectAnimator.ofPropertyValuesHolder(dots.get(i), translationY);
//            animators[i].setRepeatCount(-1);
//            animators[i].setRepeatMode(ValueAnimator.REVERSE);
//            animators[i].setDuration(500);
//            animators[i].setStartDelay(i * 100);
//            animators[i].start();
//        }
//    }
//
//    /**
//     * Stops the loading animation
//     */
//    public void stopAnimation() {
//        // Stop the animations
//        for (ObjectAnimator animator : animators) {
//            animator.cancel();
//        }
//
//        // Hide the dots
//        for (View dot : dots) {
//            dot.setVisibility(View.GONE);
//        }
//    }

    /**
     * Updates the text of the item. It also changes the first letter to uppercase.
     *
     * @param newText The new text to display
     */
    public void setText(String newText) {
        this.text = Utils.capFirstLetter(newText.trim());
    }

    /**
     * @return The item's text
     */
    public String getText() {
        return text;
    }

    /**
     * ViewHolder pattern class
     */
    public static class ViewHolder extends ConversationItem.ViewHolder {

        /** The displayed text */
        @InjectView(R.id.text) public TextView textView;

        /** The CardView wrapper */
        @InjectView(R.id.card_view) public CardView cardView;

        ///** The dot views for the loading animation */
        //@InjectViews({R.id.dot1, R.id.dot2, R.id.dot3}) public List<View> dots;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }

    }

}
