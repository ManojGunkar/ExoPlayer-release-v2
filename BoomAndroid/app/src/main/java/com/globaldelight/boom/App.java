package com.globaldelight.boom;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.globaldelight.boom.business.client.BusinessHandler;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.handler.PlayingQueue.PlayerEventHandler;
import com.globaldelight.boom.handler.PlayingQueue.PlayingQueueHandler;
import com.globaldelight.boom.task.PlayerService;
import com.globaldelight.boom.utils.handlers.CloudMediaItemDBHelper;
import com.globaldelight.boom.utils.handlers.FavoriteDBHelper;
import com.globaldelight.boom.utils.handlers.PlaylistDBHelper;
import com.globaldelight.boom.utils.handlers.Preferences;
import com.globaldelight.boom.utils.handlers.UpNextDBHelper;
import com.globaldelight.boom.utils.handlers.UserPreferenceHandler;
import com.globaldelight.boom.utils.Utils;

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
    private static PlayerService service;
    private static UserPreferenceHandler userPreferenceHandler;
    private static DropboxAPI<AndroidAuthSession> dropboxAPI;

    private static BusinessHandler businessHandler;

    public static App getApplication() {
        return application;
    }

    public static void setService(PlayerService service) {
        App.service = service;
    }

    public static PlayerService getService(){
        return App.service;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Fabric.with(this, new Crashlytics());
        }catch (Exception e){}
        application = this;

        playingQueueHandler = PlayingQueueHandler.getHandlerInstance(application);

        boomPlayListhelper = new PlaylistDBHelper(application);

        favoriteDBHelper = new FavoriteDBHelper(application);

        upNextDBHelper = new UpNextDBHelper(application);

        cloudMediaItemDBHelper = new CloudMediaItemDBHelper(application);

        playingQueueHandler.getUpNextList();

        userPreferenceHandler = new UserPreferenceHandler(application);

        try{
            businessHandler = BusinessHandler.getBusinessHandlerInstance(application);
        }catch (Exception e){}

        FlurryAnalyticHelper.init(this);

        registerActivityLifecycleCallbacks(this);

        showInternetPopUp();
    }

    private void showInternetPopUp() {

        Utils.InternetPopup(application);
    }

    public static BusinessHandler getBusinessHandler(){
        return businessHandler;
    }

    public static PlayerEventHandler getPlayerEventHandler() {
        return PlayerEventHandler.getPlayerEventInstance(application, service);
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
        if(isExpired()) {
            Toast.makeText(this, activity.getResources().getString(R.string.app_expire), Toast.LENGTH_LONG).show();
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
        if(Utils.isAppExpireEnable()) {
            String expiryDateString = "MAR-20-2017";
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");
                Date expiryDate = sdf.parse(expiryDateString);
                Date today = new Date();
                return today.after(expiryDate);
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}
