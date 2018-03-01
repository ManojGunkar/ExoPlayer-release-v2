package com.globaldelight.boom.utils;

import com.globaldelight.boom.BuildConfig;

/**
 * Created by adarsh on 20/12/17.
 */

public class Log {
    private static boolean LOG_ENABLED = BuildConfig.ENABLE_LOG;

    public static void d(String tag, String msg) {
        if ( LOG_ENABLED ) {
            android.util.Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if ( LOG_ENABLED ) {
            android.util.Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable e) {
        if ( LOG_ENABLED ) {
            android.util.Log.e(tag, msg, e);
        }
    }

    public static void v(String tag, String msg) {
        if ( LOG_ENABLED ) {
            android.util.Log.v(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if ( LOG_ENABLED ) {
            android.util.Log.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable e) {
        if ( LOG_ENABLED ) {
            android.util.Log.w(tag, msg, e);
        }
    }

    public static void w(String tag, Throwable e) {
        if ( LOG_ENABLED ) {
            android.util.Log.w(tag, e);
        }
    }

    public static void i(String tag, String msg) {
        if ( LOG_ENABLED ) {
            android.util.Log.i(tag, msg);
        }
    }
}
