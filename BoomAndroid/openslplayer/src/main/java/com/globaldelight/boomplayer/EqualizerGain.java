package com.globaldelight.boomplayer;

import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Rahul Agarwal on 17-10-16.
 */

public class EqualizerGain {
    private static Map<Integer, double[]> EQ_GAIN = new Hashtable<>();

    public static float[] getEqGain(int equalizerId){
        double[] gains = EQ_GAIN.get(equalizerId);
        float[] floatGains = new float[gains.length];
        for ( int i = 0; i < gains.length; i++ ){
            floatGains[i] = (float) gains[i];
        }

        return floatGains;
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
    
    private static double EQ_BASSBOOST[] = {4.5, 5.5, 4.5, 3.0, 2.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    private static double EQ_ACOUSTIC[] = {4.5, 5.5, 4.5, 4.0, 3.0, 2.0, 1.0, 1.0, 1.0, 2.0, 2.5, 1.75, 1.5, 3.75, 3.75, 4.5};
    private static double EQ_SIXTIES[] = {3.0, 3.1, 3.0, 4.0, 4.5, 4.0, 1.5, -1.5, 2.0, 2.0, 0.0, -2.0, 0.0, 0.0, 1.0, 0.0};
    private static double EQ_CLASSICAL[] = {4.0, 4.1, 3.7, 3.7, 3.3, 2.6, 1.0, 0.0, -0.5, -1.0, -0.5, 0.0, 2.0, 3.0, 3.5, 4.0};
    private static double EQ_DUBSTEP[] = {5.0, 5.0, 4.5, 4.5, 3.0, 2.4, 2.0, 1.0, 0.0, -2.0, -0.5, 1.0, 2.5, 3.0, 4.5, 4.0};
    private static double EQ_ELECTRONIC[] = {3.6, 4.1, 3.2, 2.0, 1.0, 0.0, 0.0, -1.0, -1.0, 3.0, 1.0, 3.0, 3.5, 4.0, 4.5, 5.0};
    private static double EQ_FLAT[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    private static double EQ_HIPHOP[] = {4.0, 4.5, 4.1, 1.0, -1.0, 4.0, 3.0, -1.0, -1.5, -1.5, 0.0, 3.0, -1.0, -1.0, 3.0, 4.0};
    private static double EQ_HOUSE[] = {-1.0, 1.0, 2.0, 4.0, 4.0, 4.0, 3.0, -2.0, -2.0, 0.0, -1.0, -2.0, 1.0, 3.0, 1.0, 0.0};
    private static double EQ_JAZZ[] = {4.0, 4.0, 3.0, -1.0, 2.0, 3.0, 2.0, -1.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 4.0};
    private static double EQ_LOUD[] = {4.0, 4.5, 4.0, 2.0, 3.0, 4.0, 2.0, 0.0, -1.0, 1.0, 4.0, 1.0, -1.0, 4.0, 4.0, 3.0};
    private static double EQ_MUSIC[] = {4.0, 5.0, 4.5, 2.6, 2.0, 2.21, 2.0, 2.5, 1.75, 2.0, 2.0, 3.0, 3.0, 4.0, 4.75, 4.2};
    private static double EQ_PARTY[] = {4.5, 5.0, 4.0, 2.0, 2.0, 1.5, 1.0, -1.0, 2.5, 1.0, 1.0, 1.0, 1.5, 3.5, 5.0, 3.0};
    private static double EQ_REGGAE[] = {1.0, 3.5, 5.0, 4.5, 4.5, 2.5, 1.0, 0.0, -1.0, -1.0, 0.0, 2.0, 3.0, 4.0, 2.5, 1.0};
    private static double EQ_SOFT[] = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, -1.0, -1.0, 0.0, -1.0, 0.0, -1.0, -1.0, -1.0, -1.0};
    private static double EQ_TREBLE[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,1.0, 3.0, 5.0, 4.0, 2.0};
    private static double EQ_VOCALS[] = {-1.0, -1.5, -1.0, -0.5, 1.0, 1.0, 2.0, 3.0, 4.0, 4.0, 2.0, 1.0, 0.0, -1.0, -2.0, -3.0};
    private static double EQ_RNB[] = {3.0, 5.0, 4.0, 2.0, 2.0, 1.0, 0.0, 0.0, -1.0, 1.0, 2.0, 3.5, 4.0, 4.5, 4.0, 4.0};
    private static double EQ_METAL[] = {5.0, 5.0, 4.5, 2.0, 1.0, 3.0, 2.0, 0.0, 0.0, 0.0, 2.0, 3.0, 0.0, 3.0, 4.0, 5.0};
// need to update
    private static double EQ_POP[] = {4.0, 5.0, 4.5, 2.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 2.0, 3.0, 1.0, 4.0, 5.0, 4.0};
    private static double EQ_ROCK[] = {4.0, 5.0, 4.5, 2.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 2.0, 3.0, 1.0, 4.0, 5.0, 4.0};
    private static double EQ_AUTO[] = EQ_MUSIC;
}
