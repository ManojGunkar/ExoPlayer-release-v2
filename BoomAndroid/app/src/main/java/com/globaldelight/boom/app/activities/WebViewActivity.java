package com.globaldelight.boom.app.activities;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;

import com.globaldelight.boom.BuildConfig;
import com.globaldelight.boom.R;
import com.globaldelight.boom.utils.Utils;

import java.util.Locale;

/**
 * Created by Venkata N M on 3/20/2017.
 */

public class WebViewActivity  extends Activity {

    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        Uri uri = Uri.parse(BuildConfig.FEEDBACK_URL).buildUpon()
                .appendQueryParameter("language", Locale.getDefault().getLanguage())
                .appendQueryParameter("deviceid", Utils.getFingerPrint(this))
                .build();
        String webViewUrl = uri.toString();

        webView = findViewById(R.id.webView_layout);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setBackgroundColor(0x00000000);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setSupportZoom(true);
        webView.loadUrl(webViewUrl);
    }
}