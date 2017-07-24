package com.globaldelight.boom.business.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.globaldelight.boom.business.model.PostSaveEmailObject;
import com.globaldelight.boom.business.model.RespSaveEmailObject;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Venkata N M on 1/12/2017.
 */

public class PostSaveEmailData extends HandleJsobObject {
    private static PostSaveEmailData mInstance = null;

    private PostSaveEmailData(Context context) {
        super(context);
    }

    public static PostSaveEmailData getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PostSaveEmailData(context);
        }
        return mInstance;
    }

    public void PostSaveEmailData(String appaccesstoken, String deviceid, boolean newsletteroptin, String source, String emailid, final OnServerResponseListener<RespSaveEmailObject, String> listener) {
        PostSaveEmailObject applications = new PostSaveEmailObject(appaccesstoken,  deviceid, newsletteroptin, source, emailid);
        JSONObject applicationquery = null;
        String applicationString = null;
        JSONObject application = null;
        applicationString = getGson().toJson(applications);
        try {
            application = new JSONObject(applicationString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new PostJsonCustomRequest(Request.Method.POST, UrlFactory.postSaveEmailAddress(), application, new Response.Listener<JSONObject>() {
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
                RespSaveEmailObject acessTokenObject = getNewGson().fromJson(response.toString(), RespSaveEmailObject.class);

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

