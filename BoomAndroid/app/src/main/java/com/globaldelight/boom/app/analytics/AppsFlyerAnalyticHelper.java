package com.globaldelight.boom.app.analytics;

import android.app.Application;
import android.content.Context;

import com.appsflyer.AppsFlyerLib;

import java.util.Map;

/**
 * Created by nidhin on 18/11/16.
 */

public class AppsFlyerAnalyticHelper {
public static final String AppsFlyerKey="h2ZJnNztpho9T5a7pbyxt9";


    public static void startTracking(Application application) {
        AppsFlyerLib.getInstance().startTracking(application,AppsFlyerKey);
    }
    public static void trackEvent(Context context, String eventName, Map eventValues){

        AppsFlyerLib.getInstance().trackEvent( context,  eventName,  eventValues);
    }
}
