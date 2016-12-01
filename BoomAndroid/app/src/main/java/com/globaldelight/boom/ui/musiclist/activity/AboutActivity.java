package com.globaldelight.boom.ui.musiclist.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {
    Context mContext;
    TextView lblAboutTitle, lblABoutDesc, lblRateApp, lblShareApp, lblContactUs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutt);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        mContext = this;
        lblAboutTitle = (TextView) findViewById(R.id.lbl_title_about);
        lblABoutDesc = (TextView) findViewById(R.id.lbl_desc_about);
        lblRateApp = (TextView) findViewById(R.id.rate_app);
        lblShareApp = (TextView) findViewById(R.id.share_app);
        lblContactUs = (TextView) findViewById(R.id.contact_us);
        lblRateApp.setOnClickListener(this);
        lblShareApp.setOnClickListener(this);
        lblContactUs.setOnClickListener(this);

        String title = getResources().getString(R.string.title_about);
        Spannable wordtoSpan = new SpannableString(title);

        wordtoSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.white_active)), 0, title.length() - 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblAboutTitle.setText(wordtoSpan);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rate_app:
                Uri uri = Uri.parse("market://details?id=" + mContext.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + mContext.getPackageName())));
                }
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ABOUT_RATE_BUTTON_TAPPED);
                break;
            case R.id.share_app:
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "BOOM");
                    String sAux = "\nLet me recommend you this application\n\n";
                    sAux = sAux + "https://play.google.com/store/apps/details?id=com.globaldelight.boom \n\n";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "choose one"));
                } catch (Exception e) {
                    //e.toString();
                }
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ABOUT_SHARE_BUTTON_TAPPED);

                break;
            case R.id.contact_us:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "boomandroid@globaldelight.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "I Need Some Help With Boom for iOS");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello Team,");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ABOUT_CONTACT_US_BUTTON_TAPPED);

                break;

        }
    }
}