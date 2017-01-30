package com.globaldelight.boomplayer;

import android.content.res.AssetManager;
import java.nio.ByteBuffer;


/**
 * Created by Rahul Agarwal on 19-09-16.
 * OpenSLPlayer is the interface for native BoomPlayer
 */
public class OpenSLPlayer {

    static {
        System.loadLibrary("BoomPlayer");
    }

    /** Native methods, implemented in jni folder */
    public native static void createEngine(AssetManager assetManager, int sampleRate, int frameCount, boolean useFloat);

    public native static void releaseEngine();

    public native boolean createAudioPlayer(int sampleRate, int channels);

    public native boolean shutdown(boolean wait);

    public native int write(ByteBuffer buf, int offset, int frameCount);

    public native void setPlayingAudioPlayer(boolean isPlaying);

    public native void seekTo(long position);

    public native void enableAudioEffect(boolean enable);

    public native void enable3DAudio(boolean enable);

    public native void enableSuperBass(boolean enable);

    public native void setQuality(int quality);

    public native void enableEqualizer(boolean enable);

    public native void setIntensity(double value);

    public native void setEqualizer(int id, float []bandGains);

    public native void setSpeakerState(int speakerId, float value);

    public native void setHeadPhoneType(int headPhoneType);

    public native int getHeadPhoneType();

    public native boolean get3DAudioState();

    public native boolean getEffectsState();

    public native boolean getIntensity();

    public native int getEqualizerId();

    public native float getSpeakerState(int speakerId);

    public native void setVolumeAudioPlayer(int millibel);

    public native void setMutAudioPlayer(boolean mute);
}
