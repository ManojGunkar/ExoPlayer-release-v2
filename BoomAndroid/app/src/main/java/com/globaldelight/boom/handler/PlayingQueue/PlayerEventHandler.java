package com.globaldelight.boom.handler.PlayingQueue;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.media.AudioManager;

import com.globaldelight.boomplayer.AudioEffect;
import com.globaldelight.boomplayer.OpenSLPlayer;
import com.globaldelight.boomplayer.PlayerEvents;
import com.globaldelight.boom.App;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.task.PlayerService;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;

import static com.globaldelight.boom.handler.PlayingQueue.PlayerEventHandler.PlayState.play;
import static com.globaldelight.boom.handler.PlayingQueue.PlayerEventHandler.PlayState.stop;

/**
 * Created by Rahul Agarwal on 03-10-16.
 */

public class PlayerEventHandler implements QueueEvent, AudioManager.OnAudioFocusChangeListener {
    private static IMediaItemBase playingItem;
    private static OpenSLPlayer mPlayer;
    private static PlayerEventHandler handler;
    private Context context;
    private Handler uiHandler;

    private PlayerService service;
    private AudioManager  audioManager;
    private AudioManager.OnAudioFocusChangeListener focusChangeListener;

    private PlayerEventHandler(Context context, PlayerService service){
        this.context = context;
        mPlayer = new OpenSLPlayer(context, playerEvents);
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        focusChangeListener = this;
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
            audioManager.abandonAudioFocus(this);
            mPlayer.stop();
        }
        playingItem = App.getPlayingQueueHandler().getUpNextList().getPlayingItem();
        if(null != playingItem) {
            int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if ( result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ) {
                mPlayer.setDataSource(((MediaItem) playingItem).getItemUrl());
                mPlayer.play();
                context.sendBroadcast(new Intent(PlayerService.ACTION_GET_SONG));
            }
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
            audioManager.abandonAudioFocus(focusChangeListener);
            context.sendBroadcast(new Intent(PlayerService.ACTION_PLAY_STOP));
        }

        @Override public void onStart(String mime, int sampleRate, int channels, long duration) {

        }
        @Override public void onPlayUpdate(final int percent, final long currentms, final long totalms) {
            Intent intent = new Intent();
            intent.setAction(PlayerService.ACTION_TRACK_POSITION_UPDATE);
            intent.putExtra("percent", percent);
            intent.putExtra("currentms", currentms);
            intent.putExtra("totalms", totalms);
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
            audioManager.abandonAudioFocus(this);
            mPlayer.pause();
            return PlayState.pause;
        } else /*if(mPlayer.isPause())*/{
            int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mPlayer.play();
                return play;
            }
            else {
                return stop;
            }
        }
    }

    public void stop() {
        if(!mPlayer.isStopped()){
            audioManager.abandonAudioFocus(this);
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
        audioManager.abandonAudioFocus(this);
        mPlayer.stop();
    }

    public void addSongToQueue() {
    }

    public void updateEffect() {
        mPlayer.updatePlayerEffect();
    }

    public boolean resetShuffle() {
        return App.getPlayingQueueHandler().getUpNextList().resetShuffle();
    }

    public boolean resetRepeat() {
        return App.getPlayingQueueHandler().getUpNextList().resetRepeat();
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

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            audioManager.abandonAudioFocus(this);
            this.onPlayingItemClicked();
        }
        //TODO: Handle transient changes / ducking
    }
}
