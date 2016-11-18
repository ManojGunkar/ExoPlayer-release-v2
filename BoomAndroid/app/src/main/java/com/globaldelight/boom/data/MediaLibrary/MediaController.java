package com.globaldelight.boom.data.MediaLibrary;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;

import java.util.ArrayList;
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

    }

    public void deleteBoomPlaylist(long itemId) {
        MediaLibraryHandler.getInstance(context).deleteBoomPlaylist(itemId);
    }

    public void addSongToBoomPlayList(long itemId, IMediaItemBase iMediaItemBase) {
        MediaLibraryHandler.getInstance(context).addSongToBoomPlayList(itemId, iMediaItemBase);
    }

    public void renameBoomPlaylist(String input, long itemId) {
        MediaLibraryHandler.getInstance(context).renameBoomPlaylist(input, itemId);
    }

    public LinkedList<? extends IMediaItemBase> getHistoryItemsForQueue(boolean ishistory) {
        return MediaLibraryHandler.getInstance(context).getHistoryItemsForQueue(ishistory);
    }

    public void clearList(boolean ishistory) {
        MediaLibraryHandler.getInstance(context).clearList(ishistory);
    }

    public void addSongsToList(boolean ishistory, IMediaItemBase itemBase) {
        MediaLibraryHandler.getInstance(context).addSongsToList(ishistory, itemBase);
    }

    public void removeItemToList(boolean ishistory, long itemId) {
        MediaLibraryHandler.getInstance(context).removeItemToList(ishistory, itemId);
    }
}
