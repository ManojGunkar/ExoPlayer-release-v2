package com.globaldelight.boom.business;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Rahul Agarwal on 20-02-17.
 */

public class BusinessPreferences {
    public static final String PREF_NAME = "boom_pref";


    public static final int MODE = Context.MODE_PRIVATE;

    public static final String ACTION_IN_APP_PURCHASE = "ACTION_IN_APP_PURCHASE";
    public static final String ACTION_APP_SHARED = "ACTION_APP_SHARED";

    public static void writeBoolean(Context context, String key, boolean value) {
        getEditor(context).putBoolean(key, value).commit();
    }

    public static boolean readBoolean(Context context, String key,
                                      boolean defValue) {
        return getPreferences(context).getBoolean(key, defValue);
    }

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, MODE);
    }

    public static SharedPreferences.Editor getEditor(Context context) {
        return getPreferences(context).edit();
    }
}
