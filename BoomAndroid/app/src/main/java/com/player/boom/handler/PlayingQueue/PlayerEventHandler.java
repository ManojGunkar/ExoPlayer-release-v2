package com.player.boom.handler.PlayingQueue;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.example.openslplayer.AudioEffect;
import com.example.openslplayer.OpenSLPlayer;
import com.example.openslplayer.PlayerEvents;
import com.player.boom.App;
import com.player.boom.data.DeviceMediaCollection.MediaItem;
import com.player.boom.data.MediaCollection.IMediaItemBase;
import com.player.boom.handler.IPlayerUIEvent;

/**
 * Created by Rahul Agarwal on 03-10-16.
 */

public class PlayerEventHandler implements QueueEvent {
    private static IMediaItemBase playingItem;
    private static OpenSLPlayer mPlayer;
    private static PlayerEventHandler handler;
    private Context context;
    private IPlayerUIEvent playerUIEvent = null;
    private IQueueUIEvent queueUIEvent = null;
    private Handler uiHandler;

    private PlayerEventHandler(Context context){
        this.context = context;
        mPlayer = new OpenSLPlayer(context, playerEvents);
        App.getPlayingQueueHandler().getPlayingQueue().setQueueEvent(this);
        uiHandler = new Handler();
    }

    public static PlayerEventHandler getPlayerEventInstance(Context context){
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
    public synchronized void onPlayingItemChanged() {

        if(isPlaying() || mPlayer.isPause())
            mPlayer.stop();
        playingItem = App.getPlayingQueueHandler().getPlayingQueue().getPlayingItem();
        if(null != playingItem) {
            mPlayer.setDataSource(((MediaItem) playingItem).getItemUrl());
            mPlayer.play();
        }
        if (playerUIEvent != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    playerUIEvent.updateUI();
                }
            });
        }
        if (queueUIEvent != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    queueUIEvent.onQueueUiUpdated();
                }
            });
        }
    }

    @Override
    public void onPlayingItemClicked() {
        PlayPause();
    }

    @Override
    public void onQueueUpdated() {
        if (queueUIEvent != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    queueUIEvent.onQueueUiUpdated();
                }
            });
        }
    }

    public void setQueueUIEvent(IQueueUIEvent event){
        this.queueUIEvent = event;
    }

    public void setPlayerUIEvent(IPlayerUIEvent event){
        this.playerUIEvent = event;
    }

    PlayerEvents playerEvents = new PlayerEvents() {
        @Override public void onStop() {
            if (playerUIEvent != null) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        playerUIEvent.stop();
                    }
                });
            }
        }
        @Override public void onStart(String mime, int sampleRate, int channels, long duration) {
            if (playerUIEvent != null)
                uiHandler.postDelayed(new Runnable() {
                    @Override public void run() {
                        playerUIEvent.updateUI();
                    }
                }, 80);
        }
        @Override public void onPlayUpdate(final int percent, final long currentms, final long totalms) {
            if (playerUIEvent != null)
                uiHandler.post(new Runnable() {
                    @Override public void run() {
                        playerUIEvent.updateSeek(percent, currentms, totalms);
                    }
                });
        }

        @Override
        public void onFinish() {
            playingItem = App.getPlayingQueueHandler().getPlayingQueue().getNextPlayingItem();
            if(null != playingItem) {
                mPlayer.setDataSource(((MediaItem) playingItem).getItemUrl());
                mPlayer.play();
                onQueueUpdated();
            }else {
                if (playerUIEvent != null) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            playerUIEvent.stop();
                        }
                    });
                }
            }
        }

        @Override public void onPlay() {
            if (playerUIEvent != null)
                uiHandler.post(new Runnable() {
                    @Override public void run() {
                        playerUIEvent.updateUI();
                    }
                });
        }
        @Override public void onError() {
        }
    };

    public int PlayPause() {
        if(isPlaying()){
            mPlayer.pause();
            return 0;
        } else /*if(mPlayer.isPause())*/{
            mPlayer.play();
            return 1;
        }
    }

    public void stop() {
        if(!mPlayer.isStopped()){
            mPlayer.stop();
        }
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
}
