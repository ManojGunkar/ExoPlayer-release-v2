package com.globaldelight.boom.utils.handlers;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */

public class Preferences {

    public static final String PREF_NAME = "boom_pref";


    public static final int MODE = Context.MODE_PRIVATE;


    public static final String SHAKE_GESTURE_PREF = "SHAKE_GESTURE_PREF";
    public static final String CROSS_FADE_ENABLE = "CROSS_FADE_ENABLE";
    public static final String CROSS_FADE_VALUE = "CROSS_FADE_VALUE";
    public static final String SLEEP_TIME = "SLEEP_TIME";
    public static final String SLEEP_TIMER_ENABLED = "SLEEP_TIME_ENABLED";
    //public static final String APP_NEW_LAUNCH = "APP_NEW_LAUNCH";
    public static final String APP_FRESH_LAUNCH = "APP_FIRST_LAUNCH";
    public static final String APP_LAST_OPEN = "APP_LAST_OPEN";
    public static final String INSTALL_DATE = "APP_INSTALL_DATE";
    public static final String EXTENDED_DATE = "EXTENDED_DATE";
    public static final String PLAYER_SCREEN_EFFECT_COACHMARK_ENABLE = "PLAYER_SCREEN_EFFECT_COACHMARK_ENABLE";
    public static final String PLAYER_SCREEN_LIBRARY_COACHMARK_ENABLE = "PLAYER_SCREEN_LIBRARY_COACHMARK_ENABLE";
    public static final String PLAYER_SCREEN_EFFECT_TAPANDHOLD_COACHMARK_ENABLE = "PLAYER_SCREEN_EFFECT_TAPANDHOLD_COACHMARK_ENABLE";
    public static final String EFFECT_SCREEN_TAP_SPEAKER_ENABLE = "EFFECT_SCREEN_TAP_SPEAKER_ENABLE";
    public static final String EFFECT_SCREEN_TAP_EFFECT_ENABLE = "EFFECT_SCREEN_TAP_EFFECT_ENABLE";

    public static final String PLAYER_SCREEN_HEADSET_ENABLE = "PLAYER_SCREEN_HEADSET_ENABLE";

    public static void writeBoolean(Context context, String key, boolean value) {
        getEditor(context).putBoolean(key, value).commit();
    }

    public static boolean readBoolean(Context context, String key,
                                      boolean defValue) {
        return getPreferences(context).getBoolean(key, defValue);
    }

    public static void writeInteger(Context context, String key, int value) {
        getEditor(context).putInt(key, value).commit();

    }

    public static int readInteger(Context context, String key, int defValue) {
        return getPreferences(context).getInt(key, defValue);
    }

    public static void writeString(Context context, String key, String value) {
        getEditor(context).putString(key, value).commit();

    }

    public static String readString(Context context, String key, String defValue) {
        return getPreferences(context).getString(key, defValue);
    }

    public static void writeFloat(Context context, String key, float value) {
        getEditor(context).putFloat(key, value).commit();
    }

    public static float readFloat(Context context, String key, float defValue) {
        return getPreferences(context).getFloat(key, defValue);
    }

    public static void writeLong(Context context, String key, long value) {
        getEditor(context).putLong(key, value).commit();
    }

    public static long readLong(Context context, String key, long defValue) {
        return getPreferences(context).getLong(key, defValue);
    }

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, MODE);
    }

    public static SharedPreferences.Editor getEditor(Context context) {
        return getPreferences(context).edit();
    }

}
