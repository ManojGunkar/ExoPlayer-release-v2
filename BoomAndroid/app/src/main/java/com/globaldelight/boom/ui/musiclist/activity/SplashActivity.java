package com.globaldelight.boom.ui.musiclist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.task.PlayerService;
import com.globaldelight.boom.utils.handlers.Preferences;
import com.globaldelight.boomplayer.AudioEffect;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Rahul Agarwal on 12-10-16.
 */

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_TIME_OUT = 2000;
    MixpanelAPI mixpanel;
    JSONObject propsFirst, propsLast;
    String currentDate;
    private AudioEffect audioEffectPreferenceHandler;
    public static String getToday(String format) {
        Date date = new Date();
        return new SimpleDateFormat(format).format(date);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!isExpire("DEC-31-2016")) {
            startService(new Intent(this, PlayerService.class));
            new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

                @Override
                public void run() {
                    // This method will be executed once the timer is over
                    // Start your app main activity
                    Intent i = new Intent(SplashActivity.this, BoomPlayerActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    // close this activity
                    finish();
                }
            }, SPLASH_TIME_OUT);
            audioEffectPreferenceHandler = AudioEffect.getAudioEffectInstance(this);
            //flurry
            FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_APP_OPEN);

            //get current date
            currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

            //new Launch of app.Use for tutorial
            if (!Preferences.readBoolean(this, Preferences.APP_NEW_LAUNCH, false)) {
                Preferences.writeString(this, Preferences.INSTALL_DATE, currentDate);
                audioEffectPreferenceHandler.setUserPurchaseType(AudioEffect.purchase.FIVE_DAY_OFFER);
            }

            Preferences.writeBoolean(SplashActivity.this, Preferences.APP_NEW_LAUNCH, true);


            //get last opened date
            String lastOpen = Preferences.readString(this, Preferences.APP_LAST_OPEN, currentDate);
            mixpanel = MixPanelAnalyticHelper.getInstance(this);


            //register first app open once as super property
            propsFirst = new JSONObject();
            try {
                propsFirst.put(AnalyticsHelper.EVENT_FIRST_VISIT, currentDate);
                mixpanel.registerSuperPropertiesOnce(propsFirst);//super property
            } catch (JSONException e) {
                e.printStackTrace();
            }

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
            Preferences.writeString(this, Preferences.APP_LAST_OPEN, currentDate);


        }else{
            Toast.makeText(this, "App Expired...!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private boolean isExpire(String date){
        if(date.isEmpty() || date.trim().equals("")){
            return false;
        }else{
            SimpleDateFormat sdf =  new SimpleDateFormat("MMM-dd-yyyy"); // Jan-20-2015 1:30:55 PM
            Date d=null;
            Date d1=null;
            String today=   getToday("MMM-dd-yyyy");
            try {
                //System.out.println("expdate>> "+date);
                //System.out.println("today>> "+today+"\n\n");
                d = sdf.parse(date);
                d1 = sdf.parse(today);
                if(d1.compareTo(d) <0){// not expired
                    return false;
                }else if(d.compareTo(d1)==0){// both date are same
                    if(d.getTime() < d1.getTime()){// not expired
                        return false;
                    }else if(d.getTime() == d1.getTime()){//expired
                        return true;
                    }else{//expired
                        return true;
                    }
                }else{//expired
                    return true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    protected void onDestroy() {
        MixPanelAnalyticHelper.getInstance(this).flush();
        super.onDestroy();
    }


}
