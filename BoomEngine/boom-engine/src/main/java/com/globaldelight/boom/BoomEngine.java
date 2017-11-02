package com.globaldelight.boom;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * BoomEngine is the primary class that implements the audio processing interface. Boom engine processes the input stereo audio and returns virtual surround sound audio that can be rendered to headphones connected to the Android device.
 *
 * <p>
 * Note that the engine expects audio data in ENCODING_PCM_16BIT format and returns the processed data in either ENCODING_PCM_16BIT or ENCODING_PCM_FLOAT format. The input data can contain 1, 2 or 6 channels, the processed data will always contain 2 channels.
 *
 * <p>
 * Following code snippet shows a sample usage scenario of Boom audio engine.
 * <p>
 * <pre>
 * {@code
 *
 *     ...
 *
 *     // Initialize the engine. Call this when app launches
 *     BoomEngine.init(context)
 *
 *     ...
 *
 *     // Allocate output buffer.
 *     ByteBuffer output = ByteBuffer.allocateDirect(4096*4*2); // 4096 frames of 2 channel audio in ENCODING_PCM_FLOAT format
 *
 *     // Create the engine.
 *     BoomEngine engine = new BoomEngine(44100, 2, true)
 *
 *     // Prepare the player. Note the data encoding and channel mask;
 *     // Channel mask should be CHANNEL_OUT_STEREO and
 *     // encoding should be ENCODING_PCM_FLOAT (since we set the floatAudio parameter to true while creating the engine)
 *     AudioTrack player = new AudioTrack.Builder()
 *                  .setAudioAttributes(new AudioAttributes.Builder()
 *                          .setUsage(AudioAttributes.USAGE_MEDIA)
 *                          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
 *                          .build())
 *                  .setAudioFormat(new AudioFormat.Builder()
 *                          .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
 *                          .setSampleRate(44100)
 *                          .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
 *                          .build())
 *                  .setBufferSizeInBytes(4096*4*2)
 *                  .build();
 *     ...
 *     // Set Effects
 *     // This can be done at any time after creating the engine and before releasing it.
 *     // Can be done from a different thread also.
 *     engine.enableAudioEffect(true);
 *     engine.enable3DAudio(true);
 *     engine.setIntensity(1.0f);
 *     engine.setEqualizer(Constants.EQ.AUTO, EqualizerGain.getEqGain(Constants.EQ.AUTO));
 *     ...
 *
 *     // Allocate input buffer. Input should contain data in ENCODING_PCM_16BIT format.
 *     ByteBuffer input = ByteBuffer.allocateDirect(...);
 *
 *     ...
 *
 *     player.play();
 *
 *
 *     while ( !hasMoreData ) { // While there is data in the input stream
 *         readData(input); // Read the buffer from input stream into -> input
 *
 *         while ( input.remaining() > 0 ) {
 *             output.clear();
 *             engine.process(input, output, input.remaining());
 *             if ( output.remaining() > 0 ) {
 *                  player.write(output, output.remaining(), WRITE_BLOCKING);
 *             }
 *         }
 *     }
 *
 *     ...
 *
 *     player.stop();
 *     engine.release(); // Release the engine
 *     engine = null;
 *
 *     ...
 *
 *     // At the time of closing the application.
 *     BoomEngine.finish()
 * }
 * </pre>
 *
 * The BoomEngine is thread safe; The effects can be enabled or disabled from the main thread
 * while the data processing is going on in a background thread.
 *
 *
 *
 * @see <a target="_blank" href="https://developer.android.com/reference/android/media/AudioFormat.html">Android AudioFormat documentation</a>
 *
 */
public class BoomEngine {

    static {
        System.loadLibrary("BoomEngine");
    }

    private static final String LOG_TAG = "BoomEngine";
    private static int sampleRate;
    private static int frameCount;

    /**
     * Initializes the engine. This must be called once before creating an instance of BoomEngine.
     * @param context Android context.
     */
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

        init(context.getAssets(), sampleRate, frameCount);
    }

    /**
     * Releases resources.
     */
    public native static void finish();

    /**
     * Constructor.
     *
     * @param sampleRate Sample rate of the input audio stream.
     * @param channelCount Number of channels in input audio stream. This can be 1,2 or 6 (5.1 channel).
     * @param floatAudio If the output should be in ENCODING_PCM_FLOAT format.
     *                   If true the output will be in ENCODING_PCM_FLOAT. This is preferred for high fidelity effects.
     *                   If false the output will be in ENCODING_PCM_16BIT format.
     * @see #process(ByteBuffer, ByteBuffer, int)
     *
     */
    public BoomEngine(int sampleRate, int channelCount, boolean floatAudio) {
        start(sampleRate, channelCount, floatAudio);
    }

    /**
     * Releases the engine resources. Call this when the audio stream finishes.
     */
    public void release() {
        stop();
    }

    /**
     * Apply boom audio effects to the audio buffer.
     *
     * @param inBuffer Contains input audio buffer. This buffer should be allocated by calling ByteBuffer.allocateDirect().
     *                 The caller should set the position and limit of the buffer. When the method returns the position gets updated according to the number of bytes consumed.
     *                 Input should always be in ENCODING_PCM_16BIT format.
     * @param outBuffer Output audio buffer contains processed audio date. This buffer should be allocated by calling ByteBuffer.allocateDirect().
     *                  The caller should set the position. The method will fill the buffer and sets the limit of the buffer.
     *                  Output will contain 2 channels in ENCODING_PCM_16BIT or ENCODING_PCM_FLOAT format.
     * @param size The number of bytes to be processed. This is often set to inBuffer.remaining()
     * @returns The number of bytes processed. Note that 0 is a valid return value and it is not an error.
     */
    public native int process(ByteBuffer inBuffer, ByteBuffer outBuffer, int size);

    /**
     * Clears all internal buffers.
     */
    public native void flush();

    /**
     * Enable/disable audio effects. Disabling the effects also disables all other effects including
     * 3D surround sound, equalizer and intensity.
     * @param enable If true enables audio effects.
     */
    public native void enableAudioEffect(boolean enable);

    /**
     * Enable or disable 3D surround sound.
     * @param enable If true enables 3D surround sound.
     */
    public native void enable3DAudio(boolean enable);

    /**
     * Enable/disable full bass.
     * @param enable If true enables full bass.
     */
    public native void enableSuperBass(boolean enable);

    /**
     * Sets the audio quality
     * @param quality Audio quality level.
     *                @see com.globaldelight.boom.Constants.Quality Supported Quality levels
     */
    public native void setQuality(int quality);

    /**
     * Enable/disable equalizer.
     * @param enable If true enables equalizer.
     */
    public native void enableEqualizer(boolean enable);

    /**
     * Sets the audio effect intensity.
     * @param value Level of intensity. The values should be within -1 to +1 range. Default value is 0.
     */
    public native void setIntensity(float value);

    /**
     * Sets the equalizer values and an associated integer identifier.
     * @param id Identifier for the equalizer set.
     * @param bandGains Gains in decibels of the 16-band equalizer, the range of the gain is from -10 to +10 db. 16 band frequencies are 36Hz, 50Hz, 75Hz, 100Hz, 150Hz, 250Hz, 350Hz, 500Hz, 750Hz, 1.1kHz, 1.6kHz, 2.5kHz,  3.6kHz, 5kHz, 8kHz, 12kHz.
     *                  @see EqualizerGain Predefined equalizer values.
     */
    public native void setEqualizer(int id, float []bandGains);

    /**
     * Sets the volume of each speaker.
     * @param speakerId Identifies a speaker.
     * @param value Volume of the speaker. Volume should be within 0 to 1. At present only 0 and 1
     *              are supported. 1 - To turn on the speaker and 0 to turn off.
     *              @see com.globaldelight.boom.Constants.Speaker Supported Speaker Ids
     */
    public native void setSpeakerState(int speakerId, float value);

    /**
     * Sets the type of headphone  used for listening the audio.
     * @param headPhoneType Headphone type.
     *                      @see com.globaldelight.boom.Constants.Headphone Supported headphone types
     */
    public native void setHeadPhoneType(int headPhoneType);

    /**
     * Returns the current headphone type used.
     * @see com.globaldelight.boom.Constants.Headphone Supported headphone types
     */
    public native int getHeadPhoneType();

    /**
     * Returns if the 3D surround sound is enabled.
     */
    public native boolean get3DAudioState();

    /**
     * Returns if the effects are enabled.
     */
    public native boolean getEffectsState();

    /**
     * Returns the current intensity level
     */
    public native float getIntensity();

    /**
     * Returns the current equalizer id.
     */
    public native int getEqualizerId();

    /**
     * Returns the volume of a speaker.
     * @param speakerId Speaker identifiers.
     * @see com.globaldelight.boom.Constants.Speaker Supported Speaker Ids
     */
    public native float getSpeakerState(int speakerId);


    private native void start(int sampleRate, int channelCount, boolean floatAudio);

    private native void stop();

    private native static void init(AssetManager assetManager, int sampleRate, int frameCount);
}
