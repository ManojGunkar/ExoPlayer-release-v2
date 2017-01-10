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
import java.util.List;

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

    public IMediaItemBase requestMediaCollectionItem(Context context, long mParentId, ItemType mParentType, MediaType mMediaType) {
        if(mMediaType == MediaType.DEVICE_MEDIA_LIB){
            return DeviceMediaHandler.getInstance(context).requestMediaCollectionItem(context, mParentId, mParentType);
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

    public void addSongToBoomPlayList(long itemId, ArrayList<? extends IMediaItemBase> iMediaItemBase, boolean isUpdate) {
        DeviceMediaHandler.getInstance(context).addSongToBoomPlayList(itemId, iMediaItemBase, isUpdate);
    }

    public void renameBoomPlaylist(String input, long itemId) {
        DeviceMediaHandler.getInstance(context).renameBoomPlaylist(input, itemId);
    }

    public ArrayList<? extends IMediaItemBase> getFavoriteItemList() {
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

    public void removeItemToFavoriteList(String itemTitle) {
        DeviceMediaHandler.getInstance(context).removeItemToFavList(itemTitle);
    }

    public boolean isFavouriteItems(long itemId) {
        return DeviceMediaHandler.getInstance(context).isFavouriteItems(itemId);
    }

    public boolean isFavouriteItems(String itemTitle) {
        return DeviceMediaHandler.getInstance(context).isFavouriteItems(itemTitle);
    }

    public void addUpNextItem(IMediaItemBase song, QueueType queueType) {
        DeviceMediaHandler.getInstance(context).addUpNextItem(song, queueType);
    }

    public void addUpNextItem(ArrayList<? extends IMediaItemBase> songs, QueueType queueType) {
        DeviceMediaHandler.getInstance(context).addUpNextItem(songs, queueType);
    }

    public void insertUnShuffledList(List<? extends IMediaItemBase> songs, QueueType queueType, boolean isAppend) {
        DeviceMediaHandler.getInstance(context).insertUnShuffledList(songs, queueType, isAppend);
    }

    public void insertUnShuffledList(IMediaItemBase item, QueueType queueType, boolean isAppend) {
        DeviceMediaHandler.getInstance(context).insertUnShuffledList(item, queueType, isAppend);
    }

    public ArrayList<? extends IMediaItemBase> getUpNextItemList(QueueType queueType) {
        return DeviceMediaHandler.getInstance(context).getUpNextItemList(queueType);
    }

    public void clearUpNextList(QueueType queueType) {
        DeviceMediaHandler.getInstance(context).clearUpNextList(queueType);
    }

    public ArrayList<? extends IMediaItemBase> getUnShuffledList(QueueType queueType) {
        return DeviceMediaHandler.getInstance(context).getUnShuffledList(queueType);
    }
}
