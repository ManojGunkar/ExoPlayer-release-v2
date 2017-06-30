package com.globaldelight.boom;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by adarsh on 29/06/17.
 */

public final class Constants {
    @IntDef({Quality.LOW, Quality.MID, Quality.HIGH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Quality {
        int LOW = 1, MID = 2, HIGH = 3;
    }

    @IntDef({Headphone.OVER_EAR, Headphone.ON_EAR, Headphone.IN_EAR, Headphone.IN_CANAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Headphone {
        int OVER_EAR = 0, ON_EAR = 1, IN_EAR = 2, IN_CANAL = 3;
    }

    @IntDef({Speaker.FRONT_LEFT, Speaker.FRONT_RIGHT, Speaker.SURROUND_LEFT, Speaker.SURROUND_RIGHT, Speaker.WOOFER, Speaker.TWEETER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Speaker {
        int FRONT_LEFT = 0, FRONT_RIGHT = 1, SURROUND_LEFT = 2, SURROUND_RIGHT = 3, WOOFER = 4, TWEETER = 5;
    }
}
