package com.globaldelight.boom.handler.PlayingQueue;

import com.globaldelight.boom.data.MediaLibrary.MediaType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rahul Agarwal on 16-09-16.
 */
public enum QueueType {
    History,
    Playing,
    Manual_UpNext,
    Auto_UpNext,
    Previous;
    private static final Map<Integer, QueueType> lookup = new HashMap<Integer, QueueType>();

    static{
        int ordinal = 0;
        for (QueueType suit : EnumSet.allOf(QueueType.class)) {
            lookup.put(ordinal, suit);
            ordinal+= 1;
        }
    }

    public static QueueType fromOrdinal(int ordinal) {
        return lookup.get(ordinal);
    }
}
