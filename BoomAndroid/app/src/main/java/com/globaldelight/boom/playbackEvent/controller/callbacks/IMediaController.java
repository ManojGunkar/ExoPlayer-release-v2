package com.globaldelight.boom.playbackEvent.controller.callbacks;

import com.globaldelight.boom.playbackEvent.utils.MediaType;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.local.callback.IMediaItem;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.collection.local.callback.IMediaItemCollection;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 18-02-17.
 */

public interface IMediaController {

    ArrayList<? extends IMediaItemBase> getAlbumList();

    ArrayList<? extends IMediaItemBase> getSongList();

    ArrayList<? extends IMediaItemBase> getArtistsList();

    ArrayList<? extends IMediaItemBase> getPlayList();

    ArrayList<? extends IMediaItemBase> getGenreList();

    IMediaItemCollection getBoomPlayListItem(long itemId);

    ArrayList<? extends IMediaItemBase> getBoomPlayList();

    int getFavouriteCount();

    ArrayList<? extends IMediaItemBase> getFavoriteList();

    ArrayList<? extends IMediaItemBase> getCloudList(@MediaType int mediaType);


    ArrayList<? extends IMediaItemBase> getAlbumTrackList(IMediaItemCollection collection);

    ArrayList<? extends IMediaItemBase> getArtistTrackList(IMediaItemCollection collection);

    ArrayList<? extends IMediaItemBase> getPlayListTrackList(IMediaItemCollection collection);

    ArrayList<? extends IMediaItemBase> getGenreTrackList(IMediaItemCollection collection);

    ArrayList<? extends IMediaItemBase> getBoomPlayListTrackList(long id);


    ArrayList<? extends IMediaItemBase> getArtistAlbumsList(IMediaItemCollection collection);

    ArrayList<? extends IMediaItemBase> getGenreAlbumsList(IMediaItemCollection collection);

    ArrayList<? extends IMediaItemBase> getArtistAlbumsTrackList(IMediaItemCollection collection);

    ArrayList<? extends IMediaItemBase> getGenreAlbumsTrackList(IMediaItemCollection collection);

    boolean isFavoriteItem(long trackId);

    void removeItemToFavoriteList(long trackId);

    void addItemToFavoriteList(IMediaItem item);

    ArrayList<String> getArtUrlList(MediaItemCollection collection);

    void deleteBoomPlaylist(long itemId);

    void renamePlaylist(String playlistTitle, long itemId);

    void addSongToBoomPlayList(long itemId, ArrayList<? extends IMediaItemBase> mediaElement, boolean isUpdate) ;

    void removeSongToPlayList(long itemId, int playlistId) ;

    void removeCloudMediaItemList(@MediaType int mediaType) ;

    void createBoomPlaylist(String playlist) ;

    void addSongsToCloudItemList(@MediaType int mediaType, ArrayList<IMediaItemBase> fileList);

    ArrayList<? extends IMediaItem> getAlbumTrackList(long itemId, String itemTitle) ;

    ArrayList<? extends IMediaItemBase> getArtistAlbumsTrackList(long parentId, long itemId) ;

    ArrayList<? extends IMediaItemBase> getArtistTrackList(long parentId, String parentTitle) ;

    ArrayList<? extends IMediaItemBase> getGenreAlbumsTrackList(long parentId, String parentTitle, long itemId, String itemTitle) ;

    ArrayList<? extends IMediaItemBase> getGenreTrackList(long parentId, String parentTitle) ;

    ArrayList<? extends IMediaItemBase> getPlayListTrackList(long parentId, String parentTitle) ;

    int getRecentPlayedItemCount();

    ArrayList<? extends IMediaItemBase> getRecentPlayedList();

    void setRecentPlayedItem(IMediaItemBase recentPlayedItem);
}


