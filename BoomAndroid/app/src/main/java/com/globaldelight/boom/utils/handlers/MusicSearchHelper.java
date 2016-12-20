package com.globaldelight.boom.utils.handlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;

import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaType;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Rahul Agarwal on 14-12-16.
 */

public class MusicSearchHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MusicSearchDB";
    private static final String TABLE_SEARCH = "search_table";
    private Context context;

    public static final String ITEM_KEY_ID = "_id";
    public static final String SEARCH_KEY = "FEED_TITLE";

    public MusicSearchHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SEARCH_SONG_TABLE = "CREATE TABLE "+TABLE_SEARCH+" (" +
                ITEM_KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," + SEARCH_KEY+" TEXT)";
        db.execSQL(CREATE_SEARCH_SONG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_SEARCH);
        this.onCreate(db);
    }


    public void setSearchContent(){
        clearList();
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";

        final String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor songListCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, orderBy);
        if (songListCursor != null && songListCursor.moveToFirst()) {

            int Song_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);

            int Song_Display_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DISPLAY_NAME);

            int Album_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);

            int Artist_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);

            do{
                addSong(songListCursor.getString(Song_Name_Column));
//                addSong(songListCursor.getString(Song_Display_Name_Column));
                addSong(songListCursor.getString(Album_Name_Column));
                addSong(songListCursor.getString(Artist_Name_Column));
            }while (songListCursor.moveToNext());

        }
        if (songListCursor != null) {
            songListCursor.close();
        }
    }

    private void addSong(String title) {
        removeSong(title);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.putNull(ITEM_KEY_ID);
        values.put(SEARCH_KEY, title);
        db.insert(TABLE_SEARCH, null, values);
        db.close();
    }

    public void removeSong(String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.rawQuery("DELETE FROM " + TABLE_SEARCH + " WHERE " +
                SEARCH_KEY+" = ?", new String[] { title } );

        db.close();
    }

    public void clearList(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SEARCH);
        db.close();
    }

    public Cursor getSongList(String arg) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT  * FROM " + TABLE_SEARCH +" where " + SEARCH_KEY + " like '%" + arg
                + "%'";

        return db.rawQuery(query, null);
    }
}
