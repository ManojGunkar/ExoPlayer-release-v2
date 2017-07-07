package com.globaldelight.boom.playbackEvent.handler;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.collection.local.callback.IMediaItem;
import com.globaldelight.boom.playbackEvent.utils.MediaType;
import com.globaldelight.boom.playbackEvent.controller.callbacks.IUpNextMediaEvent;
import com.globaldelight.boom.utils.helpers.DropBoxUtills;
import com.globaldelight.boom.utils.helpers.GoogleDriveHandler;
import com.globaldelight.boom.player.AudioEffect;
import com.globaldelight.boom.player.AudioPlayer;

import java.util.ArrayList;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;

/**
 * Created by Rahul Agarwal on 03-10-16.
 */

public class PlaybackManager implements IUpNextMediaEvent, AudioManager.OnAudioFocusChangeListener {
    public static boolean isLibraryResumes = false;

    private static final int NEXT = 0;
    private static final int PREVIOUS = 1;
    private static int PLAYER_DIRECTION;


    private ArrayList<Listener> mListeners = new ArrayList<>();
    private static IMediaItemBase playingItem;
    private static boolean isTrackWaiting = false;
    private AudioPlayer mPlayer;
    private Context context;
    private AudioManager audioManager;
    private MediaSession session;
    private GoogleDriveHandler googleDriveHandler;
    private Handler seekEventHandler;
    private UpNextPlayingQueue mQueue = new UpNextPlayingQueue(context);

    AudioPlayer.Callback IPlayerEvents = new AudioPlayer.Callback() {

        @Override
        public void onStateChange(@AudioPlayer.State int state) {
            switch (state) {
                case AudioPlayer.LOADING:
                    isTrackWaiting = false;
                    notifyMediaChanged();
                    if(null != getPlayingItem() && getPlayingItem().getMediaType() != MediaType.DEVICE_MEDIA_LIB){
                        notifyPlayerStateChanged();
                    }
                    break;

                case AudioPlayer.PLAYING:
                    notifyPlayerStateChanged();
                    break;

                case AudioPlayer.PAUSED:
                    notifyPlayerStateChanged();
                    break;

                case AudioPlayer.STOPPED:
                    notifyPlayerStateChanged();
                    break;
            }

        }

        @Override
        public void onPlayTimeUpdate(final long currentms, final long totalms) {
            notifyPositionUpdate();
        }

        @Override
        public void onComplete() {
            playNextSong(false);
        }


        @Override
        public void onError() {
            if(isNext() && PLAYER_DIRECTION == NEXT) {
                playNextSong(false);
            }else if(isPrevious() && PLAYER_DIRECTION == PREVIOUS) {
                playPrevSong();
            }
            notifyError();
        }
    };

    private MediaSession.Callback mediaSessionCallback = new MediaSession.Callback(){
        @Override
        public void onPlay() {
            setSessionState(PlaybackState.STATE_PLAYING);
        }

        @Override
        public void onPause() {
            setSessionState(PlaybackState.STATE_PAUSED);
        }

        @Override
        public void onSkipToNext() {
            playNextSong(true);
        }

        @Override
        public void onSkipToPrevious() {
            playPrevSong();
        }

        @Override
        public void onStop() {
            onPause();
        }
    };

    public PlaybackManager(Context context){
        this.context = context;
        mPlayer = new AudioPlayer(context, IPlayerEvents);
        mQueue = new UpNextPlayingQueue(context);
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        mQueue.setUpNextMediaEvent(this);
        googleDriveHandler = new GoogleDriveHandler(context);
        googleDriveHandler.connectToGoogleAccount();
        seekEventHandler = new Handler();
        registerSession();
    }


    public UpNextPlayingQueue queue() {
        return mQueue;
    }

    public void registerListener(Listener listener) {
        if ( mListeners.indexOf(listener) == -1) {
            mListeners.add(listener);
        }

    }

    public void unregisterListener(Listener listener) {
        if ( mListeners.indexOf(listener) != -1) {
            mListeners.remove(listener);
        }
    }

    private void notifyMediaChanged() {
        for ( Listener aListener: mListeners ) {
            aListener.onMediaChanged();
        }
    }

    private void notifyPlayerStateChanged() {
        for ( Listener aListener: mListeners ) {
            aListener.onPlayerStateChanged();
        }
    }

    private void notifyError() {
        for ( Listener aListener: mListeners ) {
            aListener.onPlayerError();
        }
    }

    private void notifyComplete() {
        for ( Listener aListener: mListeners ) {
            aListener.onPlaybackCompleted();
        }
    }

    private void notifyPositionUpdate() {
        for ( Listener aListener: mListeners ) {
            aListener.onUpdatePlayerPosition();
        }
    }



    public long getPlayerDataSourceId(){
        return mPlayer.getDataSourceId();
    }

    public boolean isPlaying(){
        return mPlayer.isPlaying();
    }

    public boolean isPaused(){
        return mPlayer.isPause();
    }

    public boolean isLoading() {
        return mPlayer.isLoading();
    }

    public boolean isStopped() {
        return mPlayer.isStopped();
    }

    public long getDuration() {
        return Math.max(0, mPlayer.getDuration());
    }

    public long getPosition() {
        return Math.max(0,mPlayer.getCurrentPosition());
    }

    @Override
    public synchronized void onPlayingItemChanged() {
        if ( isPlaying() ) {
            setSessionState(PlaybackState.STATE_PAUSED);
        }

        notifyMediaChanged();
        playingItem = mQueue.getPlayingItem();
        if ( playingItem != null ) {
            isTrackWaiting = true;
            new PlayingItemChanged().execute(playingItem);
        }
        else {
            setSessionState(PlaybackState.STATE_STOPPED);
        }
    }

    public  boolean isTrackWaitingForPlay(){
        return isTrackWaiting;
    }

    public boolean isTrackLoading(){
        return isLoading() || isTrackWaitingForPlay();
    }

    public boolean isTrackPlaying() {
        return isPlaying() || isTrackLoading();
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
                mediaItemBase.setItemArtUrl(mQueue.getAlbumArtList().get(((MediaItem)mediaItemBase).getItemAlbum()));
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
            if( null != mediaItemBase && null != dataSource) {
                if ( requestAudioFocus() ) {
                    mPlayer.setPath(dataSource);
                    mPlayer.setDataSourceId(mediaItemBase.getItemId());

                    MediaMetadata.Builder builder = new MediaMetadata.Builder();
                    builder.putString(MediaMetadata.METADATA_KEY_TITLE, mediaItemBase.getItemTitle());
                    session.setMetadata(builder.build());

                    setSessionState(PlaybackState.STATE_PLAYING);
//                    AnalyticsHelper.songSelectionChanged(context, mediaItemBase);
                }
            }else{
                isTrackWaiting = false;
                setSessionState(PlaybackState.STATE_STOPPED);
                Toast.makeText(context, context.getResources().getString(R.string.loading_problem), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void playNextSong(boolean isUser) {
        PLAYER_DIRECTION = NEXT;
        if ( isNext() ) {
            mQueue.setNextPlayingItem(isUser);
        }
        else {
            setSessionState(PlaybackState.STATE_STOPPED);
            notifyComplete();
        }
    }

    public void playPrevSong() {
        PLAYER_DIRECTION = PREVIOUS;
        mQueue.setPreviousPlayingItem();
    }

    @Override
    public void onPlayingItemClicked() {
        playPause();
    }

    @Override
    public void onQueueUpdated() {
        for (Listener aListener: mListeners ) {
            aListener.onQueueUpdated();
        }
    }

    public void playPause() {
        if ( mPlayer.getDataSourceId() == -1 ) {
            onPlayingItemChanged();
            return;
        }

        if(isPlaying()){
            setSessionState(PlaybackState.STATE_PAUSED);
        } else {
            if ( requestAudioFocus() ) {
                setSessionState(PlaybackState.STATE_PLAYING);
            }
            else {
                setSessionState(PlaybackState.STATE_STOPPED);
            }
        }
    }

    public void stop() {
        if(!isStopped() ){
            setSessionState(PlaybackState.STATE_STOPPED);
        }
    }

    public IMediaItem getPlayingItem() {
        return (IMediaItem) mQueue.getPlayingItem();
    }

    public void seek(final int progress) {
        if( !isTrackWaiting ){
            seekEventHandler.post(new Runnable() {
                @Override
                public void run() {
                    mPlayer.seek(progress);
                }
            });
        }
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

    public void setSpeakerEnable(@AudioEffect.Speaker int speaker, boolean enable) {
        mPlayer.setSpeakerEnable(speaker, enable);
    }

    public void setHighQualityEnable(boolean highQualityEnable) {
        mPlayer.setHighQualityEnable(highQualityEnable);
    }

    public void stopPlayer() {
        setSessionState(PlaybackState.STATE_STOPPED);
    }

    public void setHeadPhoneType(int headPhoneType) {
        mPlayer.setHeadPhone(headPhoneType);
    }


    public boolean resetShuffle() {
        return mQueue.resetShuffle();
    }

    public boolean resetRepeat() {
        return mQueue.resetRepeat();
    }

    public boolean isPrevious() {
        return mQueue.isPrevious();
    }

    public boolean isNext() {
        return mQueue.isNext();
    }

    void registerSession() {
        if ( session == null ) {
            session = new MediaSession(context, context.getPackageName());
            session.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
            setSessionState(PlaybackState.STATE_NONE);
            session.setCallback(mediaSessionCallback);
        }
        session.setActive(true);
    }


    void setSessionState(int state)
    {
        long actions = PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS;
        if ( state == PlaybackState.STATE_PLAYING ) {
            actions |= (PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_STOP);
        }
        else {
            actions |= PlaybackState.ACTION_PLAY;
        }

        PlaybackState pbState = new PlaybackState.Builder()
                .setActions(actions)
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
                mPlayer.stop();
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

    private static final float VOLUME_DUCK = 0.05f;
    private static final float VOLUME_FULL = 1.0f;
    private boolean mIsVolumeDucked = false;

    @Override
    public void onAudioFocusChange(int focusChange) {

        switch ( focusChange ) {
            case AUDIOFOCUS_LOSS_TRANSIENT:
                if ( isPlaying() ) {
                    mPlayer.setVolume(VOLUME_DUCK);
                    mIsVolumeDucked = true;
                }
                break;

            case AUDIOFOCUS_GAIN_TRANSIENT:
                mPlayer.setVolume(VOLUME_FULL);
                break;

            case AUDIOFOCUS_LOSS:
                setSessionState(PlaybackState.STATE_PAUSED);
                releaseAudioFocus();
                break;

            case AUDIOFOCUS_GAIN:
                if ( isPlaying() && mIsVolumeDucked ) {
                    mPlayer.setVolume(VOLUME_FULL);
                    mIsVolumeDucked = false;
                }
        }
    }

    public interface Listener {
        // when the song changes
        void onMediaChanged();

        // when all items in the queue are played
        void onPlaybackCompleted();

        // when player state changes
        void onPlayerStateChanged();

        // when there is an error in playing the song
        void onPlayerError();

        // when song is being played
        void onUpdatePlayerPosition();

        void onQueueUpdated();
    }
}
