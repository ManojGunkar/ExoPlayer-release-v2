package com.globaldelight.boomplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Created by adarsh on 20/01/17.
 */

public class AudioPlayer implements Runnable {
    /** Load jni .so on initialization */
    private final String LOG_TAG = "OpenSLPlayer";
    private OpenSLPlayer nativePlayer = new OpenSLPlayer();
    private AudioEffect audioEffect;
    private AudioTrackReader reader = null;
    private PlayerEvents events = null;
    private PlayerStates state = new PlayerStates();
    private String sourcePath = null;
    private Context mContext;
    private boolean stop = false;
    private Thread playerThread;
    private Handler handler = new Handler();
    private boolean isFinish = false;
    private int mPauseSeek = -1;
    private static boolean engineCreated = false;

    long duration = 0;

    public void setEventsListener(PlayerEvents events) {
        this.events = events;
    }

    public AudioPlayer(Context context, PlayerEvents events) {
        setEventsListener(events);
        mContext = context;
        audioEffect = AudioEffect.getAudioEffectInstance(context);
    }

    private boolean floatAudioSupported() {
        return (AudioConfiguration.getInstance(mContext).getFormat() == AudioConfiguration.FORMAT_FLOAT);
    }

    @Override
    protected void finalize() throws Throwable {
        if ( isEngineInitalized ) {
            OpenSLPlayer.releaseEngine();
        }
    }

    /**
     * For live streams, duration is 0
     * @return
     */
    public boolean isLive() {
        return (duration == 0);
    }

    public boolean isPlaying(){
        return state.isPlaying();
    }

    public boolean isPause(){
        return state.isPause();
    }

    public boolean isStopped() {
        return state.isStopped();
    }

    /**
     * set the data source, a file path or an url, or a file descriptor, to play encoded audio from
     * @param src
     */
    public void setDataSource(String src) {
        sourcePath = src;
    }

    public synchronized void play() {
        if (state.get() == PlayerStates.STOPPED) {
            stop = false;
            playerThread = new Thread(this);
            playerThread.start();
        }else if (state.get() == PlayerStates.PAUSED ) {
            nativePlayer.setPlayingAudioPlayer(true);
            state.set(PlayerStates.PLAYING);
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
        if(state.get() == PlayerStates.PAUSED) {
            state.set(PlayerStates.PLAYING);
            syncNotify();
        }
        stop = true;
        stopThread();
    }

    private void stopThread(){
        try {
            playerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        nativePlayer.setPlayingAudioPlayer(false);
        state.set(PlayerStates.PAUSED);
    }

    private void seek(long pos) {
        if(reader != null) {
            nativePlayer.seekTo(pos);
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
        while(state.get() == PlayerStates.PAUSED) {
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

        try {
            reader = new AudioTrackReader(sourcePath);
            reader.startReading();

            MediaFormat inputFormat = reader.getInputFormat();
            duration = inputFormat.getLong(MediaFormat.KEY_DURATION);

            startPlayer(reader.getOutputFormat(), true);

            state.set(PlayerStates.PLAYING);

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
            if(state.get() != PlayerStates.STOPPED) {
                state.set(PlayerStates.STOPPED);

                if (isShutdown && isFinish) {
                    postOnFinish();
                } else if (isShutdown && !isFinish) {
                    postOnStop();
                }
            }

        } catch (Exception e) {
            state.set(PlayerStates.STOPPED);
            stop = true;
            postError();
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
            while (i < buffer.size && !stop && state.get() == PlayerStates.PLAYING) {
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
                    events.onPlayUpdate(percent, curTime / 1000, duration / 1000);
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
                    events.onStart(mime, sampleRate, channels, duration);
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
        nativePlayer.setQuality(AudioConfiguration.getInstance(mContext).getQuality());
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
                nativePlayer.setIntensity((intensity-0.5)/0.5);
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
        nativePlayer.setHeadPhoneType(type);
    }
}
