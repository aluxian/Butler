package com.aluxian.butler.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Handles messages received through GCM by passing them to the GcmIntentService
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that GcmIntentService will handle the intent
        ComponentName component = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());

        // Start the service, keeping the device awake while it is launching
        startWakefulService(context, intent.setComponent(component));
        setResultCode(Activity.RESULT_OK);
    }

}
