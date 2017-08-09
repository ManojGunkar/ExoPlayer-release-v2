package com.globaldelight.boom;

import android.support.annotation.IntDef;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.audio.AudioProcessor;

import java.nio.ByteBuffer;

/**
 * Created by adarsh on 27/06/17.
 */

public class BoomAudioProcessor implements AudioProcessor {

    private static int CHANNEL_COUNT = 2;
    private BoomEngine engine = null;
    private ByteBuffer outputBuffer = ByteBuffer.allocateDirect(2048*4*2*2);
    private int sampleRate;
    private int inputChannelCount;
    private boolean isEOS = false;

    private boolean isEffectsEnbled = false;
    private boolean isSurroundSoundEnabled = true;
    private boolean isFullbassEnabled = false;
    private float   intensity = 0.0f;
    private int     equalizerId = 0;
    private @Constants.Headphone int headPhoneType = Constants.Headphone.IN_CANAL;
    private @Constants.Quality int audioQuality = Constants.Quality.HIGH;
    private float[] speakerStates = new float[] { 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f };
    private boolean mFloatAudio = false;


    public BoomAudioProcessor(boolean floatAudio) {
        mFloatAudio = floatAudio;
    }


    @Override
    public boolean configure(int sampleRateHz, int channelCount, @C.Encoding int encoding) throws UnhandledFormatException {
        if ( encoding != C.ENCODING_PCM_16BIT ) {
            throw new UnhandledFormatException(sampleRateHz, channelCount, encoding);
        }

        sampleRate = sampleRateHz;
        inputChannelCount = channelCount;
        outputBuffer.clear();

        engine = new BoomEngine(sampleRate, inputChannelCount, mFloatAudio);
        applyEffects();

        return true;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public int getOutputChannelCount() {
        return CHANNEL_COUNT;
    }

    @Override
    public int getOutputEncoding() {
        return mFloatAudio? C.ENCODING_PCM_FLOAT : C.ENCODING_PCM_16BIT;
    }

    @Override
    public void queueInput(ByteBuffer buffer) {
        if (buffer.hasRemaining()) {
            int numBytes = buffer.remaining();
            outputBuffer.clear();
            engine.process(buffer, outputBuffer, numBytes);
        }
    }

    @Override
    public void queueEndOfStream() {
        isEOS = true;
    }

    @Override
    public ByteBuffer getOutput() {
        return outputBuffer;
    }

    @Override
    public boolean isEnded() {
        return isEOS;
    }

    @Override
    public void flush() {
        engine.flush();
    }

    @Override
    public void reset() {
        if ( engine != null ) {
            engine.release();
        }
    }

    public void setEffectState(boolean enable) {
        isEffectsEnbled = enable;
        if ( engine != null ) {
            engine.enableAudioEffect(isEffectsEnbled);
            if ( isEffectsEnbled ) {
                onEffectEnabled();
            }
        }
    }

    public boolean getEffectState() {
        return isEffectsEnbled;
    }

    public void set3DAudioState(boolean state) {
        isSurroundSoundEnabled = state;
        if ( engine != null ) {
            engine.enable3DAudio(isSurroundSoundEnabled);
        }
    }

    public boolean get3DAudioState() {
        return isSurroundSoundEnabled;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
        if ( engine != null ) {
            engine.setIntensity(this.intensity);
        }
    }

    public float getIntensity() {
        return intensity;
    }


    public void setFullBassState(boolean state) {
        isFullbassEnabled = state;
        if ( engine != null ) {
            engine.enableSuperBass(isFullbassEnabled);
        }
    }

    public boolean getFullBassState() {
        return isFullbassEnabled;
    }

    public float getSpeakerState(@Constants.Speaker int speakerId) {
        return speakerStates[speakerId];
    }

    public void setSpeakerState(@Constants.Speaker int speakerId, float volume) {
        speakerStates[speakerId] = volume;
        if ( engine != null ) {
            engine.setSpeakerState(speakerId, speakerStates[speakerId]);
        }
    }


    public void setEqualizer(int equalizerId) {
        this.equalizerId = equalizerId;
        if ( engine != null ) {
            engine.setEqualizer(this.equalizerId, EqualizerGain.getEqGain(equalizerId));
        }
    }

    public int getEqualizer() {
        return equalizerId;
    }

    public void setHeadPhoneType(@Constants.Headphone int headPhoneType) {
        this.headPhoneType = headPhoneType;
        if ( engine != null ) {
            engine.setHeadPhoneType(this.headPhoneType);
        }

    }

    public void setQuality(@Constants.Quality int quality) {
        this.audioQuality = quality;
        if ( engine != null ) {
            engine.setQuality(this.audioQuality);
        }

    }



    private void applyEffects() {
        engine.setQuality(audioQuality);
        engine.setHeadPhoneType(headPhoneType);
        engine.enableAudioEffect(isEffectsEnbled);
        if ( isEffectsEnbled ) {
            onEffectEnabled();
        }
    }



    private void onEffectEnabled() {
        engine.enable3DAudio(isSurroundSoundEnabled);
        engine.enableSuperBass(isFullbassEnabled);
        engine.setIntensity(intensity);
        engine.setEqualizer(equalizerId, EqualizerGain.getEqGain(equalizerId));
        for ( int i = 0; i < 6; i++ ) {
            engine.setSpeakerState(i, speakerStates[i]);
        }
    }

}
