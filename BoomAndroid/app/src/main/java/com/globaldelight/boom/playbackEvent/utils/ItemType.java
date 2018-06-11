package com.globaldelight.boom.playbackEvent.utils;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Rahul Agarwal on 8/5/2016.
 */


@IntDef ({
        ItemType.SONGS,
        ItemType.ALBUM,
        ItemType.ARTIST,
        ItemType.PLAYLIST,
        ItemType.GENRE,
        ItemType.BOOM_PLAYLIST,
        ItemType.FAVOURITE,
        ItemType.RECENT_PLAYED,
        ItemType.LIVE_STREAM,
        ItemType.CHAPTER
})
@Retention(RetentionPolicy.SOURCE)

public @interface ItemType {
        int     SONGS = 0,
                ALBUM = 1,
                ARTIST = 2,
                PLAYLIST = 4,
                GENRE = 5,
                BOOM_PLAYLIST = 6,
                FAVOURITE = 7,
                RECENT_PLAYED = 8,
                LIVE_STREAM = 9,
                CHAPTER = 10;
}

