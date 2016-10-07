package com.player.data.MediaLibrary;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

import com.player.data.MediaCollection.IMediaItemBase;
import com.player.data.DeviceMediaCollection.MediaItemCollection;
import com.player.data.DeviceMediaLibrary.DeviceMediaHandler;
import com.player.data.PlayingQueue.QueueType;

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
            DeviceMediaHandler.getInstance(context).QueryMediaSearchResult(query);
        }
    }

    public void createBoomPlaylist(String input) {
            DeviceMediaHandler.getInstance(context).createBoomPlaylist(input);
    }

    public void deleteBoomPlaylist(long itemId) {
        DeviceMediaHandler.getInstance(context).deleteBoomPlaylist(itemId);
    }

    public void addSongToBoomPlayList(long itemId, IMediaItemBase iMediaItemBase) {
        DeviceMediaHandler.getInstance(context).addSongToBoomPlayList(itemId, iMediaItemBase);
    }

    public void renameBoomPlaylist(String input, long itemId) {
        DeviceMediaHandler.getInstance(context).renameBoomPlaylist(input, itemId);
    }

    public LinkedList<? extends IMediaItemBase> getHistoryItemsForQueue(boolean ishistory) {
        return DeviceMediaHandler.getInstance(context).getHistoryList(ishistory);
    }

    public void clearList(boolean ishistory) {
        DeviceMediaHandler.getInstance(context).cliearList(ishistory);
    }

    public void addSongsToList(boolean ishistory, IMediaItemBase itemBase) {
        DeviceMediaHandler.getInstance(context).addSongsToList(ishistory, itemBase);
    }

    public void removeItemToList(boolean ishistory, long itemId) {
        DeviceMediaHandler.getInstance(context).removeItemToList(ishistory, itemId);
    }
}
