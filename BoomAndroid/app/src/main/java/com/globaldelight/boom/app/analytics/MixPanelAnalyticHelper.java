package com.globaldelight.boom.app.analytics;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.globaldelight.boom.BuildConfig;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by nidhin on 17/11/16.
 */

public class MixPanelAnalyticHelper {

    private MixpanelAPI mixpanel = null;

    private static MixPanelAnalyticHelper instance = null;

    public static MixPanelAnalyticHelper getInstance(Context context) {
        if ( instance == null ) {
            instance = new MixPanelAnalyticHelper(context.getApplicationContext());
        }
        return instance;
    }

    private MixPanelAnalyticHelper(Context context) {
        if ( BuildConfig.MIXPANEL_PROJECT_TOKEN == null ) {
            return;
        }

        mixpanel = MixpanelAPI.getInstance(context.getApplicationContext(), BuildConfig.MIXPANEL_PROJECT_TOKEN);
        String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        mixpanel.identify(android_id);
        JSONObject props = new JSONObject();
        try {
            props.put("DeviceID", android_id);
            props.put("Android Devices", android_id);
            Log.d("DeviceID", android_id);
            // mixpanel.registerSuperProperties(props);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MixpanelAPI.People people = mixpanel.getPeople();
        people.set(props);
        people.identify(android_id);
        people.initPushHandling(BuildConfig.MIXPANEL_SENDER_ID);
    }

    public void registerPush(GoogleCloudMessaging gcm) {
        String registrationId = null;
        try {
            registrationId = gcm.register(BuildConfig.MIXPANEL_SENDER_ID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MixpanelAPI.People people = mixpanel.getPeople();
        people.identify("USER_DISTINCT_ID");
        people.setPushRegistrationId(registrationId);
    }

    public static void initPushNotification(Context context) {
        getInstance(context);
    }

    public static void track(Context context, String eventName) {
        MixPanelAnalyticHelper.getInstance(context).track(eventName);
    }

    public void track(String eventName) {
        if ( mixpanel != null ) {
            mixpanel.track(eventName);
        }
    }

    public void track(String eventName, JSONObject json) {
        if ( mixpanel != null ) {
            mixpanel.track(eventName, json);
        }
    }

    public void registerSuperPropertiesOnce(JSONObject json) {
        if ( mixpanel != null ) {
            mixpanel.registerSuperPropertiesOnce(json);
        }
    }

    public void registerSuperProperties(JSONObject json) {
        if ( mixpanel != null ) {
            mixpanel.registerSuperProperties(json);
        }
    }

    public void setPeopleAnalytics(String event, String value) {
        if ( mixpanel != null ) {
            mixpanel.getPeople().set(event, value);
        }
    }

    public void setPeopleAnalytics(JSONObject json) {
        if ( mixpanel != null ) {
            mixpanel.getPeople().set(json);
        }
    }

    public void flush() {
        if ( mixpanel != null ) {
            mixpanel.flush();
        }
    }

   /* public static void track(Context context,String eventName, JSONObject properties) {
        MixpanelAPI.getInstance().

    }*/

    /*public static void trackWithStatus(String eventName, boolean status) {


    }*/


}
