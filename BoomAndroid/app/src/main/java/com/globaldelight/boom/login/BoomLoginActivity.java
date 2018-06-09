package com.globaldelight.boom.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.globaldelight.boom.R;

/**
 * Created by Manoj Kumar on 09-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class BoomLoginActivity extends AppCompatActivity {


    private String mUrl = "https://login.globaldelight.net/register/1234/c1a9b00d2496a96d899a8c8043302368d9809e9b6b2a4d22a952ce1644df325c/cutom/postmanweb";
    private WebView mWebView;
    private ProgressBar mProgressBar;

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
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.loadUrl(mUrl);
        mWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }
}
