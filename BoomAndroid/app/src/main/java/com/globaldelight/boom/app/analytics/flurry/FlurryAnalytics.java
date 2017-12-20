package com.globaldelight.boom.app.analytics.flurry;

import android.content.Context;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.globaldelight.boom.BuildConfig;

import java.util.Map;

import static android.util.Log.VERBOSE;
import static com.globaldelight.boom.BuildConfig.FLURRY_API_KEY;

/**
 * Created by Manoj Kumar on 6/20/2017.
 */

public class FlurryAnalytics {
    private final String TAG = "FlurryAnalytics";
    private Context mContext;
    private static FlurryAnalytics instance;
    private boolean enabled = true;

    private FlurryAnalytics(Context context) {
        this.mContext = context.getApplicationContext();

        try {
            new FlurryAgent.Builder()
                    .withCaptureUncaughtExceptions(true)
                    .withContinueSessionMillis(10)
                    .withLogEnabled(true)
                    .withLogLevel(VERBOSE)
                    .build(context, FLURRY_API_KEY);
            enabled = true;
        }
        catch (Exception e) {
            enabled = false;
        }
    }

    public static FlurryAnalytics getInstance(Context context) {
        if (instance == null) instance = new FlurryAnalytics(context.getApplicationContext());
        return instance;
    }

    public FlurryAnalytics startSession(Context context) {
        Log.d(TAG, "startSession ");
        if ( enabled ) {
            FlurryAgent.onStartSession(context);

        }
        return this;
    }

    public FlurryAnalytics endSession(Context context) {
        Log.d(TAG, "endSession");
        if ( enabled ) FlurryAgent.onEndSession(context);
        return this;
    }

    public FlurryAnalytics setEvent(String event) {
        Log.d(TAG, event );
        if ( enabled ) FlurryAgent.logEvent(event);
        return this;
    }

    public FlurryAnalytics setEvent(String event, boolean status) {
        Log.d(TAG, event + " status: " + (status? "true" : "false"));
        if ( enabled ) FlurryAgent.logEvent(event, status);
        return this;
    }

    public FlurryAnalytics setEvent(String event, Map<String, String> params) {
        Log.d(TAG, event + " params: " + params.toString());
        if ( enabled ) FlurryAgent.logEvent(event, params);
        return this;
    }

    public FlurryAnalytics setEvent(String event, Map<String, String> params, boolean status) {
        Log.d(TAG, event + " status: " + (status? "true" : "false") + " params: " + params.toString());
        if ( enabled ) FlurryAgent.logEvent(event, params, status);
        return this;
    }

    public FlurryAnalytics pageView() {
        Log.d(TAG, "pageView");
        if ( enabled ) FlurryAgent.onPageView();
        return this;
    }
}
