package com.globaldelight.boom.data.DeviceMediaLibrary;

import android.content.Context;
import android.support.annotation.IntRange;

import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.App;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.handler.PlayingQueue.QueueType;
import com.globaldelight.boom.handler.PlayingQueue.UpNextItem;

import java.util.ArrayList;
import java.util.LinkedList;

import static com.globaldelight.boom.data.MediaLibrary.ItemType.ALBUM;
import static com.globaldelight.boom.data.MediaLibrary.ItemType.ARTIST;
import static com.globaldelight.boom.data.MediaLibrary.ItemType.BOOM_PLAYLIST;
import static com.globaldelight.boom.data.MediaLibrary.ItemType.GENRE;
import static com.globaldelight.boom.data.MediaLibrary.ItemType.PLAYLIST;

/**
 * Created by Rahul Agarwal on 8/8/2016.
 */
public class DeviceMediaHandler {
    private Context context;
    private static DeviceMediaHandler handler =null;
    private DeviceMediaHandler(Context context){
        this.context = context;
    }

    public static DeviceMediaHandler getInstance(Context context){
        if(handler == null){
            handler = new DeviceMediaHandler(context);
        }
        return handler;
    }

    public ArrayList<? extends IMediaItemBase> QueryMediaCollectionList(ItemType itemType, Context context) {
        switch (itemType){
            case SONGS:
                return DeviceMediaQuery.getSongList(context);
            case ALBUM:
                    return DeviceMediaQuery.getAlbumList(context);
            case ARTIST:
                return DeviceMediaQuery.getArtistList(context);
            case PLAYLIST:
                return DeviceMediaQuery.getPlayList(context);
            case GENRE:
                return DeviceMediaQuery.getGenreList(context);
            case BOOM_PLAYLIST:
                return App.getBoomPlayListHelper().getAllPlaylist();
            case FAVOURITE:
                break;
            default:
                break;
        }
        return null;
    }

    public ArrayList<? extends IMediaItemBase> QueryMediaCollectionDetails(IMediaItemBase collection) {
        switch (collection.getItemType()){
            case ALBUM:
                return DeviceMediaQuery.getAlbumDetail(context, collection.getItemId(), collection.getItemTitle());
            case ARTIST:
//                Check whether Collection contains it's item list.
                if(((MediaItemCollection)collection).getMediaElement().isEmpty()){
                    return DeviceMediaQuery.getArtistsAlbumDetails(context, collection.getItemId(), collection.getItemTitle(), ((MediaItemCollection) collection).getItemCount());
                }else if(((MediaItemCollection) collection).getMediaElement().get(((MediaItemCollection) collection).getCurrentIndex()).getItemType() == ALBUM) {
                    return DeviceMediaQuery.getSongListOfArtistsAlbum(context, collection.getItemId(), collection.getItemTitle(),
                            ((MediaItemCollection) collection).getMediaElement().get(((MediaItemCollection) collection).getCurrentIndex()).getItemId(),
                            ((MediaItemCollection) collection).getMediaElement().get(((MediaItemCollection) collection).getCurrentIndex()).getItemTitle());
                }else if(((MediaItemCollection) collection).getMediaElement().get(((MediaItemCollection) collection).getCurrentIndex()).getItemType() == ItemType.SONGS){
                    return DeviceMediaQuery.getSongListOfArtist(context, collection.getItemId(), collection.getItemTitle());
                }
            case PLAYLIST:
                if (((MediaItemCollection)collection).getMediaElement().isEmpty()) {
                    return DeviceMediaQuery.getPlaylistSongs(context, collection.getItemId(), collection.getItemTitle());
                }
            case GENRE:
                if(((MediaItemCollection)collection).getMediaElement().isEmpty()){
                    return DeviceMediaQuery.getGenresAlbumDetails(context, collection.getItemId(), collection.getItemTitle(), ((MediaItemCollection) collection).getItemCount());
                }else if(((MediaItemCollection) collection).getMediaElement().get(((MediaItemCollection) collection).getCurrentIndex()).getItemType() == ALBUM) {
                    return DeviceMediaQuery.getSongListOfGenreAlbum(context, collection.getItemId(), collection.getItemTitle(),
                            ((MediaItemCollection) collection).getMediaElement().get(((MediaItemCollection) collection).getCurrentIndex()).getItemId(),
                            ((MediaItemCollection) collection).getMediaElement().get(((MediaItemCollection) collection).getCurrentIndex()).getItemTitle());
                }else if(((MediaItemCollection) collection).getMediaElement().get(((MediaItemCollection) collection).getCurrentIndex()).getItemType() == ItemType.SONGS){
                    return DeviceMediaQuery.getSongListOfGenre(context, collection.getItemId(), collection.getItemTitle());
                }
            case BOOM_PLAYLIST:
                if (((MediaItemCollection)collection).getMediaElement().isEmpty()) {
                    return App.getBoomPlayListHelper().getPlaylistSongs(collection.getItemId());
                }
            case FAVOURITE:

                break;
            default:
                break;
        }
        return null;
    }

    public ArrayList<? extends IMediaItemBase> QueryMediaCollectionForQueue(IMediaItemBase collection, @IntRange(from=-1) int position){

        if(collection.getItemType() == ALBUM && ((MediaItemCollection)collection).getMediaElement().size()==0){

            return DeviceMediaQuery.getAlbumDetail(context, collection.getItemId(), collection.getItemTitle());

        }else if(collection.getItemType() == ARTIST){
            if(((MediaItemCollection)collection).getMediaElement().size()==0 ||
                    ((MediaItemCollection)collection).getMediaElement().get(position).getItemType() == ItemType.SONGS){

                return DeviceMediaQuery.getSongListOfArtist(context, collection.getItemId(), collection.getItemTitle());

            }else if(((MediaItemCollection)collection).getMediaElement().get(position).getItemType() == ALBUM){

                return DeviceMediaQuery.getSongListOfArtistsAlbum(context, collection.getItemId(), collection.getItemTitle(),

                        ((MediaItemCollection) collection).getMediaElement().get(position).getItemId(),

                        ((MediaItemCollection) collection).getMediaElement().get(position).getItemTitle());

            }
        }else if(collection.getItemType() == GENRE){
            if(((MediaItemCollection)collection).getMediaElement().size()==0 ||
                    ((MediaItemCollection)collection).getMediaElement().get(position).getItemType() == ItemType.SONGS){

                return DeviceMediaQuery.getSongListOfGenre(context, collection.getItemId(), collection.getItemTitle());

            }else if(((MediaItemCollection)collection).getMediaElement().get(position).getItemType() == ALBUM){

                return DeviceMediaQuery.getSongListOfGenreAlbum(context, collection.getItemId(), collection.getItemTitle(),

                        ((MediaItemCollection) collection).getMediaElement().get(position).getItemId(),

                        ((MediaItemCollection) collection).getMediaElement().get(position).getItemTitle());
            }
        }else if(collection.getItemType() == PLAYLIST){
            if(((MediaItemCollection)collection).getMediaElement().size()==0){
                return DeviceMediaQuery.getPlaylistSongs(context, collection.getItemId(), collection.getItemTitle());
            }
        }else if(collection.getItemType() == BOOM_PLAYLIST){
            if(((MediaItemCollection)collection).getMediaElement().size()==0){
                return App.getBoomPlayListHelper().getPlaylistSongs(collection.getItemId());
            }
        }
        return null;
    }

    public ArrayList<String> QueryMediaCollectionArtList(MediaItemCollection collection) {
        switch (collection.getItemType()){
            case ALBUM:
                break;
            case ARTIST:
                return DeviceMediaQuery.getArtistsArtList(context, collection.getItemId(), collection.getItemTitle());
            case PLAYLIST:
                return DeviceMediaQuery.getPlaylistArtList(context, collection.getItemId(), collection.getItemTitle());
            case GENRE:
                return DeviceMediaQuery.getGenreArtList(context, collection.getItemId(), collection.getItemTitle());
            case BOOM_PLAYLIST:
                return App.getBoomPlayListHelper().getBoomPlayListArtList(collection.getItemId());
            case FAVOURITE:

                break;
            default:
                break;
        }
        return null;
    }

    public void createBoomPlaylist(String input) {
        App.getBoomPlayListHelper().createPlaylist(input);
    }

    public void deleteBoomPlaylist(long itemId) {
        App.getBoomPlayListHelper().deletePlaylist(itemId);
    }

    public void addSongToBoomPlayList(long itemId, ArrayList<? extends IMediaItemBase> iMediaItemBase) {
        App.getBoomPlayListHelper().addSongs(iMediaItemBase, itemId);
    }

    public void renameBoomPlaylist(String input, long itemId) {
        App.getBoomPlayListHelper().renamePlaylist(input, itemId);
    }

    public LinkedList<? extends IMediaItemBase> getFavItemList() {
        return App.getFavoriteDBHelper().getSongList();
    }

    public void clearFavList() {
        App.getFavoriteDBHelper().clearList();
    }

    public void addSongsToFavList(IMediaItemBase itemBase) {
        App.getFavoriteDBHelper().addSong(itemBase);
    }

    public void removeItemToFavList(long itemId) {
        App.getFavoriteDBHelper().removeSong(itemId);
    }

    public boolean isFavouriteItems(long itemId) {
        return App.getFavoriteDBHelper().isFavouriteItems(itemId);
    }

    public void addUpNextItem(IMediaItemBase song, QueueType queueType) {
        App.getUPNEXTDBHelper().addSong(song, queueType);
    }

    public void addUpNextItem(LinkedList<? extends IMediaItemBase> songs, QueueType queueType) {
        App.getUPNEXTDBHelper().addSongs(songs, queueType);
    }

    public LinkedList<? extends IMediaItemBase> getUpNextItemList(QueueType queueType) {
        return App.getUPNEXTDBHelper().getSongList(queueType);
    }

    public void clearUpNextList(QueueType queueType){
        App.getUPNEXTDBHelper().clearList(queueType);
    }
}
