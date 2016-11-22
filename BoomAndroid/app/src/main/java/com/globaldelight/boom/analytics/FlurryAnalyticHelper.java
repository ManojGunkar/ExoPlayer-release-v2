/*
 *  Copyright 2015 Yahoo Inc.
 *  Licensed under the terms of the zLib license. Please see LICENSE file for terms.
 */

package com.globaldelight.boom.analytics;

import android.content.Context;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryEventRecordStatus;
import com.globaldelight.boom.utils.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Helps with logging custom events and errors to Flurry
 */
public class FlurryAnalyticHelper {

public static void init(Context context){

    new FlurryAgent.Builder()
            .withLogEnabled(true)
            .withLogLevel(Log.DEBUG)
            .build(context, "PX85XVXZH4HVWST8Z7V3");

    Logger.LOGD("Initialized FLurry Agent");


}


    public static FlurryEventRecordStatus logEvent(String event) {
        return FlurryAgent.logEvent(event);
    }

    public static FlurryEventRecordStatus logEventWithStatus(String event, boolean status) {

        Map<String, String> articleParams = new HashMap<>();
        if (status)
            articleParams.put(AnalyticsHelper.PARAM_STATUS, AnalyticsHelper.PARAM_STATUS_ON);
        else
            articleParams.put(AnalyticsHelper.PARAM_STATUS, AnalyticsHelper.PARAM_STATUS_OFF);


        return FlurryAgent.logEvent(event, articleParams);
    }

    public static void logEvent(String eventName, Map<String, String> eventParams) {
        FlurryAgent.logEvent(eventName, eventParams);
    }

    /**
     * Logs an event for analytics.
     *
     * @param eventName   name of the event
     * @param eventParams event parameters (can be null)
     * @param timed       <code>true</code> if the event should be timed, false otherwise
     */
    public static void logEvent(String eventName, Map<String, String> eventParams, boolean timed) {
        FlurryAgent.logEvent(eventName, eventParams, timed);
    }

    /**
     * Ends a timed event that was previously started.
     *
     * @param eventName   name of the event
     * @param eventParams event parameters (can be null)
     */
    public static void endTimedEvent(String eventName, Map<String, String> eventParams) {
        FlurryAgent.endTimedEvent(eventName, eventParams);
    }


    /**
     * Ends a timed event without event parameters.
     *
     * @param eventName name of the event
     */
    public static void endTimedEvent(String eventName) {
        FlurryAgent.endTimedEvent(eventName);
    }

    /**
     * Logs an error.
     *
     * @param errorId          error ID
     * @param errorDescription error description
     * @param throwable        a {@link Throwable} that describes the error
     */
    public static void logError(String errorId, String errorDescription, Throwable throwable) {
        FlurryAgent.onError(errorId, errorDescription, throwable);
    }

    /**
     * Logs location.
     *
     * @param latitude  latitude of location
     * @param longitude longitude of location
     */
    public static void logLocation(double latitude, double longitude) {
        FlurryAgent.setLocation((float) latitude, (float) longitude);
    }

    /**
     * Logs page view counts.
     */
    public static void logPageViews() {
        FlurryAgent.onPageView();
    }

}
