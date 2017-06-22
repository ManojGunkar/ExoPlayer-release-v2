package com.globaldelight.boom.app.sharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import com.globaldelight.boom.playbackEvent.handler.UpNextPlayingQueue;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */

public class UserPreferenceHandler {
    private static final String PREF_NAME = "com.boom";
    private static final String REPEAT_MODE_KEY = "repeat_mode";
    private static final String SHUFFLE_MODE_KEY = "shuffle_mode";

    private static final String ALBUM_SORTED = "album_sorted";
    public static final int ALBUM_SORTED_BY_TITLE = 1;

    private static final String PLAYER_SEEK_POSITION = "player_seek_position";
    private static final String PLAYER_PLAYED_TIME = "played_time";
    private static final String PLAYER_REMAINS_TIME = "remains_time";


    private final SharedPreferences shp;

    private static final String PREF_ACCOUNT_NAME = "accountName";

    public UserPreferenceHandler(Context context) {
        shp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setGoogleAccountName(String accountName){
        shp.edit().putString(PREF_ACCOUNT_NAME, accountName).apply();
    }

    public String getGoogleAccountName(){
        return  shp.getString(PREF_ACCOUNT_NAME, null);
    }

    public @UpNextPlayingQueue.RepeatMode int resetRepeat(){

        int repeatMode = shp.getInt(REPEAT_MODE_KEY, 0);
        switch (repeatMode) {
            case UpNextPlayingQueue.REPEAT_NONE:
                shp.edit().putInt(REPEAT_MODE_KEY, UpNextPlayingQueue.REPEAT_ONE).apply();
                return UpNextPlayingQueue.REPEAT_ONE;

            case UpNextPlayingQueue.REPEAT_ONE:
                shp.edit().putInt(REPEAT_MODE_KEY, UpNextPlayingQueue.REPEAT_ALL).apply();
                return UpNextPlayingQueue.REPEAT_ALL;

            case UpNextPlayingQueue.REPEAT_ALL:
                shp.edit().putInt(REPEAT_MODE_KEY, UpNextPlayingQueue.REPEAT_NONE).apply();
                return UpNextPlayingQueue.REPEAT_NONE;
        }

        return UpNextPlayingQueue.REPEAT_NONE;
    }

    public @UpNextPlayingQueue.ShuffleMode int resetShuffle(){
        int shuffleMode = shp.getInt(SHUFFLE_MODE_KEY, 0);
        switch (shuffleMode) {
            case UpNextPlayingQueue.SHUFFLE_OFF:
                shp.edit().putInt(SHUFFLE_MODE_KEY, UpNextPlayingQueue.SHUFFLE_ON).apply();
                return UpNextPlayingQueue.SHUFFLE_ON;

            case UpNextPlayingQueue.SHUFFLE_ON:
                shp.edit().putInt(SHUFFLE_MODE_KEY, UpNextPlayingQueue.SHUFFLE_OFF).apply();
                return UpNextPlayingQueue.SHUFFLE_OFF;
        }

        return UpNextPlayingQueue.SHUFFLE_OFF;
    }


    public @UpNextPlayingQueue.RepeatMode int getRepeat(){
        switch ( shp.getInt(REPEAT_MODE_KEY, 0) ) {
            case UpNextPlayingQueue.REPEAT_NONE:
                return UpNextPlayingQueue.REPEAT_NONE;
            case UpNextPlayingQueue.REPEAT_ONE:
                return UpNextPlayingQueue.REPEAT_ONE;
            case UpNextPlayingQueue.REPEAT_ALL:
                return UpNextPlayingQueue.REPEAT_ALL;
        }
        return UpNextPlayingQueue.REPEAT_NONE;
    }

    public @UpNextPlayingQueue.ShuffleMode int getShuffle(){
        //noinspection ResourceType
        return shp.getInt(SHUFFLE_MODE_KEY, 0);
    }

    public int getSortedByAlbum() {
        return shp.getInt(ALBUM_SORTED, ALBUM_SORTED_BY_TITLE);
    }

    public void setPlayerSeekPosition(int playerSeekPosition) {
        shp.edit().putInt(PLAYER_SEEK_POSITION, playerSeekPosition).apply();
    }

    public int getPlayerSeekPosition(){
        return shp.getInt(PLAYER_SEEK_POSITION, 0);
    }

    public void setPlayedTime(long playedTime) {
        shp.edit().putLong(PLAYER_PLAYED_TIME, playedTime).apply();
    }

    public long getPlayedTime(){
        return shp.getLong(PLAYER_PLAYED_TIME, 0);
    }

    public void setRemainsTime(long remainsTime) {
        shp.edit().putLong(PLAYER_REMAINS_TIME, remainsTime).apply();
    }

    public long getRemainsTime(long defaultTime){
        return shp.getLong(PLAYER_REMAINS_TIME, defaultTime);
    }
}
