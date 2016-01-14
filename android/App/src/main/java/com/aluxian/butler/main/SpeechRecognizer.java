package com.aluxian.butler.main;

import android.content.Intent;
import android.speech.RecognizerIntent;

import com.aluxian.butler.MainActivity;

/**
 * Wrapper for Android's SpeechRecognizer
 */
public class SpeechRecognizer extends MainActivityDelegate {

    /** SpeechRecognizer instance */
    public android.speech.SpeechRecognizer speechRecognizer;

    /** Intent used for listening with SpeechRecognizer */
    public Intent recognizerIntent;

    public SpeechRecognizer(MainActivity mainActivity) {
        super(mainActivity);

        speechRecognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(mainActivity.getApplicationContext());
        speechRecognizer.setRecognitionListener(new RecognitionListener(mainActivity));

        recognizerIntent = new Intent();
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
    }

    /**
     * Start listening for input
     */
    public void startListening() {
        speechRecognizer.startListening(recognizerIntent);
        mainActivity.startedListening();
    }

    /**
     * Stop listening for input
     */
    public void stopListening() {
        speechRecognizer.stopListening();
    }

}
