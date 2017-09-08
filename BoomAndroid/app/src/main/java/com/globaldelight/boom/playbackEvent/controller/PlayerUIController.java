package com.globaldelight.boom.playbackEvent.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.app.receivers.PlayerServiceReceiver;
import com.globaldelight.boom.app.activities.ActivityContainer;
import com.globaldelight.boom.playbackEvent.controller.callbacks.IPlayerUIController;

/**
 * Created by Rahul Agarwal on 24-01-17.
 */

public class PlayerUIController implements IPlayerUIController {
    private Context mContext;

    public PlayerUIController(Context context){
        this.mContext = context;
    }

    @Override
    public void OnPlayPause() {
        IMediaItemBase item =  App.playbackManager().queue().getPlayingItem();
        if(null != item ) {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_PLAY_PAUSE_SONG));
        }
    }

    @Override
    public void OnPlayerSeekChange(int progress) {
        IMediaItemBase item =  App.playbackManager().queue().getPlayingItem();
        if(null != item ) {
            Intent intent = new Intent(PlayerServiceReceiver.ACTION_SEEK_SONG);
            intent.putExtra("seek", progress);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }

    @Override
    public void OnRepeatClick() {
        if(null != App.playbackManager().queue().getPlayingItem())
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_REPEAT_SONG));

    }

    @Override
    public void OnShuffleClick() {
        if(null != App.playbackManager().queue().getPlayingItem())
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_SHUFFLE_SONG));
    }

    @Override
    public void OnNextTrackClick() {
        if(App.playbackManager().queue().isNext())
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_NEXT_SONG));
    }

    @Override
    public void OnPreviousTrackClick() {
        if(App.playbackManager().queue().isPrevious())
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_PREV_SONG));
    }

    @Override
    public void OnPlayerTitleClick(Activity activity) {
    }
}
