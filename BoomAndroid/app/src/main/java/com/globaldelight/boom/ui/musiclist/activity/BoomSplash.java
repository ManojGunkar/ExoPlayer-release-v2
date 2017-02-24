package com.globaldelight.boom.ui.musiclist.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
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
import java.text.SimpleDateFormat;
import java.util.Date;

public class BoomSplash extends AppCompatActivity {
    /*private View mSplashFrame;*/
    private static final long SPLASH_TIME_OUT = 2000;
    MixpanelAPI mixpanel;
    JSONObject propsFirst, propsLast;
    String currentDate;
    private AudioEffect audioEffectPreferenceHandler;

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
                App.startPlayerService();
                startBoomLibrary();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BoomSplash.this);
                if(null == preferences.getString("Tool_install_date", null)) {
                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putString("Tool_install_date", String.valueOf(System.currentTimeMillis()));
                    edit.commit();
                }
            }
        }, SPLASH_TIME_OUT);
    }

    private void updateDataValues() {
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
        Intent i;
        if(Preferences.readBoolean(this, Preferences.ACTION_ONBOARDING_SHOWN, true)){
            i = new Intent(BoomSplash.this, OnBoardingActivity.class);
        }else{
            i = new Intent(BoomSplash.this, MainActivity.class);
        }
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    @Override
    protected void onDestroy() {
        MixPanelAnalyticHelper.getInstance(this).flush();
        super.onDestroy();
    }
}
