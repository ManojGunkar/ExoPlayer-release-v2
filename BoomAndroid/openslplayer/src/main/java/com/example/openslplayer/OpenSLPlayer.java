package com.example.openslplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.content.Context.MODE_PRIVATE;
import static com.example.openslplayer.AudioEffect.AUDIO_EFFECT_POWER;
import static com.example.openslplayer.AudioEffect.AUDIO_EFFECT_SETTING;
import static com.example.openslplayer.AudioEffect.DEFAULT_POWER;
import static com.example.openslplayer.AudioEffect.EQUALIZER_POSITION;
import static com.example.openslplayer.AudioEffect.EQUALIZER_POWER;
import static com.example.openslplayer.AudioEffect.FULL_BASS;
import static com.example.openslplayer.AudioEffect.INTENSITY_POSITION;
import static com.example.openslplayer.AudioEffect.INTENSITY_POWER;
import static com.example.openslplayer.AudioEffect.POWER_OFF;
import static com.example.openslplayer.AudioEffect.POWER_ON;
import static com.example.openslplayer.AudioEffect.SELECTED_EQUALIZER_POSITION;
import static com.example.openslplayer.AudioEffect.SPEAKER_LEFT_FRONT;
import static com.example.openslplayer.AudioEffect.SPEAKER_LEFT_SURROUND;
import static com.example.openslplayer.AudioEffect.SPEAKER_RIGHT_FRONT;
import static com.example.openslplayer.AudioEffect.SPEAKER_RIGHT_SURROUND;
import static com.example.openslplayer.AudioEffect.SPEAKER_SUB_WOOFER;
import static com.example.openslplayer.AudioEffect.SPEAKER_TWEETER;
import static com.example.openslplayer.AudioEffect.THREE_D_SURROUND_POWER;

/**
 * Created by Rahul Agarwal on 19-09-16.
 */
public class OpenSLPlayer implements Runnable {

    /** Load jni .so on initialization */
    static {
        System.loadLibrary("AudioTrackActivity");
    }
    public final String LOG_TAG = "OpenSLPlayer";
    SharedPreferences pref;
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

    public OpenSLPlayer() {

    }
    public OpenSLPlayer(Context context, PlayerEvents events) {
        setEventsListener(events);
        mContext = context;
        pref = context.getSharedPreferences(AUDIO_EFFECT_SETTING, MODE_PRIVATE);
        EqualizerGain.setEqGain();
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
    public void stop() {

        if(state.get() != PlayerStates.PAUSED) {
            doStop();
        }else{
            play();
            doStop();
        }
    }

    private void doStop(){
        stop = true;
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
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

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

//            format.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);
//            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2);
//            format.setInteger(MediaFormat.KEY_BIT_RATE, 96000);

            mime = format.getString(MediaFormat.KEY_MIME);
            sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            // if duration is 0, we are probably playing a live stream
            duration = format.getLong(MediaFormat.KEY_DURATION);
            bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE);

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
        } catch (IOException e) {
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

        codec.configure(format, null, null, 0);
        codec.start();
        ByteBuffer[] codecInputBuffers  = codec.getInputBuffers();
        ByteBuffer[] codecOutputBuffers = codec.getOutputBuffers();

        // configure OpenSLPlayer
        createEngine(mContext.getAssets());
        createAudioPlayer(1024*1024, sampleRate, channels);

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
                int inputBufIndex = codec.dequeueInputBuffer(kTimeOutUs);
                if (inputBufIndex >= 0) {
                    ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
                    int sampleSize = extractor.readSampleData(dstBuf, 0);
                    if (sampleSize < 0) {
                        //Log.d(LOG_TAG, "saw input EOS. Stopping playback");
                        sawInputEOS = true;
                        sampleSize = 0;
                    } else {
                        presentationTimeUs = extractor.getSampleTime();
                        final int percent = (duration == 0) ? 0 : (int) (100 * presentationTimeUs / duration);
                        Log.d("Finish", "" + percent);
                        if (events != null) handler.post(new Runnable() {
                            @Override
                            public void run() {
                                events.onPlayUpdate(percent, presentationTimeUs / 1000, duration / 1000);
                            }
                        });
                    }

                    codec.queueInputBuffer(inputBufIndex, 0, sampleSize, presentationTimeUs, sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);

                    if (!sawInputEOS) extractor.advance();

                } else {
                    //Log.e(LOG_TAG, "inputBufIndex " +inputBufIndex);
                }
            } // !sawInputEOS

            // decode to PCM and push it to the OpenSLPlayer player
            int res = 0;
            try {
                res = codec.dequeueOutputBuffer(info, kTimeOutUs);
            } catch (Exception e) {
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
                    while (i != chunk.length) {

                        i += write(chunk, i, chunk.length);
                    }

                }
                try {
                    codec.releaseOutputBuffer(outputBufIndex, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                    Log.d(LOG_TAG, "saw output EOS.");
                    sawOutputEOS = true;
                }
            } else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                codecOutputBuffers = codec.getOutputBuffers();
//                Log.d(LOG_TAG, "output buffers have changed.");
            } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat oformat = codec.getOutputFormat();
//                Log.d(LOG_TAG, "output format has changed to " + oformat);
            } else {
//                Log.d(LOG_TAG, "dequeueOutputBuffer returned " + res);
            }
        }

        //Log.d(LOG_TAG, "stopping...");

        if(codec != null) {
            codec.stop();
            codec.release();
            codec = null;
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


        state.set(PlayerStates.STOPPED);
        stop = true;

        if(noOutputCounter >= noOutputCounterLimit) {
            if (events != null) handler.post(new Runnable() { @Override public void run() { events.onError();  } });
        } else if(isShutdown && isFinish){
            if (events != null) handler.post(new Runnable() { @Override public void run() { events.onFinish();  } });
        } else if(isShutdown && !isFinish){
            if (events != null) handler.post(new Runnable() { @Override public void run() { events.onStop();  } });
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
    public native void createEngine(AssetManager assetManager);

    public native boolean createAudioPlayer(int size, int sampleRate, int bufferSize);

    public native int write(byte[] sData, int offset, int frameCount);

    public static native void setPlayingAudioPlayer(boolean isPlaying);

    public static native void seekTo(long position);

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

    public static native boolean readAsset(AssetManager mgr);

    public void updatePlayerEffect(){
        boolean isAudioEffect = pref.getBoolean(AUDIO_EFFECT_POWER, DEFAULT_POWER);
            setEnableEffect(isAudioEffect);
        boolean is3DAudio = pref.getBoolean(THREE_D_SURROUND_POWER, DEFAULT_POWER);
        setEnable3DAudio(is3DAudio);
        boolean isIntensity = pref.getBoolean(INTENSITY_POWER, DEFAULT_POWER);
        if(isIntensity){
            setIntensity(pref.getInt(INTENSITY_POSITION, 50)/(double)100);
        }
        boolean isEqualizer = pref.getBoolean(EQUALIZER_POWER, DEFAULT_POWER);
        setEnableEqualizer(isEqualizer);
        boolean isFullBass = pref.getBoolean(FULL_BASS, DEFAULT_POWER);
        setEnableSuperBass(isFullBass);
        int eq = pref.getInt(SELECTED_EQUALIZER_POSITION, EQUALIZER_POSITION);
        setEqualizerGain(eq);

        if(pref.getBoolean(SPEAKER_LEFT_FRONT, DEFAULT_POWER) == POWER_OFF){
            setSpeakerEnable(AudioEffect.Speaker.FrontLeft, POWER_OFF);
        }else if(pref.getBoolean(SPEAKER_LEFT_FRONT, DEFAULT_POWER) == POWER_ON){
            setSpeakerEnable(AudioEffect.Speaker.FrontLeft, POWER_ON);
        }
        if(pref.getBoolean(SPEAKER_RIGHT_FRONT, DEFAULT_POWER) == POWER_OFF){
            setSpeakerEnable(AudioEffect.Speaker.FrontRight, POWER_OFF);
        }else if(pref.getBoolean(SPEAKER_RIGHT_FRONT, DEFAULT_POWER) == POWER_ON){
            setSpeakerEnable(AudioEffect.Speaker.FrontRight, POWER_ON);
        }
        if(pref.getBoolean(SPEAKER_TWEETER, DEFAULT_POWER) == POWER_OFF){
            setSpeakerEnable(AudioEffect.Speaker.Tweeter, POWER_OFF);
        }else if(pref.getBoolean(SPEAKER_TWEETER, DEFAULT_POWER) == POWER_ON){
            setSpeakerEnable(AudioEffect.Speaker.Tweeter, POWER_ON);
        }
        if(pref.getBoolean(SPEAKER_LEFT_SURROUND, DEFAULT_POWER) == POWER_OFF){
            setSpeakerEnable(AudioEffect.Speaker.RearLeft, POWER_OFF);
        }else if(pref.getBoolean(SPEAKER_LEFT_SURROUND, DEFAULT_POWER) == POWER_ON){
            setSpeakerEnable(AudioEffect.Speaker.RearLeft, POWER_ON);
        }
        if(pref.getBoolean(SPEAKER_RIGHT_SURROUND, DEFAULT_POWER) == POWER_OFF){
            setSpeakerEnable(AudioEffect.Speaker.RearRight, POWER_OFF);
        }else if(pref.getBoolean(SPEAKER_RIGHT_SURROUND, DEFAULT_POWER) == POWER_ON){
            setSpeakerEnable(AudioEffect.Speaker.RearRight, POWER_ON);
        }
        if(pref.getBoolean(SPEAKER_SUB_WOOFER, DEFAULT_POWER) == POWER_OFF){
            setSpeakerEnable(AudioEffect.Speaker.Woofer, POWER_OFF);
        }else if(pref.getBoolean(SPEAKER_SUB_WOOFER, DEFAULT_POWER) == POWER_ON){
            setSpeakerEnable(AudioEffect.Speaker.Woofer, POWER_ON);
        }
        if(pref.getBoolean(INTENSITY_POWER, DEFAULT_POWER) == POWER_OFF){
            setHighQualityEnable(POWER_OFF);
        }else if(pref.getBoolean(INTENSITY_POWER, DEFAULT_POWER) == POWER_ON){
            setHighQualityEnable(POWER_ON);
        }

    }


    public synchronized void setIntensityValue(double intensity) {
        try {
            if(isPlaying() || isPause())
                setIntensity(intensity);
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
}
