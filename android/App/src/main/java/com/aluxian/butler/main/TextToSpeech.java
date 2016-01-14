package com.aluxian.butler.main;

import com.aluxian.butler.MainActivity;
import com.aluxian.butler.recycler.ConversationItem;
import com.aluxian.butler.utils.Constants;

import java.util.HashMap;

/**
 * Wrapper around Android's TextToSpeech class
 */
public class TextToSpeech extends android.speech.tts.TextToSpeech {

    /** MainActivity instance */
    private final MainActivity mainActivity;

    /**
     * Keep track of items being spoken
     */
    public HashMap<String, Boolean> utteranceItemsPostListen = new HashMap<>();

    public TextToSpeech(MainActivity mainActivity) {
        super(mainActivity.getApplicationContext(), new TtsInitListener(mainActivity));
        setOnUtteranceProgressListener(new TtsProgressListener(mainActivity));
        setLanguage(Constants.TEXT_TO_SPEECH_LOCALE);

        this.mainActivity = mainActivity;
    }

    public int speak(String text, int queueMode, String utteranceId) {
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);

        return super.speak(text, queueMode, params);
    }

    public void speakItem(ConversationItem item, boolean listenAfterSpeech, int queueMode) {
        String utteranceId = String.valueOf(item.hashCode());
        utteranceItemsPostListen.put(utteranceId, listenAfterSpeech);
        speak(item.getSpeakable(), queueMode, utteranceId);
    }

    public void speakItem(ConversationItem item, boolean listenAfterSpeech) {
        speakItem(item, listenAfterSpeech, TextToSpeech.QUEUE_FLUSH);
    }

    @Override
    public int stop() {
        mainActivity.itemQueue.finishedSpeaking();
        return super.stop();
    }

}
