package com.globaldelight.boom.business.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;


public class HandleJsobObject {

    private Context mContext = null;
    private RequestQueue mQueue = null;

    /**
     * Shared Preferences
     */
    private static SharedPreferences mPrefs;
    private static SharedPreferences.Editor mEditor;

    private HashMap<Context, Integer> pendingRequests;

    protected HandleJsobObject(Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);
        mContext = context;
        mQueue = queue;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mPrefs.edit();
    }

    protected Context getContext() {
        return mContext;
    }

    public RequestQueue getRequestQueue() {
        return mQueue;
    }

    protected SharedPreferences getSharedPreferences() {
        return mPrefs;
    }

    protected SharedPreferences.Editor getEditor() {
        return mEditor;
    }

    public Gson getGson() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
        return gson;
    }
    public Gson getNewGson() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();
        return gson;
    }
    public Gson getfFormatGson() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        return gson;
    }

    public String getErrorMessage(VolleyError error) {
        if (error.getStackTrace() != null) {
            error.printStackTrace();
        }

        if (error.networkResponse == null) {
            return error.getLocalizedMessage();
        }

        String errorJsonString = new String(error.networkResponse.data);
        try {
            JSONObject errorJson = new JSONObject(errorJsonString);
            JSONObject errorObj = errorJson.optJSONObject("errors");
            if (errorObj != null) {
                String message = errorObj.optString("message", null);
                if (message != null) {
                    return message;
                }

                Iterator keys = errorObj.keys();
                while (keys.hasNext()) {
                    String nextKey = (String) keys.next();
                    if (nextKey.equals("error_code")) {
                        continue;
                    }

                    Object nextObject = errorObj.opt(nextKey);

                    if (nextObject instanceof String) {
                        return (String) nextObject;
                    }

                    if (nextObject instanceof JSONArray) {
                        JSONArray nextArray = (JSONArray) nextObject;
                        if (nextArray.length() > 0) {
                            String nextMsg = nextArray.optString(0);
                            if (nextMsg != null) {
                                return nextMsg;
                            }
                        }
                    }
                }
            }
        } catch (JSONException ex) {
            try {
                String html = Html.fromHtml(errorJsonString).toString();
                return html;
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        } catch (Exception ex2) {
            ex2.printStackTrace();
        }

        return null;
    }

    protected void addToPendingRequests(Context context) {
        if (pendingRequests == null) {
            pendingRequests = new HashMap<Context, Integer>();
        }

        if (pendingRequests.containsKey(context)) {
            pendingRequests.put(context, pendingRequests.get(context) + 1);
        } else {
            pendingRequests.put(context, 1);
        }
    }

    protected void removeItemFromPendingRequests(Context context) {
        if (pendingRequests == null) {
            return;
        }

        if (pendingRequests.containsKey(context)) {
            Integer numRequests = pendingRequests.get(context) - 1;

            if (numRequests == 0) {
                pendingRequests.remove(context);
            } else {
                pendingRequests.put(context, numRequests);
            }
        }
    }

    protected boolean isRequestPendingForActivity(Context context) {
        if (pendingRequests == null) {
            return false;
        }

        return pendingRequests.containsKey(context);
    }
}
