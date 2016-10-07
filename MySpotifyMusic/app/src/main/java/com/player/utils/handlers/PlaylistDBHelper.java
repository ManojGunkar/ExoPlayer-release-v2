package com.player.utils.handlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.provider.MediaStore;
import android.widget.Switch;

import com.player.data.DeviceMediaCollection.MediaItem;
import com.player.data.DeviceMediaCollection.MediaItemCollection;
import com.player.data.MediaCollection.IMediaItem;
import com.player.data.MediaCollection.IMediaItemBase;
import com.player.data.MediaLibrary.ItemType;
import com.player.data.MediaLibrary.MediaType;
import java.util.ArrayList;


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

    public PlaylistDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_PLAYLIST_SONG_TABLE = "CREATE TABLE playlistSongs (" +
                "song_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "ItemId INTEGER," + "ItemTitle TEXT," + "ItemDisplayName TEXT,"+
                "ItemUrl TEXT," + "ItemAlbumId INTEGER," +
                "ItemAlbum TEXT," + "ItemArtistId INTEGER," +
                "ItemArtist TEXT," + "Duration TEXT," +
                "DateAdded TEXT," + "ItemArtUrl TEXT," +
                "mediaType INTEGER," + "song_playlist_id INTEGER)";

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

    private MediaItemCollection getPlaylistFromCursor(Cursor cursor) {
        int playlistId = cursor.getInt(0);

        return new MediaItemCollection(playlistId, cursor.getString(1),
                null, null,
                getPlaylistSongCount(playlistId), 0,  ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB);
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

    public void addSongs(ArrayList<? extends IMediaItemBase> songs, long playlistId) {
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
            values.put(SONG_KEY_PLAYLIST_ID, playlistId);

            db.insert(TABLE_PLAYLIST_SONGS, null, values);
        }
        db.close();
    }

    public void removeSong(long songId, int playlistId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PLAYLIST_SONGS + " WHERE " +
                SONG_KEY_REAL_ID + "='" + songId + "' AND "
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
                    if(cursor.getString(0) != null)
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
/***********************************************************************************************************************************/
}