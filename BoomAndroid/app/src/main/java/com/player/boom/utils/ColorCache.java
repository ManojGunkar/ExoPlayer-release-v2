package com.player.boom.utils;

import android.support.v4.util.LruCache;


public class ColorCache {

    private static ColorCache instance;
    private LruCache<Long, int[]> lru;

    private ColorCache() {
        lru = new LruCache<>(1024);
    }

    public static ColorCache getInstance() {
        if (instance == null) {
            instance = new ColorCache();
        }
        return instance;
    }

    public LruCache<Long, int[]> getLru() {
        return lru;
    }

}