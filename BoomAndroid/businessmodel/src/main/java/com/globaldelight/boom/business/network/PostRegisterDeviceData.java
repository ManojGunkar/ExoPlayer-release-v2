package com.globaldelight.boom.business.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.globaldelight.boom.business.model.PostRegiObject;
import com.globaldelight.boom.business.model.ResultRegiDeviceObject;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Venkata N M on 1/12/2017.
 */

public class PostRegisterDeviceData extends HandleJsobObject {
    private static PostRegisterDeviceData mInstance = null;

    private PostRegisterDeviceData(Context context) {
        super(context);
    }

    public static PostRegisterDeviceData getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PostRegisterDeviceData(context);
        }
        return mInstance;
    }

    public void postRegisterDeviceData(String appaccesstoken,
                                       String deviceid,
                                       String country,
                                       String language,
                                       String build,
                                       String version,
                                       String model,
                                       String devicetoken,
                                       String arn,
                                       String OSVersion,
                                       String timeZoneOffset,
                                       final OnServerResponseListener<ResultRegiDeviceObject, String> listener) {
//        PostData applications = new PostData(appid, apptype, deviceid, secretkey);

        PostRegiObject applications = new PostRegiObject(appaccesstoken,deviceid,country,language,build,version,model,devicetoken,arn,OSVersion,timeZoneOffset);
        JSONObject applicationquery = null;
        String applicationString = null;
        JSONObject application = null;
        applicationString = getGson().toJson(applications);
        try {
            application = new JSONObject(applicationString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new PostJsonCustomRequest(Request.Method.POST, UrlFactory.postRegisterDeviceData(), application, new Response.Listener<JSONObject>() {
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
                ResultRegiDeviceObject resultRegiDeviceObject = getNewGson().fromJson(response.toString(), ResultRegiDeviceObject.class);

                if (listener != null) {
                    if (businessMOdelType != null) {
                        listener.onSuccess(resultRegiDeviceObject);
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

