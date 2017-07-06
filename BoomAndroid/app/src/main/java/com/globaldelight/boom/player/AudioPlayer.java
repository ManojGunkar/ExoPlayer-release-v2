package com.globaldelight.boom.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.IntDef;
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
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.metadata.MetadataRenderer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by adarsh on 20/01/17.
 */

public class AudioPlayer implements ExoPlayer.EventListener {

    // Player states
    @IntDef({LOADING, PLAYING, PAUSED, STOPPED})
    @Retention(RetentionPolicy.SOURCE)
    private @interface PlayerStates {}

    private static final int LOADING = 0;
    private static final int PLAYING = 1;
    private static final int PAUSED = 2;
    private static final int STOPPED = 3;


    /** Load jni .so on initialization */
    private final String TAG = "AudioPlayer";
    private Callback callback = null;
    private @PlayerStates int  state = STOPPED;
    private String sourcePath = null;
    private long sourceId = -1;
    private Context mContext;
    private Handler handler = new Handler();
    private AudioConfiguration mAudioConfig;

    private SimpleExoPlayer mExoPlayer;
    private BoomAudioProcessor mAudioProcessor;
    private String mCurrentSourcePath;
    private Timer mPlaybackTimer = null;
    private PowerManager.WakeLock mWakeLock;
    private WifiManager.WifiLock  mWifiLock;


    public AudioPlayer(Context context, Callback callback) {
        mContext = context;
        this.callback = callback;
        mAudioConfig = AudioConfiguration.getInstance(context);

        PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

        WifiManager wm = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, TAG);
    }

    public boolean isPlaying(){
        return state == PLAYING;
    }

    public boolean isLoading() {
        return state == LOADING;
    }

    public boolean isPause(){
        return state == PAUSED;
    }

    public boolean isStopped() {
        return state == STOPPED;
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



    public void play() {

        boolean mediaHasChanged = !TextUtils.equals(sourcePath, mCurrentSourcePath);
        if (mediaHasChanged) {
            mCurrentSourcePath = sourcePath;
        }

        if ( mediaHasChanged || mExoPlayer == null || state != PAUSED ) {
            if(AudioEffect.getInstance(mContext).getSelectedEqualizerPosition() == 0) {
                setAutoEqualizer();
            }

            if ( mExoPlayer == null ) {
                BoomRenderersFactory renderersFactory = new RenderersFactory(mContext);

                mAudioProcessor = renderersFactory.getBoomAudioProcessor();
                mExoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, new DefaultTrackSelector(), new DefaultLoadControl());
                mExoPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mExoPlayer.addListener(this);

                state = LOADING;
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

        mWakeLock.acquire();

        if ( sourcePath.startsWith("http") ) {
            mWifiLock.acquire();
        }

        mExoPlayer.setPlayWhenReady(true);
        mPlaybackTimer = new Timer();
        mPlaybackTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if ( mExoPlayer != null && mExoPlayer.getDuration() >= 0 ) {
                    callback.onPlayTimeUpdate(mExoPlayer.getCurrentPosition(), mExoPlayer.getDuration());
                }
            }
        }, 0, 1000);
    }

    public void stop(){
        releaseResources(STOPPED);
        postOnStop();
    }

    private void releaseResources(int newState) {
        state = newState;
        if ( state == STOPPED ) {
            mExoPlayer.release();
            mExoPlayer.removeListener(this);
            mExoPlayer = null;
        }

        if ( mPlaybackTimer != null ) {
            mPlaybackTimer.cancel();
            mPlaybackTimer = null;
        }

        if ( mWifiLock.isHeld() ) {
            mWifiLock.release();
        }

        if ( mWakeLock.isHeld() ) {
            mWakeLock.release();
        }
    }

    public void pause() {
        if ( mExoPlayer != null ) {
            mExoPlayer.setPlayWhenReady(false);
        }

        releaseResources(PAUSED);
    }

    private void seek(long pos) {
        if ( mExoPlayer != null ) {
            mExoPlayer.seekTo(pos);
        }

        if ( state == PAUSED ) {
            if (callback != null) handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onPlayTimeUpdate(mExoPlayer.getCurrentPosition(), mExoPlayer.getDuration());
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
        if (callback != null) {
            handler.post(new Runnable() {
                @Override public void run() {
                    callback.onError();
                }
            });
        }
    }

    private void postOnStart() {
        if (callback != null) {
            handler.post(new Runnable() {
                @Override public void run() {
                    callback.onStart();
                }
            });
        }
    }

    private void postOnStop() {
        if (callback != null) handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onStop();
            }
        });
    }

    private void postOnFinish() {
        if (callback != null) handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onFinish();
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
                    if ( state == LOADING ) {
                        postOnStart();
                    }
                    state = PLAYING;
                }
                else {
                    state = PAUSED;
                }
                break;

            case ExoPlayer.STATE_ENDED:
                releaseResources(STOPPED);
                postOnFinish();

                break;
        }

    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        releaseResources(STOPPED);
        postError();
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters var1) {

    }



    public interface Callback {
        void onStart();
        void onPlayTimeUpdate(long currentms, long totalms);
        void onFinish();
        void onStop();
        void onError();
    }

    // Custom Factory that create only AudioRenderers
    static class RenderersFactory extends BoomRenderersFactory {

        private Context mContext;

        public RenderersFactory(Context context) {
            super(context, null, EXTENSION_RENDERER_MODE_ON);
            mContext = context;
        }

        @Override
        public Renderer[] createRenderers(Handler eventHandler,
                                          VideoRendererEventListener videoRendererEventListener,
                                          AudioRendererEventListener audioRendererEventListener,
                                          TextRenderer.Output textRendererOutput, MetadataRenderer.Output metadataRendererOutput) {
            ArrayList<Renderer> renderersList = new ArrayList<>();
            buildAudioRenderers(mContext, null, buildAudioProcessors(),
                    eventHandler, audioRendererEventListener, EXTENSION_RENDERER_MODE_ON, renderersList);
            return renderersList.toArray(new Renderer[renderersList.size()]);
        }

    }


}
