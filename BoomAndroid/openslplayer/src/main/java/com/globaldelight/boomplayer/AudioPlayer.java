package com.globaldelight.boomplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Created by adarsh on 20/01/17.
 */

public class AudioPlayer implements Runnable {
    /** Load jni .so on initialization */
    private final String LOG_TAG = "AudioPlayer";
    private OpenSLPlayer nativePlayer = new OpenSLPlayer();
    private AudioEffect audioEffect;
    private AudioTrackReader reader = null;
    private Callback events = null;
    private States state = new States();
    private String sourcePath = null;
    private long sourceId = -1;
    private Context mContext;
    private boolean stop = false;
    private Thread playerThread;
    private Handler handler = new Handler();
    private boolean isFinish = false;
    private int mPauseSeek = -1;
    private AudioConfiguration mAudioConfig;

    long duration = 0;

    public void setEventsListener(Callback events) {
        this.events = events;
    }

    public AudioPlayer(Context context, Callback events) {
        setEventsListener(events);
        mContext = context;
        audioEffect = AudioEffect.getAudioEffectInstance(context);
        mAudioConfig = AudioConfiguration.getInstance(context);
    }

    private boolean floatAudioSupported() {
        return (mAudioConfig.getFormat() == AudioConfiguration.FORMAT_FLOAT);
    }

    @Override
    protected void finalize() throws Throwable {
        if ( isEngineInitalized ) {
            OpenSLPlayer.releaseEngine();
        }
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
        if (state.get() == States.STOPPED) {
            stop = false;
            playerThread = new Thread(this);
            playerThread.start();
        }else if (state.get() == States.PAUSED ) {
            nativePlayer.setPlayingState(true);
            state.set(States.PLAYING);
            syncNotify();
            pauseSeek();
        }
    }

    private synchronized void pauseSeek(){
        if(mPauseSeek >= 0){
            seek(mPauseSeek * duration / 100);
            mPauseSeek = -1;
        }
    }

    /**
     * Call notify to control the PAUSE (waiting) state, when the state is changed
     */
    private synchronized void syncNotify() {
        notify();
    }

    public void stop(){
        if(state.get() == States.PAUSED) {
            state.set(States.PLAYING);
            syncNotify();
        }
        stop = true;
        stopThread();
    }

    private void stopThread(){
        try {
            playerThread.interrupt();
            playerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        nativePlayer.setPlayingState(false);
        state.set(States.PAUSED);
    }

    private void seek(long pos) {
        if(reader != null) {
            nativePlayer.flush();
            reader.seekTo(pos);
        }
    }

    public void seek(int percent) {
        if(!isPause()) {
            seek(percent * duration / 100);
        }else{
            mPauseSeek = percent;
        }
    }

    /**
     * A pause mechanism that would block current thread when pause flag is set (PAUSED)
     */
    private synchronized void waitPlay(){
        // if (duration == 0) return;
        while(state.get() == States.PAUSED) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
        if(audioEffect.getSelectedEqualizerPosition() == 0) {
            setAutoEqualizer();
        }

        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wakeLock.acquire();

        try {
            state.set(States.LOADING);

            reader = new AudioTrackReader(sourcePath);
            reader.startReading();

            MediaFormat inputFormat = reader.getInputFormat();
            duration = inputFormat.getLong(MediaFormat.KEY_DURATION);

            state.set(States.PLAYING);
            startPlayer(reader.getOutputFormat(), true);

            postOnStart(inputFormat.getString(MediaFormat.KEY_MIME),
                    inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                    inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT),
                    duration);


            // Run reading loop
            long pts = 0;
            boolean completed = false;
            while ( !stop && !completed ) {
                waitPlay();
                AudioTrackReader.SampleBuffer buffer = reader.readNextBuffer();
                switch ( reader.getState() ) {
                    case AudioTrackReader.STATE_READING:
                        onReadBuffer(buffer);
                        if (  Math.abs(buffer.timeStamp - pts) > 500000) {
                            pts = buffer.timeStamp;
                            postProgressUpdate(buffer.timeStamp, duration);
                        }
                        break;

                    case AudioTrackReader.STATE_FORMAT_CHANGED:
                        onFormatChanged(reader.getOutputFormat());
                        break;

                    case AudioTrackReader.STATE_FINISHED:
                    case AudioTrackReader.STATE_ERROR:
                        completed = true;
                        break;

                    default:
                        break;
                }
                reader.releaseBuffer(buffer);
            }

            if ( !stop  ) {
                postProgressUpdate(duration, duration);
            }

            reader.stopReading();
            reader = null;

            /*Stop player*/
            boolean isShutdown = nativePlayer.shutdown(!stop);//STOP
            if(isShutdown)
                isFinish = true;

            stop = true;
            if(state.get() != States.STOPPED) {
                state.set(States.STOPPED);

                if (isShutdown && isFinish) {
                    postOnFinish();
                } else if (isShutdown && !isFinish) {
                    postOnStop();
                }
            }
        }
        catch (InterruptedException e) {
            // This is not an error
            state.set(States.STOPPED);
            stop = true;
        }
        catch (Exception e) {
            state.set(States.STOPPED);
            stop = true;
            postError();
        }
        finally {
            wakeLock.release();
        }
    }

    private void setGenreType(String genreType) {

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

    private void startPlayer(MediaFormat format, boolean applyEffects)
    {
        Log.d(LOG_TAG, "createPlayer " + format);

        initEngine();

        // if no output format; create a player with default configuration
        if ( format == null ) {
            nativePlayer.createAudioPlayer(44100, 2);
            return;
        }

        int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        if ( sampleRate == 0 ) {
            sampleRate = 44100;
        }
        int channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        if ( channels == 0 ) {
            channels = 2;
        }

        nativePlayer.createAudioPlayer(sampleRate, channels);
        if ( applyEffects ) {
            updatePlayerEffect();
        }
    }

    private static boolean isEngineInitalized = false;
    private void initEngine() {
        if ( isEngineInitalized ) {
            return;
        }

        isEngineInitalized = true;
        AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);

        String sampleRateStr = am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        int sampleRate = Integer.parseInt(sampleRateStr);
        if ( sampleRate == 0 ) sampleRate = 44100; // if not available use 44.1kHz

        String frameSizeStr = am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        int frameCount = Integer.parseInt(frameSizeStr);
        if ( frameCount == 0 ) frameCount = 1024; // if not available use 4k byteBuffer - 1024*2*2

        Log.d(LOG_TAG, "sampleRate:"+sampleRate);
        Log.d(LOG_TAG, "frameSize:"+frameCount);

        OpenSLPlayer.createEngine(mContext.getAssets(), sampleRate, frameCount, floatAudioSupported());

    }

    private void onReadBuffer(AudioTrackReader.SampleBuffer buffer) {
        if (buffer.size > 0 && buffer.byteBuffer != null) {
            int i = 0;
            while (i < buffer.size && !stop && state.get() == States.PLAYING) {
                i += nativePlayer.write(buffer.byteBuffer, i, (int)buffer.size);
            }
        }
    }

    private void onFormatChanged(MediaFormat format) {
        nativePlayer.shutdown(false);
        startPlayer(format, false);
    }

    private void postProgressUpdate(final long curTime, final long duration) {
        if (events != null ) {
            final int percent = (duration == 0) ? 0 : (int) (100 * curTime / duration);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    events.onPlayTimeUpdate(curTime / 1000, duration / 1000);
                }
            });
        }
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
        setEnableEffect(audioEffect.isAudioEffectOn());
        nativePlayer.setQuality(mAudioConfig.getQuality());
        if(audioEffect.isAudioEffectOn()) {
            setEnable3DAudio(audioEffect.is3DSurroundOn());
            if (audioEffect.isIntensityOn()) {
                setIntensityValue(audioEffect.getIntensity() / (double) 100);
            }

            setEnableEqualizer(audioEffect.isEqualizerOn());
            setEnableSuperBass(audioEffect.isFullBassOn());
            setEqualizerGain(audioEffect.getSelectedEqualizerPosition());

            setSpeakerEnable(AudioEffect.Speaker.FrontLeft, audioEffect.isLeftFrontSpeakerOn());

            setSpeakerEnable(AudioEffect.Speaker.FrontRight, audioEffect.isRightFrontSpeakerOn());

            setSpeakerEnable(AudioEffect.Speaker.Tweeter, audioEffect.isTweeterOn());

            setSpeakerEnable(AudioEffect.Speaker.RearLeft, audioEffect.isLeftSurroundSpeakerOn());

            setSpeakerEnable(AudioEffect.Speaker.RearRight, audioEffect.isRightSurroundSpeakerOn());

            setSpeakerEnable(AudioEffect.Speaker.Woofer, audioEffect.isWooferOn());

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
        try {
            if(isPlaying() || isPause())
                nativePlayer.setIntensity((float)((intensity-0.5)/0.5));
        }catch (Exception e){

        }
    }

    public synchronized void setEnable3DAudio(boolean enable3DAudio) {
        try {
            if (isPlaying() || isPause())
                nativePlayer.enable3DAudio(enable3DAudio);
        }catch (Exception e){}
    }

    public synchronized void setEnableEffect(boolean enableEffect) {
        try {
            if(isPlaying() || isPause())
                nativePlayer.enableAudioEffect(enableEffect);
        }catch (Exception e){

        }
    }

    public synchronized void setEnableEqualizer(boolean enable){
        try {
            if(isPlaying() || isPause())
                nativePlayer.enableEqualizer(enable);
        }catch (Exception e){}
    }

    public synchronized void setEnableSuperBass(boolean enableSuperBass) {
        try {
            if(isPlaying() || isPause())
                nativePlayer.enableSuperBass(enableSuperBass);
        }catch (Exception e){}
    }

    public synchronized void setEqualizerGain(int equalizerId) {
        if(equalizerId == 0){
            equalizerId = AudioEffect.getAudioEffectInstance(mContext).getAutoEqualizerValue();
        }
        try {
            if(isPlaying() || isPause()) {
                if(null != EqualizerGain.getEqGain(equalizerId)) {
                    nativePlayer.setEqualizer(equalizerId, EqualizerGain.getEqGain(equalizerId));
                }
            }
        }catch (Exception e){}
    }

    public synchronized void setSpeakerEnable(AudioEffect.Speaker speaker, boolean value){
        try {
            if(isPlaying() || isPause())
                nativePlayer.setSpeakerState(speaker.ordinal(), value == true ? 1 : 0);
        }catch (Exception e){}
    }

    public void setHighQualityEnable(boolean highQualityEnable) {
//        try {
//            if(isPlaying() || isPause())
//                setHighQualityEnable(highQualityEnable);
//        }catch (Exception e){}
    }

    public void setHeadPhone(int type){
        try {
            if(isPlaying() || isPause())
                nativePlayer.setHeadPhoneType(type);
        }catch (Exception e){}
    }


    public interface Callback {
        public void onStart(long duration);
        public void onPlay();
        public void onPlayTimeUpdate(long currentms, long totalms);
        public void onFinish();
        public void onStop();
        public void onError();
        public void onErrorPlayAgain();
    }

    public static class States {

        public static final int LOADING = 0;
        public static final int PLAYING = 1;
        public static final int PAUSED = 2;
        public static final int STOPPED = 3;
        public int playerState = STOPPED;

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
