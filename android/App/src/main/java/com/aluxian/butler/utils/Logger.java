package com.aluxian.butler.utils;

import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Custom logger implementation. Wraps around Android's {link Log} class.
 * <p/>
 * Each class should create its own static logger instance.
 * <p/>
 * Only the messages from classes added to {link #EXCLUDED_CLASSES} are actually written to logcat.
 */
@SuppressWarnings("UnusedDeclaration")
public class Logger {

    /** Log messages delimiter */
    private static final String DELIMITER = ", ";

    /** Whether logs should be written to logcat too */
    //public static final boolean LOGGING_ENABLED = BuildConfig.DEBUG;

//    /** List of classes whose messages should really be logged and their log levels */
//    private static final HashMap<Class, EnumSet> EXCLUDED_CLASSES = new HashMap<>();
//
//    /** Fill the list of logged classes */
//    static {
//        EXCLUDED_CLASSES.put(MainActivity.class, EnumSet.allOf(LogLevel.class));
//        EXCLUDED_CLASSES.put(MainProcessor.class, EnumSet.allOf(LogLevel.class));
//        EXCLUDED_CLASSES.put(ChitChatProcessor.class, EnumSet.allOf(LogLevel.class));
//        EXCLUDED_CLASSES.put(GcmManager.class, EnumSet.allOf(LogLevel.class));
//        EXCLUDED_CLASSES.put(ProcessingHistory.class, EnumSet.allOf(LogLevel.class));
//        EXCLUDED_CLASSES.put(DatabaseImporter.class, EnumSet.allOf(LogLevel.class));
//    }

    /** Class the logger is logging */
    private Class mClass;

    /**
     * Create a new Logger instance
     *
     * @param clazz Class to log
     */
    public Logger(Class clazz) {
        mClass = clazz;
    }

    /**
     * Logs verbose messages
     *
     * @param messages Object whose values to log
     */
    public void v(Object... messages) {
        if (printToLogCat(LogLevel.VERBOSE)) {
            Log.v(mClass.getSimpleName(), TextUtils.join(DELIMITER, messages));
        }
    }

    /**
     * Logs debug messages
     *
     * @param messages Object whose values to log
     */
    public void d(Object... messages) {
        if (printToLogCat(LogLevel.DEBUG)) {
            Log.d(mClass.getSimpleName(), TextUtils.join(DELIMITER, messages));
        }
    }

    /**
     * Logs info messages
     *
     * @param messages Object whose values to log
     */
    public void i(Object... messages) {
        if (printToLogCat(LogLevel.INFO)) {
            Log.i(mClass.getSimpleName(), TextUtils.join(DELIMITER, messages));
        }
    }

    /**
     * Logs warn messages
     *
     * @param messages Object whose values to log
     */
    public void w(Object... messages) {
        if (printToLogCat(LogLevel.WARN)) {
            Log.w(mClass.getSimpleName(), TextUtils.join(DELIMITER, messages));
        }
    }

    /**
     * Logs error messages
     *
     * @param throwables Error objects to log
     */
    public void e(final Throwable... throwables) {
        if (printToLogCat(LogLevel.ERROR)) {
            Log.e(mClass.getSimpleName(), joinThrowables(DELIMITER, throwables));
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Also send the logcat
                try {
                    d("Sending logcat to Crashlytics");
                    Process process = Runtime.getRuntime().exec("logcat -d -v time *:D | tail -n 1000");
                    InputStreamReader input = new InputStreamReader(process.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(input);

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        v("Crashlytics.log(line)", line);
                        Crashlytics.log(line);
                    }

                    d("Finished sending");
                } catch (IOException e) {
                    e(e); // TODO: What if this becomes an infinite loop? :(
                }

                // Send the exceptions to Crashlytics
                for (Throwable throwable : throwables) {
                    Crashlytics.logException(throwable);
                }
            }
        }).start();
    }

    /**
     * Logs wtf messages
     *
     * @param messages Object whose values to log
     */
    public void wtf(Object... messages) {
        if (printToLogCat(LogLevel.WTF)) {
            Log.wtf(mClass.getSimpleName(), TextUtils.join(DELIMITER, messages));
        }
    }

    /**
     * @return Whether the messages logged by the {link #mClass} class should be printed or not
     */
    private boolean printToLogCat(LogLevel level) {
        return true;//BuildConfig.DEBUG;// && !EXCLUDED_CLASSES.keySet().contains(mClass);
                /*&& EXCLUDED_CLASSES.keySet().contains(mClass)
                && EXCLUDED_CLASSES.get(mClass).contains(level);*/
    }

    /**
     * The log levels supported by logcat
     */
    private static enum LogLevel {
        VERBOSE, DEBUG, INFO, WARN, ERROR, BuildConfig, WTF
    }

    /**
     * Gets the messages of the throwables, joins them and returns the new string.
     *
     * @param delimiter  Message delimiter
     * @param throwables Array of throwables whose messages to join
     * @return Joined string
     */
    private String joinThrowables(String delimiter, Throwable... throwables) {
        String[] messages = new String[throwables.length];

        for (int i = 0; i < throwables.length; i++) {
            if (throwables[i] instanceof LogCatReportException) {
                d("Sending logcat report");
                continue;
            }

            StringWriter stringWriter = new StringWriter();
            throwables[i].printStackTrace(new PrintWriter(stringWriter));
            messages[i] = stringWriter.toString();
        }

        return TextUtils.join(delimiter, messages);
    }

}
