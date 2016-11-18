package com.globaldelight.boom.handler.PlayingQueue;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.globaldelight.boomplayer.AudioEffect;
import com.globaldelight.boomplayer.OpenSLPlayer;
import com.globaldelight.boomplayer.PlayerEvents;
import com.globaldelight.boom.App;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.task.PlayerService;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;

import static com.globaldelight.boom.handler.PlayingQueue.PlayerEventHandler.PlayState.play;

/**
 * Created by Rahul Agarwal on 03-10-16.
 */

public class PlayerEventHandler implements QueueEvent {
    private static IMediaItemBase playingItem;
    private static OpenSLPlayer mPlayer;
    private static PlayerEventHandler handler;
    private Context context;
    private Handler uiHandler;

    private PlayerService service;

    private PlayerEventHandler(Context context, PlayerService service){
        this.context = context;
        mPlayer = new OpenSLPlayer(context, playerEvents);
        this.service = service;
        App.getPlayingQueueHandler().getUpNextList().setQueueEvent(this);
        uiHandler = new Handler();
    }

    public static PlayerEventHandler getPlayerEventInstance(Context context, PlayerService service){
        if(handler == null){
            handler = new PlayerEventHandler(context, service);
        }
        return handler;
    }

    public OpenSLPlayer getPlayer(){
        return mPlayer;
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
    public synchronized void onPlayingItemChanged() {

        if(isPlaying() || mPlayer.isPause()) {
            mPlayer.stop();
        }
        playingItem = App.getPlayingQueueHandler().getUpNextList().getPlayingItem();
        if(null != playingItem) {
            mPlayer.setDataSource(((MediaItem) playingItem).getItemUrl());
            mPlayer.play();
            context.sendBroadcast(new Intent(PlayerService.ACTION_GET_SONG));
        }else{
            context.sendBroadcast(new Intent(PlayerService.ACTION_PLAY_STOP));
        }
    }

    public void playNextSong(boolean isUser) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        App.getPlayingQueueHandler().getUpNextList().setNextPlayingItem(isUser);
    }

    public void playPrevSong() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        App.getPlayingQueueHandler().getUpNextList().setPreviousPlayingItem();
    }

    @Override
    public void onPlayingItemClicked() {
        PlayState state = PlayPause();

        Intent intent = new Intent();
        intent.setAction(PlayerService.ACTION_PLAYING_ITEM_CLICKED);
        intent.putExtra("play_pause", state == play ? true : false );
        context.sendBroadcast(intent);
    }

    @Override
    public void onQueueUpdated() {
        Intent intent = new Intent();
        intent.setAction(PlayerService.ACTION_UPNEXT_UPDATE);
        context.sendBroadcast(intent);
    }

    PlayerEvents playerEvents = new PlayerEvents() {
        @Override public void onStop() {
            context.sendBroadcast(new Intent(PlayerService.ACTION_PLAY_STOP));
        }

        @Override public void onStart(String mime, int sampleRate, int channels, long duration) {

        }
        @Override public void onPlayUpdate(final int percent, final long currentms, final long totalms) {
            Intent intent = new Intent();
            intent.setAction(PlayerService.ACTION_TRACK_POSITION_UPDATE);
            intent.putExtra("percent", percent);
            context.sendBroadcast(intent);
        }

        @Override
        public void onFinish() {
            playNextSong(false);
        }

        @Override public void onPlay() {
            boolean i =isPlaying();
            Log.d("hbjhbn", "jnkj"+i);
        }
        @Override public void onError() {
        }
    };

    public PlayState PlayPause() {
        if(isPlaying()){
            mPlayer.pause();
            return PlayState.pause;
        } else /*if(mPlayer.isPause())*/{
            mPlayer.play();
            return play;
        }
    }

    public void stop() {
        if(!mPlayer.isStopped()){
            mPlayer.stop();
        }
    }

    public void release() {
        mPlayer = null;
    }

    public IMediaItemBase getPlayingItem() {
        return playingItem;
    }

    public void seek(int progress) {
        mPlayer.seek(progress);
    }


    public void setEffectEnable(boolean enable) {
        mPlayer.setEnableEffect(enable);
    }

    public void set3DAudioEnable(boolean enable) {
        mPlayer.setEnable3DAudio(enable);
    }

    public void setIntensityValue(double value) {
        mPlayer.setIntensityValue(value);
    }

    public void setEqualizerEnable(boolean enable) {
        mPlayer.setEnableEqualizer(enable);
    }

    public void setSuperBassEnable(boolean enable) {
        mPlayer.setEnableSuperBass(enable);
    }

    public void setEqualizerGain(int position) {
        mPlayer.setEqualizerGain(position);
    }

    public void setSpeakerEnable(AudioEffect.Speaker speaker, boolean enable) {
        mPlayer.setSpeakerEnable(speaker, enable);
    }

    public void setHighQualityEnable(boolean highQualityEnable) {
        mPlayer.setHighQualityEnable(highQualityEnable);
    }

    public void stopPlayer() {
        mPlayer.stop();
    }

    public void addSongToQueue() {
    }

    public void updateEffect() {
        mPlayer.updatePlayerEffect();
    }

    public void resetShuffle() {
        App.getPlayingQueueHandler().getUpNextList().setShuffle();
    }

    public void resetRepeat() {
        App.getPlayingQueueHandler().getUpNextList().setRepeat();
    }

    public boolean isPrevious() {
        return App.getPlayingQueueHandler().getUpNextList().isPrevious();
    }

    public boolean isNext() {
        return App.getPlayingQueueHandler().getUpNextList().isNext();
    }

    public enum PlayState {
        play,
        pause,
        stop
    }
}
