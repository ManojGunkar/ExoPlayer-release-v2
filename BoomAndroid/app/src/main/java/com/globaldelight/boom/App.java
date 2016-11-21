package com.globaldelight.boom;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.handler.PlayingQueue.PlayerEventHandler;
import com.globaldelight.boom.handler.PlayingQueue.PlayingQueueHandler;
import com.globaldelight.boom.task.PlayerService;
import com.globaldelight.boom.utils.PlayerSettings;
import com.globaldelight.boom.utils.handlers.HistoryFavDBHelper;
import com.globaldelight.boom.utils.handlers.PlaylistDBHelper;
import com.globaldelight.boom.utils.handlers.Preferences;
import com.globaldelight.boom.utils.handlers.UserPreferenceHandler;


public class App extends Application implements SensorEventListener {

    private static App application;

    private static PlayingQueueHandler playingQueueHandler;

    private static PlaylistDBHelper boomPlayListhelper;

    private static HistoryFavDBHelper historyFavDBHelper;
    private static PlayerService service;

    private static UserPreferenceHandler userPreferenceHandler;
    float mAccelLast;
    float mAccelCurrent;
    float mAccel;
    SensorManager mSensorManager;


    public static void setService(PlayerService service) {
        App.service = service;
    }

    public static PlayerEventHandler getPlayerEventHandler() {
        return PlayerEventHandler.getPlayerEventInstance(application, service);
    }

    public static App getApplication() {
        return application;
    }

    public static PlayingQueueHandler getPlayingQueueHandler() {
        return playingQueueHandler;
    }

    public static PlaylistDBHelper getBoomPlayListHelper() {
        return boomPlayListhelper;
    }

    public static HistoryFavDBHelper getHistoryFavDBHelper() {
        return historyFavDBHelper;
    }

    public static UserPreferenceHandler getUserPreferenceHandler() {
        return userPreferenceHandler;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        application = this;

        playingQueueHandler = PlayingQueueHandler.getHandlerInstance(application);

        boomPlayListhelper = new PlaylistDBHelper(application);

        historyFavDBHelper = new HistoryFavDBHelper(application);

        playingQueueHandler.getUpNextList();

        userPreferenceHandler = new UserPreferenceHandler(application);
        FlurryAnalyticHelper.init(this);


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
        Log.i("Sensor", "mAccel" + mAccel);

    }

    public void sendMessageShakeEvent() {
        Intent intent = new Intent();
        intent.setAction(PlayerSettings.ACTION_SHAKE_EVENT);

        String selectedShakeOption = Preferences.readString(this, Preferences.SHAKE_GESTURE_PREF, PlayerSettings.ShakeGesture.SHAKE_GESTURE_NONE.toString());
        //PlayerSettings.ShakeGesture selGesture = PlayerSettings.ShakeGesture.toShakeGesture(selectedShakeOption);
        /*switch (selGesture) {
            case SHAKE_GESTURE_NONE:

                break;
            case SHAKE_GESTURE_NEXT:

                break;
            case SHAKE_GESTURE_PLAY:

                break;


        }*/
        intent.putExtra(PlayerSettings.SHAKE_EVENT_ACTION_TYPE, selectedShakeOption);
        sendBroadcast(intent);

    }
}
