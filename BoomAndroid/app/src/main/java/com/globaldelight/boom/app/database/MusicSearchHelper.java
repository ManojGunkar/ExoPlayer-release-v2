package com.globaldelight.boom.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;
import com.globaldelight.boom.app.App;
import java.util.HashMap;

/**
 * Created by Rahul Agarwal on 14-12-16.
 */

public class MusicSearchHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MusicSearchDB";
    private static final String TABLE_SEARCH = "search_table";
    private Context context;
    private volatile Boolean mFinishedLoading = false;

    public static final String ITEM_KEY_ID = "_id";
    public static final String SEARCH_KEY = "FEED_TITLE";


    public MusicSearchHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SEARCH_SONG_TABLE = "CREATE TABLE "+TABLE_SEARCH+" (" +
                ITEM_KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," + SEARCH_KEY+" TEXT UNIQUE)";
        db.execSQL(CREATE_SEARCH_SONG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_SEARCH);
        this.onCreate(db);
    }


    public synchronized void setSearchContent(){
        clearList();

        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";

        final String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor songListCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, orderBy);
        if (songListCursor != null && songListCursor.moveToFirst()) {

            int Song_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);

            int Album_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);

            int Artist_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);

            SQLiteDatabase db = this.getWritableDatabase();

            do{
                final String song = songListCursor.getString(Song_Name_Column);
                final String album = songListCursor.getString(Album_Name_Column);
                final String artist = songListCursor.getString(Artist_Name_Column);

                addEntry(song, db);
                addEntry(album, db);
                addEntry(artist, db);

            }while (songListCursor.moveToNext());

            db.close();

        }
        if (songListCursor != null) {
            songListCursor.close();
        }

        mFinishedLoading = true;
    }

    private void addEntry(String title, SQLiteDatabase db) {
        if ( title == null ) {
            return;
        }

        ContentValues values = new ContentValues();
        values.putNull(ITEM_KEY_ID);
        values.put(SEARCH_KEY, title.trim());
        db.replace(TABLE_SEARCH, null, values);

    }

    public synchronized void clearList(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SEARCH);
        db.close();
    }

    public Cursor getSongList(String arg) {
        if ( !mFinishedLoading ) {
            return null;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query(true, TABLE_SEARCH, new String[] { ITEM_KEY_ID,
                        SEARCH_KEY }, SEARCH_KEY + " LIKE ?",
                new String[] {"%"+ arg+ "%" }, null, null, null,
                null);
        return c;
    }

}
