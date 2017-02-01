package com.globaldelight.boom.business.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.globaldelight.boom.business.model.JsonResultObjects;
import com.globaldelight.boom.business.model.PostData;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Venkata N M on 1/12/2017.
 */

public class PostHandleData extends HandleJsobObject {
    private static PostHandleData mInstance = null;
    String responseServer;
    private String apptype = "ios";
    private String appid = "com.globaldelight.iBoom";
    private String country = "IN";
    private String deviceid = "1E183651-4198-4D1F-9D09-C3EF418AEBA6";
    private String locale = "en";
    private String secretkey = "0adbc1e4481e6839a903ec3509d84ac36af95471c6cf1eb2093165efcf1023be";

    private PostHandleData(Context context) {
        super(context);
    }

    public static PostHandleData getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PostHandleData(context);
        }
        return mInstance;
    }

    public void postApplication(String appid, String apptype, String country, String deviceid, String locale, String secretkey, final OnServerResponseListener<JsonResultObjects, String> listener) {
        PostData applications = new PostData(appid, apptype, country, deviceid, locale, secretkey);
        JSONObject applicationquery = null;
        String applicationString = null;
        JSONObject application = null;
        applicationString = getGson().toJson(applications);
        try {
            application = new JSONObject(applicationString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new PostJsonCustomRequest(Request.Method.POST, UrlFactory.getAddsConfigData(), application, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String businessMOdelType = null;
//                String jobSeekerId = null;
                try {
                    VolleyLog.v("Response:%n %s", response.toString(4));
                    businessMOdelType = response.optString("buisness_model_type");
//                    jobSeekerId=response.optString("userId");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JsonResultObjects jsonResultObjects = getNewGson().fromJson(response.toString(), JsonResultObjects.class);

                if (listener != null) {
                    if (businessMOdelType != null) {
                        listener.onSuccess(jsonResultObjects);
                    } else {
                        listener.onFailure("Failed To fetch Data");
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                if (listener != null) {
                    listener.onFailure(getErrorMessage(error));
                }
            }
        });
        getRequestQueue().add(request);

    }

}