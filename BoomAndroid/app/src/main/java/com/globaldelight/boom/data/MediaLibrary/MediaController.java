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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    public IMediaItemBase getMediaCollectionItem(Context context, long mParentId, ItemType mParentType, MediaType mMediaType){
        return MediaLibraryHandler.getInstance(context).requestMediaCollectionItem(context, mParentId, mParentType, mMediaType);
    }

    public ArrayList<? extends IMediaItemBase> getMediaCollectionItem(Context context, long parentId, String parentTitle, ItemType parentType, long itemId, String itemTitle, MediaType mediaType) {
        return MediaLibraryHandler.getInstance(context).getMediaCollectionItem(context, parentId, parentTitle, parentType, itemId, itemTitle, mediaType);
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

    public void addSongToBoomPlayList(long itemId, ArrayList<? extends IMediaItemBase> iMediaItemBase, boolean isUpdate) {
        MediaLibraryHandler.getInstance(context).addSongToBoomPlayList(itemId, iMediaItemBase, isUpdate);
    }

    public void renameBoomPlaylist(String input, long itemId) {
        MediaLibraryHandler.getInstance(context).renameBoomPlaylist(input, itemId);
    }

    public ArrayList<? extends IMediaItemBase> getFavouriteListItems() {
        return MediaLibraryHandler.getInstance(context).getFavoriteItemList();
    }

    public boolean isFavouriteItems(long itemId) {
        return MediaLibraryHandler.getInstance(context).isFavouriteItems(itemId);
    }

    public boolean isFavouriteItems(String itemTitle) {
        return MediaLibraryHandler.getInstance(context).isFavouriteItems(itemTitle);
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

    public void removeItemToFavoriteList(String itemTitle) {
        MediaLibraryHandler.getInstance(context).removeItemToFavoriteList(itemTitle);
    }

    public void addUpNextItem(IMediaItemBase song, QueueType queueType) {
        MediaLibraryHandler.getInstance(context).addUpNextItem(song, queueType);
    }

    public void addUpNextItem(ArrayList<? extends IMediaItemBase> songs, QueueType queueType) {
        MediaLibraryHandler.getInstance(context).addUpNextItem(songs, queueType);
    }

    public void insertUnShuffledList(List<? extends IMediaItemBase> songs, QueueType queueType, boolean isAppend) {
        MediaLibraryHandler.getInstance(context).insertUnShuffledList(songs, queueType, isAppend);
    }

    public void insertUnShuffledList(IMediaItemBase item, QueueType queueType, boolean isAppend) {
        MediaLibraryHandler.getInstance(context).insertUnShuffledList(item, queueType, isAppend);
    }

    public ArrayList<? extends IMediaItemBase> getUpNextItemList(QueueType queueType) {
        return MediaLibraryHandler.getInstance(context).getUpNextItemList(queueType);
    }

    public void clearUpNextList(QueueType queueType) {
        MediaLibraryHandler.getInstance(context).clearUpNextList(queueType);
    }

    public ArrayList<? extends IMediaItemBase> getUnShuffledList(QueueType queueType) {
        return MediaLibraryHandler.getInstance(context).getUnShuffledList(queueType);
    }

    public void addSongsToCloudItemList(ArrayList<IMediaItemBase> fileList) {
        MediaLibraryHandler.getInstance(context).addSongsToCloudItemList(fileList);
    }

    public Collection<? extends IMediaItemBase> getCloudMediaItemList(MediaType mediaType) {
        return MediaLibraryHandler.getInstance(context).getCloudItemList(mediaType);
    }

    public void removeCloudMediaItemList(MediaType mediaType) {
        MediaLibraryHandler.getInstance(context).deleteCloudMediaItemList(mediaType);
    }
}
