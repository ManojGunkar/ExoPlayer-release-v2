package com.globaldelight.boom.app;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.globaldelight.boom.BuildConfig;
import com.globaldelight.boom.R;
import com.globaldelight.boom.business.client.BusinessHandler;
import com.globaldelight.boom.app.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.app.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.playbackEvent.handler.PlayerEventHandler;
import com.globaldelight.boom.playbackEvent.handler.PlayingQueueHandler;
import com.globaldelight.boom.app.service.PlayerService;
import com.globaldelight.boom.app.database.CloudMediaItemDBHelper;
import com.globaldelight.boom.app.database.FavoriteDBHelper;
import com.globaldelight.boom.app.database.PlaylistDBHelper;
import com.globaldelight.boom.app.database.UpNextDBHelper;
import com.globaldelight.boom.app.sharedPreferences.UserPreferenceHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class App extends Application implements Application.ActivityLifecycleCallbacks {

    private static App application;
    private static PlayingQueueHandler playingQueueHandler;
    private static PlaylistDBHelper boomPlayListhelper;
    private static FavoriteDBHelper favoriteDBHelper;
    private static UpNextDBHelper upNextDBHelper;
    private static CloudMediaItemDBHelper cloudMediaItemDBHelper;
//    private static PlayerService service;
    private static UserPreferenceHandler userPreferenceHandler;
    private static DropboxAPI<AndroidAuthSession> dropboxAPI;

//    private static MixpanelAPI mixpanel;

    public static App getApplication() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        mixpanel = MixPanelAnalyticHelper.getInstance(this);
        MixPanelAnalyticHelper.initPushNotification(this);
        try {
            Fabric.with(this, new Crashlytics());
        }catch (Exception e){}
        application = this;



        playingQueueHandler = PlayingQueueHandler.getInstance(application);

        boomPlayListhelper = new PlaylistDBHelper(application);

        favoriteDBHelper = new FavoriteDBHelper(application);

        upNextDBHelper = new UpNextDBHelper(application);

        cloudMediaItemDBHelper = new CloudMediaItemDBHelper(application);

        userPreferenceHandler = new UserPreferenceHandler(application);

        FlurryAnalyticHelper.init(this);

        registerActivityLifecycleCallbacks(this);
    }

    public static PlayerEventHandler getPlayerEventHandler() {
        return PlayerEventHandler.getPlayerEventInstance(application);
    }

    public static UserPreferenceHandler getUserPreferenceHandler() {
        return userPreferenceHandler;
    }

    public static UpNextDBHelper getUPNEXTDBHelper() {
        return upNextDBHelper;
    }

    public static CloudMediaItemDBHelper getCloudMediaItemDBHelper() {
        return cloudMediaItemDBHelper;
    }

    public static PlayingQueueHandler getPlayingQueueHandler() {
        return playingQueueHandler;
    }

    public static PlaylistDBHelper getBoomPlayListHelper() {
        return boomPlayListhelper;
    }

    public static FavoriteDBHelper getFavoriteDBHelper() {
        return favoriteDBHelper;
    }

    public static void startPlayerService(){
        application.startService(new Intent(application, PlayerService.class));
    }

    public static void setDropboxAPI(DropboxAPI<AndroidAuthSession> dropboxAPI) {
        App.dropboxAPI = dropboxAPI;
    }

    public static DropboxAPI<AndroidAuthSession> getDropboxAPI(){
        return App.dropboxAPI;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        application = null;
        playingQueueHandler.Terminate();
       MixPanelAnalyticHelper.getInstance(this).flush();
    }

    public void onActivityCreated(Activity var1, Bundle var2) {

    }

    public void onActivityStarted(Activity activity) {
        Boolean terminate = false;
        int terminateReason = R.string.app_expire;
        if( isExpired() ) {
            terminateReason = R.string.app_expire;
            terminate = true;
        }
        else if ( !isSupportedDevice() ) {
            terminateReason = R.string.app_unsupported;
            terminate = true;
        }

        if ( terminate ) {
            Toast.makeText(this, activity.getResources().getString(terminateReason), Toast.LENGTH_LONG).show();
            activity.finishAndRemoveTask();

            // Terminate the App process after 2 seconds
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.exit(0);
                }
            }, 2000);
        }
    }

    public void onActivityResumed(Activity activity) {
    }

    public void onActivityPaused(Activity var1) {
    }

    public void onActivityStopped(Activity var1) {
    }

    public void onActivitySaveInstanceState(Activity var1, Bundle var2) {
    }

    public void onActivityDestroyed(Activity var1) {

    }

    private boolean isExpired(){
        if( BuildConfig.EXPIRY_DATE != null ) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date expiryDate = sdf.parse(BuildConfig.EXPIRY_DATE);
                Date today = new Date();
                return today.after(expiryDate);
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private boolean isSupportedDevice(){
        return true; //Build.MANUFACTURER.equalsIgnoreCase("Celkon");
    }
}
