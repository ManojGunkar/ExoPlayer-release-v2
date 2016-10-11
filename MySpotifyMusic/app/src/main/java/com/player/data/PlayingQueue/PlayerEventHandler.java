package com.player.data.PlayingQueue;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.example.openslplayer.OpenSLPlayer;
import com.example.openslplayer.PlayerEvents;
import com.player.App;
import com.player.data.DeviceMediaCollection.MediaItem;
import com.player.data.MediaCollection.IMediaItemBase;
import com.player.ui.UIEvent;

/**
 * Created by Rahul Agarwal on 03-10-16.
 */

public class PlayerEventHandler implements QueueEvent {
    private static IMediaItemBase playingItem;
    private static OpenSLPlayer mPlayer;
    private static PlayerEventHandler handler;
    private Context context;
    private UIEvent uiEvent = null;
    private Handler uiHandler;

    private PlayerEventHandler(Context context){
        this.context = context;
        mPlayer = new OpenSLPlayer(playerEvents);
        App.getPlayingQueueHandler().getPlayingQueue().setQueueEvent(this);
        uiHandler = new Handler();
    }

    public static PlayerEventHandler getQueueEventInstance(Context context){
        if(handler == null){
            handler = new PlayerEventHandler(context);
        }
        return handler;
    }

    public boolean isPlaying(){
        return mPlayer.isPlaying();
    }

    public boolean isPaused(){
        return mPlayer.isPause();
    }

    public boolean isStopped() {
        return mPlayer.isStopped();
    }

    @Override
    public void onPlayingItemChanged() {

        if(isPlaying() || mPlayer.isPause()){
            mPlayer.stop();
        }

        playingItem = App.getPlayingQueueHandler().getPlayingQueue().getPlayingItem();
        mPlayer.setDataSource(((MediaItem) playingItem).getItemUrl());
        mPlayer.play();
    }

    @Override
    public void onQueueUpdated() {

    }

    public void setUIEvent(UIEvent event){
        this.uiEvent = event;
    }

    PlayerEvents playerEvents = new PlayerEvents() {
        @Override public void onStop() {
            if (uiEvent != null)
                uiHandler.post(new Runnable() {
                    @Override public void run() {
                        uiEvent.stop();
                    }
                });
        }
        @Override public void onStart(String mime, int sampleRate, int channels, long duration) {

        }
        @Override public void onPlayUpdate(final int percent, final long currentms, final long totalms) {
            if (uiEvent != null)
                uiHandler.post(new Runnable() {
                    @Override public void run() {
                        uiEvent.updateSeek(percent, currentms, totalms);
                    }
                });
        }

        @Override
        public void onFinish() {

        }

        @Override public void onPlay() {
            if (uiEvent != null)
                uiHandler.post(new Runnable() {
                    @Override public void run() {
                        uiEvent.updateUI();
                    }
                });
        }
        @Override public void onError() {
        }
    };

    public int Play() {
        if(isPlaying()){
            mPlayer.pause();
            return 0;
        }else if(App.getPlayingQueueHandler().getPlayingQueue().getPlayingItem() != null && mPlayer.isStopped()){
            mPlayer.setDataSource(((MediaItem) playingItem).getItemUrl());
            mPlayer.play();
            return 1;
        }else if(App.getPlayingQueueHandler().getPlayingQueue().getPlayingItem() != null && !mPlayer.isStopped()){
            mPlayer.play();
            return 1;
        }else{
            Toast.makeText(context, "Playing Item is Empty.", Toast.LENGTH_LONG).show();
            return -1;
        }
    }

    public void stop() {
        if(!mPlayer.isStopped()){
            mPlayer.stop();
        }
    }

    public void next() {
        if(App.getPlayingQueueHandler().getPlayingQueue().getPlayingQueue().get(QueueType.Manual_UpNext).size() > 0){

            App.getPlayingQueueHandler().getPlayingQueue().addListItemToPlaying(QueueType.Manual_UpNext, 0);

        }else if(App.getPlayingQueueHandler().getPlayingQueue().getPlayingQueue().get(QueueType.Auto_UpNext).size() > 0){

            App.getPlayingQueueHandler().getPlayingQueue().addListItemToPlaying(QueueType.Auto_UpNext, 0);

        }else{
            Toast.makeText(context, "Up Next is Empty.", Toast.LENGTH_LONG).show();
        }
    }

    public void previous() {
        if(App.getPlayingQueueHandler().getPlayingQueue().getPlayingQueue().get(QueueType.Manual_UpNext).size() > 0){

            App.getPlayingQueueHandler().getPlayingQueue().addListItemToPlaying(QueueType.Manual_UpNext, App.getPlayingQueueHandler().getPlayingQueue().getPlayingQueue().get(QueueType.Manual_UpNext).size()-1);

        }else if(App.getPlayingQueueHandler().getPlayingQueue().getPlayingQueue().get(QueueType.Auto_UpNext).size() > 0){

            App.getPlayingQueueHandler().getPlayingQueue().addListItemToPlaying(QueueType.Auto_UpNext, App.getPlayingQueueHandler().getPlayingQueue().getPlayingQueue().get(QueueType.Auto_UpNext).size()-1);

        }else{
            Toast.makeText(context, "Up Next is Empty.", Toast.LENGTH_LONG).show();
        }
    }

    public IMediaItemBase getPlayingItem() {
        return playingItem;
    }

    public void seek(int progress) {
        mPlayer.seek(progress);
    }
}
