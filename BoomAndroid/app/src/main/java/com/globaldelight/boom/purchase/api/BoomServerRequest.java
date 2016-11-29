package com.globaldelight.boom.purchase.api;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.globaldelight.boom.utils.Logger;
import com.globaldelight.boom.utils.handlers.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by nidhin on 29/11/16.
 */

public class BoomServerRequest {
    private final static String SECRET_KEY = "b32a9642113a9e52fe9d200a031f14792eef5d2e589ecad2638f5dd189dfcb23";
    private final static String APP_TYPE = "android";
    private final static String APP_ID = "com.globaldelight.boom";
    private final static String BASE_URL = "http://devboom.globaldelight.net/";
    //private final static String DEVICE_ID="android";


    public static String getAccessToken(final Context context) {
        String accessToken = Preferences.getPreferences(context).getString(Preferences.ACCESS_TOKEN, "");
        if (accessToken.equals("")) {

            final String URL = BASE_URL + "appauthentication/";
            String deviceid = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            // Post params to be sent to the server
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("secretkey", SECRET_KEY);
            params.put("apptype", APP_TYPE);
            params.put("appid", APP_ID);
            params.put("deviceid", deviceid);

            JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            String appaccesstoken = "";
                            try {
                                appaccesstoken = response.getString("appaccesstoken");
                                Preferences.writeString(context, Preferences.ACCESS_TOKEN, appaccesstoken);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            Logger.LOGD("Response:%n %s", response.toString());

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.e("Error: ", error.getMessage());
                }
            });

            // add the request object to the queue to be executed
            //   ApplicationController.getInstance().addToRequestQueue(req);
            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(req);
        }

        return accessToken;
    }

    public static void register(final Context ctx) {
        String model = android.os.Build.MODEL;
        String build = Build.PRODUCT;
        String osVersion = System.getProperty("os.version");
        // String myDeviceModel = android.os.Build.MODEL;


        final String URL = BASE_URL + "register/";
        String deviceid = Settings.Secure.getString(ctx.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("model", model);
        params.put("OSVersion", osVersion);
        params.put("build", build);
        params.put("deviceid", deviceid);
        params.put("appaccesstoken", getAccessToken(ctx));

        JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Logger.LOGD("Response:%n %s", response.toString());

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        // add the request object to the queue to be executed
        //   ApplicationController.getInstance().addToRequestQueue(req);
        RequestQueue queue = Volley.newRequestQueue(ctx);
        queue.add(req);

    }

        /* "_id":ObjectId("583d1e76d6a8a7f30c5b7846"),
              "deviceid":"272CCD55-921C-4C81-9EC6-8C676738D18E”, < Sent from app>
          "created_at":"1480401135",
              "appaccesstoken":"de45cda219d5874bd25ce994b6fdb26e9968f8a447f4814d25a779e245b1e740”,
          "language":"en”, < Sent from app>
          "country":"IN",< Sent from app>
              "updated_at":"1480402058",
              "arn":"arn:aws:sns:us-east-1:724555098295:endpoint/APNS_SANDBOX/iBoom_Dev/c7ba0799-0c7b-3b22-b819-1936d6ef77d5”, < Sent from app - push notification id>
          "version":"1.1.1”, < Sent from app>
          "devicetoken":"67fc025f854e9736fe220439800154174dd2a608bb99c5f5577d986c6ab1db4b”,
          "build":"1.1.1003”, < Sent from app>
          "model":"iPhone 6s Plus”, < Sent from app>
          "OSVersion":"10.1.1”, < Sent from app>
          "timeZoneOffset”:19800 < Sent from app>*/

    public static void saveUserMail(final Context ctx) {
        final String URL = BASE_URL + "saveuseremail/";
        String deviceid = Settings.Secure.getString(ctx.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("model", SECRET_KEY);

        // params.put("OSVersion", osVersion);
        /// params.put("build", build);
        // params.put("deviceid", deviceid);

        JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String appaccesstoken = "";
                        try {
                            appaccesstoken = response.getString("appaccesstoken");
                            Preferences.writeString(ctx, Preferences.ACCESS_TOKEN, appaccesstoken);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        Logger.LOGD("Response:%n %s", response.toString());

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        // add the request object to the queue to be executed
        //   ApplicationController.getInstance().addToRequestQueue(req);
        RequestQueue queue = Volley.newRequestQueue(ctx);
        queue.add(req);
    }
}
