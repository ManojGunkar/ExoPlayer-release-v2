package com.globaldelight.boom.utils.handlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.globaldelight.boom.Media.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.Media.ItemType;
import com.globaldelight.boom.Media.MediaType;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * Created by Rahul Agarwal on 28-11-16.
 */

public class UpNextDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private Context context;
    private static final String DATABASE_NAME = "UpNextDB";
    private static final String TABLE_UPNEXT = "history_table";

    private static final String SONG_KEY_ID = "song_id";
    private static final String SONG_KEY_REAL_ID = "ItemId";
    private static final String TITLE = "ItemTitle";
    private static final String DISPLAY_NAME = "ItemDisplayName";
    private static final String DATA_PATH = "ItemUrl";
    private static final String ALBUM_ID = "ItemAlbumId";
    private static final String ALBUM = "ItemAlbum";
    private static final String ARTIST_ID = "ItemArtistId";
    private static final String ARTIST = "ItemArtist";
    private static final String DURATION = "Duration";
    private static final String DATE_ADDED = "DateAdded";
    private static final String ALBUM_ART = "ItemArtUrl";
    private static final String MEDIA_TYPE = "mediaType";
    private static final String ITEM_TYPE = "itemType";
    private static final String PARENT_TYPE = "itemParentType";
    private static final String PARENT_ID = "parentId";
    private static final String PARENT_TITLE = "parentTitle";
    private static final String QUEUE_TYPE = "queue_type";

    public UpNextDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_HISTORY_TABLE = "CREATE TABLE "+ TABLE_UPNEXT +" (" +
                SONG_KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
                SONG_KEY_REAL_ID+" INTEGER," + TITLE+" TEXT," + DISPLAY_NAME+" TEXT,"+
                DATA_PATH+" TEXT," + ALBUM_ID+" INTEGER," +
                ALBUM+" TEXT," + ARTIST_ID+" INTEGER," +
                ARTIST+" TEXT," + DURATION+" TEXT," +
                DATE_ADDED+" TEXT," + ALBUM_ART+" TEXT," + ITEM_TYPE + " INTEGER," +
                MEDIA_TYPE+" INTEGER," + PARENT_TYPE + " INTEGER," +
                PARENT_ID+" INTEGER," + PARENT_TITLE + " TEXT," + QUEUE_TYPE+" INTEGER)";

        db.execSQL(CREATE_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_UPNEXT);
        this.onCreate(db);
    }

/*History Table List*/

    public synchronized void addItemsToRecentPlayedList(IMediaItemBase song) {
        removeSong(song.getItemId());

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.putNull(SONG_KEY_ID);
        values.put(SONG_KEY_REAL_ID, song.getItemId());
        values.put(TITLE, song.getItemTitle());
        values.put(DISPLAY_NAME, ((IMediaItem)song).getItemDisplayName());
        values.put(DATA_PATH, ((IMediaItem)song).getItemUrl());
        values.put(ALBUM_ID, ((IMediaItem)song).getItemAlbumId());
        values.put(ALBUM, ((IMediaItem)song).getItemAlbum());
        values.put(ARTIST_ID, ((IMediaItem)song).getItemArtistId());
        values.put(ARTIST, ((IMediaItem)song).getItemArtist());
        values.put(DURATION, ((IMediaItem)song).getDurationLong());
        values.put(DATE_ADDED, ((IMediaItem)song).getDateAdded());
        values.put(ALBUM_ART, ((IMediaItem)song).getItemArtUrl());
        values.put(ITEM_TYPE, ((IMediaItem)song).getItemType().ordinal());
        values.put(MEDIA_TYPE, ((IMediaItem)song).getMediaType().ordinal());
        values.put(PARENT_TYPE, ((IMediaItem)song).getParentType().ordinal());
        values.put(PARENT_ID, ((IMediaItem)song).getParentId());
        values.put(PARENT_TITLE, ((IMediaItem)song).getParentTitle());

        db.insert(TABLE_UPNEXT, null, values);
        db.close();
    }


    public synchronized void removeSong(long songId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_UPNEXT + " WHERE " +
                SONG_KEY_REAL_ID + "='" + songId + "'");
        db.close();
    }

    public synchronized void clearList(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_UPNEXT );
        db.close();
    }

    public synchronized ArrayList<? extends IMediaItemBase> getRecentPlayedItemList() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<IMediaItemBase> songList = new ArrayList<>();
        String query = "SELECT  * FROM " + TABLE_UPNEXT ;

        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String duration = cursor.getString(9);
                    String dateAdded = cursor.getString(10);
                        songList.add(new MediaItem(cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5),
                                cursor.getString(6), cursor.getInt(7), cursor.getString(8), Long.parseLong(duration), Long.parseLong(dateAdded), cursor.getString(11),
                                ItemType.fromOrdinal(cursor.getInt(12)), MediaType.fromOrdinal(cursor.getInt(13)), ItemType.fromOrdinal(cursor.getInt(14)), cursor.getInt(15), cursor.getString(16)));
                } while (cursor.moveToNext());
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        db.close();
        return songList;
    }

    public int getRecentPlayedCount(){
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        try {
            count = (int) DatabaseUtils.queryNumEntries(db, TABLE_UPNEXT, null);
        }catch (Exception e){

        }finally {
            db.close();
        }
        return count;
    }

    public ArrayList<String> getRecentArtList() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> artList = new ArrayList<>();
        String query = "SELECT  "+ALBUM_ART+" FROM " + TABLE_UPNEXT;
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    if(cursor.getString(0) != null && !cursor.getString(0).equals(MediaItem.UNKNOWN_ART_URL))
                        artList.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        }catch (Exception e){

        }finally {
            cursor.close();
        }
        db.close();
        return artList;
    }
}
