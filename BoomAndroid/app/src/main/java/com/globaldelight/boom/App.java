package com.globaldelight.boom;

import android.app.Application;

import com.globaldelight.boom.handler.PlayingQueue.PlayingQueueHandler;
import com.globaldelight.boom.task.PlayerService;
import com.globaldelight.boom.utils.handlers.HistoryFavDBHelper;
import com.globaldelight.boom.utils.handlers.PlaylistDBHelper;
import com.globaldelight.boom.utils.handlers.UserPreferenceHandler;
import com.globaldelight.boom.handler.PlayingQueue.PlayerEventHandler;


public class App extends Application {

    private static App application;

    private static PlayingQueueHandler playingQueueHandler;

    private static PlaylistDBHelper boomPlayListhelper;

    private static HistoryFavDBHelper historyFavDBHelper;
    private static PlayerService service;

    private static UserPreferenceHandler userPreferenceHandler;

    public static void setService(PlayerService service) {
        App.service = service;
    }

    public static PlayerEventHandler getPlayerEventHandler() {
        return PlayerEventHandler.getPlayerEventInstance(application, service);
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

    }

    public static App getApplication() {
        return application;
    }

    public static PlayingQueueHandler getPlayingQueueHandler(){
        return playingQueueHandler;
    }

    public static PlaylistDBHelper getBoomPlayListHelper(){
        return boomPlayListhelper;
    }

    public static HistoryFavDBHelper getHistoryFavDBHelper(){
        return historyFavDBHelper;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        application = null;
        playingQueueHandler.Terminate();
        /*playlistManager = null;*/
    }

    public static UserPreferenceHandler getUserPreferenceHandler() {
        return userPreferenceHandler;
    }
}
