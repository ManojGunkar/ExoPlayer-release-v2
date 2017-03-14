package com.globaldelight.boom.Media;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rahul Agarwal on 8/5/2016.
 */
public enum ItemType {
    SONGS,
    ALBUM,
    ARTIST,
    PLAYLIST,
    GENRE,
    BOOM_PLAYLIST,
    FAVOURITE,
    RECENT_PLAYED;
    private static final Map<Integer, ItemType> lookup = new HashMap<Integer, ItemType>();

    static{
        int ordinal = 0;
        for (ItemType suit : EnumSet.allOf(ItemType.class)) {
            lookup.put(ordinal, suit);
            ordinal+= 1;
        }
    }

    public static ItemType fromOrdinal(int ordinal) {
        return lookup.get(ordinal);
    }
}
