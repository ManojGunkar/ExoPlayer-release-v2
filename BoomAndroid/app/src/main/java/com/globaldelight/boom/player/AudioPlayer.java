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
    public @interface State {}

    public static final int LOADING = 0;
    public static final int PLAYING = 1;
    public static final int PAUSED = 2;
    public static final int STOPPED = 3;


    /** Load jni .so on initialization */
    private final String TAG = "AudioPlayer";
    private Callback mCallback = null;
    private @State int  state = STOPPED;
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
        mCallback = callback;
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

    public long getDuration() {
        if ( mExoPlayer != null ) {
            return mExoPlayer.getDuration();
        }
        return 0;
    }

    public long getCurrentPosition() {
        if ( mExoPlayer != null ) {
            return mExoPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void setVolume(float volume) {
        if ( mExoPlayer != null ) {
            mExoPlayer.setVolume(volume);
        }
    }



    public void play() {
        boolean mediaHasChanged = !TextUtils.equals(sourcePath, mCurrentSourcePath);
        if (mediaHasChanged) {
            mCurrentSourcePath = sourcePath;
            releaseResources(true);
        }

        if ( mediaHasChanged || mExoPlayer == null  ) {
            if(AudioEffect.getInstance(mContext).getSelectedEqualizerPosition() == 0) {
                setAutoEqualizer();
            }

            if ( mExoPlayer == null ) {
                BoomRenderersFactory renderersFactory = new RenderersFactory(mContext);

                mAudioProcessor = renderersFactory.getBoomAudioProcessor();
                mExoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, new DefaultTrackSelector(), new DefaultLoadControl());
                mExoPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mExoPlayer.addListener(this);
            }

            updatePlayerEffect();

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
            // {@code onPlayerStateChanged} mCallback when the stream is ready to play.
            mExoPlayer.prepare(mediaSource);


            state = LOADING;
            postStateChange();
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
                    mCallback.onPlayTimeUpdate(mExoPlayer.getCurrentPosition(), mExoPlayer.getDuration());
                }
            }
        }, 0, 1000);
    }

    public void stop(){
        state = STOPPED;
        releaseResources(true);
        postStateChange();
    }

    private void releaseResources(boolean releasePlayer) {
        if ( releasePlayer && mExoPlayer != null ) {
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
        state = PAUSED;
        postStateChange();
        releaseResources(false);
    }

    private void seek(long pos) {
        if ( mExoPlayer != null ) {
            mExoPlayer.seekTo(pos);
        }

        if ( state == PAUSED ) {
            if (mCallback != null) handler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onPlayTimeUpdate(mExoPlayer.getCurrentPosition(), mExoPlayer.getDuration());
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
        if (mCallback != null) {
            handler.post(new Runnable() {
                @Override public void run() {
                    mCallback.onError();
                }
            });
        }
    }

    private void postStateChange() {
        if (mCallback != null) {
            handler.post(new Runnable() {
                @Override public void run() {
                    mCallback.onStateChange(state);
                }
            });
        }
    }

    private void postOnFinish() {
        if (mCallback != null) handler.post(new Runnable() {
            @Override
            public void run() {
                mCallback.onComplete();
            }
        });
    }

    public void updatePlayerEffect(){
        final AudioEffect audioEffect = AudioEffect.getInstance(mContext);
        mAudioProcessor.setQuality(mAudioConfig.getQuality());
        setHeadPhone(audioEffect.getHeadPhoneType());
        setEnableEffect(audioEffect.isAudioEffectOn());
        setEnable3DAudio(audioEffect.is3DSurroundOn());
        setIntensityValue(audioEffect.isIntensityOn()? audioEffect.getIntensity() / (double) 100 : 0.5);
        setEnableSuperBass(audioEffect.isFullBassOn());
        setEqualizerGain(audioEffect.isEqualizerOn()? audioEffect.getSelectedEqualizerPosition() : 7);
        setSpeakerEnable(AudioEffect.SPEAKER_FRONT_LEFT, audioEffect.isLeftFrontSpeakerOn());
        setSpeakerEnable(AudioEffect.SPEAKER_FRONT_RIGHT, audioEffect.isRightFrontSpeakerOn());
        setSpeakerEnable(AudioEffect.SPEAKER_TWEETER, audioEffect.isTweeterOn());
        setSpeakerEnable(AudioEffect.SPEAKER_SURROUND_LEFT, audioEffect.isLeftSurroundSpeakerOn());
        setSpeakerEnable(AudioEffect.SPEAKER_SURROUND_RIGHT, audioEffect.isRightSurroundSpeakerOn());
        setSpeakerEnable(AudioEffect.SPEAKER_WOOFER, audioEffect.isWooferOn());
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
                    state = PLAYING;
                }
                else {
                    state = PAUSED;
                }
                postStateChange();
                break;

            case ExoPlayer.STATE_ENDED:
                releaseResources(true);
                postOnFinish();
                break;
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        releaseResources(true);
        postError();
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters var1) {

    }


    public interface Callback {
        void onStateChange(@State int state);
        void onPlayTimeUpdate(long currentms, long totalms);
        void onComplete();
        void onError();
    }

    // Custom Factory that create only AudioRenderers
    private static class RenderersFactory extends BoomRenderersFactory {

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
