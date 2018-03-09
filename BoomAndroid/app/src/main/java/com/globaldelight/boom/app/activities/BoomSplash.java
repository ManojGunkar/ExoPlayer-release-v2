package com.globaldelight.boom.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.AnalyticsHelper;
import com.globaldelight.boom.app.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.app.sharedPreferences.Preferences;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BoomSplash extends AppCompatActivity {
    private static final long SPLASH_TIME_OUT = 1000;
    private static final String TAG = "BoomSplash";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()){
            finish();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSplashCash();
        updateDataValues();
    }

    private void initSplashCash() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startBoom();
            }
        }, SPLASH_TIME_OUT);
    }

    private void startBoom() {
        App.startPlayerService();
        startBoomLibrary();
    }

    private void updateDataValues() {
        JSONObject propsFirst, propsLast;
        String currentDate;
        MixpanelAPI mixpanel;

        FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.EVENT_APP_OPEN);

        //get current date
        currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        //new Launch of app.Use for tutorial
        if (Preferences.readBoolean(this, Preferences.APP_FRESH_LAUNCH, true)) {
            //register first app open once as super property
            Preferences.writeBoolean(this,Preferences.APP_FRESH_LAUNCH, false);
            propsFirst = new JSONObject();
            try {
                propsFirst.put(AnalyticsHelper.EVENT_FIRST_VISIT, currentDate);
                MixPanelAnalyticHelper.getInstance(this).registerSuperPropertiesOnce(propsFirst);//super property
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //get last opened date
        String lastOpen = Preferences.readString(this, Preferences.APP_LAST_OPEN, currentDate);
        propsLast = new JSONObject();


        try {
            propsLast.put(AnalyticsHelper.EVENT_LAST_APP_OPEN, lastOpen);
            MixPanelAnalyticHelper.getInstance(this).registerSuperProperties(propsLast);//super property
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        MixPanelAnalyticHelper.initPushNotification(this);
        MixPanelAnalyticHelper.getInstance(this).setPeopleAnalytics(AnalyticsHelper.EVENT_LAST_APP_OPEN, lastOpen);
        MixPanelAnalyticHelper.getInstance(this).setPeopleAnalytics(AnalyticsHelper.EVENT_APP_OPEN, currentDate);
          /*  String android_id = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            MixPanelAnalyticHelper.getInstance(this).getPeople().set("Device_ID", android_id);*/

        Preferences.writeString(this, Preferences.APP_LAST_OPEN, currentDate);
    }


    private void startBoomLibrary(){
        Intent i;
        if(Preferences.readBoolean(this, Preferences.ACTION_ONBOARDING_SHOWN, true)){
            i = new Intent(BoomSplash.this, OnBoardingActivity.class);
        }else{
            i = new Intent(BoomSplash.this, LibraryActivity.class);
        }
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MixPanelAnalyticHelper.getInstance(this).flush();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAnalytics.getInstance(this).startSession(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAnalytics.getInstance(this).endSession(this);
    }
}
