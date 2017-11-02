package com.globaldelight.boom.player;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;

import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.globaldelight.boom.*;
import com.globaldelight.boom.BuildConfig;

import org.json.JSONObject;

/**
 * Created by adarsh on 25/01/17.
 */

public class AudioConfiguration {
    // values for float
    final static int FORMAT_FLOAT = 1;
    final static int FORMAT_INT16 = 2;


    private static final String AUDIO_CONFIG_SETTING = "audio_config_settings";
    private  @Constants.Quality int quality = Constants.Quality.HIGH;
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

    public @Constants.Quality int getQuality() {
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
        final String BASE_URL = BuildConfig.AUDIO_CONFIG_URL;
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("action", "query")
                .appendQueryParameter("manufacturer", Build.MANUFACTURER)
                .appendQueryParameter("modelname", Build.MODEL)
                .build();

        String configURL = uri.toString();


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

        StringRequest configRequest = new StringRequest(Request.Method.GET, configURL, listener, errorListener);
        queue.add(configRequest);
    }

    private @Constants.Quality int toQuality(String quality) {
        switch (quality) {
            default:
            case "high":
                return Constants.Quality.HIGH;
            case "mid":
                return Constants.Quality.MID;
            case "low":
                return Constants.Quality.LOW;
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
