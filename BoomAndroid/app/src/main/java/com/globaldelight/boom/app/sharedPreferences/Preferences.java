package com.globaldelight.boom.app.sharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */

public class Preferences {

    public static final String PREF_NAME = "boom_pref";


    public static final int MODE = Context.MODE_PRIVATE;


    public static final String SLEEP_TIME = "SLEEP_TIME";
    public static final String SLEEP_TIMER_ENABLED = "SLEEP_TIME_ENABLED";
    public static final String APP_FRESH_LAUNCH = "APP_FIRST_LAUNCH";
    public static final String APP_LAST_OPEN = "APP_LAST_OPEN";

    public static final String TOOLTIP_SWITCH_EFFECT_LARGE_PLAYER = "TOOLTIP_SWITCH_EFFECT_LARGE_PLAYER";
    public static final String TOOLTIP_SWITCH_EFFECT_SCREEN_EFFECT = "TOOLTIP_SWITCH_EFFECT_SCREEN_EFFECT";
    public static final String TOOLTIP_OPEN_EFFECT_MINI_PLAYER = "TOOLTIP_OPEN_EFFECT_MINI_PLAYER";
    public static final String TOOLTIP_USE_HEADPHONE_LIBRARY = "TOOLTIP_USE_HEADPHONE_LIBRARY";
    public static final String TOOLTIP_CHOOSE_HEADPHONE_LIBRARY = "TOOLTIP_CHOOSE_HEADPHONE_LIBRARY";
    public static final String TOOLTIP_USE_24_HEADPHONE_LIBRARY = "TOOLTIP_USE_24_HEADPHONE_LIBRARY";
    public static final String HEADPHONE_CONNECTED = "HEADPHONE_CONNECTED";
    public static final String ACTION_ONBOARDING_SHOWN = "ACTION_ONBOARDING_SHOWN";
    public static final String GOOGLE_DRIVE_ACCOUNT_CHANGED = "GOOGLE_DRIVE_ACCOUNT_CHANGED";
    public static final String INAPP_PURCHASE_PRICE_VALUE = "INAPP_PURCHASE_PRICE_VALUE";

    public static final String PLAYING_ITEM_INDEX_IN_UPNEXT= "PLAYING_ITEM_INDEX_IN_UPNEXT";
    public static final String ON_BOARDING_COMPLETED_ON_FIRST_ATTEMPT = "OnBoarding Completed on First Attempt";

    public static void writeBoolean(Context context, String key, boolean value) {
        getEditor(context).putBoolean(key, value).apply();
    }

    public static boolean readBoolean(Context context, String key,
                                      boolean defValue) {
        return getPreferences(context).getBoolean(key, defValue);
    }

    public static void writeString(Context context, String key, String value) {
        getEditor(context).putString(key, value).apply();

    }

    public static String readString(Context context, String key, String defValue) {
        return getPreferences(context).getString(key, defValue);
    }

    public static void writeLong(Context context, String key, long value) {
        getEditor(context).putLong(key, value).apply();
    }

    public static long readLong(Context context, String key, long defValue) {
        return getPreferences(context).getLong(key, defValue);
    }

    public static void writeInteger(Context context, String key, int value) {
        getEditor(context).putInt(key, value).apply();
    }

    public static int readInteger(Context context, String key, int defValue) {
        return getPreferences(context).getInt(key, defValue);
    }

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, MODE);
    }

    public static SharedPreferences.Editor getEditor(Context context) {
        return getPreferences(context).edit();
    }
}
