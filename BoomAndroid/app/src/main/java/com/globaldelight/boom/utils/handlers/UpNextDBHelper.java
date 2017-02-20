package com.globaldelight.boom.utils.handlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.globaldelight.boom.Media.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.Media.ItemType;
import com.globaldelight.boom.Media.MediaType;
import com.globaldelight.boom.handler.PlayingQueue.QueueType;

import java.util.ArrayList;
import java.util.Collection;
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

    public synchronized void insertUnShuffledList(IMediaItemBase item, QueueType queueType, boolean isAppend) {
        if(!isAppend)
            clearList(queueType);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.putNull(SONG_KEY_ID);
        values.put(SONG_KEY_REAL_ID, item.getItemId());
        values.put(TITLE, item.getItemTitle());
        values.put(DISPLAY_NAME, ((IMediaItem)item).getItemDisplayName());
        values.put(DATA_PATH, ((IMediaItem)item).getItemUrl());
        values.put(ALBUM_ID, ((IMediaItem)item).getItemAlbumId());
        values.put(ALBUM, ((IMediaItem)item).getItemAlbum());
        values.put(ARTIST_ID, ((IMediaItem)item).getItemArtistId());
        values.put(ARTIST, ((IMediaItem)item).getItemArtist());
        values.put(DURATION, ((IMediaItem)item).getDurationLong());
        values.put(DATE_ADDED, ((IMediaItem)item).getDateAdded());
        values.put(ALBUM_ART, ((IMediaItem)item).getItemArtUrl());
        values.put(ITEM_TYPE, ((IMediaItem)item).getItemType().ordinal());
        values.put(MEDIA_TYPE, ((IMediaItem)item).getMediaType().ordinal());
        values.put(PARENT_TYPE, ((IMediaItem)item).getParentType().ordinal());
        values.put(PARENT_ID, ((IMediaItem)item).getParentId());
        values.put(PARENT_TITLE, ((IMediaItem)item).getParentTitle());
        values.put(QUEUE_TYPE, queueType.ordinal());
        db.insert(TABLE_UPNEXT, null, values);
        db.close();
    }

    public synchronized void insertUnShuffledList(List<? extends IMediaItemBase> songs, QueueType queueType, boolean isAppend) {
        if(!isAppend)
            clearList(queueType);
//        Collections.reverse(songs);
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            for (int i = 0; i < songs.size(); i++) {
                final MediaItem item = (MediaItem) songs.get(i);
                ContentValues values = new ContentValues();

                values.putNull(SONG_KEY_ID);
                values.put(SONG_KEY_REAL_ID, item.getItemId());
                values.put(TITLE, item.getItemTitle());
                values.put(DISPLAY_NAME, item.getItemDisplayName());
                values.put(DATA_PATH, item.getItemUrl());
                values.put(ALBUM_ID, item.getItemAlbumId());
                values.put(ALBUM, item.getItemAlbum());
                values.put(ARTIST_ID, item.getItemArtistId());
                values.put(ARTIST, item.getItemArtist());
                values.put(DURATION, item.getDurationLong());
                values.put(DATE_ADDED, item.getDateAdded());
                values.put(ALBUM_ART, item.getItemArtUrl());
                values.put(ITEM_TYPE, item.getItemType().ordinal());
                values.put(MEDIA_TYPE, item.getMediaType().ordinal());
                values.put(PARENT_TYPE, item.getParentType().ordinal());
                values.put(PARENT_ID, item.getParentId());
                values.put(PARENT_TITLE, item.getParentTitle());
                values.put(QUEUE_TYPE, queueType.ordinal());

                db.insert(TABLE_UPNEXT, null, values);
            }
            db.close();
        }catch (ConcurrentModificationException e){
            e.printStackTrace();
        }catch (IllegalStateException e){
            e.printStackTrace();
        }
    }

    public synchronized void addSongsToUpNext(ArrayList<? extends IMediaItemBase> songs, QueueType queueType) {
        clearList(queueType);
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
            values.put(ITEM_TYPE, ((IMediaItem)songs.get(i)).getItemType().ordinal());
            values.put(MEDIA_TYPE, ((IMediaItem)songs.get(i)).getMediaType().ordinal());
            values.put(PARENT_TYPE, ((IMediaItem)songs.get(i)).getParentType().ordinal());
            values.put(PARENT_ID, ((IMediaItem)songs.get(i)).getParentId());
            values.put(PARENT_TITLE, ((IMediaItem)songs.get(i)).getParentTitle());
            values.put(QUEUE_TYPE, queueType.ordinal());

            db.insert(TABLE_UPNEXT, null, values);
        }
        db.close();
    }

/*History Table List*/

    public synchronized void addSong(IMediaItemBase song, QueueType queueType) {
        if(queueType == QueueType.History){
            removeSong(song.getItemId(), queueType);
        }
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
        values.put(QUEUE_TYPE, queueType.ordinal());

        db.insert(TABLE_UPNEXT, null, values);
        db.close();
    }

    public synchronized ArrayList<? extends IMediaItemBase> getUpNextSongs(QueueType queueType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<MediaItem> songList = new ArrayList<>();
        String query = "SELECT  * FROM " + TABLE_UPNEXT + " WHERE " +
                QUEUE_TYPE + "='" + queueType.ordinal() + "'";

        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String duration = cursor.getString(9);
                    String dateAdded = cursor.getString(10);

                    songList.add(new MediaItem(cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5),
                            cursor.getString(6), cursor.getInt(7), cursor.getString(8), Long.parseLong(duration),
                            Long.parseLong(dateAdded), cursor.getString(11), ItemType.fromOrdinal(cursor.getInt(12)), MediaType.fromOrdinal(cursor.getInt(13)), ItemType.fromOrdinal(cursor.getInt(14)), cursor.getInt(15), cursor.getString(16)));
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

    public synchronized void removeSong(long songId, QueueType queueType) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_UPNEXT + " WHERE " +
                SONG_KEY_REAL_ID + "='" + songId + "' AND "+QUEUE_TYPE+ "='" + queueType.ordinal() + "'");
        db.close();
    }

    public synchronized void clearList(QueueType queueType){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_UPNEXT + " WHERE " + QUEUE_TYPE + "='" + queueType.ordinal() + "'");
        db.close();
    }

    public synchronized void clearList(int queueType){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_UPNEXT + " WHERE " + QUEUE_TYPE + "='" + queueType + "'");
        db.close();
    }

    public synchronized ArrayList<? extends IMediaItemBase> getUnShuffledList(QueueType queueType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<IMediaItemBase> songList = new ArrayList<>();
        String query = "SELECT  * FROM " + TABLE_UPNEXT + " WHERE " +
                QUEUE_TYPE + "='" + queueType.ordinal() + "'";

        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
//                    IMediaItem item =  new MediaItem(0, null, context.getResources().getString(R.string.all_songs), null, 0, null, 0, null, 0, 0, null,
//                            itemType, itemList.get(position).getMediaType(), itemType, itemList.get(position).getParentId(), null );
                    String duration = cursor.getString(9);
                    String dateAdded = cursor.getString(10);

                    long itemId = cursor.getInt(1);
                    String itemTitle = cursor.getString(2);
                    String displayName = cursor.getString(3);
                    ItemType itemType = ItemType.fromOrdinal(cursor.getInt(12));
                    MediaType mediaType = MediaType.fromOrdinal(cursor.getInt(13));
                    ItemType parentType = ItemType.fromOrdinal(cursor.getInt(14));
                    long parentId = cursor.getInt(15);
                    String parentTitle = cursor.getString(16);
                    if(displayName.equals(context.getResources().getString(R.string.all_songs))){
                        switch (parentType){
                            case FAVOURITE:
                                songList.addAll(MediaController.getInstance(context).getFavoriteList());
                                break;
                            case ALBUM:
                                songList.addAll(MediaController.getInstance(context).getAlbumTrackList(itemId, itemTitle));
                            case ARTIST:
                                if(itemType == ItemType.ALBUM){
                                    songList.addAll(MediaController.getInstance(context).getArtistAlbumsTrackList(parentId, parentTitle, itemId, itemTitle));
                                }else{
                                    songList.addAll(MediaController.getInstance(context).getArtistTrackList(parentId, parentTitle));
                                }
                                break;
                            case GENRE:
                                if(itemType == ItemType.ALBUM){
                                    songList.addAll(MediaController.getInstance(context).getGenreAlbumsTrackList(parentId, parentTitle, itemId, itemTitle));
                                }else{
                                    songList.addAll(MediaController.getInstance(context).getGenreTrackList(parentId, parentTitle));
                                }
                                break;
                            case BOOM_PLAYLIST:
                                songList.addAll(MediaController.getInstance(context).getBoomPlayListTrackList(parentId));
                            case PLAYLIST:
                                songList.addAll(MediaController.getInstance(context).getPlayListTrackList(parentId, parentTitle));
                                break;
                            default:
                                switch (mediaType){
                                    case DEVICE_MEDIA_LIB:
                                        songList.addAll(MediaController.getInstance(context).getSongList());
                                        break;
                                    default:
                                        songList.addAll(MediaController.getInstance(context).getCloudList(mediaType));
                                        break;
                                }
                                break;
                        }
                    }else {
                        songList.add(new MediaItem(itemId, itemTitle, displayName, cursor.getString(4), cursor.getInt(5),
                                cursor.getString(6), cursor.getInt(7), cursor.getString(8), Long.parseLong(duration),
                                Long.parseLong(dateAdded), cursor.getString(11), itemType, mediaType, parentType, parentId, parentTitle));
                    }
                } while (cursor.moveToNext());
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            cursor.close();
        }
        db.close();
//        if(songList.size() > 0)
//            Collections.reverse(songList);
        return songList;
    }
}
