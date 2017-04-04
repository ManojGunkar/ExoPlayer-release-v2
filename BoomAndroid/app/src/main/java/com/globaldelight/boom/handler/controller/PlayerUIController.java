package com.globaldelight.boom.handler.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.manager.PlayerServiceReceiver;
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
        IMediaItemBase item =  App.getPlayingQueueHandler().getUpNextList().getPlayingItem();
        if(null != item && !App.getPlayerEventHandler().isTrackWaitingForPlay()) {
            mContext.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_PLAY_PAUSE_SONG));
        }
    }

    @Override
    public void OnPlayerSeekChange(int progress) {
        IMediaItemBase item =  App.getPlayingQueueHandler().getUpNextList().getPlayingItem();
        if(null != item && !App.getPlayerEventHandler().isTrackWaitingForPlay()) {
            Intent intent = new Intent(PlayerServiceReceiver.ACTION_SEEK_SONG);
            intent.putExtra("seek", progress);
            mContext.sendBroadcast(intent);
        }
    }

    @Override
    public void OnRepeatClick() {
        if(null != App.getPlayingQueueHandler().getUpNextList().getPlayingItem())
            mContext.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_REPEAT_SONG));

    }

    @Override
    public void OnShuffleClick() {
        if(null != App.getPlayingQueueHandler().getUpNextList().getPlayingItem())
            mContext.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_SHUFFLE_SONG));
    }

    @Override
    public void OnNextTrackClick() {
        if(App.getPlayingQueueHandler().getUpNextList().isNext() && !App.getPlayerEventHandler().isTrackWaitingForPlay())
            mContext.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_NEXT_SONG));
    }

    @Override
    public void OnPreviousTrackClick() {
        if(App.getPlayingQueueHandler().getUpNextList().isPrevious() && !App.getPlayerEventHandler().isTrackWaitingForPlay())
            mContext.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_PREV_SONG));
    }

    @Override
    public void OnUpNextClick(Activity activity) {
        startUpNextActivity(activity);
    }

    private void startUpNextActivity(Activity activity) {
        Intent queueIntent = new Intent(activity, ActivityContainer.class);
        queueIntent.putExtra("container", R.string.up_next);
        activity.startActivity(queueIntent);
    }

    @Override
    public void OnPlayerTitleClick(Activity activity) {
    }
}
