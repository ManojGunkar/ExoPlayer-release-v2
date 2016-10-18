package com.example.openslplayer;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rahul Agarwal on 08-10-16.
 */

public class AudioEffect {
    public static String AUDIO_EFFECT_SETTING = "audio_effect_settings";

    public static boolean POWER_ON = true;
    public static boolean POWER_OFF = false;
    public static boolean DEFAULT_POWER = true;
    public static boolean EFFECT_DEFAULT_POWER = false;

    public static int SPEAKER_COUNT = 6;

    public static int EQUALIZER_POSITION = 0;

    public static String AUDIO_EFFECT_POWER = "audio_effect_power";
    public static String THREE_D_SURROUND_POWER = "3d_surround_power";
    public static String INTENSITY_POWER = "intensity_power";
    public static String EQUALIZER_POWER = "equalizer_power";

    public static String SPEAKER_LEFT_FRONT = "speaker_left_front";
    public static String SPEAKER_RIGHT_FRONT = "speaker_right_front";
    public static String SPEAKER_LEFT_SURROUND = "speaker_left_surround";
    public static String SPEAKER_RIGHT_SURROUND = "speaker_right_surround";
    public static String SPEAKER_SUB_WOOFER = "speaker_sub_woofer";
    public static String SPEAKER_TWEETER = "speaker_tweeter";

    public static String HEADSET_PLUGGED_INFO = "headset_plugged";

    public static String INTENSITY_POSITION = "intensity_position";
    public static String SELECTED_EQUALIZER_POSITION = "selected_equalizer_position";

    public static String SPEAKER_POWER = "speaker_power";

    public static String FULL_BASS = "full_bass";

    public enum equalizer{
        on,
        off,
    }

    public enum Speaker{
        FrontLeft,
        FrontRight,
        RearLeft,
        RearRight,
        Woofer,
        Tweeter;
        private static final Map<Integer, Speaker> lookup = new HashMap<Integer, Speaker>();
        static{
            int ordinal = 0;
            for (Speaker suit : EnumSet.allOf(Speaker.class)) {
                lookup.put(ordinal, suit);
                ordinal+= 1;
            }
        }

        public static Speaker fromOrdinal(int ordinal) {
            return lookup.get(ordinal);
        }
    }
}
