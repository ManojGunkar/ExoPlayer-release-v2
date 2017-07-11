package com.globaldelight.boom.player;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.IntDef;

import com.globaldelight.boom.Constants;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Observable;

/**
 * Created by Rahul Agarwal on 08-10-16.
 */

public class AudioEffect extends Observable {
    private static AudioEffect handler = null;
    private static final String AUDIO_EFFECT_SETTING = "audio_effect_settings";

    public static final String HEAD_PHONE_TYPE_PROPERTY = "audio_head_phone_type";
    public static final String AUDIO_EFFECT_PROPERTY = "audio_effect_power";
    public static final String SURROUND_SOUND_PROPERTY = "3d_surround_power";
    public static final String INTENSITY_STATE_PROPERTY = "intensity_power";
    public static final String EQUALIZER_STATE_PROPERTY = "equalizer_power";
    public static final String SPEAKER_LEFT_FRONT_PROPERTY = "speaker_left_front";
    public static final String SPEAKER_RIGHT_FRONT_PROPERTY = "speaker_right_front";
    public static final String SPEAKER_LEFT_SURROUND_PROPERTY = "speaker_left_surround";
    public static final String SPEAKER_RIGHT_SURROUND_PROPERTY = "speaker_right_surround";
    public static final String SPEAKER_WOOFER_PROPERTY = "speaker_sub_woofer";
    public static final String SPEAKER_TWEETER_PROPERTY = "speaker_tweeter";
    public static final String INTENSITY_PROPERTY = "intensity_position";
    public static final String EQUALIZER_PROPERTY = "selected_equalizer_position";
    public static final String FULL_BASS_PROPERTY = "full_bass";


    private static final boolean POWER_ON = true;
    private static final boolean POWER_OFF = false;
    private static final boolean DEFAULT_POWER = POWER_ON;

    private static final int EQUALIZER_POSITION = 0;
    private static final int AUTO_EQUALIZER_POSITION = 12;



    public static final String AUTO_EQUALIZER = "auto_equalizer";




    private static final String ALL_SPEAKER_POWER = "all_speaker_power";


    private final SharedPreferences shp;
    private final SharedPreferences.Editor editor;
    private Context context;

    private AudioEffect(Context context) {
        this.context = context.getApplicationContext();
        shp = context.getSharedPreferences(AUDIO_EFFECT_SETTING, Context.MODE_PRIVATE);
        editor = shp.edit();
    }

    public static AudioEffect getInstance(Context context) {
        if (handler == null)
            handler = new AudioEffect(context);
        return handler;
    }

    public int getHeadPhoneType() {
        return shp.getInt(HEAD_PHONE_TYPE_PROPERTY, 0);
    }

    public void setHeadPhoneType(int type){
        if ( getHeadPhoneType() != type ) {
            editor.putInt(HEAD_PHONE_TYPE_PROPERTY, type).apply();
            notify(HEAD_PHONE_TYPE_PROPERTY);
        }
    }

    public boolean isAudioEffectOn(){
        return shp.getBoolean(AUDIO_EFFECT_PROPERTY, POWER_OFF);
    }

    public void setEnableAudioEffect(boolean enableEffect) {
        if ( isAudioEffectOn() != enableEffect ) {
            editor.putBoolean(AUDIO_EFFECT_PROPERTY, enableEffect).apply();
            notify(AUDIO_EFFECT_PROPERTY);
        }
    }

    public boolean is3DSurroundOn(){
        return shp.getBoolean(SURROUND_SOUND_PROPERTY, DEFAULT_POWER);
    }

    public void setEnable3DSurround(boolean enable3DSurround){
        if ( is3DSurroundOn() != enable3DSurround ) {
            editor.putBoolean(SURROUND_SOUND_PROPERTY, enable3DSurround).apply();
            notify(SURROUND_SOUND_PROPERTY);
        }
    }

    public boolean isFullBassOn(){
        boolean isOn = AudioConfiguration.getInstance(context).getQuality() == Constants.Quality.HIGH;
        return shp.getBoolean(FULL_BASS_PROPERTY, isOn);
    }

    public void setEnableFullBass(boolean enableBass) {
        if ( isFullBassOn() != enableBass ) {
            editor.putBoolean(FULL_BASS_PROPERTY, enableBass).apply();
            notify(FULL_BASS_PROPERTY);
        }
    }

    public boolean isIntensityOn(){
        return shp.getBoolean(INTENSITY_STATE_PROPERTY, DEFAULT_POWER) || is3DSurroundOn();
    }

    public int getIntensity(){
        return shp.getInt(INTENSITY_PROPERTY, 50);
    }

    public void setIntensity(int intensity) {
        if ( getIntensity() != intensity ) {
            editor.putInt(INTENSITY_PROPERTY, intensity).apply();
            notify(INTENSITY_PROPERTY);
        }
    }

    public void setEnableIntensity(boolean enableIntensity) {
        if ( isIntensityOn() != enableIntensity ) {
            editor.putBoolean(INTENSITY_STATE_PROPERTY, enableIntensity).apply();
            notify(INTENSITY_STATE_PROPERTY);
        }
    }

    public boolean isEqualizerOn(){
        return shp.getBoolean(EQUALIZER_STATE_PROPERTY, DEFAULT_POWER) || is3DSurroundOn();
    }

    public void setEnableEqualizer(boolean enableEq) {
        if ( isEqualizerOn() != enableEq ) {
            editor.putBoolean(EQUALIZER_STATE_PROPERTY, enableEq).apply();
            notify(EQUALIZER_STATE_PROPERTY);
        }
    }

    public int getSelectedEqualizerPosition(){
        return shp.getInt(EQUALIZER_PROPERTY, EQUALIZER_POSITION);
    }

    public void setSelectedEqualizerPosition(int position){
        if ( getSelectedEqualizerPosition() != position ) {
            editor.putInt(EQUALIZER_PROPERTY, position).apply();
            notify(EQUALIZER_PROPERTY);
        }
    }

    public void setAutoEqualizerPosition(int position){
        editor.putInt(AUTO_EQUALIZER, position).apply();
    }

    public int getAutoEqualizerValue(){
        return shp.getInt(AUTO_EQUALIZER, AUTO_EQUALIZER_POSITION);
    }


    public boolean isLeftFrontSpeakerOn(){
        return shp.getBoolean(SPEAKER_LEFT_FRONT_PROPERTY, DEFAULT_POWER);
    }

    public void setEnableLeftFrontSpeaker(boolean enable) {
        if ( isLeftFrontSpeakerOn() != enable ) {
            editor.putBoolean(SPEAKER_LEFT_FRONT_PROPERTY, enable).apply();
            notify(SPEAKER_LEFT_FRONT_PROPERTY);
        }
    }

    public boolean isRightFrontSpeakerOn(){
        return shp.getBoolean(SPEAKER_RIGHT_FRONT_PROPERTY, DEFAULT_POWER);
    }

    public void setEnableRightFrontSpeaker(boolean enable) {
        if ( isRightFrontSpeakerOn() != enable ) {
            editor.putBoolean(SPEAKER_RIGHT_FRONT_PROPERTY, enable).apply();
            notify(SPEAKER_RIGHT_FRONT_PROPERTY);
        }
    }

    public boolean isLeftSurroundSpeakerOn(){
        return shp.getBoolean(SPEAKER_LEFT_SURROUND_PROPERTY, DEFAULT_POWER);
    }

    public void setEnableLeftSurroundSpeaker(boolean enable) {
        if ( isLeftSurroundSpeakerOn() != enable ) {
            editor.putBoolean(SPEAKER_LEFT_SURROUND_PROPERTY, enable).apply();
            notify(SPEAKER_LEFT_SURROUND_PROPERTY);
        }
    }

    public boolean isRightSurroundSpeakerOn(){
        return shp.getBoolean(SPEAKER_RIGHT_SURROUND_PROPERTY, DEFAULT_POWER);
    }

    public void setEnableRightSurroundSpeaker(boolean enable) {
        if ( isRightSurroundSpeakerOn() != enable ) {
            editor.putBoolean(SPEAKER_RIGHT_SURROUND_PROPERTY, enable).apply();
            notify(SPEAKER_RIGHT_SURROUND_PROPERTY);
        }
    }

    public boolean isWooferOn(){
        return shp.getBoolean(SPEAKER_WOOFER_PROPERTY, DEFAULT_POWER);
    }

    public void setEnableWoofer(boolean enable) {
        if ( isWooferOn() != enable ) {
            editor.putBoolean(SPEAKER_WOOFER_PROPERTY, enable).apply();
            notify(SPEAKER_WOOFER_PROPERTY);
        }
    }

    public boolean isTweeterOn(){
        return shp.getBoolean(SPEAKER_TWEETER_PROPERTY, DEFAULT_POWER);
    }

    public void setEnableTweeter(boolean enable) {
        if ( isTweeterOn() != enable ) {
            editor.putBoolean(SPEAKER_TWEETER_PROPERTY, enable).apply();
            notify(SPEAKER_TWEETER_PROPERTY);
        }
    }

    public void setOnAllSpeaker(boolean enable) {
        editor.putBoolean(ALL_SPEAKER_POWER, enable);
        editor.commit();
    }

    public boolean isAllSpeakerOn(){
        return shp.getBoolean(ALL_SPEAKER_POWER, DEFAULT_POWER);
    }


    public void notify(final String property) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                notifyChanges(property);
            }
        });
    }


    private void notifyChanges(String property) {
        setChanged();
        notifyObservers(property);
    }

    @IntDef ({SPEAKER_FRONT_LEFT, SPEAKER_FRONT_RIGHT, SPEAKER_SURROUND_LEFT, SPEAKER_SURROUND_RIGHT, SPEAKER_WOOFER, SPEAKER_TWEETER })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Speaker {}

    public static final int SPEAKER_FRONT_LEFT = 0;
    public static final int SPEAKER_FRONT_RIGHT = 1;
    public static final int SPEAKER_SURROUND_LEFT = 2;
    public static final int SPEAKER_SURROUND_RIGHT = 3;
    public static final int SPEAKER_WOOFER = 4;
    public static final int SPEAKER_TWEETER = 5;

}
