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
    public static final String ACTION_APP_SHARED_DATE = "ACTION_APP_SHARED_DATE";
    public static final String ACTION_APP_SHARED_DIALOG_SHOWN = "ACTION_APP_SHARED_DIALOG_SHOWN";
    public static final String ACTION_APP_INTERNET_DIALOG_SHOWN = "ACTION_APP_INTERNET_DIALOG_SHOWN";
    public static final String ACTION_APP_EXPIRE_DIALOG_SHOWN = "ACTION_APP_EXPIRE_DIALOG_SHOWN";
    public static final String ACTION_EMAIL_DIALOG_SHOWN = "ACTION_EMAIL_DIALOG_SHOWN";
    public static final String STORE_CLOSED_WITH_PURCHASE = "STORE_CLOSED_WITH_PURCHASE";

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

    public static void writeLong(Context context, String key, long value) {
        getEditor(context).putLong(key, value).commit();
    }

    public static Long readLong(Context context, String key,
                                      long defValue) {
        return getPreferences(context).getLong(key, defValue);
    }
}
