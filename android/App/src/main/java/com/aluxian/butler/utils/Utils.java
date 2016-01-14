package com.aluxian.butler.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;

import com.activeandroid.Model;
import com.aluxian.butler.MainActivity;
import com.aluxian.butler.database.pojos.Contact;
import com.aluxian.butler.main.MainActivityDelegate;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static android.provider.ContactsContract.CommonDataKinds.Phone;

/**
 * Utility methods used in the app
 */
public class Utils extends MainActivityDelegate {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") public static final Logger LOG = new Logger(Utils.class);

    /** Stores the names of preference keys */
    public static final HashMap<String, String> PROPERTY_NAMES = new HashMap<>();

    static {
        PROPERTY_NAMES.put("USER_FIRSTNAME", "First Name");
        PROPERTY_NAMES.put("USER_GENDER", "Gender");
    }

    public Utils(MainActivity mainActivity) {
        super(mainActivity);
    }

    /**
     * @return A configured Gson object
     */
    public static Gson gson() {
        return new GsonBuilder()
                // Exclude Models because Gson doesn't like them
                .setExclusionStrategies(new SpecificInstanceOfExclusionStrategy(Model.class))
                .create();
    }

    /**
     * Excludes the given class and its subclasses
     */
    private static class SpecificInstanceOfExclusionStrategy implements ExclusionStrategy {

        /**
         * Super class to exclude
         */
        private final Class<?> superClass;

        public SpecificInstanceOfExclusionStrategy(Class<?> superClass) {
            this.superClass = superClass;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return superClass.isAssignableFrom(clazz);
        }

        @Override
        public boolean shouldSkipField(FieldAttributes field) {
            return superClass.isAssignableFrom(field.getDeclaredClass());
        }

    }

    /**
     * Converts an array of HashMap value pairs back to a HashMap object
     *
     * @param strings String representation of the array
     * @return HashMap object
     */
    public static HashMap<String, String> hashMapFromStrings(String strings) {
        strings = strings.substring(1, strings.length() - 1);

        String[] pairs = strings.split(",\\s*");
        HashMap<String, String> hashMap = new HashMap<>();

        for (String pair : pairs) {
            String[] splitPair = pair.split("=");
            hashMap.put(splitPair[0], splitPair[1]);
        }

        return hashMap;
    }

    /**
     * @return The time in a long explicit format. E.g. 10:17 => 17 minutes past 10
     */
    public static String getLongExplicitTime() {
        int hour = Calendar.getInstance().get(Calendar.HOUR);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);

        // Half past hour
        if (minute == 30) {
            return String.format("half past %d", hour);
        }

        // Minutes to hour
        else if (minute > 30) {
            // Get the next hour
            hour++;

            // Set hour to 12 if it's 0
            if (hour == 0) {
                hour = 12;
            }

            // Convert to "to" minutes
            minute = 60 - minute;

            // Say "quarter" instead of 15
            if (minute == 15) {
                return String.format("a quarter to %d", hour);
            } else {
                return String.format("%d minutes to %d", minute, hour);
            }
        }

        // Minutes past hour
        else {
            // Set hour to 12 if it's 0
            if (hour == 0) {
                hour = 12;
            }

            // Say "quarter" instead of 15
            if (minute == 15) {
                return String.format("a quarter past %d", hour);
            } else {
                return String.format("%d minutes past %d", minute, hour);
            }
        }
    }

    /**
     * @return The unique ID of the user
     */
    public String getUserId() {
        return getUserId(mainActivity.getApplicationContext());
    }

    /**
     * @param context Context from which to get the Android ID
     * @return The unique ID of the user
     */
    public static String getUserId(Context context) {
        return Build.SERIAL + "-" + Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * Call the given number
     *
     * @param number Number to call
     */
    public void callNumber(String number) {
        callNumber(mainActivity, number);
    }

    /**
     * Call the given number
     *
     * @param activity Activity from which to start the call intent
     * @param number   Number to call
     */
    public static void callNumber(Activity activity, String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        activity.startActivity(intent);
    }

    /**
     * Capitalise the first letter
     *
     * @param newText String whose first letter to capitalise
     * @return The capitalised string
     */
    public static String capFirstLetter(String newText) {
        if (newText == null) {
            return null;
        } else if (newText.length() == 0) {
            return "";
        }

        char[] chars = newText.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK
     */
    public boolean checkPlayServices() {
        return checkPlayServices(mainActivity);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK
     */
    public static boolean checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity.getApplicationContext());

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity, 9000).show();
                return true;
            }

            return false;
        }

        return true;
    }

    /**
     * Search for a contact. Only matches contacts who have a name that exactly matches {link #contactName}
     * <p/>
     * E.g. `Mike` would match `Josh Mike`, `Mike Smith`, but not `Mikey Smithson`
     *
     * @param contactName Name to search for
     * @return A list of possible matches
     */
    public List<Contact> searchContactByName(String contactName) {
        return searchContactByName(mainActivity, contactName);
    }

    /**
     * Search for a contact. Only matches contacts who have a name that exactly matches {link #contactName}
     * <p/>
     * E.g. `Mike` would match `Josh Mike`, `Mike Smith`, but not `Mikey Smithson`
     *
     * @param context     Context from which to get the Context Resolver
     * @param contactName Name to search for
     * @return A list of possible matches
     */
    public static List<Contact> searchContactByName(Context context, String contactName) {
        List<Contact> contacts = new ArrayList<>();

        // Find matching contacts
        Cursor cursor = context.getContentResolver().query(
                Phone.CONTENT_URI,
                new String[]{Phone._ID, Phone.DISPLAY_NAME, Phone.NUMBER, Phone.TYPE},
                Phone.DISPLAY_NAME + " LIKE ? AND " + Phone.NUMBER + " IS NOT NULL",
                new String[]{"%" + contactName + "%"},
                ContactsContract.Contacts.DISPLAY_NAME
        );

        while (cursor.moveToNext()) {
            String displayName = cursor.getString(1);

            if (displayName.toLowerCase().matches("(.+\\s+)*" + contactName.toLowerCase() + "(,?\\s+.+)*")) {
                contacts.add(new Contact(
                        cursor.getString(0),
                        displayName,
                        cursor.getString(2),
                        Phone.getTypeLabel(context.getResources(), cursor.getInt(3), "").toString()
                ));
            }
        }

        cursor.close();
        return contacts;
    }

    /**
     * Converts a Bundle object into a JsonObject
     *
     * @param bundle Bundle to convert
     * @return A JsonElement
     */
    public static JsonElement bundleToJson(Bundle bundle) {
        JsonObject jsonObject = new JsonObject();

        for (String key : bundle.keySet()) {
            jsonObject.add(key, new Gson().toJsonTree(bundle.get(key)));
        }

        return jsonObject;
    }

}

//    /**
//     * @return The first non-null object of the given parameters
//     */
//    public static Object firstNonNull(Object... objects) {
//        for (Object object : objects) {
//            if (object != null) {
//                return object;
//            }
//        }
//
//        return null;
//    }

//    /**
//     * The reverse of @{link Arrays.toString()}
//     *
//     * @param toString String representation of the array
//     * @return The array
//     */
//    public static String[] arrayFromStrings(String toString) {
//        toString = toString.substring(1, toString.length() - 1);
//        return toString.split(",\\s*");
//    }
