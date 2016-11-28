package com.globaldelight.boom.utils.handlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaLibrary.MediaType;

import java.util.LinkedList;

/**
 * Created by Rahul Kumar Agrawal on 7/5/2016.
 */
public class FavoriteDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "PlaylistDB";
    private static final String TABLE_FAVORITE = "favorite";
    private Context context;

    private static final String ITEM_KEY_ID = "db_item_id";
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

//    for history use only
//    private static final String ISHISTORY = "is_history";

//    for favorite use only
//    private static final String ISFAVORITE = "is_favorite";

    public FavoriteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FAVORITE_SONG_TABLE = "CREATE TABLE "+TABLE_FAVORITE+" (" +
                SONG_KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
                SONG_KEY_REAL_ID+" INTEGER," + TITLE+" TEXT," + DISPLAY_NAME+" TEXT,"+
                DATA_PATH+" TEXT," + ALBUM_ID+" INTEGER," +
                ALBUM+" TEXT," + ARTIST_ID+" INTEGER," +
                ARTIST+" TEXT," + DURATION+" TEXT," +
                DATE_ADDED+" TEXT," + ALBUM_ART+" TEXT," +
                MEDIA_TYPE+" INTEGER," + "song_playlist_id INTEGER)";
        db.execSQL(CREATE_FAVORITE_SONG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_FAVORITE);
        this.onCreate(db);
    }

    public void addSong(IMediaItemBase song) {

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
        values.put(MEDIA_TYPE, ((IMediaItem)song).getMediaType().ordinal());

        db.insert(TABLE_FAVORITE, null, values);
        db.close();
    }

    public void addSongs(LinkedList<? extends IMediaItemBase> songs) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < songs.size(); i++) {
            ContentValues values = new ContentValues();

            values.putNull(SONG_KEY_ID);
            values.put(SONG_KEY_REAL_ID, songs.get(i).getItemId());
            values.put(TITLE, songs.get(i).getItemTitle());
            values.put(DISPLAY_NAME, ((IMediaItem)songs.get(i)).getItemDisplayName());
            values.put(DATA_PATH, ((IMediaItem)songs.get(i)).getItemUrl());
            values.put(ALBUM_ID, ((IMediaItem)songs.get(i)).getItemAlbumId());
            values.put(ALBUM, ((IMediaItem)songs.get(i)).getItemAlbum());
            values.put(ARTIST_ID, ((IMediaItem)songs.get(i)).getItemArtistId());
            values.put(ARTIST, ((IMediaItem)songs.get(i)).getItemArtist());
            values.put(DURATION, ((IMediaItem)songs.get(i)).getDurationLong());
            values.put(DATE_ADDED, ((IMediaItem)songs.get(i)).getDateAdded());
            values.put(ALBUM_ART, ((IMediaItem)songs.get(i)).getItemArtUrl());
            values.put(MEDIA_TYPE, ((IMediaItem)songs.get(i)).getMediaType().ordinal());

            db.insert(TABLE_FAVORITE, null, values);
        }
        db.close();
    }

    public void removeSong(long songId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_FAVORITE + " WHERE " +
                SONG_KEY_REAL_ID + "='" + songId + "'");
        db.close();
    }

    public void clearList(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_FAVORITE);
        db.close();
    }

    public LinkedList<? extends IMediaItemBase> getSongList() {
        SQLiteDatabase db = this.getWritableDatabase();
        LinkedList<MediaItem> songList = new LinkedList<>();
        String query = "SELECT  * FROM " + TABLE_FAVORITE + " ORDER BY "+SONG_KEY_ID + " DESC";

        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String duration = cursor.getString(9);
                    String dateAdded = cursor.getString(10);

                    songList.add(new MediaItem(cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5),
                            cursor.getString(6), cursor.getInt(7), cursor.getString(8), Long.parseLong(duration),
                            Long.parseLong(dateAdded), cursor.getString(11), ItemType.SONGS, MediaType.fromOrdinal(cursor.getInt(12))));
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

    public boolean isFavouriteItems(long itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT  * FROM " + TABLE_FAVORITE+ " WHERE " +
                SONG_KEY_REAL_ID + "='" + itemId + "'";
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }
}
