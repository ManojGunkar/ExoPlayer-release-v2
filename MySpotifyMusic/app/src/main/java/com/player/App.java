package com.player;

import android.app.Application;

import com.player.data.Handler.PlayingQueueHandler;
import com.player.utils.handlers.HistoryFavDBHelper;
import com.player.utils.handlers.PlaylistDBHelper;


public class App extends Application {

    private static App application;

    private static PlayingQueueHandler playingQueueHandler;

    private static PlaylistDBHelper boomPlayListhelper;

    private static HistoryFavDBHelper historyFavDBHelper;


    @Override
    public void onCreate() {
        super.onCreate();

        application = this;

        playingQueueHandler = PlayingQueueHandler.getHandlerInstance(application);

        boomPlayListhelper = new PlaylistDBHelper(application);

        historyFavDBHelper = new HistoryFavDBHelper(application);

        playingQueueHandler.getPlayingQueue().getPlayingQueue();

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
}
