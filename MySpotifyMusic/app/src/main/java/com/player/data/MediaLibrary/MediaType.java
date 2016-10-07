package com.player.data.MediaLibrary;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rahul Agarwal on 8/5/2016.
 */

public enum MediaType {
    DEVICE_MEDIA_LIB, GMUSIC_MEDIA_LIB, SPOTIFY;

    private static final Map<Integer, MediaType> lookup = new HashMap<Integer, MediaType>();

    static{
        int ordinal = 0;
        for (MediaType suit : EnumSet.allOf(MediaType.class)) {
            lookup.put(ordinal, suit);
            ordinal+= 1;
        }
    }

    public static MediaType fromOrdinal(int ordinal) {
        return lookup.get(ordinal);
    }
}