package com.aluxian.butler.main;

import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.view.animation.DecelerateInterpolator;

import com.aluxian.butler.MainActivity;
import com.aluxian.butler.recycler.items.TextItem;

import java.util.ArrayList;

/**
 * Handle speech recognition events
 */
public class RecognitionListener extends MainActivityDelegate implements android.speech.RecognitionListener {

    public RecognitionListener(MainActivity mainActivity) {
        super(mainActivity);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        mainActivity.temporaryConversationItem = new TextItem("...", false);
        mainActivity.conversationRecyclerAdapter.addItem(mainActivity.temporaryConversationItem);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        if (mainActivity.temporaryConversationItem == null) {
            // This should not happen, but better be sure
            return;
        }

        // Update the temporary item with the partial results
        ArrayList<String> resultsList = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        ArrayList<String> unstableList = partialResults.getStringArrayList("android.speech.extra.UNSTABLE_TEXT");

        String result = resultsList.get(0);
        String unstable = unstableList != null ? unstableList.get(0) : "";

        // The stable results are shown, the unstable ones are replaced by three dots
        if (result.length() > 0) {
            mainActivity.temporaryConversationItem.setText(resultsList.get(0) + (unstable.length() > 0 ? " ..." : ""));
            mainActivity.conversationRecyclerAdapter.changedItem(mainActivity.temporaryConversationItem);
        }
    }

    @Override
    public void onResults(Bundle results) {
        mainActivity.stoppedListening();

        if (mainActivity.temporaryConversationItem == null) {
            // An error occurred instead
            return;
        }

        // Update the temporary item with the final results
        ArrayList<String> resultsList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String result = resultsList.get(0);

        mainActivity.temporaryConversationItem.setText(result);
        mainActivity.conversationRecyclerAdapter.changedItem(mainActivity.temporaryConversationItem);

        // Process the result
        mainActivity.processInputTask = new ProcessInputTask(mainActivity);
        mainActivity.processInputTask.execute(result);
    }

    @Override
    public void onError(int error) {
        mainActivity.stoppedListening();

        // Remove the temporary item
        if (mainActivity.temporaryConversationItem != null) {
            mainActivity.conversationRecyclerAdapter.removeItem(mainActivity.temporaryConversationItem);
            mainActivity.temporaryConversationItem = null;
        }
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        // Animate the shadow around the microphone button
        float newScale = (rmsdB + 2.0f) / 12f * 0.33f + 0.67f;
        mainActivity.micShadowView.animate()
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(50)
                .scaleX(newScale)
                .scaleY(newScale)
                .start();
    }

    @Override
    public void onBeginningOfSpeech() {}

    @Override
    public void onEndOfSpeech() {}

    @Override
    public void onEvent(int eventType, Bundle params) {}

    @Override
    public void onBufferReceived(byte[] buffer) {}

}
