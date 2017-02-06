package com.globaldelight.boom.handler.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.globaldelight.boom.App;
import com.globaldelight.boom.manager.PlayerServiceReceiver;
import com.globaldelight.boom.ui.musiclist.activity.MediaCollectionActivity;
import com.globaldelight.boom.ui.musiclist.activity.ActivityContainer;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;

/**
 * Created by Rahul Agarwal on 24-01-17.
 */

public class PlayerUIController implements IPlayerUIController {
    private Context mContext;
    private static IPlayerUIController handler;

    public PlayerUIController(Context context){
        this.mContext = context;
    }

    public static void registerPlayerUIController(IPlayerUIController aaIPlayerUIController){
        handler = aaIPlayerUIController;
    }

    public static void unregisterPlayerUIController(){
        handler = null;
    }

    @Override
    public void OnPlayPause() {
        if(null != App.getPlayerEventHandler().getPlayingItem())
            mContext.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_PLAY_PAUSE_SONG));
    }

    @Override
    public void OnPlayerSeekChange(int progress) {
        Intent intent = new Intent(PlayerServiceReceiver.ACTION_SEEK_SONG);
        intent.putExtra("seek", progress);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void OnRepeatClick() {
        mContext.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_REPEAT_SONG));
    }

    @Override
    public void OnShuffleClick() {
        mContext.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_SHUFFLE_SONG));
    }

    @Override
    public void OnNextTrackClick() {
        if(App.getPlayerEventHandler().isNext())
            mContext.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_NEXT_SONG));
    }

    @Override
    public void OnPreviousTrackClick() {
        if(App.getPlayerEventHandler().isPrevious())
        mContext.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_PREV_SONG));
    }

    @Override
    public void OnUpNextClick(Activity activity) {
        startUpNextActivity(activity);
        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_QUEUE_BUTTON_FROM_PLAYER_SCREEN);
    }

    private void startUpNextActivity(Activity activity) {
        Intent queueIntent = new Intent(activity, ActivityContainer.class);
        queueIntent.putExtra("container", "upnext");
        activity.startActivity(queueIntent);
    }

    @Override
    public void OnPlayerTitleClick(Activity activity) {
        MediaItem item = (MediaItem) App.getPlayerEventHandler().getPlayingItem();
        startCollectionListActivity(activity, item);
    }

    private void startCollectionListActivity(final Activity activity, MediaItem item) {
        final Intent listIntent = new Intent(activity, MediaCollectionActivity.class);
        listIntent.putExtra("media_item", item);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                activity.startActivity(listIntent);
            }
        });
    }
}
