package com.globaldelight.boomplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Rahul Agarwal on 08-10-16.
 */

public class AudioEffect {
    private static AudioEffect handler = null;
    private static final String AUDIO_EFFECT_SETTING = "audio_effect_settings";

    private static final boolean POWER_ON = true;
    private static final boolean POWER_OFF = false;
    private static final boolean DEFAULT_POWER = POWER_ON;

    private static final int EQUALIZER_POSITION = 0;
    private static final int AUTO_EQUALIZER_POSITION = 12;

    private static final String HEAD_PHONE_TYPE = "audio_head_phone_type";

    private static final String AUDIO_EFFECT_POWER = "audio_effect_power";
    private static final String THREE_D_SURROUND_POWER = "3d_surround_power";
    private static final String INTENSITY_POWER = "intensity_power";
    private static final String EQUALIZER_POWER = "equalizer_power";
    private static final String AUTO_EQUALIZER = "auto_equalizer";

    private static final String SPEAKER_LEFT_FRONT_KEY = "speaker_left_front";
    private static final String SPEAKER_RIGHT_FRONT_KEY = "speaker_right_front";
    private static final String SPEAKER_LEFT_SURROUND_KEY = "speaker_left_surround";
    private static final String SPEAKER_RIGHT_SURROUND_KEY = "speaker_right_surround";
    private static final String SPEAKER_SUB_WOOFER_KEY = "speaker_sub_woofer";
    private static final String SPEAKER_TWEETER_KEY = "speaker_tweeter";

    private static final String INTENSITY_POSITION = "intensity_position";
    private static final String SELECTED_EQUALIZER_POSITION = "selected_equalizer_position";


    private static final String ALL_SPEAKER_POWER = "all_speaker_power";

    private static final String FULL_BASS = "full_bass";

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
        return shp.getInt(HEAD_PHONE_TYPE, 0);
    }

    public void setHeadPhoneType(int type){
        editor.putInt(HEAD_PHONE_TYPE, type).apply();
    }

    public boolean isAudioEffectOn(){
        return shp.getBoolean(AUDIO_EFFECT_POWER, POWER_OFF);
    }

    public void setEnableAudioEffect(boolean enableEffect) {
        editor.putBoolean(AUDIO_EFFECT_POWER, enableEffect);
        editor.commit();
    }

    public boolean is3DSurroundOn(){
        return shp.getBoolean(THREE_D_SURROUND_POWER, DEFAULT_POWER);
    }

    public void setEnable3DSurround(boolean enable3DSurround){
        editor.putBoolean(THREE_D_SURROUND_POWER, enable3DSurround);
        editor.commit();
    }

    public boolean isFullBassOn(){
        boolean isOn = AudioConfiguration.getInstance(context).getQuality() == AudioConfiguration.QUALITY_HIGH;
        return shp.getBoolean(FULL_BASS, isOn);
    }

    public void setEnableFullBass(boolean enableBass) {
        editor.putBoolean(FULL_BASS, enableBass);
        editor.commit();
    }

    public boolean isIntensityOn(){
        return shp.getBoolean(INTENSITY_POWER, DEFAULT_POWER);
    }

    public int getIntensity(){
        return shp.getInt(INTENSITY_POSITION, 50);
    }

    public void setIntensity(int intensity) {
        editor.putInt(INTENSITY_POSITION, intensity);
        editor.commit();
    }

    public void setEnableIntensity(boolean enableIntensity) {
        editor.putBoolean(INTENSITY_POWER, enableIntensity);
        editor.commit();
    }

    public boolean isEqualizerOn(){
        return shp.getBoolean(EQUALIZER_POWER, DEFAULT_POWER);
    }

    public void setEnableEqualizer(boolean enableEq) {
        editor.putBoolean(EQUALIZER_POWER, enableEq);
        editor.commit();
    }

    public int getSelectedEqualizerPosition(){
        return shp.getInt(SELECTED_EQUALIZER_POSITION, EQUALIZER_POSITION);
    }

    public void setSelectedEqualizerPosition(int position){
        editor.putInt(SELECTED_EQUALIZER_POSITION, position);
        editor.commit();
    }

    public void setAutoEqualizerPosition(int position){
        editor.putInt(AUTO_EQUALIZER, position);
        editor.commit();
    }

    public int getAutoEqualizerValue(){
        return shp.getInt(AUTO_EQUALIZER, AUTO_EQUALIZER_POSITION);
    }


    public boolean isLeftFrontSpeakerOn(){
        return shp.getBoolean(SPEAKER_LEFT_FRONT_KEY, DEFAULT_POWER);
    }

    public void setEnableLeftFrontSpeaker(boolean enable) {
        editor.putBoolean(SPEAKER_LEFT_FRONT_KEY, enable);
        editor.commit();
    }

    public boolean isRightFrontSpeakerOn(){
        return shp.getBoolean(SPEAKER_RIGHT_FRONT_KEY, DEFAULT_POWER);
    }

    public void setEnableRightFrontSpeaker(boolean enable) {
        editor.putBoolean(SPEAKER_RIGHT_FRONT_KEY, enable);
        editor.commit();
    }

    public boolean isLeftSurroundSpeakerOn(){
        return shp.getBoolean(SPEAKER_LEFT_SURROUND_KEY, DEFAULT_POWER);
    }

    public void setEnableLeftSurroundSpeaker(boolean enable) {
        editor.putBoolean(SPEAKER_LEFT_SURROUND_KEY, enable);
        editor.commit();
    }

    public boolean isRightSurroundSpeakerOn(){
        return shp.getBoolean(SPEAKER_RIGHT_SURROUND_KEY, DEFAULT_POWER);
    }

    public void setEnableRightSurroundSpeaker(boolean enable) {
        editor.putBoolean(SPEAKER_RIGHT_SURROUND_KEY, enable);
        editor.commit();
    }

    public boolean isWooferOn(){
        return shp.getBoolean(SPEAKER_SUB_WOOFER_KEY, DEFAULT_POWER);
    }

    public void setEnableWoofer(boolean enable) {
        editor.putBoolean(SPEAKER_SUB_WOOFER_KEY, enable);
        editor.commit();
    }

    public boolean isTweeterOn(){
        return shp.getBoolean(SPEAKER_TWEETER_KEY, DEFAULT_POWER);
    }

    public void setEnableTweeter(boolean enable) {
        editor.putBoolean(SPEAKER_TWEETER_KEY, enable);
        editor.commit();
    }

    public void setOnAllSpeaker(boolean enable) {
        editor.putBoolean(ALL_SPEAKER_POWER, enable);
        editor.commit();
    }

    public boolean isAllSpeakerOn(){
        return shp.getBoolean(ALL_SPEAKER_POWER, DEFAULT_POWER);
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
