package com.globaldelight.boom.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.base.IMediaItem;
import com.globaldelight.boom.collection.base.IMediaElement;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.playbackEvent.utils.MediaType;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 11-02-17.
 */

public class CloudMediaItemDBHelper  extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "BoomCloud";
    private static final String TABLE_CLOUD_DATA = "cloud_data";
    private Context context;

    private static final String ITEM_KEY_ID = "db_item_id";
    private static final String SONG_KEY_ID = "song_id";
    private static final String SONG_KEY_REAL_ID = "ItemId";
    private static final String TITLE = "ItemTitle";
    private static final String DATA_PATH = "ItemUrl";
    private static final String MEDIA_TYPE = "mediaType";

    public CloudMediaItemDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FAVORITE_SONG_TABLE = "CREATE TABLE "+TABLE_CLOUD_DATA+" (" +
                SONG_KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
                SONG_KEY_REAL_ID+" INTEGER," + TITLE+" TEXT," +
                DATA_PATH+" TEXT," + MEDIA_TYPE+" INTEGER," + "song_playlist_id INTEGER)";
        db.execSQL(CREATE_FAVORITE_SONG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_CLOUD_DATA);
        this.onCreate(db);
    }

    public synchronized void addSong(IMediaElement song) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.putNull(SONG_KEY_ID);
        values.put(SONG_KEY_REAL_ID, song.getId());
        values.put(TITLE, song.getTitle());
        values.put(DATA_PATH, ((IMediaItem)song).getItemUrl());
        values.put(MEDIA_TYPE, song.getMediaType());

        db.insert(TABLE_CLOUD_DATA, null, values);
        db.close();
    }

    public synchronized void addSongs(@MediaType int mediaType, ArrayList<? extends IMediaElement> songs) {
        clearList(mediaType);
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < songs.size(); i++) {
            ContentValues values = new ContentValues();

            values.putNull(SONG_KEY_ID);
            values.put(SONG_KEY_REAL_ID, songs.get(i).getId());
            values.put(TITLE, songs.get(i).getTitle());
            values.put(DATA_PATH, ((IMediaItem)songs.get(i)).getItemUrl());
            values.put(MEDIA_TYPE, songs.get(i).getMediaType());

            db.insert(TABLE_CLOUD_DATA, null, values);
        }
        db.close();
    }

    public synchronized void clearList(@MediaType int mediaType){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_CLOUD_DATA+ " WHERE " +
                MEDIA_TYPE + "='" + mediaType + "'");
        db.close();
    }

    public synchronized ArrayList<? extends IMediaElement> getSongList(@MediaType int mediaType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<MediaItem> songList = new ArrayList<>();
        String query = "SELECT  * FROM " + TABLE_CLOUD_DATA + " WHERE " +
                MEDIA_TYPE + "='" + mediaType + "'"/* + " ORDER BY "+SONG_KEY_ID*/;

        Cursor cursor = db.rawQuery(query, null);

//        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    //noinspection ResourceType
                    songList.add(new MediaItem(String.valueOf(cursor.getInt(1)),
                            cursor.getString(2),
                            cursor.getString(3),
                            ItemType.SONGS,
                            cursor.getInt(4),
                            ItemType.SONGS));
                } while (cursor.moveToNext());
            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
            cursor.close();
//        }
        db.close();
        return songList;
    }
}
