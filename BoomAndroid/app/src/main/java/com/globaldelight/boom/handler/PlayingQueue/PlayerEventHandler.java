package com.globaldelight.boom.handler.PlayingQueue;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.globaldelight.boom.App;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.task.PlayerService;
import com.globaldelight.boomplayer.AudioEffect;
import com.globaldelight.boomplayer.OpenSLPlayer;
import com.globaldelight.boomplayer.PlayerEvents;

import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static com.globaldelight.boom.handler.PlayingQueue.PlayerEventHandler.PlayState.pause;
import static com.globaldelight.boom.handler.PlayingQueue.PlayerEventHandler.PlayState.play;
import static com.globaldelight.boom.handler.PlayingQueue.PlayerEventHandler.PlayState.stop;

/**
 * Created by Rahul Agarwal on 03-10-16.
 */

public class PlayerEventHandler implements QueueEvent, AudioManager.OnAudioFocusChangeListener {
    public static boolean isPlayerResume = false;
    private static IMediaItemBase playingItem;
    private static OpenSLPlayer mPlayer;
    private static PlayerEventHandler handler;
    private static int NEXT = 0;
    private static int PREVIOUS = 1;
    private static int PLAYER_DIRECTION;
    private Context context;
    private Handler uiHandler;
    private PlayerService service;
    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener focusChangeListener;
    private MediaSession session;
    PlayerEvents playerEvents = new PlayerEvents() {
        @Override
        public void onStop() {
            playingItem = null;
            context.sendBroadcast(new Intent(PlayerService.ACTION_PLAY_STOP));
        }

        @Override
        public void onStart(String mime, int sampleRate, int channels, long duration) {

        }

        @Override
        public void onPlayUpdate(final int percent, final long currentms, final long totalms) {
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

        @Override
        public void onPlay() {
        }

        @Override
        public void onError() {
            if(isNext() && PLAYER_DIRECTION == NEXT) {
                playNextSong(false);
            }else if(isPrevious() && PLAYER_DIRECTION == PREVIOUS) {
                playPrevSong();
            }else {
                App.getPlayingQueueHandler().getUpNextList().managePlayedItem(true);
                context.sendBroadcast(new Intent(PlayerService.ACTION_PLAY_STOP));
            }
            Toast.makeText(context, "Error in playing Song", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onErrorPlayAgain() {
            onPlayingItemChanged();
        }
    };
    private MediaSession.Callback mediaSessionCallback = new MediaSession.Callback(){
        @Override
        public void onPlay() {
            setSessionState(PlaybackState.STATE_PLAYING);

            Intent intent = new Intent();
            intent.setAction(PlayerService.ACTION_PLAYING_ITEM_CLICKED);
            intent.putExtra("play_pause", true );
            context.sendBroadcast(intent);
        }

        @Override
        public void onPause() {
            setSessionState(PlaybackState.STATE_PAUSED);

            Intent intent = new Intent();
            intent.setAction(PlayerService.ACTION_PLAYING_ITEM_CLICKED);
            intent.putExtra("play_pause", false );
            context.sendBroadcast(intent);
        }

        @Override
        public void onSkipToNext() {
            handler.playNextSong(true);
        }

        @Override
        public void onSkipToPrevious() {
            handler.playPrevSong();
        }

        @Override
        public void onStop() {
            //TODO: implement proper stop behaviour
            // handler.stopPlayer();
            onPause();
        }
    };

    private PlayerEventHandler(Context context, PlayerService service){
        this.context = context;
        mPlayer = new OpenSLPlayer(context, playerEvents);
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        focusChangeListener = this;
        this.service = service;
        App.getPlayingQueueHandler().getUpNextList().setQueueEvent(this);
        uiHandler = new Handler();
        registerSession();
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
        if(null != mPlayer)
            return mPlayer.isPlaying();
        return false;
    }

    public boolean isPaused(){
        if(null != mPlayer)
            return mPlayer.isPause();
        return false;
    }

    public boolean isStopped() {
        if(null != mPlayer)
            return mPlayer.isStopped();
        return false;
    }

    @Override
    public synchronized void onPlayingItemChanged() {

        if(isPlaying() || isPaused()) {
            setSessionState(PlaybackState.STATE_STOPPED);
        }
        playingItem = App.getPlayingQueueHandler().getUpNextList().getPlayingItem();
        if(null != mPlayer && null != playingItem ) {
            if ( requestAudioFocus() ) {
                mPlayer.setDataSource(((MediaItem) playingItem).getItemUrl());
                setSessionState(PlaybackState.STATE_PLAYING);
                playingItem.setItemArtUrl(App.getPlayingQueueHandler().getUpNextList().getAlbumArtList().get(((MediaItem) playingItem).getItemAlbum()));
                context.sendBroadcast(new Intent(PlayerService.ACTION_GET_SONG));
                AnalyticsHelper.songSelectionChanged(context, playingItem);
            }
        }else{
            context.sendBroadcast(new Intent(PlayerService.ACTION_PLAY_STOP));
        }
    }

    public void playNextSong(boolean isUser) {
        if(isNext() || App.getUserPreferenceHandler().getRepeat() == UpNextList.REPEAT.one) {
            long sleepTime = 100;
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            PLAYER_DIRECTION = NEXT;
            App.getPlayingQueueHandler().getUpNextList().setNextPlayingItem(isUser);
        }else{
            context.sendBroadcast(new Intent(PlayerService.ACTION_LAST_PLAYED_SONG));
        }
        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_MOVE_TO_NEXT_SONG);
    }

    public void playPrevSong() {
        long sleepTime = 100;
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PLAYER_DIRECTION = PREVIOUS;
        App.getPlayingQueueHandler().getUpNextList().setPreviousPlayingItem();
        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_MOVE_TO_PRE_SONG);
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

    public PlayState PlayPause() {
        if(isPlaying()){
            setSessionState(PlaybackState.STATE_PAUSED);
            FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_PAUSE_PLAYING);
            return pause;
        } else {
            if ( requestAudioFocus() ) {
                setSessionState(PlaybackState.STATE_PLAYING);
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_PLAY_PLAYING);
                return play;
            }
            else {
                return stop;
            }
        }
    }

    public void stop() {
        if(!isStopped()){
            setSessionState(PlaybackState.STATE_STOPPED);
        }
    }

    public void release() {
        mPlayer = null;
    }

    public IMediaItemBase getPlayingItem() {
        return App.getPlayingQueueHandler().getUpNextList().getPlayingItem()/*playingItem*/;
    }

    public void seek(int progress) {
        if(null != mPlayer)
            mPlayer.seek(progress);
    }


    public void setEffectEnable(boolean enable) {
        if(null != mPlayer)
            mPlayer.setEnableEffect(enable);
    }

    public void set3DAudioEnable(boolean enable) {
        if(null != mPlayer)
            mPlayer.setEnable3DAudio(enable);
    }

    public void setIntensityValue(double value) {
        if(null != mPlayer)
            mPlayer.setIntensityValue(value);
    }

    public void setEqualizerEnable(boolean enable) {
        if(null != mPlayer)
            mPlayer.setEnableEqualizer(enable);
    }

    public void setSuperBassEnable(boolean enable) {
        if(null != mPlayer)
            mPlayer.setEnableSuperBass(enable);
    }

    public void setEqualizerGain(int position) {
        if(null != mPlayer)
            mPlayer.setEqualizerGain(position);
    }

    public void setSpeakerEnable(AudioEffect.Speaker speaker, boolean enable) {
        if(null != mPlayer)
            mPlayer.setSpeakerEnable(speaker, enable);
    }

    public void setHighQualityEnable(boolean highQualityEnable) {
        if(null != mPlayer)
            mPlayer.setHighQualityEnable(highQualityEnable);
    }

    public void stopPlayer() {
        setSessionState(PlaybackState.STATE_STOPPED);
    }

    public void updateEffect() {
        if(null != mPlayer)
            mPlayer.updatePlayerEffect();
    }

    public void setHeadPhoneType(int headPhoneType) {
        if(null != mPlayer)
            mPlayer.setHeadPhone(headPhoneType);
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

    void registerSession() {
        if ( session == null ) {
            session = new MediaSession(context, context.getPackageName());
            session.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
            setSessionState(PlaybackState.STATE_STOPPED);
            session.setCallback(mediaSessionCallback);

//            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
//            ComponentName comp = new ComponentName(context.getPackageName(), PlayerService.RemoteControlReceiver.class.getName());
//            mediaButtonIntent.setComponent(comp);
//            PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(context, 0, mediaButtonIntent, 0);
//            session.setMediaButtonReceiver(mediaPendingIntent);
        }


        session.setActive(true);
    }

    void unregisterSession() {
        session.release();
    }

    void setSessionState(int state)
    {
        if(null == mPlayer)
            return;
        PlaybackState pbState = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS | PlaybackState.ACTION_STOP)
                .setState(state, 0, 1, 0)
                .build();
        session.setPlaybackState(pbState);
        switch ( state ) {
            case PlaybackState.STATE_PLAYING:
                mPlayer.play();
                break;

            case PlaybackState.STATE_PAUSED:
                mPlayer.pause();
                break;

            case PlaybackState.STATE_STOPPED:
                if (mPlayer.isPlaying() || mPlayer.isPause()) {
                    mPlayer.stop();
                }
                break;
        }
    }

    public boolean requestAudioFocus() {
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            session.setActive(true);
            return true;
        }

        return false;
    }

    public void releaseAudioFocus() {
        audioManager.abandonAudioFocus(this);
        session.setActive(false);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if(null == mPlayer)
            return;
        Intent intent = new Intent();
        intent.setAction(PlayerService.ACTION_PLAYING_ITEM_CLICKED);
        if (focusChange == AUDIOFOCUS_LOSS_TRANSIENT) {
            intent.putExtra("play_pause", false );
            mPlayer.pause();
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) {
            intent.putExtra("play_pause", true );
            mPlayer.play();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            releaseAudioFocus();
            intent.putExtra("play_pause", false );
            mPlayer.pause();
        }
        else {
            intent = null;
        }

        if ( intent != null ) {
            context.sendBroadcast(intent);
        }
    }

    public enum PlayState {
        play,
        pause,
        stop
    }
}
