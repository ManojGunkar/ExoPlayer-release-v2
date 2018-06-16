package com.globaldelight.boom.app.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.login.api.LoginApiController;
import com.globaldelight.boom.app.login.api.RequestBody;
import com.globaldelight.boom.app.login.api.request.SocialRequestBody;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
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

    public static final String ACTION_LOGIN_SUCCESS = "com.globaldelight.boom.LOGIN_SUCCESS";


    private static final int FACEBOOK_RES_CODE = 01;
    private static final int GOOGLE_RES_CODE = 02;
    private static final String FB_ACCESS_TOKEN = "218364201905576|tkOpImTZkiQaAu1ci-aQogE82pw";
    private static final String SERVER_CLIENT_ID = "312070820740-1tq83s4oo4i46b6psmb09ncao9eg2mrj.apps.googleusercontent.com";

    private GoogleSignInClient mGoogleClient;

    private WebView mWebView;
    private ProgressBar mProgressBar;
    private CallbackManager callbackManager;
    private String TAG = "boomlogin";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }


    private void initView() {
        setContentView(R.layout.activity_boom_login);
        Toolbar toolbar = findViewById(R.id.toolbar_boom_login);
        toolbar.setTitle(R.string.login);
        setSupportActionBar(toolbar);
        mWebView = findViewById(R.id.webView_boom_login);
        mProgressBar = findViewById(R.id.progress_boom_login);
        BoomAPIHelper.getInstance(this).getLoginPageUrl(result -> {
            hitWebView(result.get());
        });
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
                } else if (url.startsWith("boom3dapp://")) {
                    Uri uri = Uri.parse(url);
                    String session = uri.getQueryParameter("session");
                    String userid = uri.getQueryParameter("userid");
                    onLoginSuccess();
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
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        loginWithFacebook(loginResult);
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


    private void loginWithFacebook(LoginResult result) {
        BoomAPIHelper.getInstance(BoomLoginActivity.this).loginWithFacebook(FB_ACCESS_TOKEN, result.getAccessToken().getToken(), (res)->{
            if ( true || res.isSuccess() ) {
                onLoginSuccess();
            }
            else {
                // show login failed
            }
        });
    }

    private void loginWithGp() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(SERVER_CLIENT_ID)
                .requestEmail()
                .build();

        mGoogleClient =  GoogleSignIn.getClient(this, gso);
        startActivityForResult(mGoogleClient.getSignInIntent(), GOOGLE_RES_CODE);
    }


    private void handleSignInResult(@NonNull Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();

            // TODO(developer): send ID Token to server and validate
            BoomAPIHelper.getInstance(this).loginWithGoogle(idToken, result -> {
                if ( result.isSuccess() ) {
                    onLoginSuccess();
                }
            });

        } catch (ApiException e) {
            Log.w(TAG, "handleSignInResult:error", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (callbackManager != null)
            callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_RES_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private void onLoginSuccess() {
        LocalBroadcastManager.getInstance(BoomLoginActivity.this).sendBroadcast(new Intent(ACTION_LOGIN_SUCCESS));
        finish();
    }
}
