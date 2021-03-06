package com.globaldelight.boom;

import java.util.Hashtable;
import java.util.Map;

/**
 * Defines some commonly used Equalizer values
 */
public class EqualizerGain {

    /**
     * Get the equalizer values
     *
     * @param equalizerId Id of the Equalizer
     * @return Returns an array of size 16 containing equalizer values
     * @see com.globaldelight.boom.Constants.EQ Predefined equalizer ids
     */
    public static float[] getEqGain(@Constants.EQ int equalizerId){
        double[] gains = EQ_GAIN.get(equalizerId);
        float[] floatGains = new float[gains.length];
        for ( int i = 0; i < gains.length; i++ ){
            floatGains[i] = (float) gains[i];
        }

        return floatGains;
    }

//  1 -  36.0
//  2 -  50.0
//  3 -  75.0
//  4 -  100.0
//  5 -  150.0
//  6 -  250.0
//  7 -  350.0
//  8 -  500.0
//  9 -  750.0
//  10 - 1.1k
//  11 - 1.6k
//  12 - 2.5k
//  13 - 3.6k
//  14 - 5k
//  15 - 8k
//  16 - 12k

    private static double EQ_AUTO[] =      {4.1, 4.5, 4.1, 2.84+0.33, 2.66+0.33, 3.2, 3.2+0.5, 1.4+0.6+0.2, 1.94+0.5, 3.2+0.25, 2.94, 2.4, 2.422, 2.021, 2.21, 2.21};
    //    private static double EQ_AUTO[] =      {4.1, 4.5, 4.1, 2.84, 2.66, 3.2, 3.2, 1.4, 1.94, 4.2, 3.74, 2.4, 2.422, 2.021, 4.2, 3.2};
    //    private static double EQ_AUTO[] =      {4.2, 4.6, 4.5, 2.6, 2.0, 2.31, 2.0, 1.9, 2.21, 2.0, 2.0, 2.4, 2.022, 2.21, 3.93, 4.6};
    //                                                                                                                  10
    // private static double EQ_VIDEO[] =      {4.1, 4.5, 4.1, 2.84+0.2, 2.66+0.2, 3.2, 3.2+0.25, 1.4+0.6+0.1, 1.94+0.5, 3.2+0.25, 2.94, 2.4, 2.12, 2.021, 2.21, 2.4};
    private static double EQ_BASSBOOST[] = {4.5, 5.5, 4.5, 3.0, 2.2, 1.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    private static double EQ_ACOUSTIC[] = {4.5, 4.5, 4.5, 3.7, 3.0, 2.0, 1.0, 1.0, 1.25, 2.0, 2.5, 1.75, 1.75, 2.8, 3.02, 3.2};
    private static double EQ_SIXTIES[] = {2.94, 3.1, 3.0, 4.0, 4.5, 4.0, 1.5, -1.5, 2.0, 2.0, 0.0, -2.2, 0.0, 0.5, 0.25, 0.0};
    private static double EQ_CLASSICAL[] = {4.0, 4.1, 3.7, 3.7, 3.3, 2.6, 1.0, 0.0, -0.75, -1.25, -0.75, 0.0, 2.0, 2.4, 2.4, 2.0};
    private static double EQ_DUBSTEP[] = {5.0, 5.0, 4.5, 4.5, 3.0, 2.4, 2.0, 1.25, 0.0, -2.0, -0.5, 1.0, 2.5, 2.8, 2.8, 2.93};
    private static double EQ_ELECTRONIC[] = {3.7, 4.2, 3.2, 2.0, 1.5, 0.5, 0.0, -1.25, -1.0, 3.0, 1.0, 2.7, 3.11, 3.01, 2.84, 3.1};
    private static double EQ_FLAT[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    private static double EQ_HIPHOP[] = {4.2, 4.3, 4.1, 1.0, -0.5, 4.0, 3.0, -1.2, -1.5, -1.5, 0.0, 3.0, -1.75, -0.5, 2.84, 3.1};
    private static double EQ_HOUSE[] = {-1.0, 1.0, 2.0, 4.0, 4.5, 4.75, 3.0, -2.0, -1.75, 0.0, -1.0, -2.0, 2.21, 3.3, 0.5, 0.0};
    private static double EQ_JAZZ[] = {4.2, 4.0, 3.3, -0.5, 2.8, 3.7, 2.0, -1.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 3.11, 3.2};
    private static double EQ_LOUD[] = {4.0, 4.5, 4.0, 2.5, 3.3, 4.0, 2.0, -0.5, -1.0, 1.0, 4.0, 1.0, -1.0, 3.2, 3.7, 1.5};
    //    private static double EQ_MUSIC[] = {4.0, 5.0, 4.5, 2.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 2.0, 3.0, 1.0, 4.0, 5.0, 4.0};
    //    private static double EQ_MUSIC[] = {4.0, 5.0, 4.5, 3.0, 4.0, 4.0, 3.0, 2.0, 2.0, 3.0, 2.0, 3.0+1.0, 1.0+2.0, 3.0, 4.0, 3.0};

    private static double EQ_PARTY[] = {4.2, 4.6, 4.0, 3.3, 2.2, 1.75, 1.0, -1.25, 0.0, 1.25, 1.0, 1.5, 2.5, 3.2, 3.11, 3.2};
    private static double EQ_REGGAE[] = {1.0, 3.5, 4.6, 4.2, 4.2, 2.75, 1.0, -0.75, -1.2, -1.0, 0.0, 2.0, 3.0, 4.0, 2.5, 1.0};
    private static double EQ_SOFT[] = {0.25, 0.25, 0.5, 0.5, 1.22, 1.22, 1.0, -1.0, -1.0, 0.0, -1.0, -1.2, -1.5, -1.5, -1.9, -2.21};
    private static double EQ_TREBLE[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.2, 1.5, 2.4, 3.2, 3.2, 3.1};
    private static double EQ_VOCALS[] = {-1.5, -1.5, -1.22, -0.5, 0.5, 1.0, 2.0, 3.0, 3.7, 4.0, 2.21, 1.0, 0.0, -1.0, -2.0, -3.0};
    private static double EQ_RNB[] = {3.0, 4.0, 4.2, 2.9, 2.71, 1.5, 0.0, -0.5, -1.0, 0.5, 1.0, 2.8, 3.2, 2.82, 3.11, 3.2};
    private static double EQ_METAL[] = {4.2, 4.65, 4.51, 3.3, 0.5, 3.3, 2.0, 0.0, 0.5, 0.5, 2.5, 3.2, 0.0, 2.8, 3.1, 3.3};
    // need to update
    //private static double EQ_POP[] = {4.0, 5.0, 4.5, 2.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 2.0, 3.0, 1.0, 4.0, 5.0, 4.0};
    private static double EQ_POP[] = {0.0, 0.5, 0.5, 1.0, 1.72, 2.8, 3.2, 1.2, 3.2, 3.7, 2.2, 1.0, 0.0, 0.0, -1.0, -1.5};
    private static double EQ_ROCK[] = {4.0, 4.5, 4.2, 3.7, 3.2, 2.3, 1.0, 0.0, -1.0, -1.0, -1.0, 1.0, 2.4, 3.2, 3.1, 3.2};
    private static double EQ_MUSIC[] = EQ_AUTO;

    private static Map<Integer, double[]> EQ_GAIN = new Hashtable<>();
    static {
        EQ_GAIN.clear();
        EQ_GAIN.put(Constants.EQ.AUTO, EQ_AUTO);
        EQ_GAIN.put(Constants.EQ.BASSBOOST, EQ_BASSBOOST);
        EQ_GAIN.put(Constants.EQ.ACOUSTIC, EQ_ACOUSTIC);
        EQ_GAIN.put(Constants.EQ.SIXTIES, EQ_SIXTIES);
        EQ_GAIN.put(Constants.EQ.CLASSICAL, EQ_CLASSICAL);
        EQ_GAIN.put(Constants.EQ.DUBSTEP, EQ_DUBSTEP);
        EQ_GAIN.put(Constants.EQ.ELECTRONIC, EQ_ELECTRONIC);
        EQ_GAIN.put(Constants.EQ.FLAT, EQ_FLAT);
        EQ_GAIN.put(Constants.EQ.HIPHOP, EQ_HIPHOP);
        EQ_GAIN.put(Constants.EQ.HOUSE, EQ_HOUSE);
        EQ_GAIN.put(Constants.EQ.JAZZ, EQ_JAZZ);
        EQ_GAIN.put(Constants.EQ.LOUD, EQ_LOUD);
        EQ_GAIN.put(Constants.EQ.MUSIC, EQ_MUSIC);
        EQ_GAIN.put(Constants.EQ.PARTY, EQ_PARTY);
        EQ_GAIN.put(Constants.EQ.POP, EQ_POP);
        EQ_GAIN.put(Constants.EQ.REGGAE, EQ_REGGAE);
        EQ_GAIN.put(Constants.EQ.ROCK, EQ_ROCK);
        EQ_GAIN.put(Constants.EQ.SOFT, EQ_SOFT);
        EQ_GAIN.put(Constants.EQ.TREBLE, EQ_TREBLE);
        EQ_GAIN.put(Constants.EQ.VOCALS, EQ_VOCALS);
        EQ_GAIN.put(Constants.EQ.RNB, EQ_RNB);
        EQ_GAIN.put(Constants.EQ.METAL, EQ_METAL);
    }
}
