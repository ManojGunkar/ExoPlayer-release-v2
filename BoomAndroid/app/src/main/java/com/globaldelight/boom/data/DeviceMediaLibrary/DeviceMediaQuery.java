package com.globaldelight.boom.data.DeviceMediaLibrary;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.globaldelight.boom.App;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.handler.search.SearchResult;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.utils.handlers.UserPreferenceHandler;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 8/8/2016.
 */
public class DeviceMediaQuery {


    public static ArrayList<? extends IMediaItemBase> getSongList(Context context){
        Log.e("ToTaL_TiMe : ", "On_StArT : "+System.currentTimeMillis());
        ArrayList<MediaItem> songList = new ArrayList<>();

        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";

        final String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor songListCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, orderBy);
        Log.e("ToTaL_TiMe : ", "On_EnD : "+System.currentTimeMillis());
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
            int album_art = songListCursor.getColumnIndex
                    (MediaStore.Audio.AlbumColumns.ALBUM_ART);
            do{
                songList.add(new MediaItem(songListCursor.getLong(Song_Id_Column), songListCursor.getString(Song_Name_Column),
                        songListCursor.getString(Song_Display_Name_Column), songListCursor.getString(Song_Path_Column),
                        songListCursor.getLong(Album_ID_Column), songListCursor.getString(Album_Name_Column),
                        songListCursor.getLong(Artist_ID_Column), songListCursor.getString(Artist_Name_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : songListCursor.getString(Artist_Name_Column),
                        songListCursor.getLong(Duration_Column), songListCursor.getLong(Date_Added_Column),
                        /*getAlbumArtByAlbum(context, songListCursor.getString(Album_Name_Column))*/ null, ItemType.SONGS,
                        MediaType.DEVICE_MEDIA_LIB, ItemType.SONGS, 0));
            }while (songListCursor.moveToNext());

        }
        Log.e("ToTaL_TiMe : ", "On_EnD : "+System.currentTimeMillis());
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

    public static String getAlbumArtByAlbumId(Context context, long albumId) {
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

/*****************************************************Album Query****************************************************************/

    public static ArrayList<? extends IMediaItemBase> getAlbumList(Context context) {
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
                albumList.add(new MediaItemCollection(albumListCursor.getLong(Item_ID_Column),
                        albumListCursor.getString(Item_Title_Column),
                        albumListCursor.getString(Item_Sub_Title_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : albumListCursor.getString(Item_Sub_Title_Column),
                        albumListCursor.getString(Item_Album_Art_Path_Column),
                        albumListCursor.getInt(Item_Count_Column), 0,  ItemType.ALBUM, MediaType.DEVICE_MEDIA_LIB));
            }
            while (albumListCursor.moveToNext());
        }
        if (albumListCursor != null) {
            albumListCursor.close();
        }
        return albumList;
    }

    public static IMediaItemBase getAlbum(Context context, long id) {
        System.gc();
        MediaItemCollection album = null;
        final String where = MediaStore.Audio.Albums._ID+ "="+id;

        Cursor albumListCursor = context.getContentResolver().
                query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, where, null, null);

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

                if(albumListCursor.getInt(Item_Count_Column) > 0)
                    album = new MediaItemCollection(albumListCursor.getLong(Item_ID_Column),
                            albumListCursor.getString(Item_Title_Column),
                            albumListCursor.getString(Item_Sub_Title_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : albumListCursor.getString(Item_Sub_Title_Column),
                            albumListCursor.getString(Item_Album_Art_Path_Column),
                            albumListCursor.getInt(Item_Count_Column), 0,  ItemType.ALBUM, MediaType.DEVICE_MEDIA_LIB);
        }
        if (albumListCursor != null) {
            albumListCursor.close();
        }
        return album;
    }

    public static ArrayList<? extends IMediaItemBase> getAlbumDetail(Context context, long itemId, String itemTitle) {
        System.gc();
        ArrayList<MediaItem> songList = new ArrayList<>();

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
                songList.add(new MediaItem(songListCursor.getLong(Song_Id_Column), songListCursor.getString(Song_Title_Column),
                        songListCursor.getString(Song_Display_Name_Column), songListCursor.getString(Song_Path_Column),
                        songListCursor.getLong(Album_ID_Column), songListCursor.getString(Album_Name_Column), songListCursor.getLong(Artist_ID_Column),
                        songListCursor.getString(Artist_Name_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : songListCursor.getString(Artist_Name_Column),
                        songListCursor.getLong(Duration_Column), songListCursor.getLong(Date_Added_Column),
                        getAlbumArtByAlbum(context, songListCursor.getString(Album_Name_Column)), ItemType.SONGS, MediaType.DEVICE_MEDIA_LIB, ItemType.ALBUM, itemId));
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

                artistList.add(new MediaItemCollection(artistListCursor.getLong(Item_ID_Column),
                        artistListCursor.getString(Item_Title_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : artistListCursor.getString(Item_Title_Column),
                        null, /*getAlbumArtByArtist(context, artistListCursor.getString(Item_Title_Column))*/ null,
                        artistListCursor.getInt(Item_Count_Column), artistListCursor.getInt(numOfAlbumsColumn), ItemType.ARTIST,
                        MediaType.DEVICE_MEDIA_LIB));
            }
            while (artistListCursor.moveToNext());
        }
        if (artistListCursor != null) {
            artistListCursor.close();
        }
        return artistList;
    }

    public static IMediaItemBase getArtist(Context context, long id) {
        MediaItemCollection artist = null;
        final String where = MediaStore.Audio.Artists._ID+ "="+id;

        Cursor artistListCursor = context.getContentResolver().
                query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, where, null, null);

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

            artist = new MediaItemCollection(artistListCursor.getLong(Item_ID_Column),
                        artistListCursor.getString(Item_Title_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : artistListCursor.getString(Item_Title_Column),
                        null, getAlbumArtByArtist(context, artistListCursor.getString(Item_Title_Column)),
                        artistListCursor.getInt(Item_Count_Column), artistListCursor.getInt(numOfAlbumsColumn), ItemType.ARTIST,
                        MediaType.DEVICE_MEDIA_LIB);
        }
        if (artistListCursor != null) {
            artistListCursor.close();
        }
        return artist;
    }

    public static ArrayList<? extends IMediaItemBase> getArtistsAlbumDetails(Context context, long itemId, String itemTitle, int itemCount) {
        final ArrayList<MediaItemCollection> albumList = new ArrayList<>();
        System.gc();
        Cursor albumListCursor = context.getContentResolver().
                query(MediaStore.Audio.Artists.Albums.getContentUri("external", itemId),
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

            albumList.add(new MediaItemCollection(itemId, context.getResources().getString(R.string.all_songs), null, null, itemCount, 0, ItemType.SONGS, MediaType.DEVICE_MEDIA_LIB));

            do {
                albumList.add(new MediaItemCollection(albumListCursor.getLong(Item_ID_Column), albumListCursor.getString(Item_Title_Column),
                        albumListCursor.getString(Item_Sub_Title_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : albumListCursor.getString(Item_Sub_Title_Column),
                        albumListCursor.getString(Item_Album_Art_Path_Column),
                        albumListCursor.getInt(Item_Count_Column), 0, ItemType.ALBUM, MediaType.DEVICE_MEDIA_LIB));
            }
            while (albumListCursor.moveToNext());
        }
        if (albumListCursor != null) {
            albumListCursor.close();
        }
        return albumList;
    }

    public static int getItemSongCount(Context context, String itemName){
        int count = 0;
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Media.IS_MUSIC + "=1");
        selection.append(" AND " + MediaStore.Audio.Artists.ARTIST + "='" + itemName.replace("'", "''") + "'");
        Cursor countCursor = context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Artists.NUMBER_OF_TRACKS}, selection.toString(), null, null);
        if(countCursor != null)
            count = Integer.parseInt(countCursor.getString(0));
        countCursor.close();
        return count;
    }

    public static ArrayList<String> getArtistsArtList(Context context, long itemId, String itemTitle) {
        System.gc();
        ArrayList<String> urlList = new ArrayList<>();
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.IS_MUSIC + "=1 AND ");
        where.append(MediaStore.Audio.Media.ARTIST_ID + "='" + itemId + "'");

        final String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor songListCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where.toString(), null, orderBy);
        if (songListCursor != null && songListCursor.moveToFirst()) {

            int Album_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);

            do{
                String url = getAlbumArtByAlbumId(context, songListCursor.getLong(Album_ID_Column));
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

    public static ArrayList<? extends IMediaItemBase> getSongListOfArtistsAlbum(Context context, long parentId, String parentTitle, long itemId, String itemTitle) {
        System.gc();
        ArrayList<MediaItem> songList = new ArrayList<>();

        String where = MediaStore.Audio.Media.IS_MUSIC + "=1 AND "+MediaStore.Audio.Media.ARTIST_ID+ "="+parentId+" AND "+
                MediaStore.Audio.Media.ALBUM_ID + "="+itemId;

//        String whereVal[] = { album};
        String orderBy = MediaStore.Audio.Media.ALBUM + " COLLATE NOCASE ASC";//MediaStore.Audio.Media.ALBUM_ID;

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
                songList.add(new MediaItem(songListCursor.getLong(Song_Id_Column), songListCursor.getString(Song_Name_Column),
                        songListCursor.getString(Song_Display_Name_Column), songListCursor.getString(Song_Path_Column),
                        songListCursor.getLong(Album_ID_Column), songListCursor.getString(Album_Name_Column),
                        songListCursor.getLong(Artist_ID_Column),
                        songListCursor.getString(Artist_Name_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : songListCursor.getString(Artist_Name_Column),
                        songListCursor.getLong(Duration_Column), songListCursor.getLong(Date_Added_Column),
                        null, ItemType.SONGS,
                        MediaType.DEVICE_MEDIA_LIB, ItemType.ARTIST, parentId));
            }while (songListCursor.moveToNext());
        }
        if (songListCursor != null) {
            songListCursor.close();
        }
        return songList;
    }

    public static ArrayList<? extends IMediaItemBase> getSongListOfArtist(Context context, long itemId, String itemTitle) {
        if(itemTitle.equalsIgnoreCase(context.getResources().getString(R.string.unknown_artist))){
            itemTitle = "<unknown>";
        }
        ArrayList<MediaItem> songList = new ArrayList<>();
        System.gc();
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media.IS_MUSIC + "=1 AND ");
        if(itemTitle.equals("item") || (itemTitle.equals("") || itemTitle.isEmpty())) {
            where.append(MediaStore.Audio.Media.ARTIST_ID + "='" + itemId + "'");
        }else {
            where.append(MediaStore.Audio.Media.ARTIST + "='" + itemTitle.replace("'", "''") + "'");
        }
        final String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor songListCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where.toString(), null, orderBy);
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
                songList.add(new MediaItem(songListCursor.getLong(Song_Id_Column), songListCursor.getString(Song_Name_Column),
                        songListCursor.getString(Song_Display_Name_Column), songListCursor.getString(Song_Path_Column),
                        songListCursor.getLong(Album_ID_Column), songListCursor.getString(Album_Name_Column),
                        songListCursor.getLong(Artist_ID_Column),
                        songListCursor.getString(Artist_Name_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : songListCursor.getString(Artist_Name_Column),
                        songListCursor.getLong(Duration_Column), songListCursor.getLong(Date_Added_Column),
                        null, ItemType.SONGS,
                        MediaType.DEVICE_MEDIA_LIB, ItemType.ARTIST, itemId));
            }while (songListCursor.moveToNext());
        }
        if (songListCursor != null) {
            songListCursor.close();
        }
        return songList;
    }

    //    get art url of the ic_artist
    public static String getAlbumArtByArtist(Context context, String artist) {
        if(artist.equalsIgnoreCase(context.getResources().getString(R.string.unknown_artist))){
            artist = "<unknown>";
        }
        final String where = MediaStore.Audio.Media.ARTIST + "=?";

        Cursor albumListCursor = context.getContentResolver().
                query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Albums.ALBUM_ART}, where, new String[]{artist}, null);

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
                   count = makePlayListSongCursor(context, playListCursor.getLong(playListCursor.getColumnIndex(MediaStore.Audio.Playlists._ID))).getCount();
               }catch (Exception e){}

               playList.add(new MediaItemCollection(
                       playListCursor.getLong(playListCursor.getColumnIndex(MediaStore.Audio.Playlists._ID)),
                       playListCursor.getString(playListCursor.getColumnIndex(MediaStore.Audio.Playlists.NAME)),
                       null, null, count, 0, ItemType.PLAYLIST, MediaType.DEVICE_MEDIA_LIB));
           }while (playListCursor.moveToNext());
        }
        return playList;
    }

    public static IMediaItemBase getPlaylistItem(Context context, long id) {
        //Get a cursor of all genres in MediaStore.
        MediaItemCollection playlist = null;
        final String where = MediaStore.Audio.Playlists._ID+ "="+id;
        Cursor playListCursor = context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Playlists._ID, MediaStore.Audio.Playlists.NAME }, where, null, null);

        //Iterate thru all playlist in MediaStore.
        if(playListCursor.getCount()>0) {
            playListCursor.moveToFirst();
                int count = 0;
                try {
                    count = makePlayListSongCursor(context, playListCursor.getLong(playListCursor.getColumnIndex(MediaStore.Audio.Playlists._ID))).getCount();
                }catch (Exception e){}

                playlist = new MediaItemCollection(
                        playListCursor.getLong(playListCursor.getColumnIndex(MediaStore.Audio.Playlists._ID)),
                        playListCursor.getString(playListCursor.getColumnIndex(MediaStore.Audio.Playlists.NAME)),
                        null, null, count, 0, ItemType.PLAYLIST, MediaType.DEVICE_MEDIA_LIB);
        }
        return playlist;
    }

    public static ArrayList<String> getPlaylistArtList(Context context, long itemId, String itemTitle) {
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

    private static Cursor makePlayListSongCursor(Context context, long playlistId) {
        // Match the songs up with the playList
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Playlists.Members.IS_MUSIC + "=1");
        selection.append(" AND " + MediaStore.Audio.Playlists.Members.TITLE + "!=''"); //$NON-NLS-2$
        return context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId), new String[] {
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

    public static ArrayList<? extends IMediaItemBase> getPlaylistSongs(Context context, long itemId, String itemTitle) {
//        returns all the songs of playlist
        System.gc();
        ArrayList<MediaItem> songList = new ArrayList<>();

        String where = MediaStore.Audio.Playlists.Members.IS_MUSIC + "=1 AND "+MediaStore.Audio.Playlists.Members.TITLE + "!=''";

        String whereVal[] = {String.valueOf(itemId)};
        String orderBy = MediaStore.Audio.Playlists.Members.TITLE+ " COLLATE NOCASE ASC";

        Cursor songListCursor = context.getContentResolver().query(MediaStore.Audio.Playlists.Members.getContentUri("external", itemId),
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
                songList.add(new MediaItem(songListCursor.getLong(Song_Id_Column), songListCursor.getString(Song_Name_Column),
                        songListCursor.getString(Song_Display_Name_Column), songListCursor.getString(Song_Path_Column),
                        songListCursor.getLong(Album_ID_Column), songListCursor.getString(Album_Name_Column),
                        songListCursor.getLong(Artist_ID_Column), songListCursor.getString(Artist_Name_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : songListCursor.getString(Artist_Name_Column),
                        songListCursor.getLong(Duration_Column), songListCursor.getLong(Date_Added_Column),
                        null, ItemType.SONGS, MediaType.DEVICE_MEDIA_LIB, ItemType.PLAYLIST, itemId));
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
                        genreAlbumCount = getGenreAlbumCursor(context, genreId).getCount();
                    }catch (Exception e){}

                    genreList.add(new MediaItemCollection(genreId, genreListCursor.getString(genreListCursor.getColumnIndex(MediaStore.Audio.Genres.NAME)),
                            genreSongCursor.getString(3), /*getAlbumArtByAlbum(context, genreSongCursor.getString(3))*/ null, genreSongCount, genreAlbumCount,
                            ItemType.GENRE, MediaType.DEVICE_MEDIA_LIB));
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

    public static IMediaItemBase getGenre(Context context, long id) {
        MediaItemCollection genre = null;
        final String where = MediaStore.Audio.Genres._ID+ "="+id;
        Cursor genreListCursor = context.getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME }, where, null, null);

        //Iterate thru all genres in MediaStore.
        if(genreListCursor.getCount()>0) {
            genreListCursor.moveToFirst();

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
                        genreAlbumCount = getGenreAlbumCursor(context, genreId).getCount();
                    }catch (Exception e){}

                    genre = new MediaItemCollection(genreId, genreListCursor.getString(genreListCursor.getColumnIndex(MediaStore.Audio.Genres.NAME)),
                            genreSongCursor.getString(3), getAlbumArtByAlbum(context, genreSongCursor.getString(3)), genreSongCount, genreAlbumCount,
                            ItemType.GENRE, MediaType.DEVICE_MEDIA_LIB);
                }
                // Close the cursor
                if (genreSongCursor != null) {
                    genreSongCursor.close();
                }
        }
        if (genreListCursor != null) {
            genreListCursor.close();
        }
        return genre;
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

    private static Cursor getGenreAlbumCursor(Context context, long genreId){
        Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId);
        String selection = MediaStore.Audio.Media.ALBUM + " IS NOT NULL) GROUP BY (" + MediaStore.Audio.Media.ALBUM;
        String orderBy = MediaStore.Audio.Media.ALBUM;
        return context.getContentResolver().query(uri, null, selection, null, orderBy);
    }

    public static ArrayList<? extends IMediaItemBase> getGenresAlbumDetails(Context context, long itemId, String itemTitle, int itemCount) {
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
            genreAlbumList.add(new MediaItemCollection(itemId, context.getResources().getString(R.string.all_songs), null, null, itemCount, 0, ItemType.SONGS, MediaType.DEVICE_MEDIA_LIB));
            do{
                String albumTitle = albumListCursor.getString(Item_Title_Column);
                int count =0;
                try{
                    count = getSongOfGenreAlbumCursor(context, itemId, albumTitle).getCount();
                }catch (NullPointerException e){

                }

                genreAlbumList.add(new MediaItemCollection(albumListCursor.getLong(Item_ID_Column), albumTitle,
                        albumListCursor.getString(Item_Sub_Title_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : albumListCursor.getString(Item_Sub_Title_Column),
                        null, count, 0, ItemType.ALBUM, MediaType.DEVICE_MEDIA_LIB ));

            }while (albumListCursor.moveToNext());
            // Close the cursor
            if (albumListCursor != null) {
                albumListCursor.close();
            }
        }
        return genreAlbumList;
    }

    public static ArrayList<String> getGenreArtList(Context context, long itemId, String itemTitle) {
        ArrayList<String> artList = new ArrayList<String>();
        System.gc();
        StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Genres.Members.IS_MUSIC + "=1");
        selection.append(" AND " + MediaStore.Audio.Genres.Members.TITLE + "!=''"); //$NON-NLS-2$
        Cursor songListCursor = context.getContentResolver().query(
                MediaStore.Audio.Genres.Members.getContentUri("external", itemId), null, selection.toString(), null, MediaStore.Audio.Genres.Members._ID);

        if (songListCursor != null && songListCursor.moveToFirst()) {

            int Album_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);

            do{
                String url = getAlbumArtByAlbumId(context, songListCursor.getLong(Album_ID_Column));
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

    public static ArrayList<? extends IMediaItemBase> getSongListOfGenreAlbum(Context context, long parentId, String parentTitle, long itemId, String itemTitle) {
        ArrayList<MediaItem> songList = new ArrayList<>();
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
                songList.add(new MediaItem(songListCursor.getLong(Song_Id_Column), songListCursor.getString(Song_Name_Column),
                        songListCursor.getString(Song_Display_Name_Column), songListCursor.getString(Song_Path_Column),
                        songListCursor.getLong(Album_ID_Column), songListCursor.getString(Album_Name_Column),
                        songListCursor.getLong(Artist_ID_Column), songListCursor.getString(Artist_Name_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : songListCursor.getString(Artist_Name_Column),
                        songListCursor.getLong(Duration_Column), songListCursor.getLong(Date_Added_Column),
                        null, ItemType.SONGS, MediaType.DEVICE_MEDIA_LIB, ItemType.GENRE, parentId));
            } while (songListCursor.moveToNext());
            if (songListCursor != null) {
                songListCursor.close();
            }
        }
        return songList;
    }

    private static Cursor getSongOfGenreAlbumCursor(Context context, final Long genreId, final String album){
        Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId);
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
                + MediaStore.Audio.Media.ALBUM + "=" + "'"+album+"'";
        final String orderBy = MediaStore.Audio.Media.TITLE;

        return context.getContentResolver().query(uri, null, where, null, orderBy)/*.getCount()*/;
    }

    public static ArrayList<? extends IMediaItemBase> getSongListOfGenre(Context context, long itemId, String itemTitle) {
        //Get a cursor of all genres in MediaStore.
        ArrayList<MediaItem> songList = new ArrayList<>();
        StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Genres.Members.IS_MUSIC + "=1");
        selection.append(" AND " + MediaStore.Audio.Genres.Members.TITLE + "!=''"); //$NON-NLS-2$
        Cursor songListCursor = context.getContentResolver().query(
                MediaStore.Audio.Genres.Members.getContentUri("external", itemId), null, selection.toString(), null, MediaStore.Audio.Genres.Members._ID);

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
                songList.add(new MediaItem(songListCursor.getLong(Song_Id_Column), songListCursor.getString(Song_Name_Column),
                        songListCursor.getString(Song_Display_Name_Column), songListCursor.getString(Song_Path_Column),
                        songListCursor.getLong(Album_ID_Column), songListCursor.getString(Album_Name_Column),
                        songListCursor.getLong(Artist_ID_Column), songListCursor.getString(Artist_Name_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : songListCursor.getString(Artist_Name_Column),
                        songListCursor.getLong(Duration_Column), songListCursor.getLong(Date_Added_Column),
                        null, ItemType.SONGS, MediaType.DEVICE_MEDIA_LIB, ItemType.GENRE, itemId));
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
                songList.add(new MediaItem(songListCursor.getLong(Song_Id_Column), songListCursor.getString(Song_Name_Column),
                        songListCursor.getString(Song_Display_Name_Column), songListCursor.getString(Song_Path_Column),
                        songListCursor.getLong(Album_ID_Column), songListCursor.getString(Album_Name_Column),
                        songListCursor.getLong(Artist_ID_Column), songListCursor.getString(Artist_Name_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : songListCursor.getString(Artist_Name_Column),
                        songListCursor.getLong(Duration_Column), songListCursor.getLong(Date_Added_Column),
                        null, ItemType.SONGS,
                        MediaType.DEVICE_MEDIA_LIB, ItemType.SONGS, 0));

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
                    albumList.add(new MediaItemCollection(albumListCursor.getLong(Item_ID_Column),
                            albumListCursor.getString(Item_Title_Column),
                            albumListCursor.getString(Item_Sub_Title_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : albumListCursor.getString(Item_Sub_Title_Column),
                            albumListCursor.getString(Item_Album_Art_Path_Column),
                            albumListCursor.getInt(Item_Count_Column), 0,  ItemType.ALBUM, MediaType.DEVICE_MEDIA_LIB));

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

                artistList.add(new MediaItemCollection(artistListCursor.getLong(Item_ID_Column),
                        artistListCursor.getString(Item_Title_Column).equalsIgnoreCase("<unknown>") ? context.getResources().getString(R.string.unknown_artist) : artistListCursor.getString(Item_Title_Column),
                        null, /*getAlbumArtByArtist(context, artistListCursor.getString(Item_Title_Column))*/ null,
                        artistListCursor.getInt(Item_Count_Column), artistListCursor.getInt(numOfAlbumsColumn), ItemType.ARTIST,
                        MediaType.DEVICE_MEDIA_LIB));

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
