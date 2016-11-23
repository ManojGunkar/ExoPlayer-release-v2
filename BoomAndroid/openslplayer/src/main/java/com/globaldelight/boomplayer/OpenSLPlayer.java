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
import android.os.Handler;
import android.util.Log;
import java.nio.ByteBuffer;


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

        Log.e(LOG_TAG, "sampleRate:"+sampleRate);
        Log.e(LOG_TAG, "frameSize:"+frameCount);

        createEngine(mContext.getAssets(), sampleRate, frameCount);
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

    public void play() {
        if (state.get() == PlayerStates.STOPPED) {
            stop = false;
            playerThread = new Thread(this);
            playerThread.start();
        }else if (state.get() == PlayerStates.PAUSED ) {
            setPlayingAudioPlayer(true);
            state.set(PlayerStates.PLAYING);
            syncNotify();
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
        seekTo(pos);
        extractor.seekTo(pos, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
    }

    public void seek(int percent) {
        long pos = percent * duration / 100;
        seek(pos);
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
            try{
                Thread.sleep(100);
            }catch (InterruptedException e){

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
            e.printStackTrace();
            //Log.e(LOG_TAG, "exception:"+e.getMessage());

            if (events != null) handler.post(new Runnable() { @Override public void run() { events.onError();  } });
            return;
        }

        // Read track header
        MediaFormat format = null;
        try {
            format = extractor.getTrackFormat(0);
            if(format != null) {
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
        Log.d(LOG_TAG, "Track info: mime:" + mime + " sampleRate:" + sampleRate + " channels:" + channels + " bitrate:" + bitrate + " duration:" + duration);

        // check we have audio content we know
        if (format == null || !mime.startsWith("audio/")) {
            if (events != null) handler.post(new Runnable() { @Override public void run() { events.onError();  } });
            return;
        }
        // create the actual decoder, using the mime to select
        try {
            codec = MediaCodec.createDecoderByType(mime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // check we have a valid codec instance
        if (codec == null) {
            if (events != null) handler.post(new Runnable() { @Override public void run() { events.onError();  } });
            return;
        }

        //state.set(PlayerStates.PAUSED);
        if (events != null)
            handler.post(new Runnable() {
                @Override public void run() {
                    events.onStart(mime, sampleRate, channels, duration);
                }
            });

        try {
            codec.configure(format, null, null, 0);
            codec.start();
        }catch (Exception e){

        }
        ByteBuffer[] codecInputBuffers  = null;
        ByteBuffer[] codecOutputBuffers = null;

        try{
            codecInputBuffers = codec.getInputBuffers();
            codecOutputBuffers = codec.getOutputBuffers();
        }catch (IllegalStateException e){

        }

        // configure OpenSLPlayer
        createAudioPlayer(256*1024, sampleRate, 2);

        extractor.selectTrack(0);

        // start decoding
        final long kTimeOutUs = 1000;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        boolean sawInputEOS = false;
        boolean sawOutputEOS = false;
        int noOutputCounter = 0;
        int noOutputCounterLimit = 10;

        state.set(PlayerStates.PLAYING);
        updatePlayerEffect();
        while (!sawOutputEOS && noOutputCounter < noOutputCounterLimit && !stop) {

            if(playerThread.isInterrupted()){
                return;
            }

            // pause implementation
            waitPlay();

            noOutputCounter++;
            // read a buffer before feeding it to the decoder
            if (!sawInputEOS) {
                int inputBufIndex = 0;
                try{
                    inputBufIndex = codec.dequeueInputBuffer(kTimeOutUs);
                }catch (IllegalStateException e){

                }
                if (inputBufIndex >= 0) {
                    ByteBuffer dstBuf = null;
                    if(null != codecInputBuffers[inputBufIndex]){
                        dstBuf = codecInputBuffers[inputBufIndex];
                    }
                    int sampleSize = extractor.readSampleData(dstBuf, 0);
                    if (sampleSize < 0) {
                        //Log.d(LOG_TAG, "saw input EOS. Stopping playback");
                        sawInputEOS = true;
                        sampleSize = 0;
                    } else {
                        presentationTimeUs = extractor.getSampleTime();
                        final int percent = (duration == 0) ? 0 : (int) (100 * presentationTimeUs / duration);
                        if (events != null) handler.post(new Runnable() {
                            @Override
                            public void run() {
                                events.onPlayUpdate(percent, presentationTimeUs / 1000, duration / 1000);
                            }
                        });
                    }
                    try {
                        codec.queueInputBuffer(inputBufIndex, 0, sampleSize, presentationTimeUs, sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
                    }catch (IllegalStateException e){

                    }
                    if (!sawInputEOS) extractor.advance();

                } else {
                    //Log.e(LOG_TAG, "inputBufIndex " +inputBufIndex);
                }
            } // !sawInputEOS

            // decode to PCM and push it to the OpenSLPlayer player
            int res = 0;
            try {
                res = codec.dequeueOutputBuffer(info, kTimeOutUs);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            if (res >= 0) {
                if (info.size > 0) noOutputCounter = 0;

                int outputBufIndex = res;
                ByteBuffer buf = codecOutputBuffers[outputBufIndex];

                final byte[] chunk = new byte[info.size];
                buf.get(chunk);
                buf.clear();

                if (chunk.length > 0) {
                    int i = 0;
                    while (i != chunk.length && !stop && state.get() == PlayerStates.PLAYING) {
                        i += write(chunk, i, chunk.length);
                    }
                }
                try {
                    codec.releaseOutputBuffer(outputBufIndex, false);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                    Log.d(LOG_TAG, "saw output EOS.");
                    sawOutputEOS = true;
                }
            } else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                try {
                    codecOutputBuffers = codec.getOutputBuffers();
                }catch (IllegalStateException e){

                }
//                Log.d(LOG_TAG, "output buffers have changed.");
            } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat oformat = null;
                try {
                    oformat = codec.getOutputFormat();
                }catch (IllegalStateException e){}
//                Log.d(LOG_TAG, "output format has changed to " + oformat);
            } else {
//                Log.d(LOG_TAG, "dequeueOutputBuffer returned " + res);
            }
        }

        //Log.d(LOG_TAG, "stopping...");

        if(codec != null) {
            try {
                codec.stop();
            }catch (IllegalStateException e){

            }
            codec.release();
            codec = null;
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


            if (noOutputCounter >= noOutputCounterLimit) {
                if (events != null) handler.post(new Runnable() {
                    @Override
                    public void run() {
                        events.onError();
                    }
                });
            } else if (isShutdown && isFinish) {
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



    /** Native methods, implemented in jni folder */
    public native void createEngine(AssetManager assetManager, int sampleRate, int frameCount);

    public native void releaseEngine();

    public native boolean createAudioPlayer(int size, int sampleRate, int bufferSize);

    public native int write(byte[] sData, int offset, int frameCount);

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
            if(isPlaying() || isPause())
                enable3DAudio(enable3DAudio);
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
