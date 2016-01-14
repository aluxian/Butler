package com.aluxian.butler.utils;

import com.aluxian.butler.BuildConfig;

import java.util.Locale;

/**
 * Application-wide configuration
 */
public class Constants {

    /** The url of the CoreNLP server used for parsing */
    public static final String CORE_NLP_SERVER_URL = BuildConfig.CORENLP_SERVER;

    /** The key used to connect to the API */
    public static final String FREEBASE_API_KEY = BuildConfig.FREEBASE_API_KEY;

    /** The Freebase API url */
    public static final String FREEBASE_API_URL = "https://www.googleapis.com/freebase/v1";

    /** The Freebase user content API url */
    public static final String FREEBASE_USERCONTENT_URL = "https://usercontent.googleapis.com/freebase/v1";

    /** The minimum amount of time the loading indicator for the assistant must be shown before the actual answer */
    public static final int MIN_ANSWER_DELAY = 750; // millis

    /** The sender ID used for GCM */
    public static final String GCM_SENDER_ID = "691961761485";

    /** New Relic key */
    public static final String NEW_RELIC_KEY = "AAe8de197abfcf84c5d3fbc946fec45e75bf5f5288";

    /** Mixpanel API token */
    public static final String MIXPANEL_TOKEN = "ceb77e49f4c4eef568135c0d83e53bce";

    /** Locale used to set the language for the TTS engine */
    public static final Locale TEXT_TO_SPEECH_LOCALE = Locale.UK;

}
