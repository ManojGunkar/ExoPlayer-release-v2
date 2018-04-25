package com.globaldelight.boom.playbackEvent.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.base.IMediaItem;
import com.globaldelight.boom.app.adapters.search.utils.SearchResult;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.base.IMediaElement;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.app.sharedPreferences.UserPreferenceHandler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Rahul Agarwal on 8/8/2016.
 */
public class DeviceMediaLibrary {


    private static DeviceMediaLibrary sInstance = null;

    public static DeviceMediaLibrary getInstance(Context context) {
        if ( sInstance == null ) {
            sInstance = new DeviceMediaLibrary(context.getApplicationContext());
        }

        return sInstance;
    }


    private Context mContext;
    private HashMap<String, String> mAlbumArtList = new HashMap<>();
    private HashMap<String, String> mArtistArtList = new HashMap<>();

    private DeviceMediaLibrary(Context context) {
        mContext = context;
    }

    public void initAlbumAndArtist() {
        initAlbumArtList();
        initArtistArtList();
    }

    public String getAlbumArt(String album) {
        return mAlbumArtList.get(album);
    }

    public String getArtistArt(String itemId) {
        return mArtistArtList.get(itemId);
    }

    private void initAlbumArtList() {
        HashMap<String, String> artWthAlbumName = new HashMap<>();
        Cursor albumListCursor = mContext.getContentResolver().
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

        mAlbumArtList = artWthAlbumName;
        return;
    }

    private void initArtistArtList() {
        HashMap<String, String> artistList = new HashMap<>();
        System.gc();
        final String orderBy = MediaStore.Audio.Artists.ARTIST;
        Cursor artistListCursor = mContext.getContentResolver().
                query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, null, null, orderBy);

        if (artistListCursor != null && artistListCursor.moveToFirst()) {
            //get columns

            int Item_ID_Column = artistListCursor.getColumnIndex
                    (MediaStore.Audio.Artists._ID);

            //add albums to list
            do {
                String id = String.valueOf(artistListCursor.getLong(Item_ID_Column));
                String artistId = String.valueOf(artistListCursor.getLong(Item_ID_Column));
                artistList.put(id, getAlbumArtByArtist(mContext, artistId));
            }
            while (artistListCursor.moveToNext());
        }
        if (artistListCursor != null) {
            artistListCursor.close();
        }

        mArtistArtList = artistList;
        return;
    }

    public static String getAlbumArtByArtist(Context context, String artistId) {
        final String where = MediaStore.Audio.Media.ARTIST_ID + "=?";

        Cursor albumListCursor = context.getContentResolver().
                query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Albums.ALBUM_ART}, where, new String[]{artistId}, null);

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



    public static ArrayList<? extends IMediaItem> getSongList(Context context){
        ArrayList<IMediaItem> songList = new ArrayList<>();

        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";

        final String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor songListCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, orderBy);
        if (songListCursor != null && songListCursor.moveToFirst()) {

            int Song_Id_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);

            int Song_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);

            int Song_Display_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DISPLAY_NAME);

            int Song_Path_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);

            int Album_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);

            int Album_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);

            int Artist_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST_ID);

            int Artist_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);

            int Duration_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);

            int Date_Added_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);
            do{
                String artistName = songListCursor.getString(Artist_Name_Column);
                if ( artistName == null || artistName.equalsIgnoreCase("<unknown>") ) {
                    artistName = context.getResources().getString(R.string.unknown_artist);
                }
                songList.add(new MediaItem(Long.toString(songListCursor.getLong(Song_Id_Column)),
                        songListCursor.getString(Song_Name_Column),
                        songListCursor.getString(Song_Display_Name_Column), songListCursor.getString(Song_Path_Column),
                        Long.toString(songListCursor.getLong(Album_ID_Column)),
                        songListCursor.getString(Album_Name_Column),
                        Long.toString(songListCursor.getLong(Artist_ID_Column)),
                        artistName,
                        songListCursor.getLong(Duration_Column),
                        songListCursor.getLong(Date_Added_Column),
                        /*getAlbumArtByAlbum(context, songListCursor.getString(Album_Name_Column))*/ null, ItemType.SONGS,
                        MediaType.DEVICE_MEDIA_LIB, ItemType.SONGS,
                        Long.toString(0),
                        null));
            }while (songListCursor.moveToNext());

        }
        if (songListCursor != null) {
            songListCursor.close();
        }
        return songList;
    }

    public static String getAlbumArtByAlbum(Context context, String album) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums.ALBUM + "=?",
                new String[]{String.valueOf(album)},
                null);
        String str = null;
        if (cursor != null && cursor.moveToFirst()) {
            str = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
        }

        if (cursor != null) {
            cursor.close();
        }
        return str;
    }

    public static String getAlbumArtByAlbumId(Context context, String albumId) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?",
                new String[]{String.valueOf(albumId)},
                null);
        String str = null;
        if (cursor != null && cursor.moveToFirst()) {
            str = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
        }

        if (cursor != null) {
            cursor.close();
        }
        return str;
    }

/*****************************************************Albums Query****************************************************************/

    public static ArrayList<? extends IMediaElement> getAlbumList(Context context) {
        ArrayList<MediaItemCollection> albumList = new ArrayList<>();
        System.gc();
        String orderBy;
        if(App.getUserPreferenceHandler().getSortedByAlbum() == UserPreferenceHandler.ALBUM_SORTED_BY_TITLE){
            orderBy = MediaStore.Audio.Albums.ALBUM;
        }else{
            orderBy = MediaStore.Audio.Albums.ARTIST;
        }
        Cursor albumListCursor = context.getContentResolver().
                query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, null, null, orderBy);

        if (albumListCursor != null && albumListCursor.moveToFirst()) {
            //get columns
            int Item_ID_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Albums._ID);

            int Item_Title_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ALBUM);

            int Item_Sub_Title_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ARTIST);

            int Item_Count_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Albums.NUMBER_OF_SONGS);

            int Item_Album_Art_Path_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ALBUM_ART);

            //add albums to list
            do {
                if(albumListCursor.getInt(Item_Count_Column) > 0)
                albumList.add(new MediaItemCollection(Long.toString(albumListCursor.getLong(Item_ID_Column)),
                        albumListCursor.getString(Item_Title_Column),
                        albumListCursor.getString(Item_Sub_Title_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : albumListCursor.getString(Item_Sub_Title_Column),
                        albumListCursor.getString(Item_Album_Art_Path_Column),
                        albumListCursor.getInt(Item_Count_Column), 0,  ItemType.ALBUM, MediaType.DEVICE_MEDIA_LIB, ItemType.ALBUM));
            }
            while (albumListCursor.moveToNext());
        }
        if (albumListCursor != null) {
            albumListCursor.close();
        }
        return albumList;
    }

    public static ArrayList<? extends IMediaItem> getAlbumDetail(Context context, String itemId, String itemTitle) {
        System.gc();
        ArrayList<IMediaItem> songList = new ArrayList<>();

        String where = MediaStore.Audio.Media.IS_MUSIC + "=1 AND "+MediaStore.Audio.Media.ALBUM_ID + "=?";

        String whereVal[] = {String.valueOf(itemId)};
        String orderBy = MediaStore.Audio.Media.ALBUM + " COLLATE NOCASE ASC";;//MediaStore.Audio.Media.ALBUM_ID;

        Cursor songListCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, whereVal, orderBy);

        if (songListCursor != null && songListCursor.moveToFirst()) {

            int Song_Id_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);

            int Song_Title_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);

            int Song_Display_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DISPLAY_NAME);

            int Song_Path_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);

            int Album_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);

            int Album_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);

            int Artist_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST_ID);

            int Artist_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);

            int Duration_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);

            int Date_Added_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);

            do{
                songList.add(new MediaItem(Long.toString(songListCursor.getLong(Song_Id_Column)),
                        songListCursor.getString(Song_Title_Column),
                        songListCursor.getString(Song_Display_Name_Column),
                        songListCursor.getString(Song_Path_Column),
                        Long.toString(songListCursor.getLong(Album_ID_Column)),
                        songListCursor.getString(Album_Name_Column),
                        Long.toString(songListCursor.getLong(Artist_ID_Column)),
                        songListCursor.getString(Artist_Name_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : songListCursor.getString(Artist_Name_Column),
                        songListCursor.getLong(Duration_Column), songListCursor.getLong(Date_Added_Column),
                        getAlbumArtByAlbum(context, songListCursor.getString(Album_Name_Column)), ItemType.SONGS, MediaType.DEVICE_MEDIA_LIB, ItemType.ALBUM, itemId, null));
            }while (songListCursor.moveToNext());
        }
        if (songListCursor != null) {
            songListCursor.close();
        }
        return songList;
    }

/*****************************************************Artist Query****************************************************************/

    public static ArrayList<MediaItemCollection> getArtistList(Context context) {
        ArrayList<MediaItemCollection> artistList = new ArrayList<>();
        System.gc();
        final String orderBy = MediaStore.Audio.Artists.ARTIST;
        Cursor artistListCursor = context.getContentResolver().
                query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, null, null, orderBy);

        if (artistListCursor != null && artistListCursor.moveToFirst()) {
            //get columns

            int Item_ID_Column = artistListCursor.getColumnIndex
                    (MediaStore.Audio.Artists._ID);

            int Item_Title_Column = artistListCursor.getColumnIndex
                    (MediaStore.Audio.Artists.ARTIST);

            int numOfAlbumsColumn = artistListCursor.getColumnIndex
                    (MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);

            int Item_Count_Column = artistListCursor.getColumnIndex
                    (MediaStore.Audio.Artists.NUMBER_OF_TRACKS);

            //add albums to list
            do {
                String artistTitle = artistListCursor.getString(Item_Title_Column);
                if ( artistTitle == null || artistTitle.equalsIgnoreCase("<unknown>") ) {
                    artistTitle = context.getResources().getString(R.string.unknown_artist);
                }

                artistList.add(new MediaItemCollection(Long.toString(artistListCursor.getLong(Item_ID_Column)),
                        artistTitle,
                        null, /*getAlbumArtByArtist(context, artistListCursor.getString(Item_Title_Column))*/ null,
                        artistListCursor.getInt(Item_Count_Column), artistListCursor.getInt(numOfAlbumsColumn), ItemType.ARTIST,
                        MediaType.DEVICE_MEDIA_LIB, ItemType.ARTIST));
            }
            while (artistListCursor.moveToNext());
        }
        if (artistListCursor != null) {
            artistListCursor.close();
        }
        return artistList;
    }

    public static ArrayList<? extends IMediaElement> getArtistsAlbumDetails(Context context, String itemId, String itemTitle, int itemCount) {
        final ArrayList<MediaItemCollection> albumList = new ArrayList<>();
        System.gc();
        Cursor albumListCursor = context.getContentResolver().
                query(MediaStore.Audio.Artists.Albums.getContentUri("external", Long.parseLong(itemId)),
                        null, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
        if (albumListCursor != null && albumListCursor.moveToFirst()) {
            //get columns
            int Item_ID_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Albums._ID);

            int Item_Title_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Artists.Albums.ALBUM);

            int Item_Sub_Title_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Artists.Albums.ARTIST);

            int Item_Count_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Albums.NUMBER_OF_SONGS);

            int Item_Album_Art_Path_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ALBUM_ART);
            //add albums to list

            albumList.add(new MediaItemCollection(itemId, context.getResources().getString(R.string.all_songs), null, null, itemCount, 0, ItemType.SONGS, MediaType.DEVICE_MEDIA_LIB, ItemType.ARTIST));

            do {
                albumList.add(new MediaItemCollection(Long.toString(albumListCursor.getLong(Item_ID_Column)), albumListCursor.getString(Item_Title_Column),
                        albumListCursor.getString(Item_Sub_Title_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : albumListCursor.getString(Item_Sub_Title_Column),
                        albumListCursor.getString(Item_Album_Art_Path_Column),
                        albumListCursor.getInt(Item_Count_Column), 0, ItemType.ALBUM, MediaType.DEVICE_MEDIA_LIB, ItemType.ARTIST));
            }
            while (albumListCursor.moveToNext());
        }
        if (albumListCursor != null) {
            albumListCursor.close();
        }
        return albumList;
    }

    public static ArrayList<String> getArtistsArtList(Context context, String itemId, String itemTitle) {
        System.gc();
        ArrayList<String> urlList = new ArrayList<>();
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.IS_MUSIC + "=1 AND ");
        where.append(MediaStore.Audio.Media.ARTIST_ID + "=?");

        String whereVal[] = {String.valueOf(itemId)};

        final String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor songListCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where.toString(), whereVal, orderBy);
        if (songListCursor != null && songListCursor.moveToFirst()) {

            int Album_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);

            do{
                String url = getAlbumArtByAlbumId(context, Long.toString(songListCursor.getLong(Album_ID_Column)));
                if(url != null){
                    urlList.add(url);
                }
            }while (songListCursor.moveToNext() && urlList.size() < 6);
        }
        if (songListCursor != null) {
            songListCursor.close();
        }
        return urlList;
    }

    public static ArrayList<? extends IMediaItem> getSongListOfArtistsAlbum(Context context, String parentId, String itemId) {
        System.gc();
        ArrayList<IMediaItem> songList = new ArrayList<>();

        String where = MediaStore.Audio.Media.IS_MUSIC + "=1 AND "+MediaStore.Audio.Media.ARTIST_ID+ "=? AND "+
                MediaStore.Audio.Media.ALBUM_ID + "=?";

        String whereVal[] = {String.valueOf(parentId), String.valueOf(itemId)};
        String orderBy = MediaStore.Audio.Media.ALBUM + " COLLATE NOCASE ASC";//MediaStore.Audio.Media.ALBUM_ID;

        Cursor songListCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, whereVal, orderBy);

        if (songListCursor != null && songListCursor.moveToFirst()) {

            int Song_Id_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);

            int Song_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);

            int Song_Display_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DISPLAY_NAME);

            int Song_Path_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);

            int Album_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);

            int Album_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);

            int Artist_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST_ID);

            int Artist_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);

            int Duration_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);

            int Date_Added_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);

            do{
                songList.add(new MediaItem(Long.toString(songListCursor.getLong(Song_Id_Column)),
                        songListCursor.getString(Song_Name_Column),
                        songListCursor.getString(Song_Display_Name_Column),
                        songListCursor.getString(Song_Path_Column),
                        Long.toString(songListCursor.getLong(Album_ID_Column)),
                        songListCursor.getString(Album_Name_Column),
                        Long.toString(songListCursor.getLong(Artist_ID_Column)),
                        songListCursor.getString(Artist_Name_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : songListCursor.getString(Artist_Name_Column),
                        songListCursor.getLong(Duration_Column),
                        songListCursor.getLong(Date_Added_Column),
                        null, ItemType.SONGS,
                        MediaType.DEVICE_MEDIA_LIB, ItemType.ARTIST,
                        parentId,
                        null));
            }while (songListCursor.moveToNext());
        }
        if (songListCursor != null) {
            songListCursor.close();
        }
        return songList;
    }

    public static ArrayList<? extends IMediaItem> getSongListOfArtist(Context context, String itemId, String itemTitle) {
        if(itemTitle.equalsIgnoreCase(context.getResources().getString(R.string.unknown_artist))){
            itemTitle = "<unknown>";
        }
        ArrayList<IMediaItem> songList = new ArrayList<>();
        System.gc();
        final String orderBy = MediaStore.Audio.Media.TITLE;
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.IS_MUSIC + "=1 AND ");
        String whereVal[] = new String[1];
        if(itemTitle.equals("item") || (itemTitle.equals("") || itemTitle.isEmpty())) {
            where.append(MediaStore.Audio.Media.ARTIST_ID + "=?");
            whereVal[0] = String.valueOf(itemId);
        }else {
            where.append(MediaStore.Audio.Media.ARTIST + "=?");
            whereVal[0] = itemTitle;
        }

        Cursor songListCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where.toString(), whereVal, orderBy);
        if (songListCursor != null && songListCursor.moveToFirst()) {
            int Song_Id_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);

            int Song_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);

            int Song_Display_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DISPLAY_NAME);

            int Song_Path_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);

            int Album_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);

            int Album_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);

            int Artist_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST_ID);

            int Artist_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);

            int Duration_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);

            int Date_Added_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);

            do{
                songList.add(new MediaItem(Long.toString(songListCursor.getLong(Song_Id_Column)),
                        songListCursor.getString(Song_Name_Column),
                        songListCursor.getString(Song_Display_Name_Column), songListCursor.getString(Song_Path_Column),
                        Long.toString(songListCursor.getLong(Album_ID_Column)),
                        songListCursor.getString(Album_Name_Column),
                        Long.toString(songListCursor.getLong(Artist_ID_Column)),
                        songListCursor.getString(Artist_Name_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : songListCursor.getString(Artist_Name_Column),
                        songListCursor.getLong(Duration_Column), songListCursor.getLong(Date_Added_Column),
                        null, ItemType.SONGS,
                        MediaType.DEVICE_MEDIA_LIB, ItemType.ARTIST,
                        itemId, null));
            }while (songListCursor.moveToNext());
        }
        if (songListCursor != null) {
            songListCursor.close();
        }
        return songList;
    }


/*****************************************************Playlist Query****************************************************************/

    public static ArrayList<MediaItemCollection> getPlayList(Context context) {
        //Get a cursor of all genres in MediaStore.

        ArrayList<MediaItemCollection> playList = new ArrayList<MediaItemCollection>();
        String orderBy = MediaStore.Audio.Playlists.NAME;
        Cursor playListCursor = context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME }, null, null, orderBy);

        //Iterate thru all playlist in MediaStore.
        if(playListCursor.getCount()>0) {
            playListCursor.moveToFirst();
           do{
               int count = 0;
               try {
                   count = makePlayListSongCursor(context, Long.toString(playListCursor.getLong(playListCursor.getColumnIndex(MediaStore.Audio.Playlists._ID)))).getCount();
               }catch (Exception e){}

               playList.add(new MediaItemCollection(
                       Long.toString(playListCursor.getLong(playListCursor.getColumnIndex(MediaStore.Audio.Playlists._ID))),
                       playListCursor.getString(playListCursor.getColumnIndex(MediaStore.Audio.Playlists.NAME)),
                       null, null, count, 0, ItemType.PLAYLIST, MediaType.DEVICE_MEDIA_LIB, ItemType.PLAYLIST));
           }while (playListCursor.moveToNext());
        }
        return playList;
    }

    public static ArrayList<String> getPlaylistArtList(Context context, String itemId, String itemTitle) {
        Cursor songListCursor = makePlayListSongCursor(context, itemId);
        ArrayList<String> urlList = new ArrayList<>();
        // Gather the data
        if (songListCursor != null && songListCursor.moveToFirst()) {

            do {
                // Copy the album name
                String url = getAlbumArtByAlbum(context, songListCursor.getString(3));
                if (url != null) {
                    urlList.add(url);
                }
            } while (songListCursor.moveToNext() && urlList.size() < 6);
        }
        return urlList;
    }

    private static Cursor makePlayListSongCursor(Context context, String playlistId) {
        // Match the songs up with the playList
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Playlists.Members.IS_MUSIC + "=1");
        selection.append(" AND " + MediaStore.Audio.Playlists.Members.TITLE + "!=''"); //$NON-NLS-2$
        return context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", Long.parseLong(playlistId)), new String[] {
                        /* 0 */
                        MediaStore.Audio.Playlists.Members._ID,
                        /* 1 */
                        MediaStore.Audio.Playlists.Members.TITLE,
                        /* 2 */
                        MediaStore.Audio.Playlists.Members.ALBUM_ID,
                        /* 3 */
                        MediaStore.Audio.Playlists.Members.ALBUM,
                        /* 4 */
                        MediaStore.Audio.Playlists.Members.ARTIST

                }, selection.toString(), null, MediaStore.Audio.Playlists.Members.PLAY_ORDER);
    }

    public static ArrayList<? extends IMediaItem> getPlaylistSongs(Context context, String itemId, String itemTitle) {
//        returns all the songs of playlist
        System.gc();
        ArrayList<IMediaItem> songList = new ArrayList<>();

        String where = MediaStore.Audio.Playlists.Members.IS_MUSIC + "=1 AND "+MediaStore.Audio.Playlists.Members.TITLE + "!=''";

        String whereVal[] = {String.valueOf(itemId)};
        String orderBy = MediaStore.Audio.Playlists.Members.TITLE+ " COLLATE NOCASE ASC";

        Cursor songListCursor = context.getContentResolver().query(MediaStore.Audio.Playlists.Members.getContentUri("external", Long.parseLong(itemId)),
                null, where, null, orderBy);

        if (songListCursor != null && songListCursor.moveToFirst()) {

            int Song_Id_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);

            int Song_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);

            int Song_Display_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DISPLAY_NAME);

            int Song_Path_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);

            int Album_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);

            int Album_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);

            int Artist_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST_ID);

            int Artist_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);

            int Duration_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);

            int Date_Added_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);

            do{
                songList.add(new MediaItem(
                        Long.toString(songListCursor.getLong(Song_Id_Column)),
                        songListCursor.getString(Song_Name_Column),
                        songListCursor.getString(Song_Display_Name_Column), songListCursor.getString(Song_Path_Column),
                        Long.toString(songListCursor.getLong(Album_ID_Column)),
                        songListCursor.getString(Album_Name_Column),
                        Long.toString(songListCursor.getLong(Artist_ID_Column)),
                        songListCursor.getString(Artist_Name_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : songListCursor.getString(Artist_Name_Column),
                        songListCursor.getLong(Duration_Column),
                        songListCursor.getLong(Date_Added_Column),
                        null, ItemType.SONGS, MediaType.DEVICE_MEDIA_LIB, ItemType.PLAYLIST,
                        itemId, null));
            }while (songListCursor.moveToNext());
        }
        if (songListCursor != null) {
            songListCursor.close();
        }
        return songList;
    }

/*****************************************************Genre Query****************************************************************/

    public static ArrayList<MediaItemCollection> getGenreList(Context context) {
        //Get a cursor of all genres in MediaStore.
        ArrayList<MediaItemCollection> genreList = new ArrayList<MediaItemCollection>();
        String orderBy = MediaStore.Audio.Genres.NAME;
        Cursor genreListCursor = context.getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME }, null, null, orderBy);

        //Iterate thru all genres in MediaStore.
        if(genreListCursor.getCount()>0) {
            genreListCursor.moveToFirst();

            do {

                Cursor genreSongCursor = makeGenreSongCursor(context, genreListCursor.getLong(genreListCursor.getColumnIndex(MediaStore.Audio.Genres._ID)));
                // Gather the data
                if (genreSongCursor != null && genreSongCursor.moveToFirst()) {

                    Long genreId = genreListCursor.getLong(genreListCursor.getColumnIndex(MediaStore.Audio.Genres._ID));
                    int genreSongCount = 0;
                    try{
                        genreSongCount = genreSongCursor.getCount();
                    }catch (Exception e){}

                    int genreAlbumCount = 0;
                    try{
                        genreAlbumCount = getGenreAlbumCursor(context, Long.toString(genreId)).getCount();
                    }catch (Exception e){}

                    genreList.add(new MediaItemCollection(Long.toString(genreId), genreListCursor.getString(genreListCursor.getColumnIndex(MediaStore.Audio.Genres.NAME)),
                            genreSongCursor.getString(3), /*getAlbumArtByAlbum(context, genreSongCursor.getString(3))*/ null, genreSongCount, genreAlbumCount,
                            ItemType.GENRE, MediaType.DEVICE_MEDIA_LIB, ItemType.GENRE));
                }
                // Close the cursor
                if (genreSongCursor != null) {
                    genreSongCursor.close();
                }
            } while (genreListCursor.moveToNext());
        }
        if (genreListCursor != null) {
            genreListCursor.close();
        }
        return genreList;
    }
    
    private static final Cursor makeGenreSongCursor(final Context context, final Long genreId) {
        // Match the songs up with the genre
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Genres.Members.IS_MUSIC + "=1");
        selection.append(" AND " + MediaStore.Audio.Genres.Members.TITLE + "!=''"); //$NON-NLS-2$
        return context.getContentResolver().query(
                MediaStore.Audio.Genres.Members.getContentUri("external", genreId), new String[] {
                        /* 0 */
                        MediaStore.Audio.Genres.Members._ID,
                        /* 1 */
                        MediaStore.Audio.Genres.Members.TITLE,
                        /* 2 */
                        MediaStore.Audio.Genres.Members.ALBUM_ID,
                        /* 3 */
                        MediaStore.Audio.Genres.Members.ALBUM,
                        /* 4 */
                        MediaStore.Audio.Genres.Members.ARTIST
                }, selection.toString(), null, MediaStore.Audio.Genres.Members._ID);
    }

    private static Cursor getGenreAlbumCursor(Context context, String genreId){
        Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", Long.parseLong(genreId));
        String selection = MediaStore.Audio.Media.ALBUM + " IS NOT NULL) GROUP BY (" + MediaStore.Audio.Media.ALBUM;
        String orderBy = MediaStore.Audio.Media.ALBUM;
        return context.getContentResolver().query(uri, null, selection, null, orderBy);
    }

    public static ArrayList<? extends IMediaElement> getGenresAlbumDetails(Context context, String itemId, String itemTitle, int itemCount) {
        Cursor albumListCursor = getGenreAlbumCursor(context, itemId);
        ArrayList<MediaItemCollection> genreAlbumList = new ArrayList<MediaItemCollection>();

        int Item_ID_Column = albumListCursor.getColumnIndex
                (MediaStore.Audio.Albums.ALBUM_ID);

        int Item_Title_Column = albumListCursor.getColumnIndex
                (MediaStore.Audio.Albums.ALBUM);

        int Item_Sub_Title_Column = albumListCursor.getColumnIndex
                (MediaStore.Audio.Albums.ARTIST);

        int Item_count_Column = albumListCursor.getColumnIndex
                (MediaStore.Audio.Albums.NUMBER_OF_SONGS);

        if(albumListCursor != null && albumListCursor.moveToFirst()){
            genreAlbumList.add(new MediaItemCollection(itemId, context.getResources().getString(R.string.all_songs), null, null, itemCount, 0, ItemType.SONGS, MediaType.DEVICE_MEDIA_LIB, ItemType.GENRE));
            do{
                String albumTitle = albumListCursor.getString(Item_Title_Column);
                int count =0;
                try{
                    count = getSongOfGenreAlbumCursor(context, itemId, albumTitle).getCount();
                }catch (NullPointerException e){

                }

                genreAlbumList.add(new MediaItemCollection(Long.toString(albumListCursor.getLong(Item_ID_Column)), albumTitle,
                        albumListCursor.getString(Item_Sub_Title_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : albumListCursor.getString(Item_Sub_Title_Column),
                        null, count, 0, ItemType.ALBUM, MediaType.DEVICE_MEDIA_LIB, ItemType.GENRE));

            }while (albumListCursor.moveToNext());
            // Close the cursor
            if (albumListCursor != null) {
                albumListCursor.close();
            }
        }
        return genreAlbumList;
    }

    public static ArrayList<String> getGenreArtList(Context context, String itemId, String itemTitle) {
        ArrayList<String> artList = new ArrayList<String>();
        System.gc();
        StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Genres.Members.IS_MUSIC + "=1");
        selection.append(" AND " + MediaStore.Audio.Genres.Members.TITLE + "!=''"); //$NON-NLS-2$
        Cursor songListCursor = context.getContentResolver().query(
                MediaStore.Audio.Genres.Members.getContentUri("external", Long.parseLong(itemId)), null, selection.toString(), null, MediaStore.Audio.Genres.Members._ID);

        if (songListCursor != null && songListCursor.moveToFirst()) {

            int Album_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);

            do{
                String url = getAlbumArtByAlbumId(context, Long.toString(songListCursor.getLong(Album_ID_Column)));
                if(url != null){
                    artList.add(url);
                }
            }while (songListCursor.moveToNext() && artList.size() < 6);

        }
        if (songListCursor != null) {
            songListCursor.close();
        }
        return artList;
    }

    public static ArrayList<? extends IMediaItem> getSongListOfGenreAlbum(Context context, String parentId, String parentTitle, String itemId, String itemTitle) {
        ArrayList<IMediaItem> songList = new ArrayList<>();
        Cursor songListCursor = getSongOfGenreAlbumCursor(context, parentId, itemTitle);
        int Song_Id_Column = songListCursor.getColumnIndex
                (MediaStore.Audio.Media._ID);

        int Song_Name_Column = songListCursor.getColumnIndex
                (MediaStore.Audio.Media.TITLE);

        int Song_Display_Name_Column = songListCursor.getColumnIndex
                (MediaStore.Audio.Media.DISPLAY_NAME);

        int Song_Path_Column = songListCursor.getColumnIndex
                (MediaStore.Audio.Media.DATA);

        int Album_ID_Column = songListCursor.getColumnIndex
                (MediaStore.Audio.Media.ALBUM_ID);

        int Album_Name_Column = songListCursor.getColumnIndex
                (MediaStore.Audio.Media.ALBUM);

        int Artist_ID_Column = songListCursor.getColumnIndex
                (MediaStore.Audio.Media.ARTIST_ID);

        int Artist_Name_Column = songListCursor.getColumnIndex
                (MediaStore.Audio.Media.ARTIST);

        int Duration_Column = songListCursor.getColumnIndex
                (MediaStore.Audio.Media.DURATION);

        int Date_Added_Column = songListCursor.getColumnIndex
                (MediaStore.Audio.Media.DATE_ADDED);
        if (songListCursor != null && songListCursor.moveToFirst()) {
            do {
                songList.add(new MediaItem(Long.toString(songListCursor.getLong(Song_Id_Column)),
                        songListCursor.getString(Song_Name_Column),
                        songListCursor.getString(Song_Display_Name_Column), songListCursor.getString(Song_Path_Column),
                        Long.toString(songListCursor.getLong(Album_ID_Column)),
                        songListCursor.getString(Album_Name_Column),
                        Long.toString(songListCursor.getLong(Artist_ID_Column)),
                        songListCursor.getString(Artist_Name_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : songListCursor.getString(Artist_Name_Column),
                        songListCursor.getLong(Duration_Column),
                        songListCursor.getLong(Date_Added_Column),
                        null, ItemType.SONGS, MediaType.DEVICE_MEDIA_LIB, ItemType.GENRE,
                        parentId, null));
            } while (songListCursor.moveToNext());
            if (songListCursor != null) {
                songListCursor.close();
            }
        }
        return songList;
    }

    private static Cursor getSongOfGenreAlbumCursor(Context context,  String genreId, final String album){
        Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", Long.parseLong(genreId));
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
                + MediaStore.Audio.Media.ALBUM + "=?";
        final String orderBy = MediaStore.Audio.Media.TITLE;

        return context.getContentResolver().query(uri, null, where, new String[]{album}, orderBy)/*.getCount()*/;
    }

    public static ArrayList<? extends IMediaItem> getSongListOfGenre(Context context, String itemId, String itemTitle) {
        //Get a cursor of all genres in MediaStore.
        ArrayList<IMediaItem> songList = new ArrayList<>();
        StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Genres.Members.IS_MUSIC + "=1");
        selection.append(" AND " + MediaStore.Audio.Genres.Members.TITLE + "!=''"); //$NON-NLS-2$
        Cursor songListCursor = context.getContentResolver().query(
                MediaStore.Audio.Genres.Members.getContentUri("external", Long.parseLong(itemId)), null, selection.toString(), null, MediaStore.Audio.Genres.Members._ID);

        if (songListCursor != null && songListCursor.moveToFirst()) {
            int Song_Id_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);

            int Song_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);

            int Song_Display_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DISPLAY_NAME);

            int Song_Path_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);

            int Album_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);

            int Album_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);

            int Artist_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST_ID);

            int Artist_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);

            int Duration_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);

            int Date_Added_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);

            do {
                songList.add(new MediaItem(Long.toString(songListCursor.getLong(Song_Id_Column)),
                        songListCursor.getString(Song_Name_Column),
                        songListCursor.getString(Song_Display_Name_Column), songListCursor.getString(Song_Path_Column),
                        Long.toString(songListCursor.getLong(Album_ID_Column)),
                        songListCursor.getString(Album_Name_Column),
                        Long.toString(songListCursor.getLong(Artist_ID_Column)),
                        songListCursor.getString(Artist_Name_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : songListCursor.getString(Artist_Name_Column),
                        songListCursor.getLong(Duration_Column), songListCursor.getLong(Date_Added_Column),
                        null, ItemType.SONGS, MediaType.DEVICE_MEDIA_LIB, ItemType.GENRE, itemId, null));
            } while (songListCursor.moveToNext());

            // Close the cursor
            if (songListCursor != null) {
                songListCursor.close();
            }
        }
        return songList;
    }

    /*********************************************Search Queries*******************************************************/

    public static SearchResult searchSong(Context context, String sQuery, boolean isPartial) {
        ArrayList<MediaItem> songList = new ArrayList<>();
        Cursor songListCursor = context.getContentResolver().
                query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.IS_MUSIC + "=? AND "
                                + MediaStore.Audio.Media.TITLE + " LIKE ?",
                        new String[] {"1", "%"+ sQuery+ "%" }, MediaStore.Audio.Media.TITLE);

        int count = 0;
        if (songListCursor != null && songListCursor.moveToFirst()) {

            int Song_Id_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);

            int Song_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);

            int Song_Display_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DISPLAY_NAME);

            int Song_Path_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);

            int Album_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);

            int Album_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);

            int Artist_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST_ID);

            int Artist_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);

            int Duration_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);

            int Date_Added_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);

            do{
                songList.add(new MediaItem(Long.toString(songListCursor.getLong(Song_Id_Column)),
                        songListCursor.getString(Song_Name_Column),
                        songListCursor.getString(Song_Display_Name_Column), songListCursor.getString(Song_Path_Column),
                        Long.toString(songListCursor.getLong(Album_ID_Column)), songListCursor.getString(Album_Name_Column),
                        Long.toString(songListCursor.getLong(Artist_ID_Column)), songListCursor.getString(Artist_Name_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : songListCursor.getString(Artist_Name_Column),
                        songListCursor.getLong(Duration_Column), songListCursor.getLong(Date_Added_Column),
                        null, ItemType.SONGS,
                        MediaType.DEVICE_MEDIA_LIB, ItemType.SONGS, "0", null));

                if(isPartial && count == 3){
                    break;
                }
                count++;
            }while (songListCursor.moveToNext());
        count = songListCursor.getCount();
        }
        if (songListCursor != null) {
            songListCursor.close();
        }
        return new SearchResult(songList, count);
    }

    public static SearchResult searchAlbum(Context context, String sQuery, boolean isPartial) {
        ArrayList<MediaItemCollection> albumList = new ArrayList<>();
        System.gc();
        Cursor albumListCursor = context.getContentResolver().
                query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Albums.ALBUM + " LIKE ?",
                        new String[] {"%"+ sQuery+ "%" }, MediaStore.Audio.Albums.ALBUM);

        int count = 0;
        if (albumListCursor != null && albumListCursor.moveToFirst()) {
            //get columns
            int Item_ID_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Albums._ID);

            int Item_Title_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ALBUM);

            int Item_Sub_Title_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ARTIST);

            int Item_Count_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Albums.NUMBER_OF_SONGS);

            int Item_Album_Art_Path_Column = albumListCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ALBUM_ART);

            //add albums to list
            do {
                if(albumListCursor.getInt(Item_Count_Column) > 0)
                    albumList.add(new MediaItemCollection(Long.toString(albumListCursor.getLong(Item_ID_Column)),
                            albumListCursor.getString(Item_Title_Column),
                            albumListCursor.getString(Item_Sub_Title_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : albumListCursor.getString(Item_Sub_Title_Column),
                            albumListCursor.getString(Item_Album_Art_Path_Column),
                            albumListCursor.getInt(Item_Count_Column), 0, ItemType.ALBUM, MediaType.DEVICE_MEDIA_LIB, ItemType.ALBUM));

                if(isPartial && count == 3){
                    break;
                }
                count++;
            } while (albumListCursor.moveToNext());
            count = albumListCursor.getCount();
        }
        if (albumListCursor != null) {
            albumListCursor.close();
        }
        return new SearchResult(albumList, count);
    }

    public static SearchResult searchArtist(Context context, String sQuery, boolean isPartial) {

        ArrayList<MediaItemCollection> artistList = new ArrayList<>();
        Cursor artistListCursor = context.getContentResolver().
                query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Artists.ARTIST + " LIKE ?",
                new String[] {"%"+ sQuery+ "%" }, MediaStore.Audio.Artists.ARTIST);

        int count = 0;
        if (artistListCursor != null && artistListCursor.moveToFirst()) {
            //get columns

            int Item_ID_Column = artistListCursor.getColumnIndex
                    (MediaStore.Audio.Artists._ID);

            int Item_Title_Column = artistListCursor.getColumnIndex
                    (MediaStore.Audio.Artists.ARTIST);

            int numOfAlbumsColumn = artistListCursor.getColumnIndex
                    (MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);

            int Item_Count_Column = artistListCursor.getColumnIndex
                    (MediaStore.Audio.Artists.NUMBER_OF_TRACKS);

            //add albums to list
            do {

                artistList.add(new MediaItemCollection(Long.toString(artistListCursor.getLong(Item_ID_Column)),
                        artistListCursor.getString(Item_Title_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : artistListCursor.getString(Item_Title_Column),
                        null, /*getAlbumArtByArtist(context, artistListCursor.getString(Item_Title_Column))*/ null,
                        artistListCursor.getInt(Item_Count_Column), artistListCursor.getInt(numOfAlbumsColumn), ItemType.ARTIST,
                        MediaType.DEVICE_MEDIA_LIB, ItemType.ARTIST));

                if(isPartial && count == 3){
                    break;
                }
                count++;
            } while (artistListCursor.moveToNext());
            count = artistListCursor.getCount();
        }
        if (artistListCursor != null) {
            artistListCursor.close();
        }
        return new SearchResult(artistList, count);
    }

}
