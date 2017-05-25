package com.globaldelight.boom.app.analytics;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by nidhin on 17/11/16.
 */

public class MixPanelAnalyticHelper {

    public static String projectToken = "598c4011c2d961ef3b7f248459fa30c7";

    private static String SENDER_ID = "862807752058";
    public static  MixpanelAPI mixpanel;

    public static MixpanelAPI getInstance(Context context) {
         mixpanel = MixpanelAPI.getInstance(context, projectToken);
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
        people.initPushHandling(SENDER_ID);
        return mixpanel;
    }

        public void registerPush(GoogleCloudMessaging gcm){
        String registrationId = null;
        try {
            registrationId = gcm.register(SENDER_ID);
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

   /* public static void track(Context context,String eventName, JSONObject properties) {
        MixpanelAPI.getInstance().

    }*/

    /*public static void trackWithStatus(String eventName, boolean status) {


    }*/


}
