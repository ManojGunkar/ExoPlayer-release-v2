package com.globaldelight.boom.handler.PlayingQueue;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.manager.PlayerServiceReceiver;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.Media.MediaType;
import com.globaldelight.boom.task.PlayerService;
import com.globaldelight.boom.utils.helpers.DropBoxUtills;
import com.globaldelight.boom.utils.helpers.GoogleDriveHandler;
import com.globaldelight.boomplayer.AudioEffect;
import com.globaldelight.boomplayer.AudioPlayer;
import com.globaldelight.boomplayer.IPlayerEvents;

import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static com.globaldelight.boom.handler.PlayingQueue.PlayerEventHandler.PlayState.pause;
import static com.globaldelight.boom.handler.PlayingQueue.PlayerEventHandler.PlayState.play;
import static com.globaldelight.boom.handler.PlayingQueue.PlayerEventHandler.PlayState.stop;
import static com.globaldelight.boom.manager.PlayerServiceReceiver.ACTION_PLAY_STOP;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;

/**
 * Created by Rahul Agarwal on 03-10-16.
 */

public class PlayerEventHandler implements IUpNextMediaEvent, AudioManager.OnAudioFocusChangeListener {
    public static boolean isLibraryResumes = false;
    private static IMediaItemBase playingItem;
    private static boolean isTrackWaiting = false;
    private static AudioPlayer mPlayer;
    private static PlayerEventHandler handler;
    private static int NEXT = 0;
    private static int PREVIOUS = 1;
    private static int PLAYER_DIRECTION;
    private Context context;
    private PlayerService service;
    private AudioManager audioManager;
    private MediaSession session;
    private GoogleDriveHandler googleDriveHandler;
    private Handler seekEventHandler;
    private static boolean isSeeked = false;

    IPlayerEvents IPlayerEvents = new IPlayerEvents() {
        @Override
        public void onStop() {
            playingItem = null;
            context.sendBroadcast(new Intent(ACTION_PLAY_STOP));
        }

        @Override
        public void onStart(String mime, int sampleRate, int channels, long duration) {
            isTrackWaiting = false;
            context.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_GET_SONG));
            if(null != getPlayingItem() && getPlayingItem().getMediaType() != MediaType.DEVICE_MEDIA_LIB){
                context.sendBroadcast(new Intent(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY));
            }
        }

        @Override
        public void onPlayUpdate(final int percent, final long currentms, final long totalms) {
            Intent intent = new Intent();
            intent.setAction(PlayerServiceReceiver.ACTION_TRACK_POSITION_UPDATE);
            intent.putExtra("percent", percent);
            intent.putExtra("currentms", currentms);
            intent.putExtra("totalms", totalms);
            context.sendBroadcast(intent);
            isSeeked = false;
        }

        @Override
        public void onFinish() {
            playNextSong(false);
        }

        @Override
        public void onPlay() {
            Log.d("Start : ","Playing");
        }

        @Override
        public void onError() {
            if(isNext() && PLAYER_DIRECTION == NEXT) {
                playNextSong(false);
            }else if(isPrevious() && PLAYER_DIRECTION == PREVIOUS) {
                playPrevSong();
            }else {
                context.sendBroadcast(new Intent(ACTION_PLAY_STOP));
            }
            context.sendBroadcast(new Intent(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY));
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
            intent.setAction(PlayerServiceReceiver.ACTION_PLAYING_ITEM_CLICKED);
            intent.putExtra("play_pause", true );
            context.sendBroadcast(intent);
        }

        @Override
        public void onPause() {
            setSessionState(PlaybackState.STATE_PAUSED);

            Intent intent = new Intent();
            intent.setAction(PlayerServiceReceiver.ACTION_PLAYING_ITEM_CLICKED);
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
            onPause();
        }
    };

    private PlayerEventHandler(Context context, PlayerService service){
        this.context = context;
        if(null == mPlayer)
            mPlayer = new AudioPlayer(context, IPlayerEvents);
        if(null == audioManager)
            audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(null == this.service)
            this.service = service;
        App.getPlayingQueueHandler().getUpNextList().setUpNextMediaEvent(this);
        googleDriveHandler = new GoogleDriveHandler(context);
        googleDriveHandler.connectToGoogleAccount();
        seekEventHandler = new Handler();
        registerSession();
    }

    public static PlayerEventHandler getPlayerEventInstance(Context context, PlayerService service){
        if(handler == null){
            handler = new PlayerEventHandler(context, service);
        }
        return handler;
    }

    public AudioPlayer getPlayer(){
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

    public boolean isLoading() {
        if(null != mPlayer)
            return mPlayer.isLoading();
        return false;
    }

    public boolean isStopped() {
        if(null != mPlayer)
            return mPlayer.isStopped();
        return false;
    }

    @Override
    public synchronized void onPlayingItemChanged() {
        if(isPlaying() || isPaused() || isLoading() ) {
            setSessionState(PlaybackState.STATE_STOPPED);
        }
        context.sendBroadcast(new Intent(ACTION_PLAY_STOP));
        context.sendBroadcast(new Intent(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY));

        playingItem = App.getPlayingQueueHandler().getUpNextList().getPlayingItem();
        isTrackWaiting = true;
        new PlayingItemChanged().execute(playingItem);
    }

    public  boolean isTrackWaitingForPlay(){
        return isTrackWaiting;
    }

    public boolean isTrackLoading(){
        if(null != mPlayer)
            mPlayer.isLoading();
        return false;
    }

    private class PlayingItemChanged extends AsyncTask<IMediaItemBase, Void, String>{
        IMediaItemBase mediaItemBase;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(IMediaItemBase... params) {
            String dataSource = null;
            mediaItemBase = params[0];
            if(mediaItemBase.getMediaType() == MediaType.DEVICE_MEDIA_LIB){
                dataSource = ((MediaItem)mediaItemBase).getItemUrl();
                mediaItemBase.setItemArtUrl(App.getPlayingQueueHandler().getUpNextList().getAlbumArtList().get(((MediaItem)mediaItemBase).getItemAlbum()));
            }else if(mediaItemBase.getMediaType() == MediaType.DROP_BOX){
                if(null != App.getDropboxAPI()){
                    return DropBoxUtills.getDropboxItemUrl(((MediaItem)mediaItemBase).getItemUrl());
                }
                return null;
            }else if(mediaItemBase.getMediaType() == MediaType.GOOGLE_DRIVE){

                String access_token = googleDriveHandler.getAccessTokenApi();
                if(null != access_token) {
                    return ((MediaItem)mediaItemBase).getItemUrl() + access_token;
                }
                return null;
            }
            return dataSource;
        }

        @Override
        protected void onPostExecute(String dataSource) {
            super.onPostExecute(dataSource);
            if(null != mPlayer && null != mediaItemBase && null != dataSource) {
                if ( requestAudioFocus() ) {
                    mPlayer.setDataSource(dataSource);
                    setSessionState(PlaybackState.STATE_PLAYING);
                    AnalyticsHelper.songSelectionChanged(context, mediaItemBase);
                }
            }else{
                if(null == dataSource && mediaItemBase.getMediaType() == MediaType.GOOGLE_DRIVE){
                    Toast.makeText(context, context.getResources().getString(R.string.loading_problem), Toast.LENGTH_SHORT).show();
                }else if(null == dataSource && mediaItemBase.getMediaType() == MediaType.DROP_BOX){
                    Toast.makeText(context, context.getResources().getString(R.string.loading_problem), Toast.LENGTH_SHORT).show();
                }
                isTrackWaiting = false;
                context.sendBroadcast(new Intent(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY));
                context.sendBroadcast(new Intent(ACTION_PLAY_STOP));
            }
        }
    }

    public void playNextSong(boolean isUser) {
        PLAYER_DIRECTION = NEXT;
        App.getPlayingQueueHandler().getUpNextList().setNextPlayingItem(isUser);
        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_MOVE_TO_NEXT_SONG);
    }

    public void playPrevSong() {
        PLAYER_DIRECTION = PREVIOUS;
        App.getPlayingQueueHandler().getUpNextList().setPreviousPlayingItem();
        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_MOVE_TO_PRE_SONG);
    }

    @Override
    public void onPlayingItemClicked() {
        PlayState state = PlayPause();
        Intent intent = new Intent();
        intent.setAction(PlayerServiceReceiver.ACTION_PLAYING_ITEM_CLICKED);
        intent.putExtra("play_pause", state == play ? true : false );
        context.sendBroadcast(intent);
    }

    @Override
    public void onQueueUpdated() {
        Intent intent = new Intent();
        intent.setAction(PlayerServiceReceiver.ACTION_UPNEXT_UPDATE);
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
        if(!isStopped() && null != mPlayer){
            setSessionState(PlaybackState.STATE_STOPPED);
        }
    }

    public void release() {
        mPlayer = null;
    }

    public IMediaItem getPlayingItem() {
        return (IMediaItem) App.getPlayingQueueHandler().getUpNextList().getPlayingItem();
    }

    public void seek(final int progress) {
        if(null != mPlayer && !isSeeked && !isTrackWaiting){
            isSeeked = true;
            seekEventHandler.post(new Runnable() {
                @Override
                public void run() {
                    mPlayer.seek(progress);
                }
            });
        }
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
                if (mPlayer.isPlaying() || mPlayer.isPause() || mPlayer.isLoading()) {
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
        intent.setAction(PlayerServiceReceiver.ACTION_PLAYING_ITEM_CLICKED);
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
