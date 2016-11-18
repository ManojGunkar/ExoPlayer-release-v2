package com.globaldelight.boom.analytics;

import android.content.Context;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

/**
 * Created by nidhin on 17/11/16.
 */

public class MixPanelAnalyticHelper {

    public static String projectToken = "598c4011c2d961ef3b7f248459fa30c7";

    public static MixpanelAPI getInstance(Context context) {


        return MixpanelAPI.getInstance(context, projectToken);
    }

    public static void track(Context context, String eventName) {

        getInstance(context).track(eventName);
    }

    public static void track(String eventName, JSONObject properties) {


    }

    public static void trackWithStatus(String eventName, boolean status) {


    }

}
