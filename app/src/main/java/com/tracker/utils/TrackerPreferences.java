package com.tracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by shubhamlamba on 09/02/18.
 */

public class TrackerPreferences {

    private static TrackerPreferences oxigenPrefrences;
    private SharedPreferences sharedPreferences;
    private Context mContext;

    private TrackerPreferences() {
        // Do nothing
    }

    private TrackerPreferences(Context context) {
        mContext = context.getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
    }

    public static TrackerPreferences getInstance(Context context) {
        if (oxigenPrefrences == null) {
            oxigenPrefrences = new TrackerPreferences(context);
        }
        return oxigenPrefrences;
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public void setString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public void setBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public void setLong(String key, Long value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    /*public void removeKey(Map<String, ?> allEntries) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            editor.remove(entry.getKey().toString());
        }
        editor.commit();
    }*/

    public void removeKey(String[] keyList) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (String key : keyList) {
            editor.remove(key);
        }
        editor.commit();
    }

    public long getLong(String key) {
        return sharedPreferences.getLong(key, 0);
    }

    /*public void logOut() {
        Map<String, ?> allEntries = sharedPreferences.getAll();
        removeKey(allEntries);
    }*/


    public void setInteger(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public int getInteger(String key) {
        return sharedPreferences.getInt(key, 0);
    }

//    public void logOut() {
//        String[] prefsList = {
//                PrefrenceConstants.MOBILE_NO,
//                PrefrenceConstants.USER_NAME,
//                PrefrenceConstants.EMAIL,
//                PrefrenceConstants.NAME,
//                PrefrenceConstants.MIDDLE_NAME,
//                PrefrenceConstants.LAST_NAME,
//
//        };
//
//        removeKey(prefsList);
//    }
}
