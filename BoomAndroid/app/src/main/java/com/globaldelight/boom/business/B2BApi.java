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

    public static final int ERROR_INVALID_CODE = -1;
    public static final int ERROR_EXPIRED = -2;
    public static final int ERROR_NOT_REACHABLE = -3;

    public static class Result<T> {
        private int mStatus;
        private T mResult;

        public Result(int status) {
            mStatus = status;
            mResult = null;
        }

        public Result(T result) {
            mStatus = 0;
            mResult = result;
        }

        public int getStatus() {
            return mStatus;
        }

        public T getResult() {
            return mResult;
        }
    }

    private static final String VERIFY_URL = "http://www.boom3dapp.com/registration/verify/";

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

            return new Result<Receipt>(-1);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return new Result<Receipt>(-1);

        }
        catch (IOException e) {
            e.printStackTrace();
            return new Result<Receipt>(-1);
        }
    }

    private OkHttpClient getClient() {
        if ( mClient == null ) {
            mClient = new OkHttpClient();
        }
        return mClient;
    }
}


