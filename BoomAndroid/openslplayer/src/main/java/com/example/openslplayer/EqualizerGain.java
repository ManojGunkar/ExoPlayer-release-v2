package com.example.openslplayer;

import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Rahul Agarwal on 17-10-16.
 */

public class EqualizerGain {
    private static Map<Integer, float[]> EQ_GAIN = new Hashtable<>();

    public static float[] getEqGain(int equalizerId){
        return EQ_GAIN.get(equalizerId);
    }

    public static int getEqualizerSize(){
        return EQ_GAIN.size();
    }

    public static void setEqGain(){

        EQ_GAIN.clear();
        EQ_GAIN.put(0, EQ_AUTO);
        EQ_GAIN.put(1, EQ_BASSBOOST);
        EQ_GAIN.put(2, EQ_ACOUSTIC);
        EQ_GAIN.put(3, EQ_SIXTIES);
        EQ_GAIN.put(4, EQ_CLASSICAL);
        EQ_GAIN.put(5, EQ_DUBSTEP);
        EQ_GAIN.put(6, EQ_ELECTRONIC);
        EQ_GAIN.put(7, EQ_FLAT);
        EQ_GAIN.put(8, EQ_HIPHOP);
        EQ_GAIN.put(9, EQ_HOUSE);
        EQ_GAIN.put(10, EQ_JAZZ);
        EQ_GAIN.put(11, EQ_LOUD);
        EQ_GAIN.put(12, EQ_MUSIC);
        EQ_GAIN.put(13, EQ_PARTY);
        EQ_GAIN.put(14, EQ_POP);
        EQ_GAIN.put(15, EQ_REGGAE);
        EQ_GAIN.put(16, EQ_ROCK);
        EQ_GAIN.put(17, EQ_SOFT);
        EQ_GAIN.put(18, EQ_TREBLE);
        EQ_GAIN.put(19, EQ_VOCALS);
        EQ_GAIN.put(20, EQ_RNB);
        EQ_GAIN.put(21, EQ_METAL);
    }

    private static float EQ_AUTO[] = {};
    private static float EQ_BASSBOOST[] = {};
    private static float EQ_ACOUSTIC[] = {};
    private static float EQ_SIXTIES[] = {};
    private static float EQ_CLASSICAL[] = {};
    private static float EQ_DUBSTEP[] = {};
    private static float EQ_ELECTRONIC[] = {};
    private static float EQ_FLAT[] = {};
    private static float EQ_HIPHOP[] = {};
    private static float EQ_HOUSE[] = {};
    private static float EQ_JAZZ[] = {};
    private static float EQ_LOUD[] = {};
    private static float EQ_MUSIC[] = {};
    private static float EQ_PARTY[] = {};
    private static float EQ_POP[] = {};
    private static float EQ_REGGAE[] = {};
    private static float EQ_ROCK[] = {};
    private static float EQ_SOFT[] = {};
    private static float EQ_TREBLE[] = {};
    private static float EQ_VOCALS[] = {};
    private static float EQ_RNB[] = {};
    private static float EQ_METAL[] = {};
}
