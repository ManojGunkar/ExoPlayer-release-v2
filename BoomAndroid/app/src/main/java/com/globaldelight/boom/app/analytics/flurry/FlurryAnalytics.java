package com.globaldelight.boom.app.analytics.flurry;

import android.content.Context;

import com.flurry.android.FlurryAgent;
import com.globaldelight.boom.BuildConfig;

import java.util.Map;

import static android.util.Log.VERBOSE;
import static com.globaldelight.boom.BuildConfig.FLURRY_API_KEY;

/**
 * Created by Manoj Kumar on 6/20/2017.
 */

public class FlurryAnalytics {

    private Context context;
    private static FlurryAnalytics instance;

    private FlurryAnalytics(Context context) {
        this.context = context.getApplicationContext();

        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .withCaptureUncaughtExceptions(true)
                .withContinueSessionMillis(10)
                .withLogEnabled(true)
                .withLogLevel(VERBOSE)
                .build(context, FLURRY_API_KEY);
    }

    public static FlurryAnalytics getInstance(Context context) {
        if (instance == null) instance = new FlurryAnalytics(context.getApplicationContext());
        return instance;
    }

    public FlurryAnalytics startSession() {
        FlurryAgent.onStartSession(context);
        return this;
    }

    public FlurryAnalytics endSession() {
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
