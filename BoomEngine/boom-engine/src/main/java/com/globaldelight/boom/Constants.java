package com.globaldelight.boom;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines constants used in BoomEngine
 */
public final class Constants {
    /**
     * Audio Quality levels
     */
    @IntDef({Quality.LOW, Quality.MID, Quality.HIGH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Quality {
        int LOW = 1;
        int MID = 2;
        int HIGH = 3;
    }

    /**
     * Headphone types
     */
    @IntDef({
            Headphone.OVER_EAR,
            Headphone.ON_EAR,
            Headphone.IN_EAR,
            Headphone.IN_CANAL
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Headphone {
        int OVER_EAR = 0;
        int ON_EAR = 1;
        int IN_EAR = 2;
        int IN_CANAL = 3;
    }

    /**
     * Speaker ids
     */
    @IntDef({
            Speaker.FRONT_LEFT,
            Speaker.FRONT_RIGHT,
            Speaker.SURROUND_LEFT,
            Speaker.SURROUND_RIGHT,
            Speaker.WOOFER,
            Speaker.TWEETER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Speaker {
        int FRONT_LEFT = 0;
        int FRONT_RIGHT = 1;
        int SURROUND_LEFT = 2;
        int SURROUND_RIGHT = 3;
        int WOOFER = 4;
        int TWEETER = 5;
    }

    /**
     * Predefined equalizer ids
     */
    @IntDef({
            EQ.AUTO,
            EQ.BASSBOOST,
            EQ.ACOUSTIC,
            EQ.SIXTIES,
            EQ.CLASSICAL,
            EQ.DUBSTEP,
            EQ.ELECTRONIC,
            EQ.FLAT,
            EQ.HIPHOP,
            EQ.HOUSE,
            EQ.JAZZ,
            EQ.LOUD,
            EQ.MUSIC,
            EQ.PARTY,
            EQ.POP,
            EQ.REGGAE,
            EQ.ROCK,
            EQ.SOFT,
            EQ.TREBLE,
            EQ.VOCALS,
            EQ.RNB,
            EQ.METAL
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface EQ {
        int AUTO = 0;
        int BASSBOOST = 1;
        int ACOUSTIC = 2;
        int SIXTIES = 3;
        int CLASSICAL = 4;
        int DUBSTEP = 5;
        int ELECTRONIC = 6;
        int FLAT = 7;
        int HIPHOP = 8;
        int HOUSE = 9;
        int JAZZ = 10;
        int LOUD = 11;
        int MUSIC = 12;
        int PARTY = 13;
        int POP = 14;
        int REGGAE = 15;
        int ROCK = 16;
        int SOFT = 17;
        int TREBLE = 18;
        int VOCALS = 19;
        int RNB = 20;
        int METAL = 21;
    }

    private Constants() {
    }
}
