package com.globaldelight.boom.business;

import android.content.Context;
import android.os.Build;

import com.globaldelight.boom.BuildConfig;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by adarsh on 12/12/17.
 */

public class B2BApi {

    private static final String VERIFY_URL = "http://www.boom3dapp.com/registration/verify/";
    private static final String VERSION_URL = "https://www.boom3dapp.com/registration/version/android/";
    private static final String FEEDBACK_URL = "https://www.boom3dapp.com/feedback/send/android";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Context mContext;
    private OkHttpClient mClient;

    private static B2BApi sInstance = null;
    public static B2BApi getInstance(Context context) {
        if ( sInstance == null ) {
            sInstance = new B2BApi(context.getApplicationContext());
        }
        return sInstance;
    }

    private B2BApi(Context context) {
        mContext = context;
    }

    public Result<Receipt> verify(String code) {
        try {
            String iid = InstanceID.getInstance(mContext).getId();

            JSONObject json = new JSONObject();
            json.put("code", code);
            json.put("deviceId", iid);
            json.put("model", Build.MODEL);
            json.put("build", Integer.toString(BuildConfig.VERSION_CODE));
            json.put("version", BuildConfig.VERSION_NAME);
            json.put("vendor", "inceptive");

            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url(VERIFY_URL)
                    .post(body)
                    .build();

            Response response = getClient().newCall(request).execute();
            if ( response.isSuccessful() ) {
                String string = response.body().string();
                JSONObject result = new JSONObject(string);
                int status = result.getInt("status");
                if ( status == 200 ) {
                    Receipt receipt = new Receipt("none", code, iid, Build.MODEL);
                    return new Result<Receipt>(receipt);
                }
            }

            return new Result<Receipt>(ErrorCode.FAILED);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return new Result<Receipt>(ErrorCode.FAILED);

        }
        catch (IOException e) {
            e.printStackTrace();
            return new Result<Receipt>(ErrorCode.NETWORK_ERROR);
        }
    }

    public Result<Void> submitFeedback(String email, String subject, String description) {
        try {
            String iid = InstanceID.getInstance(mContext).getId();

            JSONObject json = new JSONObject();

            json.put("email", email);
            json.put("subject", subject);
            json.put("description", description);
            json.put("deviceId", iid);
            json.put("model", Build.MODEL);
            json.put("build", Integer.toString(BuildConfig.VERSION_CODE));
            json.put("version", BuildConfig.VERSION_NAME);
            json.put("vendor", "inceptive");

            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url(FEEDBACK_URL)
                    .post(body)
                    .build();

            Response response = getClient().newCall(request).execute();
            if ( response.isSuccessful() ) {
                String string = response.body().string();
                JSONObject result = new JSONObject(string);
                int status = result.getInt("status");
                if ( status == 200 ) {
                    return new Result<Void>(ErrorCode.SUCCESS);
                }
            }

            return new Result<Void>(ErrorCode.FAILED);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return new Result<Void>(ErrorCode.FAILED);

        }
        catch (IOException e) {
            e.printStackTrace();
            return new Result<Void>(ErrorCode.NETWORK_ERROR);
        }
    }

    public Result<String> checkForUpdate() {
        try {
            Request request = new Request.Builder()
                    .url(VERSION_URL)
                    .build();

            Response response = getClient().newCall(request).execute();
            if ( response.isSuccessful() ) {
                String string = response.body().string();
                JSONObject result = new JSONObject(string);
                String version = result.getString("version");
                return new Result<String>(version);
            }

            return new Result<String>(ErrorCode.FAILED);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return new Result<String>(ErrorCode.FAILED);

        }
        catch (IOException e) {
            e.printStackTrace();
            return new Result<String>(ErrorCode.NETWORK_ERROR);
        }
    }

    private OkHttpClient getClient() {
        if ( mClient == null ) {
            mClient = new OkHttpClient();
        }
        return mClient;
    }
}


