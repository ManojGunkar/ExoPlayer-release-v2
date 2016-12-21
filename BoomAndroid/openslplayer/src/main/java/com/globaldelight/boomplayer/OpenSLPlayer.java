package com.globaldelight.boomplayer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.browse.MediaBrowser;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.example.openslplayer.R;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;


/**
 * Created by Rahul Agarwal on 19-09-16.
 */
public class OpenSLPlayer implements Runnable {

    /** Load jni .so on initialization */
    static {
        System.loadLibrary("AudioTrackActivity");
    }
    public final String LOG_TAG = "OpenSLPlayer";
    AudioEffect audioEffectPreferenceHandler;
    private MediaExtractor extractor;
    private MediaCodec codec;
    private PlayerEvents events = null;
    private PlayerStates state = new PlayerStates();
    private String sourcePath = null;
    private int sourceRawResId = -1;
    private Context mContext;
    private boolean stop = false;
    private static Thread playerThread;
    Handler handler = new Handler();
    private static boolean isFinish = false;
    private static int mPauseSeek = -1;

    String mime = null;
    int sampleRate = 0, channels = 0, bitrate = 0;
    long presentationTimeUs = 0, duration = 0;

    public void setEventsListener(PlayerEvents events) {
        this.events = events;
    }

    public OpenSLPlayer(Context context, PlayerEvents events) {
        setEventsListener(events);
        mContext = context;
        audioEffectPreferenceHandler = AudioEffect.getAudioEffectInstance(context);
        EqualizerGain.setEqGain();

        AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);

        String sampleRateStr = am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        int sampleRate = Integer.parseInt(sampleRateStr);
        if ( sampleRate == 0 ) sampleRate = 44100; // if not available use 44.1kHz

        String frameSizeStr = am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        int frameCount = Integer.parseInt(frameSizeStr);
        if ( frameCount == 0 ) frameCount = 1024; // if not available use 4k buffer - 1024*2*2

        Log.d(LOG_TAG, "sampleRate:"+sampleRate);
        Log.d(LOG_TAG, "frameSize:"+frameCount);

        createEngine(mContext.getAssets(), sampleRate, frameCount, floatAudioSupported());
    }

    private boolean floatAudioSupported() {
        if ( Build.BRAND.equalsIgnoreCase("vivo") ) {
            return false;
        }

        return true;
    }

    @Override
    protected void finalize() throws Throwable {
        releaseEngine();
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

    public void setDataSource(Context context, int resid) {
        mContext = context;
        sourceRawResId = resid;
    }

    public synchronized void play() {
        if (state.get() == PlayerStates.STOPPED) {
            stop = false;
            playerThread = new Thread(this);
            playerThread.start();
        }else if (state.get() == PlayerStates.PAUSED ) {
            setPlayingAudioPlayer(true);
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
    public synchronized void syncNotify() {
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

    public void stopThread(){
        try {
            playerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        setPlayingAudioPlayer(false);
        state.set(PlayerStates.PAUSED);
    }

    public void seek(long pos) {
        if(extractor != null) {
            seekTo(pos);
            extractor.seekTo(pos, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        }
    }

    public void seek(int percent) {
        if(!isPause() && extractor != null) {
            seek(percent * duration / 100);
        }else{
            mPauseSeek = percent;
        }
    }

    /**
     * A pause mechanism that would block current thread when pause flag is set (PAUSED)
     */
    public synchronized void waitPlay(){
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
        if(AudioEffect.getAudioEffectInstance(mContext).getSelectedEqualizerPosition() == 0) {
            try {
                MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                metadataRetriever.setDataSource(this.sourcePath);
                setGenreType(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
            } catch (IllegalArgumentException e) {
                setGenreType(null);
            } catch (SecurityException e1) {
                setGenreType(null);
            } catch (Exception e) {
                setGenreType(null);
            }
        }

        // extractor gets information about the stream
        extractor = new MediaExtractor();
        // try to set the source, this might fail
        try {
            if (sourcePath != null) extractor.setDataSource(this.sourcePath);
            if (sourceRawResId != -1) {
                AssetFileDescriptor fd = mContext.getResources().openRawResourceFd(sourceRawResId);
                extractor.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getDeclaredLength());
                fd.close();
            }
        } catch (Exception e) {
            if (events != null) handler.post(new Runnable() { @Override public void run() { events.onError();  } });
            return;
        }

        // Read track header
        MediaFormat format = null;
        try {
            format = extractor.getTrackFormat(0);
            if(null != format) {
                mime = format.getString(MediaFormat.KEY_MIME);
                sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                // if duration is 0, we are probably playing a live stream
                duration = format.getLong(MediaFormat.KEY_DURATION);
                bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Reading format parameters exception:"+e.getMessage());
            // don't exit, tolerate this error, we'll fail later if this is critical
        }

        // check we have audio content we know
        if (format == null || !mime.startsWith("audio/")) {
            if (events != null) handler.post(new Runnable() { @Override public void run() { events.onError();  } });
            return;
        }

        // create the actual decoder, using the mime to select
        try {
            codec = MediaCodec.createDecoderByType(mime);
        } catch (IOException e) {
            e.printStackTrace();
        }catch (IllegalArgumentException  e){
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        // check we have a valid codec instance
        if (codec == null) {
            if (events != null) handler.post(new Runnable() { @Override public void run() { events.onError();  } });
            return;
        }

        if (events != null)
            handler.post(new Runnable() {
                @Override public void run() {
                    events.onStart(mime, sampleRate, channels, duration);
                }
            });

        try {
            codec.configure(format, null, null, 0);
            codec.start();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        } catch (MediaCodec.CodecException e){
            e.printStackTrace();
        } catch (IllegalStateException e){
            e.printStackTrace();
        }catch (MediaCodec.CryptoException e){

        }

        MediaFormat outputFormat = codec.getOutputFormat();
        createPlayer(outputFormat);

        if(null != extractor) {
            extractor.selectTrack(0);
        }

        if (extractor == null) {
            if (events != null) handler.post(new Runnable() { @Override public void run() { events.onError();  } });
            return;
        }

        // start decoding
        final long kTimeOutUs = 1000;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        boolean sawInputEOS = false;
        boolean sawOutputEOS = false;



        presentationTimeUs = 0;
        state.set(PlayerStates.PLAYING);
        updatePlayerEffect();
        try {
            while (!sawOutputEOS && !stop) {
                try {
                    if (playerThread.isInterrupted()) {
                        return;
                    }
                }catch (Exception e){
                    return;
                }

                // pause implementation
                waitPlay();

                // read a buffer before feeding it to the decoder
                if (!sawInputEOS) {
                    try {
                        int inputBufIndex = codec.dequeueInputBuffer(kTimeOutUs);
                        if (inputBufIndex >= 0) {
                            ByteBuffer dstBuf = codec.getInputBuffer(inputBufIndex);
                            int sampleSize = 0;
                            if (null != extractor) {
                                sampleSize = extractor.readSampleData(dstBuf, 0);
                            }
                            if (sampleSize < 0) {
                                //Log.d(LOG_TAG, "saw input EOS. Stopping playback");
                                sawInputEOS = true;
                                sampleSize = 0;
                            } else {
                                long curTime = extractor.getSampleTime();
                                if (events != null && Math.abs(curTime - presentationTimeUs) > 500000) {
                                    presentationTimeUs = curTime;
                                    final int percent = (duration == 0) ? 0 : (int) (100 * presentationTimeUs / duration);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            events.onPlayUpdate(percent, presentationTimeUs / 1000, duration / 1000);
                                        }
                                    });
                                }
                            }
                            codec.queueInputBuffer(inputBufIndex, 0, sampleSize, presentationTimeUs, sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
                            if (!sawInputEOS) {
                                extractor.advance();
                            }
                        }
                    } catch (MediaCodec.CodecException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                } // !sawInputEOS

                // decode to PCM and push it to the OpenSLPlayer player
                try {
                    int outputBufIndex = codec.dequeueOutputBuffer(info, kTimeOutUs);
                    if (outputBufIndex >= 0) {
                        ByteBuffer buf = codec.getOutputBuffer(outputBufIndex);
                        if (info.size > 0 && null != buf) {
                            int i = 0;
                            while (i < info.size && !stop && state.get() == PlayerStates.PLAYING) {
                                i += write(buf, i, info.size);
                            }
                        }
                        codec.releaseOutputBuffer(outputBufIndex, false);
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            sawOutputEOS = true;
                        }
                    } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        MediaFormat newFormat = codec.getOutputFormat();
                        if ( !isSameFormat(newFormat, outputFormat) ) {
                            outputFormat = newFormat;
                            Log.d(LOG_TAG,"Format changed" + outputFormat);
                            shutdown(false);
                            createPlayer(outputFormat);
                        }

                        // Restart player
                    }
                } catch (MediaCodec.CodecException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //Log.d(LOG_TAG, "stopping...");

        if ( !stop && events != null ) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    events.onPlayUpdate(100, duration / 1000, duration / 1000);
                }
            });
        }

        if(codec != null) {
            try {
                codec.stop();
                codec.release();
                codec = null;
            }catch (IllegalStateException e){
                e.printStackTrace();
            }
        }

        if ( extractor != null ) {
            extractor.release();
            extractor = null;
        }

        /*Stop player*/
        boolean isShutdown = false;
        if(stop) {
            isShutdown = shutdown(false);//STOP
        }else{
            isShutdown = shutdown(true);// FINISH
            if(isShutdown)
                isFinish = true;
        }
        // clear source and the other globals
        sourcePath = null;
        sourceRawResId = -1;
        duration = 0;
        mime = null;
        sampleRate = 0; channels = 0; bitrate = 0;
        presentationTimeUs = 0; duration = 0;

        stop = true;
        if(state.get() != PlayerStates.STOPPED) {
            state.set(PlayerStates.STOPPED);

            if (isShutdown && isFinish) {
                if (events != null) handler.post(new Runnable() {
                    @Override
                    public void run() {
                        events.onFinish();
                    }
                });
            } else if (isShutdown && !isFinish) {
                if (events != null) handler.post(new Runnable() {
                    @Override
                    public void run() {
                        events.onStop();
                    }
                });
            }
        }
    }

    private void setGenreType(String genreType) {

            if (null != genreType) {
                for (int i = 0; i <= mContext.getResources().getStringArray(R.array.mapped_eq_key).length - 1; i++) {
                    if (genreType.toUpperCase().contains(mContext.getResources().getStringArray(R.array.mapped_eq_key)[i].toUpperCase())) {
                        String genre = mContext.getResources().getStringArray(R.array.mapped_eq_value)[i];
                        List t = Arrays.asList(mContext.getResources().getStringArray(R.array.eq_names));
                        AudioEffect.getAudioEffectInstance(mContext).setAutoEqualizerPosition(t.indexOf(genre.toUpperCase()));
                        Log.d("Song Genre : ", genreType);
                        Log.d("Selected Song Genre : ", genre);
                        return;
                    }
                }
        }
        AudioEffect.getAudioEffectInstance(mContext).setAutoEqualizerPosition(12);
        Log.d("Selected Song Genre : ", "MUSIC");
    }

    public void SupportedCodec() {
        String results = "";
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            // grab results and put them in a list
            String name = codecInfo.getName();
            boolean isEncoder = codecInfo.isEncoder();
            String[] types = codecInfo.getSupportedTypes();
            String typeList = "";
            for (String s:types) typeList += s + " ";
            results += (i+1) + ". " + name+ " " + typeList + "\n\n";
        }
        Log.d("Supported Formats : ", results);
    }

    private void createPlayer(MediaFormat format)
    {
        Log.d(LOG_TAG, "createPlayer " + format);

        // if no output format; create a player with default configuration
        if ( format == null ) {
            createAudioPlayer(44100, 2);
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

        createAudioPlayer(sampleRate, channels);
    }



    private boolean isSameFormat(MediaFormat f1, MediaFormat f2) {
        return (f1.getInteger(MediaFormat.KEY_SAMPLE_RATE) == f2.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                && f1.getInteger(MediaFormat.KEY_CHANNEL_COUNT) == f2.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
    }



    /** Native methods, implemented in jni folder */
    public native void createEngine(AssetManager assetManager, int sampleRate, int frameCount, boolean useFloat);

    public native void releaseEngine();

    public native boolean createAudioPlayer(int sampleRate, int channels);

    public native int write(ByteBuffer buf, int offset, int frameCount);

    public static native void setPlayingAudioPlayer(boolean isPlaying);

    public static native void seekTo(long position);

    public native void setHeadPhoneType(int headPhoneType);

    public native int getHeadPhoneType();

    public native void enableAudioEffect(boolean enable);

    public native void enable3DAudio(boolean enable);

    public native void enableSuperBass(boolean enable);

    public native void enableHighQuality(boolean enable);

    public native void enableEqualizer(boolean enable);

    public native void setIntensity(double value);

    public native void SetEqualizer(int id, float []bandGains);

    public native void SetSpeakerState(int speakerId, float value);

    public native boolean  Get3DAudioState();

    public native boolean GetEffectsState();

    public native boolean GetIntensity();

    public native int GetEqualizerId();

    public native float GetSpeakerState(int speakerId);

    public native boolean shutdown(boolean enable);

    public static native void setVolumeAudioPlayer(int millibel);

    public static native void setMutAudioPlayer(boolean mute);


    public void updatePlayerEffect(){

        setEnableEffect(audioEffectPreferenceHandler.isAudioEffectOn());
        if(audioEffectPreferenceHandler.isAudioEffectOn()) {
            setEnable3DAudio(audioEffectPreferenceHandler.is3DSurroundOn());
            if (audioEffectPreferenceHandler.isIntensityOn()) {
                setIntensityValue(audioEffectPreferenceHandler.getIntensity() / (double) 100);
            }
            setEnableEqualizer(audioEffectPreferenceHandler.isEqualizerOn());
            setEnableSuperBass(audioEffectPreferenceHandler.isFullBassOn());
            setEqualizerGain(audioEffectPreferenceHandler.getSelectedEqualizerPosition());

            setSpeakerEnable(AudioEffect.Speaker.FrontLeft, audioEffectPreferenceHandler.isLeftFrontSpeakerOn());

            setSpeakerEnable(AudioEffect.Speaker.FrontRight, audioEffectPreferenceHandler.isRightFrontSpeakerOn());

            setSpeakerEnable(AudioEffect.Speaker.Tweeter, audioEffectPreferenceHandler.isTweeterOn());

            setSpeakerEnable(AudioEffect.Speaker.RearLeft, audioEffectPreferenceHandler.isLeftSurroundSpeakerOn());

            setSpeakerEnable(AudioEffect.Speaker.RearRight, audioEffectPreferenceHandler.isRightSurroundSpeakerOn());

            setSpeakerEnable(AudioEffect.Speaker.Woofer, audioEffectPreferenceHandler.isWooferOn());

            setHighQualityEnable(audioEffectPreferenceHandler.isIntensityOn());

            setHeadPhone(audioEffectPreferenceHandler.getHeadPhoneType());
        }
    }


    public synchronized void setIntensityValue(double intensity) {
        try {
            if(isPlaying() || isPause())
                setIntensity((intensity-0.5)/0.5);
        }catch (Exception e){}
    }

    public synchronized void setEnable3DAudio(boolean enable3DAudio) {
        try {
            if(AudioEffect.getAudioEffectInstance(mContext).isMasterEffectControlEnabled()) {
                if (isPlaying() || isPause())
                    enable3DAudio(enable3DAudio);
            }
        }catch (Exception e){}
    }

    public synchronized void setEnableEffect(boolean enableEffect) {
        try {
            if(isPlaying() || isPause())
                enableAudioEffect(enableEffect);
        }catch (Exception e){}
    }

    public synchronized void setEnableEqualizer(boolean enable){
        try {
            if(isPlaying() || isPause())
                enableEqualizer(enable);
        }catch (Exception e){}
    }

    public synchronized void setEnableSuperBass(boolean enableSuperBass) {
        try {
            if(isPlaying() || isPause())
                enableSuperBass(enableSuperBass);
        }catch (Exception e){}
    }

    public synchronized void setEqualizerGain(int equalizerId) {
        if(equalizerId == 0){
            equalizerId = AudioEffect.getAudioEffectInstance(mContext).getAutoEqualizerValue();
        }
        try {
            if(EqualizerGain.getEqualizerSize() <= 0){
                EqualizerGain.setEqGain();
            }else {
                if(isPlaying() || isPause())
                    if(null != EqualizerGain.getEqGain(equalizerId))
                        SetEqualizer(equalizerId, EqualizerGain.getEqGain(equalizerId));
            }
        }catch (Exception e){}
    }

    public synchronized void setSpeakerEnable(AudioEffect.Speaker speaker, boolean value){
        try {
            if(isPlaying() || isPause())
                SetSpeakerState(speaker.ordinal(), value == true ? 1 : 0);
        }catch (Exception e){}
    }

    public void setHighQualityEnable(boolean highQualityEnable) {
//        try {
//            if(isPlaying() || isPause())
//                setHighQualityEnable(highQualityEnable);
//        }catch (Exception e){}
    }

    public void setHeadPhone(int type){
        setHeadPhoneType(type);
    }
}
