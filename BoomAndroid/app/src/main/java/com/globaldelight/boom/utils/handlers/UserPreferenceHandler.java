package com.globaldelight.boom.utils.handlers;

import android.content.Context;
import android.content.SharedPreferences;

import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.handler.PlayingQueue.UpNextList;

import java.util.ArrayList;


public class UserPreferenceHandler {

    private static final String LIBRARY_FROM = "com.boom.player.library";
    private static final String PREF_NAME = "com.boom";
    private static final String REPEAT_ALL = "repeat_all";
    private static final String REPEAT_ONE = "repeat_one";
    private static final String REPEAT_NONE = "repeat_none";
    private static final String SHUFFLE = "shuffle";
    private static final String SHUFFLE_NONE = "shuffle_none";

    private static final String ALBUM_SORTED = "album_sorted";
    public static final int ALBUM_SORTED_BY_ARTIST = 0;
    public static final int ALBUM_SORTED_BY_TITLE = 1;
    private static final boolean LIB_FROM_HOME = true;

    private static final String PLAYER_SEEK_POSITION = "player_seek_position";
    private static final String PLAYER_PLAYED_TIME = "played_time";
    private static final String PLAYER_REMAINS_TIME = "remains_time";

    private final SharedPreferences shp;
    private final SharedPreferences.Editor editor;

    private static ArrayList<MediaItem> list = new ArrayList<>();
    private static ArrayList<Long> idList = new ArrayList<>();
    private static long boomPlayListId;
    private int playerSeekPosition;

    public UserPreferenceHandler(Context context) {
        shp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = shp.edit();
    }

    public SharedPreferences.Editor getEditor(){
        return editor;
    }

    public void commit(){
        editor.commit();
    }


    public UpNextList.REPEAT resetRepeat(){
        if(shp.getBoolean(REPEAT_NONE, true)){
            shp.edit().putBoolean(REPEAT_NONE, false).apply();
            shp.edit().putBoolean(REPEAT_ONE, true).apply();
            shp.edit().putBoolean(REPEAT_ALL, false).apply();
            return UpNextList.REPEAT.one;
        }else if(shp.getBoolean(REPEAT_ONE, false)){
            shp.edit().putBoolean(REPEAT_NONE, false).apply();
            shp.edit().putBoolean(REPEAT_ONE, false).apply();
            shp.edit().putBoolean(REPEAT_ALL, true).apply();
            return UpNextList.REPEAT.all;
        }else if(shp.getBoolean(REPEAT_ALL, false)){
            shp.edit().putBoolean(REPEAT_NONE, true).apply();
            shp.edit().putBoolean(REPEAT_ONE, false).apply();
            shp.edit().putBoolean(REPEAT_ALL, false).apply();
        }
        return UpNextList.REPEAT.none;
    }

    public UpNextList.SHUFFLE resetShuffle(){
        if(shp.getBoolean(SHUFFLE_NONE, true)){
            shp.edit().putBoolean(SHUFFLE_NONE, false).apply();
            shp.edit().putBoolean(SHUFFLE, true).apply();
            return UpNextList.SHUFFLE.all;
        }else if(shp.getBoolean(SHUFFLE, false)){
            shp.edit().putBoolean(SHUFFLE_NONE, true).apply();
            shp.edit().putBoolean(SHUFFLE, false).apply();
        }
        return UpNextList.SHUFFLE.none;
    }


    public void setRepeatAllEnable(boolean enable) {
        shp.edit().putBoolean(REPEAT_ALL, enable).apply();
    }

    public void setRepeatOneEnable(boolean enable) {
        shp.edit().putBoolean(REPEAT_ONE, enable).apply();
    }


    public UpNextList.REPEAT getRepeat(){
        if(shp.getBoolean(REPEAT_ALL, false)){
            return UpNextList.REPEAT.all;
        }else if(shp.getBoolean(REPEAT_ONE, false)){
            return UpNextList.REPEAT.one;
        }else{
            return UpNextList.REPEAT.none;
        }
    }


    public UpNextList.SHUFFLE getShuffle(){
        if(shp.getBoolean(SHUFFLE, false)){
            return UpNextList.SHUFFLE.all;
        }else{
            return UpNextList.SHUFFLE.none;
        }
    }

    public void setAlbumSorted(int type){
        shp.edit().putInt(ALBUM_SORTED, type).apply();
    }

    public int getSortedByAlbum() {
        return shp.getInt(ALBUM_SORTED, ALBUM_SORTED_BY_TITLE);
    }

    /*Boom PlayList*/

    public void setLibraryStartFromHome(boolean isFromHome) {
        shp.edit().putBoolean(LIBRARY_FROM, isFromHome ? LIB_FROM_HOME : !LIB_FROM_HOME).apply();
    }

    public boolean isLibFromHome(){
        return shp.getBoolean(LIBRARY_FROM, LIB_FROM_HOME);
    }

    public void addItemToPlayList(MediaItem item) {
        if(idList.contains(item.getItemId())){
            list.remove(idList.indexOf(item.getItemId()));
            idList.remove(item.getItemId());
        }else {
            idList.add(item.getItemId());
            list.add(item);
        }
    }

    public ArrayList<MediaItem> getItemList(){
        return list;
    }

    public ArrayList<Long> getItemIDList(){
        return idList;
    }

    public void clearItemList(){
        list.clear();
        idList.clear();
    }

    public void setBoomPlayListId(long boomPlayListId) {
        this.boomPlayListId = boomPlayListId;
    }

    public long getBoomPlayListId(){
        return this.boomPlayListId;
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

    public long getRemainsTime(){
        return shp.getLong(PLAYER_REMAINS_TIME, 0);
    }
}
