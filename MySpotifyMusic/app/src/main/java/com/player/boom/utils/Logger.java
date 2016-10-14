package com.player.boom.utils;

import android.util.Log;

/**
 * Created by Rahul Kumar Agrawal on 23-May-2016.
 */
public class Logger {
    final static String TAG="TrackPlayer";

    public static void LOGD(String MSG){
        Log.d(TAG,MSG);
    }

    public static void LOGD(String Tag, String MSG){
        Log.d(Tag,MSG);
    }

    public static void LOGI(String MSG){
        Log.i(TAG,MSG);
    }

    public static void LOGI(String Tag, String MSG){
        Log.i(Tag,MSG);
    }

    public static void LOGE(String MSG){
        Log.e(TAG,MSG);
    }

    public static void LOGE(String Tag, String MSG){
        Log.e(Tag,MSG);
    }

    public static void LOGW(String MSG){
        Log.w(TAG,MSG);
    }

    public static void LOGW(String Tag, String MSG){
        Log.w(Tag,MSG);
    }

    public static void LOGV(String MSG){
        Log.v(TAG,MSG);
    }

    public static void LOGV(String Tag, String MSG){
        Log.v(Tag,MSG);
    }
}
