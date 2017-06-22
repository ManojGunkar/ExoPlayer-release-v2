package com.globaldelight.boom.app.analytics.flurry;

import android.content.Context;

import com.flurry.android.FlurryAgent;

import java.util.Map;

/**
 * Created by Manoj Kumar on 6/20/2017.
 */

public class FlurryAnalytics {

    private Context context;
    private static FlurryAnalytics instance;
    
    private final static String DEVELOPMENT_FLURRY_API_KEY = "MGGVKKG7JYXGC3N89B4V";
    private final static String PRODUCTION_FLURRY_API_KEY = "XRPWTN4PDGJJQX6NNJPN";

    private FlurryAnalytics(Context context) {
        this.context = context;
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(context.getApplicationContext(), DEVELOPMENT_FLURRY_API_KEY);
    }

    public static FlurryAnalytics getInstance(Context context) {
        if (instance == null) instance = new FlurryAnalytics(context);
        return instance;
    }

    public FlurryAnalytics startSession() {
        FlurryAgent.onStartSession(context);
        return this;
    }

    public  FlurryAnalytics endSession() {
        FlurryAgent.onEndSession(context);
        return this;
    }

    public FlurryAnalytics setEvent(String event) {
        FlurryAgent.logEvent(event);
        return this;
    }

    public FlurryAnalytics setEvent(String event, boolean status) {
        FlurryAgent.logEvent(event, status);
        return this;
    }

    public FlurryAnalytics setEvent(String event, Map<String, String> params) {
        FlurryAgent.logEvent(event, params);
        return this;
    }

    public FlurryAnalytics setEvent(String event, Map<String, String> params, boolean status) {
        FlurryAgent.logEvent(event, params, status);
        return this;
    }
}
