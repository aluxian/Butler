package com.aluxian.butler.gcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.aluxian.butler.MainActivity;
import com.aluxian.butler.R;
import com.aluxian.butler.database.models.GcmMessage;
import com.aluxian.butler.utils.Logger;
import com.aluxian.butler.utils.Preferences;
import com.aluxian.butler.utils.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Intent service which handles GCM messages
 */
public class GcmIntentService extends IntentService {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") private static final Logger LOG = new Logger(GcmIntentService.class);

    /** Key used to send data with the notification intent */
    public static final String KEY_SERVER_MESSAGE = "gcm_server_message";

    /** ID used for notifications */
    private static final int NOTIFICATION_ID = 10;

    public GcmIntentService() {
        super(GcmIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String messageType = GoogleCloudMessaging.getInstance(getApplicationContext()).getMessageType(intent);
        Bundle extras = intent.getExtras();

        if (!extras.isEmpty()) {
            switch (messageType) {
                case GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR:
                    LOG.d(messageType, extras);
                    // TODO Maybe resend
                    break;

                case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
                    LOG.d(messageType, extras);
                    // TODO Not sure what to do
                    break;

                case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE:
                    LOG.d(messageType, extras);
                    GcmServerMessage serverMessage = Utils.gson()
                            .fromJson(Utils.bundleToJson(extras), GcmServerMessage.class);

                    switch (serverMessage.type) {
                        case REGISTER_RESPONSE: {
                            // Store the auth token
                            String authToken = String.valueOf(serverMessage.content);
                            LOG.d("New authToken", authToken);

                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                    .edit().putString(GcmManager.PROPERTY_AUTH_TOKEN, authToken).apply();

                            break;
                        }

                        case FRIEND_REQUEST: {
                            notify("Friend Request Received", "From " + serverMessage.content, serverMessage);

                            break;
                        }

                        case FRIEND_REQUEST_NOT_FOUND: {
                            GcmMessage gcmMessage = GcmMessage.load(GcmMessage.class,
                                    Long.valueOf(serverMessage.messageId));
                            AndroidMessage.FriendRequest friendRequest = Utils.gson()
                                    .fromJson(gcmMessage.data, AndroidMessage.FriendRequest.class);

                            String userName = friendRequest.targetOwnerName;
                            notify("Friend Request Response", "User not found: " + userName, serverMessage);

                            break;
                        }

                        case FRIEND_REQUEST_RESPONSE: {
                            GcmMessage gcmMessage = GcmMessage.load(GcmMessage.class,
                                    Long.valueOf(serverMessage.messageId));
                            AndroidMessage.FriendRequest friendRequest = Utils.gson()
                                    .fromJson(gcmMessage.data, AndroidMessage.FriendRequest.class);

                            String userName = friendRequest.targetOwnerName;
                            String status = Boolean.valueOf(String.valueOf(serverMessage.content))
                                    ? "accepted" : "declined";

                            String description = userName + " has " + status + " your request";
                            notify("Your friend has responded", description, serverMessage);

                            break;
                        }

                        case TELL_MESSAGE: {
                            String[] splitContent = String.valueOf(serverMessage.content).split("\\|");
                            String friendName = splitContent[0];
                            String message = splitContent[1];

                            notify("You received a message", "From " + friendName + ": " + message, serverMessage);
                            break;
                        }

                        case PREDICTION: {
                            notify("Butler wants to talk to you", String.valueOf(serverMessage.content), serverMessage);
                            break;
                        }
                    }

                    break;
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Try to tell the app about the new message. If the app is not running, a notification will be displayed instead.
     *
     * @param title            The notification title
     * @param message          The notification message
     * @param gcmServerMessage Extra data to send with the intent
     */
    private void notify(String title, String message, GcmServerMessage gcmServerMessage) {
        String json = Utils.gson().toJson(gcmServerMessage);

        // Try with a broadcast
        Intent intent = new Intent(MainActivity.FILTER_ACTION_GCM).putExtra(KEY_SERVER_MESSAGE, json);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        defaultPrefs.edit().putBoolean(Preferences.DEF_PREF_GCM_BROADCAST_RECEIVED, false).apply();

        int timeout = 2000;

        while (timeout > 0) {
            try {
                timeout -= 100;
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}

            if (defaultPrefs.getBoolean(Preferences.DEF_PREF_GCM_BROADCAST_RECEIVED, false)) {
                return;
            }
        }

        // Display a notification instead
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.putExtra(KEY_SERVER_MESSAGE, json);

        PendingIntent pendingIntent = PendingIntent
                .getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);

        if (message != null) {
            builder
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentTitle(title);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

}
