package com.globaldelight.boom.playbackEvent.utils;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rahul Agarwal on 8/5/2016.
 */
@IntDef({
        MediaType.DEVICE_MEDIA_LIB,
        MediaType.DROP_BOX,
        MediaType.GOOGLE_DRIVE,
        MediaType.SEARCH})
@Retention(RetentionPolicy.SOURCE)
public @interface MediaType {
    int     DEVICE_MEDIA_LIB = 0,
            DROP_BOX = 1,
            GOOGLE_DRIVE = 2,
            SEARCH = 4;
}
