package com.globaldelight.boomplayer;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rahul Agarwal on 08-10-16.
 */

public class AudioEffect {
    private static AudioEffect handler = null;
    private static String AUDIO_EFFECT_SETTING = "audio_effect_settings";

    private static boolean POWER_ON = true;
    private static boolean POWER_OFF = false;
    private static boolean DEFAULT_POWER = true;
    private static boolean EFFECT_DEFAULT_POWER = false;
    private static int DEFAULT_USER_PURCHASE_TYPE = 0;
    private static int SPEAKER_COUNT = 6;

    private static int EQUALIZER_POSITION = 0;
    private static int AUTO_EQUALIZER_POSITION = 12;
    private static String USER_PURCHASE_TYPE = "user_purchase_type";
    private static String HEAD_PHONE_TYPE = "audio_head_phone_type";

    private static String AUDIO_EFFECT_POWER = "audio_effect_power";
    private static String THREE_D_SURROUND_POWER = "3d_surround_power";
    private static String INTENSITY_POWER = "intensity_power";
    private static String EQUALIZER_POWER = "equalizer_power";
    private static String AUTO_EQUALIZER = "auto_equalizer";

    private static String SPEAKER_LEFT_FRONT = "speaker_left_front";
    private static String SPEAKER_RIGHT_FRONT = "speaker_right_front";
    private static String SPEAKER_LEFT_SURROUND = "speaker_left_surround";
    private static String SPEAKER_RIGHT_SURROUND = "speaker_right_surround";
    private static String SPEAKER_SUB_WOOFER = "speaker_sub_woofer";
    private static String SPEAKER_TWEETER = "speaker_tweeter";

    private static String HEADSET_PLUGGED_INFO = "headset_plugged";

    private static String INTENSITY_POSITION = "intensity_position";
    private static String SELECTED_EQUALIZER_POSITION = "selected_equalizer_position";

    private static String ALL_SPEAKER_POWER = "all_speaker_power";

    private static String FULL_BASS = "full_bass";
    private static String EFFECT_MASTER_PURCHASE_CONTROL = "effect_paid_master_control";

    private final SharedPreferences shp;
    private final SharedPreferences.Editor editor;

    private AudioEffect(Context context) {
        shp = context.getSharedPreferences(AUDIO_EFFECT_SETTING, Context.MODE_PRIVATE);
        editor = shp.edit();
    }

    public static AudioEffect getAudioEffectInstance(Context context) {
        if (handler == null)
            handler = new AudioEffect(context);
        return handler;
    }

    public int getHeadPhoneType() {
        return shp.getInt(HEAD_PHONE_TYPE, headphone.OVER_EAR.ordinal());
    }

    public void setHeadPhoneType(headphone type){
        editor.putInt(HEAD_PHONE_TYPE, type.ordinal());
        editor.commit();
    }

    public int getUserPurchaseType() {
        return shp.getInt(USER_PURCHASE_TYPE, purchase.FIVE_DAY_OFFER.ordinal());
    }

    public void setUserPurchaseType(purchase purchaseType) {
        editor.putInt(USER_PURCHASE_TYPE, purchaseType.ordinal());
        editor.commit();
    }

    public boolean isAudioEffectOn(){
        return shp.getBoolean(AUDIO_EFFECT_POWER, EFFECT_DEFAULT_POWER);
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

    public boolean isMasterEffectControlEnabled() {
        return shp.getBoolean(EFFECT_MASTER_PURCHASE_CONTROL, false);
    }

    public void setMasterEffectControl(boolean enableMasterControl) {
        editor.putBoolean(EFFECT_MASTER_PURCHASE_CONTROL, enableMasterControl);
        editor.commit();
    }
    public boolean isFullBassOn(){
        return shp.getBoolean(FULL_BASS, DEFAULT_POWER);
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
        return shp.getBoolean(SPEAKER_LEFT_FRONT, DEFAULT_POWER);
    }

    public void setEnableLeftFrontSpeaker(boolean enable) {
        editor.putBoolean(SPEAKER_LEFT_FRONT, enable);
        editor.commit();
    }

    public boolean isRightFrontSpeakerOn(){
        return shp.getBoolean(SPEAKER_RIGHT_FRONT, DEFAULT_POWER);
    }

    public void setEnableRightFrontSpeaker(boolean enable) {
        editor.putBoolean(SPEAKER_RIGHT_FRONT, enable);
        editor.commit();
    }

    public boolean isLeftSurroundSpeakerOn(){
        return shp.getBoolean(SPEAKER_LEFT_SURROUND, DEFAULT_POWER);
    }

    public void setEnableLeftSurroundSpeaker(boolean enable) {
        editor.putBoolean(SPEAKER_LEFT_SURROUND, enable);
        editor.commit();
    }

    public boolean isRightSurroundSpeakerOn(){
        return shp.getBoolean(SPEAKER_RIGHT_SURROUND, DEFAULT_POWER);
    }

    public void setEnableRightSurroundSpeaker(boolean enable) {
        editor.putBoolean(SPEAKER_RIGHT_SURROUND, enable);
        editor.commit();
    }

    public boolean isWooferOn(){
        return shp.getBoolean(SPEAKER_SUB_WOOFER, DEFAULT_POWER);
    }

    public void setEnableWoofer(boolean enable) {
        editor.putBoolean(SPEAKER_SUB_WOOFER, enable);
        editor.commit();
    }

    public boolean isTweeterOn(){
        return shp.getBoolean(SPEAKER_TWEETER, DEFAULT_POWER);
    }

    public void setEnableTweeter(boolean enable) {
        editor.putBoolean(SPEAKER_TWEETER, enable);
        editor.commit();
    }

    public void setOnAllSpeaker(boolean enable) {
        editor.putBoolean(ALL_SPEAKER_POWER, enable);
        editor.commit();
    }

    public boolean isAllSpeakerOn(){
        return shp.getBoolean(ALL_SPEAKER_POWER, DEFAULT_POWER);
    }

    public void setEnableHeadsetPlugged(boolean enable){
        editor.putBoolean(HEADSET_PLUGGED_INFO, enable);
        editor.commit();
    }

    public enum headphone {
        OVER_EAR,
        ON_EAR,
        IN_EAR,
        IN_CANAL;

        private static final Map<Integer, headphone> lookup = new HashMap<Integer, headphone>();

        static {
            int ordinal = 0;
            for (headphone suit : EnumSet.allOf(headphone.class)) {
                lookup.put(ordinal, suit);
                ordinal += 1;
            }
        }

        public static headphone fromOrdinal(int ordinal) {
            return lookup.get(ordinal);
        }
    }

    public enum purchase {
        NORMAL_USER,
        FIVE_DAY_OFFER,
        EXTENDED_FIVE_DAY_OFFER,
        PAID_USER;


        private static final Map<Integer, purchase> lookup = new HashMap<Integer, purchase>();

        static {
            int ordinal = 0;
            for (purchase suit : EnumSet.allOf(purchase.class)) {
                lookup.put(ordinal, suit);
                ordinal += 1;
            }
        }

        public static purchase fromOrdinal(int ordinal) {
            return lookup.get(ordinal);
        }
    }

    public enum equalizer {
        on,
        off,
    }

    public enum Speaker {
        FrontLeft,
        FrontRight,
        RearLeft,
        RearRight,
        Woofer,
        Tweeter;
        private static final Map<Integer, Speaker> lookup = new HashMap<Integer, Speaker>();

        static {
            int ordinal = 0;
            for (Speaker suit : EnumSet.allOf(Speaker.class)) {
                lookup.put(ordinal, suit);
                ordinal += 1;
            }
        }

        public static Speaker fromOrdinal(int ordinal) {
            return lookup.get(ordinal);
        }
    }


}
