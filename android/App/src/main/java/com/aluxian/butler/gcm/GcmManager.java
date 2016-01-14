package com.aluxian.butler.gcm;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.aluxian.butler.BuildConfig;
import com.aluxian.butler.MainActivity;
import com.aluxian.butler.database.models.GcmMessage;
import com.aluxian.butler.main.MainActivityDelegate;
import com.aluxian.butler.utils.Constants;
import com.aluxian.butler.utils.Logger;
import com.aluxian.butler.utils.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class GcmManager extends MainActivityDelegate {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") private static final Logger LOG = new Logger(GcmManager.class);

    /** SharedPreferences key which stores the GCM registration ID of the device */
    public static final String PROPERTY_REG_ID = "gcm_reg_id";

    /** The app version when the last reg ID was create */
    public static final String PROPERTY_REG_APP_VERSION = "gcm_reg_app_version";

    /** Key to get the authToken from the default preferences */
    public static final String PROPERTY_AUTH_TOKEN = "auth_token";

    /** GCM instance */
    private GoogleCloudMessaging googleCloudMessaging;

    public GcmManager(MainActivity mainActivity) {
        super(mainActivity);
        googleCloudMessaging = GoogleCloudMessaging.getInstance(mainActivity.getApplicationContext());
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing registration ID
     */
    public String getRegistrationId() {
        SharedPreferences prefs = mainActivity.preferences.getDefaultPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, null);

        if (TextUtils.isEmpty(registrationId)) {
            LOG.d("Registration ID not found in the default SharedPreferences");
            return null;
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_REG_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = BuildConfig.VERSION_CODE;

        if (registeredVersion != currentVersion) {
            LOG.d("Registration ID is outdated");
            return null;
        }

        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's shared preferences.
     */
    public void registerInBackground() {
        LOG.d("registerInBackground()");

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                long backoffTime = 250; // ms

                while (true) {
                    try {
                        String registrationId = googleCloudMessaging.register(Constants.GCM_SENDER_ID);
                        LOG.d("Successfully registered", registrationId);

                        // Persist the registration ID - no need to register again
                        mainActivity.preferences.getDefaultPreferences().edit()
                                .putString(PROPERTY_REG_ID, registrationId)
                                .putInt(PROPERTY_REG_APP_VERSION, BuildConfig.VERSION_CODE)
                                .apply();

                        // Register with the cloud
                        registerInTheCloud();

                        break;
                    } catch (IOException e) {
                        LOG.e(e);

                        // Give up
                        if (backoffTime > 30_000) {
                            LOG.d("Registration error, gave up");
                            break;
                        }

                        // Exponential backoff
                        backoffTime *= 2;

                        LOG.d("Registration error, backing off for " + backoffTime + "ms");

                        try {
                            Thread.sleep(backoffTime);
                        } catch (InterruptedException ignored) {}
                    }
                }

                return null;
            }
        }.execute();
    }

    /**
     * Register the user in the cloud
     */
    public void registerInTheCloud() {
        LOG.d("registerInTheCloud()");

        GcmMessage gcmMessage = new GcmMessage(
                AndroidMessage.RequestType.REGISTER,
                GcmMessage.MessageDirection.SENT);
        gcmMessage.save();

        mainActivity.gcmManager.send(new AndroidMessage(
                gcmMessage.getId().toString(),
                mainActivity.utils.getUserId(),
                mainActivity.gcmManager.getRegistrationId(),
                mainActivity.gcmManager.getAuthToken(),
                gcmMessage.type,
                new AndroidMessage.RegisterRequest(
                        mainActivity.preferences.getAssistantPreferences()
                                .getString("USER_FIRSTNAME".toLowerCase(), null)
                ),
                null,
                null,
                null,
                null
        ), gcmMessage);
    }

    /**
     * Send an AndroidMessage to the cloud.
     *
     * @param androidMessage The AndroidMessage to send
     */
    public void send(final AndroidMessage androidMessage, final GcmMessage gcmMessage) {
        LOG.d("Sending GCM message", Utils.gson().toJson(androidMessage));

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                JsonElement requestData = null;

                switch (androidMessage.requestType) {
                    case REGISTER:
                        requestData = Utils.gson().toJsonTree(androidMessage.registerRequest);
                        break;
                    case FRIEND_REQUEST:
                        requestData = Utils.gson().toJsonTree(androidMessage.friendRequest);
                        break;
                    case FRIEND_REQUEST_RESPONSE:
                        requestData = Utils.gson().toJsonTree(androidMessage.friendRequestResponse);
                        break;
                }

                if (gcmMessage != null) {
                    gcmMessage.data = Utils.gson().toJson(requestData);
                    gcmMessage.save();
                }

                Bundle bundle = new Bundle();
                JsonElement message = Utils.gson().toJsonTree(androidMessage);

                for (Map.Entry<String, JsonElement> entry : message.getAsJsonObject().entrySet()) {
                    bundle.putString(entry.getKey(), Utils.gson().toJson(entry.getValue()));
                }

                LOG.d("Data bundle", bundle);

                try {
                    googleCloudMessaging.send(
                            Constants.GCM_SENDER_ID + "@gcm.googleapis.com",
                            UUID.randomUUID().toString(),
                            bundle);
                } catch (IOException e) {
                    LOG.e(e);
                }

                return null;
            }
        }.execute();
    }

    /**
     * @return The auth token of the device for cloud authentication
     */
    public String getAuthToken() {
        return mainActivity.preferences.getDefaultPreferences().getString(PROPERTY_AUTH_TOKEN, null);
    }

}
