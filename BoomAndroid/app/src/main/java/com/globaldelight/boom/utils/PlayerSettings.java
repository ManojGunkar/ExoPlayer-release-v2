package com.globaldelight.boom.utils;

/**
 * Created by nidhin on 10/11/16.
 */

public class PlayerSettings {
    public static final int SEEKBAR_GESTURE_ACTIVE_TIME = 15000;

   public static final String ACTION_STOP_PLAYER = "com.player.boom.STOP_PLAYER";
    public static final String ACTION_SHAKE_EVENT = "com.player.boom.SHAKE_EVENT";
    public static final String SHAKE_EVENT_ACTION_TYPE = "shake_event_action_type";
    public enum HeadphoneType {
        OVER_EAR,
        IN_CANAL,
        ON_EAR;

        public static HeadphoneType toHeadPhone(String myEnumString) {
            try {
                return HeadphoneType.valueOf(myEnumString);
            } catch (Exception ex) {
                // For error cases
                return OVER_EAR;
            }
        }
    }

    public enum ShakeGesture {
        SHAKE_GESTURE_NONE,
        SHAKE_GESTURE_NEXT,
        SHAKE_GESTURE_PLAY;

        public static ShakeGesture toShakeGesture(String myEnumString) {
            try {
                return ShakeGesture.valueOf(myEnumString);
            } catch (Exception ex) {
                // For error cases
                return SHAKE_GESTURE_NONE;
            }
        }
    }

    public enum SortAlbum {
        SORT_BY_ARTIST,
        SORT_BY_ALBUM;

        public static SortAlbum toSortAlbum(String myEnumString) {
            try {
                return SortAlbum.valueOf(myEnumString);
            } catch (Exception ex) {
                // For error cases
                return SORT_BY_ARTIST;
            }
        }
    }
}
