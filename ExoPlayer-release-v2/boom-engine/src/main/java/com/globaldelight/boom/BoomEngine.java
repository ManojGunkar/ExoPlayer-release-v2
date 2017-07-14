package com.globaldelight.boom;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by adarsh on 23/06/17.
 */

public class BoomEngine {

    static {
        System.loadLibrary("BoomEngine");
    }

    public static final String LOG_TAG = "BoomEngine";

    public static int sampleRate;
    public static int frameCount;

    public static void init(Context context) {
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        String sampleRateStr = am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        sampleRate = Integer.parseInt(sampleRateStr);
        if ( sampleRate == 0 ) sampleRate = 44100; // if not available use 44.1kHz

        String frameSizeStr = am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        frameCount = Integer.parseInt(frameSizeStr);
        if ( frameCount == 0 ) frameCount = 1024; // if not available use 4k byteBuffer - 1024*2*2

        Log.d(LOG_TAG, "sampleRate:"+sampleRate);
        Log.d(LOG_TAG, "frameSize:"+frameCount);

        boolean isFloatAudio = (getFormat() == AudioFormat.ENCODING_PCM_FLOAT);

        init(context.getAssets(), sampleRate, frameCount);
    }

    public static int getFormat() {
        return AudioFormat.ENCODING_PCM_FLOAT;
    }

    public static int getSampleRate() {
        return sampleRate;
    }

    public static int getFrameCount() {
        return frameCount;
    }

    public native static void init(AssetManager assetManager, int sampleRate, int frameCount);

    public native static void finish();

    public BoomEngine(int sampleRate, int channelCount, boolean floatAudio) {
        start(sampleRate, channelCount, floatAudio);
    }

    public void release() {
        stop();
    }

    public native void start(int sampleRate, int channelCount, boolean floatAudio);

    public native int process(ByteBuffer inBuffer, ByteBuffer outBuffer, int size);

    public native int flush();

    public native void stop();

    public native void enableAudioEffect(boolean enable);

    public native void enable3DAudio(boolean enable);

    public native void enableSuperBass(boolean enable);

    public native void setQuality(int quality);

    public native void enableEqualizer(boolean enable);

    public native void setIntensity(float value);

    public native void setEqualizer(int id, float []bandGains);

    public native void setSpeakerState(int speakerId, float value);

    public native void setHeadPhoneType(int headPhoneType);

    public native int getHeadPhoneType();

    public native boolean get3DAudioState();

    public native boolean getEffectsState();

    public native float getIntensity();

    public native int getEqualizerId();

    public native float getSpeakerState(int speakerId);
}
