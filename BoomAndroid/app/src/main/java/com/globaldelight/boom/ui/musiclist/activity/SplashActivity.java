package com.globaldelight.boom.ui.musiclist.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.utils.handlers.Preferences;
import com.globaldelight.boomplayer.AudioEffect;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * Created by Rahul Agarwal on 12-10-16.
 */

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_TIME_OUT = 10000;
    MixpanelAPI mixpanel;
    JSONObject propsFirst, propsLast;
    String currentDate;
    private AudioEffect audioEffectPreferenceHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        if (!isTaskRoot()){
            finish();
            return;
        }
        super.onCreate(savedInstanceState);


        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                App.startPlayerService();
                startBoomLibrary();
                // close this activity
                //finish();
            }
        }, SPLASH_TIME_OUT);
        audioEffectPreferenceHandler = AudioEffect.getAudioEffectInstance(this);
        //flurry
        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_APP_OPEN);

        //get current date
        currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        mixpanel = MixPanelAnalyticHelper.getInstance(this);
        //new Launch of app.Use for tutorial
        if (Preferences.readBoolean(this, Preferences.APP_FRESH_LAUNCH, true)) {
            Preferences.writeString(this, Preferences.INSTALL_DATE, currentDate);
            audioEffectPreferenceHandler.setUserPurchaseType(AudioEffect.purchase.FIVE_DAY_OFFER);
            //register first app open once as super property
            propsFirst = new JSONObject();
            try {
                propsFirst.put(AnalyticsHelper.EVENT_FIRST_VISIT, currentDate);
                mixpanel.registerSuperPropertiesOnce(propsFirst);//super property
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //get last opened date
        String lastOpen = Preferences.readString(this, Preferences.APP_LAST_OPEN, currentDate);
        propsLast = new JSONObject();


        try {
            propsLast.put(AnalyticsHelper.EVENT_LAST_APP_OPEN, lastOpen);
            mixpanel.registerSuperProperties(propsLast);//super property
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MixPanelAnalyticHelper.initPushNotification(this);
        MixPanelAnalyticHelper.getInstance(this).getPeople().set(AnalyticsHelper.EVENT_LAST_APP_OPEN, lastOpen);
        MixPanelAnalyticHelper.getInstance(this).getPeople().set(AnalyticsHelper.EVENT_APP_OPEN, currentDate);
          /*  String android_id = Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            MixPanelAnalyticHelper.getInstance(this).getPeople().set("Device_ID", android_id);*/

        Preferences.writeString(this, Preferences.APP_LAST_OPEN, currentDate);
    }

    private void startBoomLibrary(){
        Intent i = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    @Override
    protected void onDestroy() {
        MixPanelAnalyticHelper.getInstance(this).flush();
        super.onDestroy();
    }

    public void validateTrialPeriod() {

        SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy");
        String installDate = Preferences.readString(this, Preferences.INSTALL_DATE, currentDate);


        try {
            Date date1 = myFormat.parse(installDate);
            Date date2 = myFormat.parse(currentDate);
            long diff = date2.getTime() - date1.getTime();
            System.out.println("Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));

            int purchaseType = audioEffectPreferenceHandler.getUserPurchaseType();

            if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) > 5 && purchaseType == AudioEffect.purchase.FIVE_DAY_OFFER.ordinal()) {
                audioEffectPreferenceHandler.setUserPurchaseType(AudioEffect.purchase.NORMAL_USER);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
