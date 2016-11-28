package com.globaldelight.boom.data.MediaLibrary;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.handler.PlayingQueue.QueueType;
import com.globaldelight.boom.handler.PlayingQueue.UpNextItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by Rahul Agarwal on 8/12/2016.
 */
public class MediaController implements IMediaController{
    private static MediaController handler;
    private Context context;

    private MediaController(Context context){
        this.context = context;
    }
    public static MediaController getInstance(Context context) {
        if(handler == null){
            handler = new MediaController(context);
        }
        return handler;
    }

    @Override
    public ArrayList<String> getArtUrlList(MediaItemCollection Collection) {
        return MediaLibraryHandler.getInstance(context).requestArtUrlList(Collection);
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getMediaCollectionItemList(ItemType itemType, @Nullable MediaType mediaType) {
            return MediaLibraryHandler.getInstance(context).requestMediaCollectionList(itemType, mediaType);
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getMediaCollectionItemDetails(IMediaItemCollection collection){
            return MediaLibraryHandler.getInstance(context).requestMediaCollectionItemDetails(collection);
    }

    public ArrayList<? extends IMediaItemBase> getMediaCollectionItemsForQueue(IMediaItemCollection collection, @IntRange(from=-1) int position){
        return MediaLibraryHandler.getInstance(context).requestMediaCollectionItemsForQueue(collection, position);
    }

    @Override
    public void doSearch(MediaType mediaType, String query) {
        MediaLibraryHandler.getInstance(context).requestMediaSearch(mediaType, query);
    }

    public void createBoomPlaylist(String input) {
        MediaLibraryHandler.getInstance(context).createBoomPlaylist(input);
        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_CREATED_NEW_PLAYLIST);

    }

    public void deleteBoomPlaylist(long itemId) {
        MediaLibraryHandler.getInstance(context).deleteBoomPlaylist(itemId);
    }

    public void addSongToBoomPlayList(long itemId, ArrayList<? extends IMediaItemBase> iMediaItemBase) {
        MediaLibraryHandler.getInstance(context).addSongToBoomPlayList(itemId, iMediaItemBase);
    }

    public void renameBoomPlaylist(String input, long itemId) {
        MediaLibraryHandler.getInstance(context).renameBoomPlaylist(input, itemId);
    }

    public LinkedList<? extends IMediaItemBase> getFavouriteListItems() {
        return MediaLibraryHandler.getInstance(context).getFavoriteItemList();
    }

    public boolean isFavouriteItems(long itemId) {
        return MediaLibraryHandler.getInstance(context).isFavouriteItems(itemId);
    }

    public void clearFavoriteList() {
        MediaLibraryHandler.getInstance(context).clearFavoriteList();
    }

    public void addSongsToFavoriteList(IMediaItemBase itemBase) {
        MediaLibraryHandler.getInstance(context).addSongsToFavoriteList(itemBase);
    }

    public void removeItemToFavoriteList(long itemId) {
        MediaLibraryHandler.getInstance(context).removeItemToFavoriteList(itemId);
    }

    public void addUpNextItem(IMediaItemBase song, QueueType queueType) {
        MediaLibraryHandler.getInstance(context).addUpNextItem(song, queueType);
    }

    public void addUpNextItem(IMediaItemBase song, int position, QueueType queueType) {
        MediaLibraryHandler.getInstance(context).addUpNextItem(song, position, queueType);
    }

    public void addItemListToUpNext(ArrayList<? extends IMediaItemBase> itemList, QueueType queueType) {
        MediaLibraryHandler.getInstance(context).addItemListUpNext(itemList, queueType);
    }

    public LinkedList<? extends IMediaItemBase> getUpNextItemList(QueueType queueType) {
        return MediaLibraryHandler.getInstance(context).getUpNextItemList(queueType);
    }

    public void removeItemFromUpNext(long songId, QueueType queueType){
        MediaLibraryHandler.getInstance(context).removeItemFromUpNext(songId, queueType);
    }

    public void clearUpNextList(QueueType queueType){
        MediaLibraryHandler.getInstance(context).clearUpNextList(queueType);
    }

    public UpNextItem getPlayingItem(){
        return MediaLibraryHandler.getInstance(context).getPlayingItem();
    }

    public void addItemAsPlaying(IMediaItemBase song, QueueType queueType){
        MediaLibraryHandler.getInstance(context).addItemAsPlaying(song, queueType);
    }
}
