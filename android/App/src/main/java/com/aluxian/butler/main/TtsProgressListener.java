package com.aluxian.butler.main;

import android.speech.tts.UtteranceProgressListener;

import com.aluxian.butler.MainActivity;

/**
 * Listen for TTS progress
 */
public class TtsProgressListener extends UtteranceProgressListener {

    /** MainActivity instance */
    private MainActivity mainActivity;

    public TtsProgressListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onStart(String utteranceId) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.startedSpeaking();
            }
        });
    }

    @Override
    public void onDone(final String utteranceId) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.stoppedSpeaking();
                mainActivity.itemQueue.finishedSpeaking();

                // Start listening if required
                if (mainActivity.textToSpeech.utteranceItemsPostListen.get(utteranceId)) {
                    mainActivity.speechRecognizer.startListening();
                }
            }
        });
    }

    @Override
    public void onError(String utteranceId) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.stoppedSpeaking();
                mainActivity.itemQueue.finishedSpeaking();
            }
        });
    }

}
