package com.globaldelight.boom;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.globaldelight.boom.manager.PlayerServiceReceiver;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.handler.PlayingQueue.PlayerEventHandler;
import com.globaldelight.boom.handler.PlayingQueue.PlayingQueueHandler;
import com.globaldelight.boom.task.PlayerService;
import com.globaldelight.boom.utils.PlayerSettings;
import com.globaldelight.boom.utils.handlers.FavoriteDBHelper;
import com.globaldelight.boom.utils.handlers.PlaylistDBHelper;
import com.globaldelight.boom.utils.handlers.Preferences;
import com.globaldelight.boom.utils.handlers.UpNextDBHelper;
import com.globaldelight.boom.utils.handlers.UserPreferenceHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;


public class App extends Application implements SensorEventListener, Application.ActivityLifecycleCallbacks {

    private static App application;

    private static PlayingQueueHandler playingQueueHandler;

    private static PlaylistDBHelper boomPlayListhelper;

    private static FavoriteDBHelper favoriteDBHelper;
    private static UpNextDBHelper upNextDBHelper;
    private static PlayerService service;

    private static UserPreferenceHandler userPreferenceHandler;
    float mAccelLast;
    float mAccelCurrent;
    float mAccel;
    SensorManager mSensorManager;
    private static DropboxAPI<AndroidAuthSession> dropboxAPI;


    public static void setService(PlayerService service) {
        App.service = service;
    }

    public static PlayerService getService(){
        return App.service;
    }

    public static PlayerEventHandler getPlayerEventHandler() {
        return PlayerEventHandler.getPlayerEventInstance(application, service);
    }

    public static App getApplication() {
        return application;
    }

    public static UserPreferenceHandler getUserPreferenceHandler() {
        return userPreferenceHandler;
    }

    public static UpNextDBHelper getUPNEXTDBHelper() {
        return upNextDBHelper;
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

        playingQueueHandler.getUpNextList();

        userPreferenceHandler = new UserPreferenceHandler(application);
        FlurryAnalyticHelper.init(this);

        registerActivityLifecycleCallbacks(this);


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        application = null;
        playingQueueHandler.Terminate();
        /*playlistManager = null;*/
        mSensorManager.unregisterListener(this);
        MixPanelAnalyticHelper.getInstance(this).flush();

    }

    @Override
    public void onSensorChanged(SensorEvent se) {
        float x = se.values[0];
        float y = se.values[1];
        float z = se.values[2];
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta; // perform low-cut filter

        if (mAccel > 8) {
            //Toast.makeText(getApplicationContext(),
            //       "You have shaken your phone", Toast.LENGTH_SHORT).show();
            sendMessageShakeEvent();
            // TODO

        }

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        Log.i("Sensor", "mAccel" + mAccel);

    }

    public synchronized void sendMessageShakeEvent() {
        Intent intent = new Intent();
        intent.setAction(PlayerSettings.ACTION_SHAKE_EVENT);

        String selectedShakeOption = Preferences.readString(this, Preferences.SHAKE_GESTURE_PREF, PlayerSettings.ShakeGesture.SHAKE_GESTURE_NONE.toString());
        PlayerSettings.ShakeGesture selGesture = PlayerSettings.ShakeGesture.toShakeGesture(selectedShakeOption);
        switch (selGesture) {
            case SHAKE_GESTURE_NONE:

                break;
            case SHAKE_GESTURE_NEXT:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(null != service) {
                            sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_NEXT_SONG));
                        }
                    }
                }, 1000);
                break;
            case SHAKE_GESTURE_PLAY:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(null != service) {
                            sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_PLAY_PAUSE_SONG));
                        }
                    }
                }, 1000);
                break;
        }
    }


    public void onActivityCreated(Activity var1, Bundle var2) {

    }

    public void onActivityStarted(Activity activity) {
        if(isExpired()) {
            Toast.makeText(this, "App Expired...!", Toast.LENGTH_LONG).show();
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
//        String expiryDateString = "FEB-10-2017";
//        try {
//            SimpleDateFormat sdf =  new SimpleDateFormat("MMM-dd-yyyy");
//            Date expiryDate = sdf.parse(expiryDateString);
//            Date today= new Date();
//            return today.after(expiryDate);
//        } catch (ParseException e) {
//            e.printStackTrace();
//            return false;
//        }
        return false;
    }
}
