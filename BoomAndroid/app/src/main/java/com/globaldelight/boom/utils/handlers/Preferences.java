package com.globaldelight.boom.utils.handlers;

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
    public static final String INSTALL_DATE = "APP_INSTALL_DATE";

    public static final String TOLLTIP_SWITCH_EFFECT_LARGE_PLAYER = "TOLLTIP_SWITCH_EFFECT_LARGE_PLAYER";
    public static final String TOLLTIP_SWITCH_EFFECT_SCREEN_EFFECT = "TOLLTIP_SWITCH_EFFECT_SCREEN_EFFECT";
    public static final String TOLLTIP_OPEN_EFFECT_MINI_PLAYER = "TOLLTIP_OPEN_EFFECT_MINI_PLAYER";
    public static final String TOLLTIP_USE_HEADPHONE_LIBRARY = "TOLLTIP_USE_HEADPHONE_LIBRARY";
    public static final String TOLLTIP_CHOOSE_HEADPHONE_LIBRARY = "TOLLTIP_CHOOSE_HEADPHONE_LIBRARY";

    public static final String HEADPHONE_CONNECTED = "HEADPHONE_CONNECTED";
    public static final String HEADPHONE_DISCONNECTED = "HEADPHONE_DISCONNECTED";

    public static void writeBoolean(Context context, String key, boolean value) {
        getEditor(context).putBoolean(key, value).commit();
    }

    public static boolean readBoolean(Context context, String key,
                                      boolean defValue) {
        return getPreferences(context).getBoolean(key, defValue);
    }

    public static void writeString(Context context, String key, String value) {
        getEditor(context).putString(key, value).commit();

    }

    public static String readString(Context context, String key, String defValue) {
        return getPreferences(context).getString(key, defValue);
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
