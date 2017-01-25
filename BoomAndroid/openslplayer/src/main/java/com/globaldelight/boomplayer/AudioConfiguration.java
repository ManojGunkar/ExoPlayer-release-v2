package com.globaldelight.boomplayer;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;

import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by adarsh on 25/01/17.
 */

public class AudioConfiguration {
    // values for quality
    final static int QUALITY_LOW = 1;
    final static int QUALITY_MID = 2;
    final static int QUALITY_HIGH = 3;

    // values for float
    final static int FORMAT_FLOAT = 1;
    final static int FORMAT_INT16 = 2;


    private static final String AUDIO_CONFIG_SETTING = "audio_config_settings";
    private int quality = QUALITY_HIGH;
    private int format = FORMAT_FLOAT;
    private Context context;
    private RequestQueue queue;


    private static AudioConfiguration instance = null;
    public static synchronized AudioConfiguration getInstance(Context context) {
        if ( instance == null ) {
            instance = new AudioConfiguration(context);
        }
        return instance;
    }


    private AudioConfiguration(Context context) {
        this.context = context.getApplicationContext();
        queue = Volley.newRequestQueue(this.context);
    }

    public void load() {
        readConfig();
        if ( isNetworkConnected(context) ) {
            downloadConfig();
        }
    }

    public int getQuality() {
        return quality;
    }

    public int getFormat() {
        return format;
    }

    private void saveConfig(String quality, String format) {
        SharedPreferences shp = context.getSharedPreferences(AUDIO_CONFIG_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putString("format", format);
        editor.putString("quality", quality);
        editor.commit();
    }

    private void readConfig() {
        SharedPreferences shp = context.getSharedPreferences(AUDIO_CONFIG_SETTING, Context.MODE_PRIVATE);
        format = toFormat(shp.getString("format", "float"));
        quality = toQuality(shp.getString("quality", "high"));
    }


    private void downloadConfig() {
        final String baseURL = "http://devboom2.globaldelight.net/audioconfig.php";

        Uri uri = Uri.parse(baseURL).buildUpon()
                .appendQueryParameter("action", "query")
                .appendQueryParameter("manufacturer", Build.MANUFACTURER)
                .appendQueryParameter("modelname", Build.MODEL)
                .build();

        String URL = uri.toString();


        Response.Listener<String> listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    String qualityStr = json.getString("quality");
                    String formatStr = json.getString("format");
                    quality = toQuality(qualityStr);
                    format = toFormat(formatStr);
                    saveConfig(qualityStr, formatStr);
                }
                catch (Exception e) {

                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        };

        StringRequest configRequest = new StringRequest(Request.Method.GET, URL, listener, errorListener);
;

        queue.add(configRequest);
    }

    private int toQuality(String quality) {
        switch (quality) {
            default:
            case "high":
                return QUALITY_HIGH;
            case "mid":
                return QUALITY_MID;
            case "low":
                return QUALITY_LOW;
        }
    }

    private int toFormat(String format) {
        switch (format) {
            default:
            case "float":
                return FORMAT_FLOAT;
            case "pcm16":
                return FORMAT_INT16;
        }
    }

    private static boolean isNetworkConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = manager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.isConnected();
    }
}
