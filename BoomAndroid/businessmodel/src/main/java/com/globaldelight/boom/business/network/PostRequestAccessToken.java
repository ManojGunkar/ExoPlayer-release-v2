package com.globaldelight.boom.business.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.globaldelight.boom.business.model.AcessTokenObject;
import com.globaldelight.boom.business.model.PostData;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Venkata N M on 1/12/2017.
 */

public class PostRequestAccessToken extends HandleJsobObject {
    private static PostRequestAccessToken mInstance = null;

    private PostRequestAccessToken(Context context) {
        super(context);
    }

    public static PostRequestAccessToken getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PostRequestAccessToken(context);
        }
        return mInstance;
    }

    public void posttReqAccessToken(String appid, String apptype, String country, String deviceid, String secretkey, final OnServerResponseListener<AcessTokenObject, String> listener) {
        PostData applications = new PostData(appid, apptype, deviceid, secretkey);
        JSONObject applicationquery = null;
        String applicationString = null;
        JSONObject application = null;
        applicationString = getGson().toJson(applications);
        try {
            application = new JSONObject(applicationString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new PostJsonCustomRequest(Request.Method.POST, UrlFactory.getAccesTokenUrl(), application, new Response.Listener<JSONObject>() {
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
                AcessTokenObject acessTokenObject = getNewGson().fromJson(response.toString(), AcessTokenObject.class);

                if (listener != null) {
                    if (businessMOdelType != null) {
                        listener.onSuccess(acessTokenObject);
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

