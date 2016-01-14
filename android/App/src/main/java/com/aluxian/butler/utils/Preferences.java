package com.aluxian.butler.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.aluxian.butler.MainActivity;
import com.aluxian.butler.main.MainActivityDelegate;

/**
 * Different SharedPreferences objects used by the app
 */
public class Preferences extends MainActivityDelegate {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") private static final Logger LOG = new Logger(Preferences.class);

    /** The name of the SharedPreferences which stores the {@link #assistantPreferences} */
    private static final String PREFS_NAME_ASSISTANT = "prefs_assistant";

    /** Default Preferences: Whether database data has been loaded from XML */
    public static final String DEF_PREF_DATA_LOADED = "data_loaded";

    /** Default Preferences: Whether a GCM broadcast has been received by MainActivity */
    public static final String DEF_PREF_GCM_BROADCAST_RECEIVED = "gcm_broadcast_received";

    /** Default Preferences: Write logged messages to logcat */
    public static final String DEF_PREF_LOGGING_ENABLED = "enable_logging";

    /** Assistant Preferences: Whether the user has finished initial set-up */
    public static final String AST_PREF_INITIAL_SETUP = "initial_setup";

    /** Store the preferences of the assistant */
    private SharedPreferences assistantPreferences;

    /** The default preferences of the app */
    private SharedPreferences defaultPreferences;

    public Preferences(MainActivity mainActivity) {
        super(mainActivity);

        // Default
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity.getApplicationContext());

        // Assistant
        assistantPreferences = mainActivity.getSharedPreferences(PREFS_NAME_ASSISTANT, Context.MODE_PRIVATE);
        assistantPreferences.registerOnSharedPreferenceChangeListener(mAssistantChangeListener);
    }

    /**
     * @return The SharedPreferences object which stores the preferences of the assistant
     */
    public SharedPreferences getAssistantPreferences() {
        return assistantPreferences;
    }

    /**
     * @return The SharedPreferences object which stores the default preferences of the app
     */
    public SharedPreferences getDefaultPreferences() {
        return defaultPreferences;
    }

    /**
     * Unregister the listener
     */
    public void onDestroy() {
        assistantPreferences.unregisterOnSharedPreferenceChangeListener(mAssistantChangeListener);
    }

    /**
     * Listens for value changes in AssistantPreferences
     */
    private SharedPreferences.OnSharedPreferenceChangeListener mAssistantChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    // Save this piece of information about the user into Mixpanel
                    if (key.startsWith("user_gender_")) {
                        mainActivity.mixpanel.getPeople().set(
                                Utils.PROPERTY_NAMES.get("USER_GENDER"),
                                Utils.capFirstLetter(key.substring(12)));
                    } else if (key.startsWith("user_")) {
                        mainActivity.mixpanel.getPeople().set(
                                Utils.PROPERTY_NAMES.get(key.toUpperCase()),
                                sharedPreferences.getAll().get(key));
                    }
                }
            };

    /**
     * @return The user's first name
     */
    public String getUserName() {
        return getAssistantPreferences().getString("USER_FIRSTNAME".toLowerCase(), null);
    }

}
