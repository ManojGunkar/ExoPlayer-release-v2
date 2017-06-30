package com.globaldelight.boom.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.globaldelight.boom.BoomAudioProcessor;
import com.globaldelight.boom.BoomRenderersFactory;
import com.globaldelight.boom.Constants;
import com.globaldelight.boom.R;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;

/**
 * Created by adarsh on 20/01/17.
 */

public class AudioPlayer implements ExoPlayer.EventListener {
    /** Load jni .so on initialization */
    private final String LOG_TAG = "AudioPlayer";
    private Callback events = null;
    private States state = new States();
    private String sourcePath = null;
    private long sourceId = -1;
    private Context mContext;
    private Handler handler = new Handler();
    private AudioConfiguration mAudioConfig;

    private SimpleExoPlayer mExoPlayer;
    private BoomAudioProcessor mAudioProcessor;
    private String mCurrentSourcePath;
    private Timer mPlaybackTimer = null;

    public void setEventsListener(Callback events) {
        this.events = events;
    }

    public AudioPlayer(Context context, Callback events) {
        setEventsListener(events);
        mContext = context;
        mAudioConfig = AudioConfiguration.getInstance(context);

    }

    private boolean floatAudioSupported() {
        return (mAudioConfig.getFormat() == AudioConfiguration.FORMAT_FLOAT);
    }

    public boolean isPlaying(){
        return state.isPlaying();
    }

    public boolean isLoading() {
        return state.isLoading();
    }

    public boolean isPause(){
        return state.isPause();
    }

    public boolean isStopped() {
        return state.isStopped();
    }

    public void setPath(String src) {
        sourcePath = src;
    }

    public void setDataSourceId(long srcId) {
        sourceId = srcId;
    }

    public long getDataSourceId(){
        return sourceId;
    }



    public synchronized void play() {

        boolean mediaHasChanged = !TextUtils.equals(sourcePath, mCurrentSourcePath);
        if (mediaHasChanged) {
            mCurrentSourcePath = new String(sourcePath);
        }

        if ( mediaHasChanged || mExoPlayer == null || state.get() != States.PAUSED ) {
            if(AudioEffect.getInstance(mContext).getSelectedEqualizerPosition() == 0) {
                setAutoEqualizer();
            }

            if ( mExoPlayer == null ) {
                BoomRenderersFactory renderersFactory = new BoomRenderersFactory(mContext, null, EXTENSION_RENDERER_MODE_OFF);

                mAudioProcessor = renderersFactory.getBoomAudioProcessor();
                mExoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, new DefaultTrackSelector(), new DefaultLoadControl());
                mExoPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mExoPlayer.addListener(this);

                state.set(States.LOADING);
                updatePlayerEffect();
            }
            // Produces DataSource instances through which media data is loaded.
            DataSource.Factory dataSourceFactory =
                    new DefaultDataSourceFactory(
                            mContext, Util.getUserAgent(mContext, "boom"), null);
            // Produces Extractor instances for parsing the media data.
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            // The MediaSource represents the media to be played.
            MediaSource mediaSource =
                    new ExtractorMediaSource(
                            Uri.parse(sourcePath), dataSourceFactory, extractorsFactory, null, null);

            // Prepares media to play (happens on background thread) and triggers
            // {@code onPlayerStateChanged} callback when the stream is ready to play.
            mExoPlayer.prepare(mediaSource, false, false);
        }

        mExoPlayer.setPlayWhenReady(true);
        mPlaybackTimer = new Timer();
        mPlaybackTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if ( mExoPlayer.getDuration() > 0 ) {
                    events.onPlayTimeUpdate(mExoPlayer.getCurrentPosition(), mExoPlayer.getDuration());
                }
            }
        }, 0, 1000);

    }

    public void stop(){
        releasePlayer();
        state.set(States.STOPPED);
        postOnStop();
    }

    private void releasePlayer() {
        mExoPlayer.release();
        mExoPlayer.removeListener(this);
        mExoPlayer = null;

        mPlaybackTimer.cancel();
        mPlaybackTimer = null;

    }


    public void pause() {
        if ( mExoPlayer != null ) {
            mExoPlayer.setPlayWhenReady(false);
        }

        mPlaybackTimer.cancel();
        state.set(States.PAUSED);
    }

    private void seek(long pos) {
        if ( mExoPlayer != null ) {
            mExoPlayer.seekTo(pos);
        }

        if ( state.get() == States.PAUSED ) {
            if (events != null) handler.post(new Runnable() {
                @Override
                public void run() {
                    events.onPlayTimeUpdate(mExoPlayer.getCurrentPosition(), mExoPlayer.getDuration());
                }
            });
        }
    }

    public void seek(int percent) {
        if ( mExoPlayer != null ) {
            seek(percent * mExoPlayer.getDuration() / 100);
        }
    }


    private void setGenreType(String genreType) {
        final AudioEffect audioEffect = AudioEffect.getInstance(mContext);
        if (null != genreType) {
            for (int i = 0; i <= mContext.getResources().getStringArray(R.array.mapped_eq_key).length - 1; i++) {
                if (genreType.toUpperCase().contains(mContext.getResources().getStringArray(R.array.mapped_eq_key)[i].toUpperCase())) {
                    String genre = mContext.getResources().getStringArray(R.array.mapped_eq_value)[i];
                    List t = Arrays.asList(mContext.getResources().getStringArray(R.array.eq_names));
                    audioEffect.setAutoEqualizerPosition(t.indexOf(genre.toUpperCase()));
                    Log.d("Song Genre : ", genreType);
                    Log.d("Selected Song Genre : ", genre);
                    return;
                }
            }
        }
        audioEffect.setAutoEqualizerPosition(12);
        Log.d("Selected Song Genre : ", "MUSIC");
    }


    private void postError() {
        if (events != null) {
            handler.post(new Runnable() {
                @Override public void run() {
                    events.onError();
                }
            });
        }
    }

    private void postOnStart(final String mime, final int sampleRate, final int channels, final long duration ) {
        if (events != null) {
            handler.post(new Runnable() {
                @Override public void run() {
                    events.onStart(duration);
                }
            });
        }
    }

    private void postOnStop() {
        if (events != null) handler.post(new Runnable() {
            @Override
            public void run() {
                events.onStop();
            }
        });
    }

    private void postOnFinish() {
        if (events != null) handler.post(new Runnable() {
            @Override
            public void run() {
                events.onFinish();
            }
        });
    }

    public void updatePlayerEffect(){
        final AudioEffect audioEffect = AudioEffect.getInstance(mContext);
        setEnableEffect(audioEffect.isAudioEffectOn());
        mAudioProcessor.setQuality(mAudioConfig.getQuality());
        if(audioEffect.isAudioEffectOn()) {
            setEnable3DAudio(audioEffect.is3DSurroundOn());
            if (audioEffect.isIntensityOn()) {
                setIntensityValue(audioEffect.getIntensity() / (double) 100);
            }

            setEnableEqualizer(audioEffect.isEqualizerOn());
            setEnableSuperBass(audioEffect.isFullBassOn());
            setEqualizerGain(audioEffect.getSelectedEqualizerPosition());

            setSpeakerEnable(AudioEffect.SPEAKER_FRONT_LEFT, audioEffect.isLeftFrontSpeakerOn());
            setSpeakerEnable(AudioEffect.SPEAKER_FRONT_RIGHT, audioEffect.isRightFrontSpeakerOn());
            setSpeakerEnable(AudioEffect.SPEAKER_TWEETER, audioEffect.isTweeterOn());
            setSpeakerEnable(AudioEffect.SPEAKER_SURROUND_LEFT, audioEffect.isLeftSurroundSpeakerOn());
            setSpeakerEnable(AudioEffect.SPEAKER_SURROUND_RIGHT, audioEffect.isRightSurroundSpeakerOn());
            setSpeakerEnable(AudioEffect.SPEAKER_WOOFER, audioEffect.isWooferOn());

            setHighQualityEnable(audioEffect.isIntensityOn());

            setHeadPhone(audioEffect.getHeadPhoneType());

        }
    }


    private void setAutoEqualizer() {
        try {
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(this.sourcePath);
            setGenreType(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
        }
        catch (Exception e) {
            setGenreType(null);
        }
    }


    public synchronized void setIntensityValue(double intensity) {
        if ( mAudioProcessor != null ) {
            mAudioProcessor.setIntensity((float)((intensity-0.5)/0.5));
        }
    }

    public synchronized void setEnable3DAudio(boolean enable3DAudio) {
        if ( mAudioProcessor != null ) {
            mAudioProcessor.set3DAudioState(enable3DAudio);
        }
    }

    public synchronized void setEnableEffect(boolean enableEffect) {
        if ( mAudioProcessor != null ) {
            mAudioProcessor.setEffectState(enableEffect);
        }
    }

    public synchronized void setEnableEqualizer(boolean enable){

    }

    public synchronized void setEnableSuperBass(boolean enableSuperBass) {
        if ( mAudioProcessor != null ) {
            mAudioProcessor.setFullBassState(enableSuperBass);
        }
    }

    public synchronized void setEqualizerGain(int equalizerId) {
        if(equalizerId == 0){
            equalizerId = AudioEffect.getInstance(mContext).getAutoEqualizerValue();
        }
        if ( mAudioProcessor != null ) {
            mAudioProcessor.setEqualizer(equalizerId);
        }

    }

    public synchronized void setSpeakerEnable(@AudioEffect.Speaker int speaker, boolean value){
        if ( mAudioProcessor != null ) {
            mAudioProcessor.setSpeakerState(speaker, value == true ? 1 : 0);
        }
    }

    public void setHighQualityEnable(boolean highQualityEnable) {
        if (mAudioProcessor != null) {
        }
    }

    public void setHeadPhone(@Constants.Headphone int type){
        if (mAudioProcessor != null) {
            mAudioProcessor.setHeadPhoneType(type);
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object o) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroupArray, TrackSelectionArray trackSelectionArray) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case ExoPlayer.STATE_IDLE:
            case ExoPlayer.STATE_BUFFERING:
                break;

            case ExoPlayer.STATE_READY:
                if ( mExoPlayer.getPlayWhenReady() ) {
                    if ( state.get() == States.LOADING ) {
                        postOnStart(null, 0, 0, mExoPlayer.getDuration());
                    }
                    state.set(States.PLAYING);
                }
                else {
                    state.set(States.PAUSED);
                }
                break;

            case ExoPlayer.STATE_ENDED:
                state.set(States.STOPPED);
                releasePlayer();
                postOnFinish();

                break;
        }

    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        postError();
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters var1) {

    }



    public interface Callback {
        void onStart(long duration);
        void onPlay();
        void onPlayTimeUpdate(long currentms, long totalms);
        void onFinish();
        void onStop();
        void onError();
        void onErrorPlayAgain();
    }

    public static class States {

        private static final int LOADING = 0;
        private static final int PLAYING = 1;
        private static final int PAUSED = 2;
        private static final int STOPPED = 3;
        private int playerState = STOPPED;

        public int get() {
            return playerState;
        }

        public void set(int state) {
            playerState = state;
        }

        public synchronized boolean isReadyToPlay() {
            return playerState == States.PAUSED;
        }

        public synchronized boolean isLoading() {
            return playerState == States.LOADING;
        }

        public synchronized boolean isPlaying() {
            return playerState == States.PLAYING;
        }

        public boolean isPause() {
            return playerState == States.PAUSED;
        }

        public synchronized boolean isStopped() {
            return playerState == States.STOPPED;
        }
    }


}
