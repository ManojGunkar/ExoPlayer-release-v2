package com.globaldelight.boom.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.globaldelight.boom.R;
import com.globaldelight.boom.login.api.LoginApiController;
import com.globaldelight.boom.login.api.RequestBody;
import com.globaldelight.boom.login.api.request.SocialRequestBody;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Manoj Kumar on 09-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class BoomLoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{


    private static final int FACEBOOK_RES_CODE = 01;
    private static final int GOOGLE_RES_CODE = 02;

    private GoogleApiClient mGoogleApiClient;

    private WebView mWebView;
    private ProgressBar mProgressBar;
    private CallbackManager callbackManager;
    private String deviceId ="12345";// Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
   // private String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    private String TAG = "boomlogin";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }


    private void getAuthVerify() {
        RequestBody body = new RequestBody();
        body.setAppid("com.globaldelight.boom3dandroid");
        body.setApptype("android");
        body.setDeviceid(deviceId);
        body.setSecretkey(LoginApiController.SECRET_KEY);
        LoginApiController.Callback client = LoginApiController.getClient(LoginApiController.APP_AUTH_BASE_URL);
        Call<JsonElement> call = client.getToken(body);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "got token");
                    JsonElement jsonElement = response.body();
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    String access_token = jsonObject.get("appaccesstoken").getAsString();
                    String url = LoginApiController.BASE_URL + "register/" + deviceId + "/" + access_token;
                    hitWebView(url);

                } else {
                    Log.d(TAG, "error code-" + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
    }


    private void initView() {
        setContentView(R.layout.activity_boom_login);
        Toolbar toolbar = findViewById(R.id.toolbar_boom_login);
        toolbar.setTitle(R.string.login);
        setSupportActionBar(toolbar);
        mWebView = findViewById(R.id.webView_boom_login);
        mProgressBar = findViewById(R.id.progress_boom_login);
        getAuthVerify();
    }

    private void hitWebView(String url) {
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.loadUrl(url);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d(TAG, "url: " + url);
                if (url.startsWith("https://login.globaldelight.net/social/auth/facebook")) {
                    mWebView.stopLoading();
                    loginWithFb();
                } else if (url.startsWith("https://login.globaldelight.net/social/auth/google/")) {
                    mWebView.stopLoading();
                    loginWithGp();
                } else if (url.startsWith("boomApp://")) {
                    finish();
                } else {
                    super.onPageStarted(view, url, favicon);
                }
            }

            public void onPageFinished(WebView view, String url) {
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void loginWithFb() {

        FacebookSdk.sdkInitialize(this.getApplicationContext());

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "Facebook access token" + loginResult.getAccessToken().toString());
                        getUserDetails(loginResult);
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });
    }

    protected void getUserDetails(LoginResult loginResult) {
        GraphRequest data_request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                (json_object, response) -> {
                    Log.d(TAG, "userProfile" + json_object.toString());
                });
        Bundle permission_param = new Bundle();
        permission_param.putString("fields", "id,name,email,picture.width(120).height(120)");
        data_request.setParameters(permission_param);
        data_request.executeAsync();
    }

    private void loginWithGp() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
              //  .requestIdToken("862807752058-d5g81f41tptroo7p1ovihbvgg2cklq3j.apps.googleusercontent.com")
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GOOGLE_RES_CODE);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();

            Log.e(TAG, "display name: " + acct.getDisplayName());

            String personName = acct.getDisplayName();
            String personPhotoUrl = acct.getPhotoUrl().toString();
            String email = acct.getEmail();

            SocialRequestBody body=new SocialRequestBody();
          //  body.set

            Log.e(TAG, "Name: " + personName + ", email: " + email
                    + ", Image: " + personPhotoUrl);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (callbackManager != null)
            callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_RES_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            String token=result.getSignInAccount().getIdToken();
            handleSignInResult(result);
            Log.d(TAG,"code:"+result.getStatus().getStatusCode());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();
    }


    private void sendSocialInfo(SocialRequestBody requestBody){
        LoginApiController.Callback callback=LoginApiController.getClient(LoginApiController.APP_AUTH_BASE_URL);
        Call<JsonElement> call=callback.sendSocialInfo(requestBody);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()){
                    Log.d(TAG,"Send successfully");

                }else {
                    Log.d(TAG,"error code"+response.code());

                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
