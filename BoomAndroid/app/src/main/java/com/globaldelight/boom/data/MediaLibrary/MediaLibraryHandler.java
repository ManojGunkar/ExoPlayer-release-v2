package com.globaldelight.boom.data.MediaLibrary;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.DeviceMediaLibrary.DeviceMediaHandler;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.handler.PlayingQueue.QueueType;
import com.globaldelight.boom.handler.PlayingQueue.UpNextItem;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Rahul Agarwal on 8/5/2016.
 */
public class MediaLibraryHandler implements IMediaLibrary{

    private static MediaLibraryHandler handler;
    private Context context;
    private MediaLibraryHandler(Context context){
        this.context = context;
    }
    public static MediaLibraryHandler getInstance(Context context) {
        if(handler == null){
            handler = new MediaLibraryHandler(context);
        }
        return handler;
    }

    @Override
    public ArrayList<String> requestArtUrlList(MediaItemCollection collection) {
        if(collection.getMediaType() == MediaType.DEVICE_MEDIA_LIB){
            return DeviceMediaHandler.getInstance(context).QueryMediaCollectionArtList(collection);
        }
        return null;
    }

    @Override
    public ArrayList<? extends IMediaItemBase> requestMediaCollectionList(ItemType itemType, @Nullable MediaType mediaType) {
        if(mediaType == MediaType.DEVICE_MEDIA_LIB){
            return DeviceMediaHandler.getInstance(context).QueryMediaCollectionList(itemType, context);
        }
        return null;
    }

    @Override
    public ArrayList<? extends IMediaItemBase> requestMediaCollectionItemDetails(IMediaItemBase collection) {
        if(collection.getMediaType() == MediaType.DEVICE_MEDIA_LIB){
            return DeviceMediaHandler.getInstance(context).QueryMediaCollectionDetails(collection);
        }
        return null;
    }

    public ArrayList<? extends IMediaItemBase> requestMediaCollectionItemsForQueue(IMediaItemBase collection, @IntRange(from=-1) int position){
        if(collection.getMediaType() == MediaType.DEVICE_MEDIA_LIB){
            return DeviceMediaHandler.getInstance(context).QueryMediaCollectionForQueue(collection, position);
        }
        return null;
    }

    @Override
    public void requestMediaSearch(MediaType mediaType, String query) {
        if(mediaType == MediaType.DEVICE_MEDIA_LIB){
//            DeviceMediaHandler.getInstance(context).QueryMediaSearchResult(query);
        }
    }

    public void createBoomPlaylist(String input) {
            DeviceMediaHandler.getInstance(context).createBoomPlaylist(input);
    }

    public void deleteBoomPlaylist(long itemId) {
        DeviceMediaHandler.getInstance(context).deleteBoomPlaylist(itemId);
    }

    public void addSongToBoomPlayList(long itemId, ArrayList<? extends IMediaItemBase> iMediaItemBase) {
        DeviceMediaHandler.getInstance(context).addSongToBoomPlayList(itemId, iMediaItemBase);
    }

    public void renameBoomPlaylist(String input, long itemId) {
        DeviceMediaHandler.getInstance(context).renameBoomPlaylist(input, itemId);
    }

    public LinkedList<? extends IMediaItemBase> getFavoriteItemList() {
        return DeviceMediaHandler.getInstance(context).getFavItemList();
    }

    public void clearFavoriteList() {
        DeviceMediaHandler.getInstance(context).clearFavList();
    }

    public void addSongsToFavoriteList(IMediaItemBase itemBase) {
        DeviceMediaHandler.getInstance(context).addSongsToFavList(itemBase);
    }

    public void removeItemToFavoriteList(long itemId) {
        DeviceMediaHandler.getInstance(context).removeItemToFavList(itemId);
    }

    public boolean isFavouriteItems(long itemId) {
        return DeviceMediaHandler.getInstance(context).isFavouriteItems(itemId);
    }

    public void addUpNextItem(IMediaItemBase song, QueueType queueType) {
        DeviceMediaHandler.getInstance(context).addUpNextItem(song, queueType);
    }

    public void addUpNextItem(IMediaItemBase song, int position, QueueType queueType) {
        DeviceMediaHandler.getInstance(context).addUpNextItem(song, position, queueType);
    }

    public void addItemListUpNext(ArrayList<? extends IMediaItemBase> itemList, QueueType queueType) {
        DeviceMediaHandler.getInstance(context).addItemListUpNext(itemList, queueType);
    }

    public LinkedList<? extends IMediaItemBase> getUpNextItemList(QueueType queueType) {
        return DeviceMediaHandler.getInstance(context).getUpNextItemList(queueType);
    }

    public void removeItemFromUpNext(long songId, QueueType queueType) {
        DeviceMediaHandler.getInstance(context).removeItemFromUpNext(songId, queueType);
    }

    public void clearUpNextList(QueueType queueType) {
        DeviceMediaHandler.getInstance(context).clearUpNextList(queueType);
    }

    public UpNextItem getPlayingItem(){
        return DeviceMediaHandler.getInstance(context).getPlayingItem();
    }

    public void addItemAsPlaying(IMediaItemBase song, QueueType queueType){
        DeviceMediaHandler.getInstance(context).addItemAsPlaying(song, queueType);
    }
}
