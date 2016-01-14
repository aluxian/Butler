package com.aluxian.butler.utils;

import com.crashlytics.android.Crashlytics;

/**
 * Exception used for the sole purpose of sending the logcat to Crashlytics.
 */
public class LogCatReportException extends Exception {

    public LogCatReportException() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                    Crashlytics.getInstance().crash();
                } catch (InterruptedException ignored) {}
            }
        }).start();
    }

}
