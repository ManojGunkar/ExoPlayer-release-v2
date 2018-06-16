package com.globaldelight.boom.app.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.globaldelight.boom.app.login.api.LoginApiController;
import com.globaldelight.boom.app.login.api.RequestBody;
import com.globaldelight.boom.app.login.api.request.SocialRequestBody;
import com.globaldelight.boom.utils.Result;
import com.globaldelight.boom.utils.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by adarsh on 13/06/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class BoomAPIHelper {

    public interface Callback <T> {
        void onComplete(Result<T> result);
    }


    private Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private static BoomAPIHelper sInstance = null;

    static BoomAPIHelper getInstance(Context context) {
        if ( sInstance == null ) {
            sInstance = new BoomAPIHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private BoomAPIHelper(Context context) {
        mContext = context;
    }

    public void getLoginPageUrl(Callback<String> callback) {
        fetchAccessToken((result -> {
            String url = LoginApiController.BASE_URL + "register/" + Utils.getDeviceId(mContext) + "/" + result.get() + "/screen?dv=ad";
            callback.onComplete(Result.success(url));
        }));
    }

    public void loginWithFacebook(String accessToken, String inputToken, Callback<Void> callback) {
        SocialRequestBody req = new SocialRequestBody();
        req.setAppaccesstoken(getAccessToken());
        req.setDeviceid(Utils.getDeviceId(mContext));
        req.setSource("facebook");
        req.setAccessToken(accessToken);
        req.setInputToken(inputToken);

        sendSocialInfo(req, callback);
    }

    public void loginWithGoogle(String token, Callback<Void> callback) {
        SocialRequestBody req = new SocialRequestBody();
        req.setAppaccesstoken(getAccessToken());
        req.setDeviceid(Utils.getDeviceId(mContext));
        req.setSource("google");
        req.setIdToken(token);
        sendSocialInfo(req, callback);
    }


    public void fetchAccessToken(Callback<String> callback) {

        if ( getAccessToken() != null ) {
            mHandler.post(()->{
                callback.onComplete(Result.success(getAccessToken()));
            });
        }

        RequestBody body = new RequestBody();
        body.setAppid("com.globaldelight.boom3dandroid");
        body.setApptype("android");
        body.setDeviceid(Utils.getDeviceId(mContext));
        body.setSecretkey(LoginApiController.SECRET_KEY);
        LoginApiController.Callback client = LoginApiController.getClient(LoginApiController.APP_AUTH_BASE_URL);
        Call<JsonElement> call = client.getToken(body);
        call.enqueue(new retrofit2.Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    JsonElement jsonElement = response.body();
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    String access_token = jsonObject.get("appaccesstoken").getAsString();
                    mHandler.post(()->{
                        storeToken(access_token);
                        callback.onComplete(Result.success(access_token));
                    });
                } else {
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
    }

    private void sendSocialInfo(SocialRequestBody requestBody, Callback<Void> completion){
        LoginApiController.Callback callback=LoginApiController.getClient(LoginApiController.APP_AUTH_BASE_URL);
        Call<JsonElement> call=callback.sendSocialInfo(requestBody);
        call.enqueue(new retrofit2.Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()){
                    mHandler.post(() -> completion.onComplete(Result.success((Void)null)));
                }else {
                    mHandler.post(() -> completion.onComplete(Result.error(response.code(), response.message())));
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
    }

    private void storeToken(String accessToken) {
        SharedPreferences prefs = mContext.getSharedPreferences("com.globaldelight.boom", Context.MODE_PRIVATE);
        prefs.edit().putString("boom-access-token", accessToken).apply();


    }

    private String getAccessToken() {
        SharedPreferences prefs = mContext.getSharedPreferences("com.globaldelight.boom", Context.MODE_PRIVATE);
        return prefs.getString("boom-access-token", null);
    }
}
