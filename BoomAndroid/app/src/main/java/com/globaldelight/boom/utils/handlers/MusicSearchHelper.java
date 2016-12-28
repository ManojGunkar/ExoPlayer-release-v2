package com.globaldelight.boom.utils.handlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;
import com.globaldelight.boom.App;
import java.util.HashMap;

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

            int Song_Display_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DISPLAY_NAME);

            int Album_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);

            int Artist_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);

            do{
                addSong(songListCursor.getString(Song_Name_Column).trim());
//                addSong(songListCursor.getString(Song_Display_Name_Column));
                addSong(songListCursor.getString(Album_Name_Column).trim());
                addSong(songListCursor.getString(Artist_Name_Column).trim());
            }while (songListCursor.moveToNext());

        }
        if (songListCursor != null) {
            songListCursor.close();
        }

    }

    private synchronized void addSong(String title) {
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
        db.delete(TABLE_SEARCH,
                SEARCH_KEY+" = ?", new String[] { title } );

        db.close();
    }

    public synchronized void clearList(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SEARCH);
        db.close();
    }

    public synchronized Cursor getSongList(String arg) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query(true, TABLE_SEARCH, new String[] { ITEM_KEY_ID,
                        SEARCH_KEY }, SEARCH_KEY + " LIKE ?",
                new String[] {"%"+ arg+ "%" }, null, null, null,
                null);
        return c;
    }

    /************************************Album Art****************************************************************/
    public static void getAlbumList(Context context) {
        HashMap<String, String> artWthAlbumName = new HashMap<>();
        Cursor albumListCursor = context.getContentResolver().
                query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, null, null, null);

        if (albumListCursor != null && albumListCursor.moveToFirst()) {
            //get columns
            int Item_Title_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ALBUM);

            int Item_Album_Art_Path_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ALBUM_ART);

            //add albums to list
            do {
                artWthAlbumName.put(albumListCursor.getString(Item_Title_Column), albumListCursor.getString(Item_Album_Art_Path_Column));
            }
            while (albumListCursor.moveToNext());
        }
        if (albumListCursor != null) {
            albumListCursor.close();
        }
        App.getPlayingQueueHandler().getUpNextList().setAlbumArtList(artWthAlbumName);
        return;
    }

    public static void getArtistList(Context context) {
        HashMap<Long, String> artistList = new HashMap<>();
        System.gc();
        final String orderBy = MediaStore.Audio.Artists.ARTIST;
        Cursor artistListCursor = context.getContentResolver().
                query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, null, null, orderBy);

        if (artistListCursor != null && artistListCursor.moveToFirst()) {
            //get columns

            int Item_ID_Column = artistListCursor.getColumnIndex
                    (MediaStore.Audio.Artists._ID);

            //add albums to list
            do {
                artistList.put(artistListCursor.getLong(Item_ID_Column), getAlbumArtByArtist(context, artistListCursor.getLong(Item_ID_Column)));
            }
            while (artistListCursor.moveToNext());
        }
        if (artistListCursor != null) {
            artistListCursor.close();
        }
        App.getPlayingQueueHandler().getUpNextList().setArtistArtList(artistList);
        return;
    }

    public static String getAlbumArtByArtist(Context context, Long artistId) {
        final String where = MediaStore.Audio.Media.ARTIST_ID + "=?";

        Cursor albumListCursor = context.getContentResolver().
                query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Albums.ALBUM_ART}, where, new String[]{String.valueOf(artistId)}, null);

        String albumArt = null;
        if (albumListCursor != null && albumListCursor.moveToFirst()) {
            //get columns
            int Item_Album_Art_Path_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ALBUM_ART);

            albumArt = albumListCursor.getString(Item_Album_Art_Path_Column);
        }
        if (albumListCursor != null) {
            albumListCursor.close();
        }
        return albumArt;
    }
}
