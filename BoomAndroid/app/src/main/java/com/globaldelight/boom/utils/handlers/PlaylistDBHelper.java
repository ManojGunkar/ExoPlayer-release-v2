package com.globaldelight.boom.utils.handlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.Media.DeviceMediaQuery;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.Media.ItemType;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.Media.MediaType;
import java.util.ArrayList;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */

public class PlaylistDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "BoomPlayList";

    private static final String TABLE_PLAYLIST_SONGS = "playlistSongs";
    private static final String TABLE_PLAYLIST = "playlist";

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
    private static final String SONG_KEY_PLAYLIST_ID = "song_playlist_id";

    private static final String PLAYLIST_KEY_ID = "playlist_id";
    private static final String PLAYLIST_KEY_NAME = "playlist_name";
    private Context mContext;

    public PlaylistDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_PLAYLIST_SONG_TABLE = "CREATE TABLE playlistSongs (" +
                SONG_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SONG_KEY_REAL_ID + " INTEGER," + TITLE + " TEXT," + DISPLAY_NAME + " TEXT,"+
                DATA_PATH + " TEXT," + ALBUM_ID + " INTEGER," +
                ALBUM + " TEXT," + ARTIST_ID + " INTEGER," +
                ARTIST + " TEXT," + DURATION + " TEXT," +
                DATE_ADDED + " TEXT," + ALBUM_ART + " TEXT," +
                MEDIA_TYPE + " INTEGER," + SONG_KEY_PLAYLIST_ID + " INTEGER)";

        String CREATE_PLAYLIST_TABLE = "CREATE TABLE playlist (" +
                "playlist_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "playlist_name TEXT)";

        db.execSQL(CREATE_PLAYLIST_SONG_TABLE);
        db.execSQL(CREATE_PLAYLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS playlistSongs");
        db.execSQL("DROP TABLE IF EXISTS playlist");
        this.onCreate(db);
    }



/***********************************************************************************************************************************/
    public void renamePlaylist(String name, long playlistId) {
        String query = "UPDATE " + TABLE_PLAYLIST + " SET " + PLAYLIST_KEY_NAME
                + "='" + name + "' WHERE "
                + PLAYLIST_KEY_ID + "='" + playlistId + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public ArrayList<? extends IMediaItemBase> getAllPlaylist() {
        String query = "SELECT  * FROM " + TABLE_PLAYLIST;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<MediaItemCollection> playlist = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    playlist.add(getPlaylistFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        db.close();
        return playlist;
    }

    public IMediaItemBase gePlaylist(long id) {
        String query = "SELECT  * FROM " + TABLE_PLAYLIST +" WHERE "+PLAYLIST_KEY_ID +"="+id;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        IMediaItemBase playlist = null;
        try {
            if (cursor.moveToFirst()) {
                    playlist = getPlaylistFromCursor(cursor);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        db.close();
        return playlist;
    }

    private MediaItemCollection getPlaylistFromCursor(Cursor cursor) {
        int playlistId = cursor.getInt(0);

        MediaItemCollection collection = new MediaItemCollection(playlistId, cursor.getString(1),
                null, null,
                getPlaylistSongCount(playlistId), 0,  ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB, ItemType.BOOM_PLAYLIST);
        /*collection.setArtUrlList(getBoomPlayListArtList(playlistId));*/
        return collection;
    }

    public void addSong(long playlistId, IMediaItemBase song) {
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
        values.put(SONG_KEY_PLAYLIST_ID, playlistId);

        db.insert(TABLE_PLAYLIST_SONGS, null, values);
        db.close();
    }

    public void addSongs(ArrayList<? extends IMediaItemBase> songs, long playlistId, boolean isUpdate) {
        if(isUpdate)
            clearList(playlistId);
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < songs.size(); i++) {
            removeSong(songs.get(i).getItemId(), (int) playlistId, db);

            if(songs.get(i).getMediaType() == MediaType.DEVICE_MEDIA_LIB && null == songs.get(i).getItemArtUrl())
                songs.get(i).setItemArtUrl(DeviceMediaQuery.getAlbumArtByAlbumId(mContext, ((IMediaItem)songs.get(i)).getItemAlbumId()));
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
            values.put(SONG_KEY_PLAYLIST_ID, playlistId);
            try {
                db.insert(TABLE_PLAYLIST_SONGS, null, values);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        db.close();
    }

    public void removeSong(long songId, int playlistId, SQLiteDatabase db) {
        db.execSQL("DELETE FROM " + TABLE_PLAYLIST_SONGS + " WHERE " +
                SONG_KEY_REAL_ID + "='" + songId + "' AND "
                + SONG_KEY_PLAYLIST_ID + "='" + playlistId + "'");
    }

    public void removeSong(long songId, int playlistId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PLAYLIST_SONGS + " WHERE " +
                SONG_KEY_REAL_ID + "='" + songId + "' AND "
                + SONG_KEY_PLAYLIST_ID + "='" + playlistId + "'");
        db.close();
    }

    public void removeSong(String songTitle, int playlistId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PLAYLIST_SONGS + " WHERE " +
                TITLE + "='" + songTitle + "' AND "
                + SONG_KEY_PLAYLIST_ID + "='" + playlistId + "'");
        db.close();
    }

    public void deletePlaylist(long playlistId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PLAYLIST + " WHERE " +
                PLAYLIST_KEY_ID + "='" + playlistId + "'");
        db.execSQL("DELETE FROM " + TABLE_PLAYLIST_SONGS + " WHERE " +
                SONG_KEY_PLAYLIST_ID + "='" + playlistId + "'");
        db.close();
    }

    public void createPlaylist(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.putNull(PLAYLIST_KEY_ID);
        values.put(PLAYLIST_KEY_NAME, name);

        db.insert(TABLE_PLAYLIST, null, values);
        db.close();
    }

    public ArrayList<? extends IMediaItemBase> getPlaylistSongs(long playlistId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<MediaItem> songList = new ArrayList<>();
        String query = "SELECT  * FROM " + TABLE_PLAYLIST_SONGS + " WHERE "
                + SONG_KEY_PLAYLIST_ID + "='" + playlistId + "'";
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String duration = cursor.getString(9);
                    String dateAdded = cursor.getString(10);

                    songList.add(new MediaItem(cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5),
                            cursor.getString(6), cursor.getInt(7), cursor.getString(8), Long.parseLong(duration),
                            Long.parseLong(dateAdded), cursor.getString(11), ItemType.SONGS, MediaType.fromOrdinal(cursor.getInt(12)), ItemType.BOOM_PLAYLIST, playlistId, null));
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

    public int getPlaylistSongCount(long playlistId) {
        String query = "select count(*) from " + TABLE_PLAYLIST_SONGS + " where "
                + SONG_KEY_PLAYLIST_ID + "='" + playlistId + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement s = db.compileStatement(query);
        int count =0;
        try{
            count = (int) s.simpleQueryForLong();
        }catch (SQLiteException e){

        }

        db.close();
        return count;
    }

    public ArrayList<String> getBoomPlayListArtList(long playlistId) {

        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> artList = new ArrayList<>();
        String query = "SELECT  "+ALBUM_ART+" FROM " + TABLE_PLAYLIST_SONGS + " WHERE "
                + SONG_KEY_PLAYLIST_ID + "='" + playlistId + "'";
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

    public void clearList(long playlistId){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PLAYLIST_SONGS + " WHERE " +
                SONG_KEY_PLAYLIST_ID + "='" + playlistId + "'");
        db.close();
    }
/***********************************************************************************************************************************/
}