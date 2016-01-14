package com.aluxian.butler.main;

import android.speech.tts.TextToSpeech;

import com.aluxian.butler.MainActivity;
import com.aluxian.butler.processing.ProcessorOutput;
import com.aluxian.butler.recycler.ConversationItem;
import com.aluxian.butler.recycler.items.TextItem;

/**
 * Listen for TTS initialisation
 */
public class TtsInitListener extends MainActivityDelegate implements TextToSpeech.OnInitListener {

    public TtsInitListener(MainActivity mainActivity) {
        super(mainActivity);
    }

    @Override
    public void onInit(int status) {
        mainActivity.ttsReady = true;

        if (status == TextToSpeech.ERROR && !(mainActivity.isListening || mainActivity.isSpeaking)) {
            // Show an error item
            ConversationItem item = new TextItem("There's been an error with my " +
                    "voice. I'm afraid I will not be able to speak until I get fixed", true);
            mainActivity.processorOutputQueue.add(new ProcessorOutput(null, false, item));
        }
    }

}
