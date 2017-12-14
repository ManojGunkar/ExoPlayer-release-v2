package com.globaldelight.boom.business;

import android.content.Context;
import android.os.Build;

import com.globaldelight.boom.BuildConfig;
import com.globaldelight.boom.utils.Utils;

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

    public static final String STORE_PAGE_URL = "https://www.boom3dapp.com/store/android/version/";
    private static final String VERIFY_URL = "http://www.boom3dapp.com/registration/verify/";
    private static final String VERSION_URL = "https://www.boom3dapp.com/registration/version/android/";

    private static final String LIMIT_REACHED = "DEVICE LIMIT REACHED";
    private static final String INVALID_CODE = "NOT VALID CODE";

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
            String iid = Utils.getFingerPrint(mContext);

            JSONObject json = new JSONObject();
            json.put("code", code);
            json.put("deviceid", iid);
            json.put("model", Build.MODEL);
            json.put("build", Integer.toString(BuildConfig.VERSION_CODE));
            json.put("version", BuildConfig.VERSION_NAME);

            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url(VERIFY_URL)
                    .post(body)
                    .build();

            Response response = getClient().newCall(request).execute();
            if ( response.isSuccessful() ) {
                Receipt receipt = Receipt.fromJSON(response.body().string());
                return new Result<Receipt>(receipt);
            }
            else {
                return new Result<Receipt>(parseError(response));
            }
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

    private @ErrorCode int parseError(Response response) {
        switch ( response.message() ) {
            case INVALID_CODE:
                return ErrorCode.INVALID_CODE;
            case LIMIT_REACHED:
                return ErrorCode.LIMIT_EXCEEDED;
            default:
                return ErrorCode.FAILED;
        }
    }
}


