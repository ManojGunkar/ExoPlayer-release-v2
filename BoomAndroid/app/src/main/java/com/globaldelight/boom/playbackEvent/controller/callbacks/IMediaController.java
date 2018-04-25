package com.globaldelight.boom.playbackEvent.controller.callbacks;

import com.globaldelight.boom.playbackEvent.utils.MediaType;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.base.IMediaItem;
import com.globaldelight.boom.collection.base.IMediaElement;
import com.globaldelight.boom.collection.base.IMediaItemCollection;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 18-02-17.
 */

public interface IMediaController {

    ArrayList<? extends IMediaElement> getAlbumList();

    ArrayList<? extends IMediaElement> getSongList();

    ArrayList<? extends IMediaElement> getArtistsList();

    ArrayList<? extends IMediaElement> getPlayList();

    ArrayList<? extends IMediaElement> getGenreList();

    IMediaItemCollection getBoomPlayListItem(long itemId);

    ArrayList<? extends IMediaElement> getBoomPlayList();

    int getFavouriteCount();

    ArrayList<? extends IMediaElement> getFavoriteList();

    ArrayList<? extends IMediaElement> getCloudList(@MediaType int mediaType);


    ArrayList<? extends IMediaElement> getAlbumTrackList(IMediaItemCollection collection);

    ArrayList<? extends IMediaElement> getArtistTrackList(IMediaItemCollection collection);

    ArrayList<? extends IMediaElement> getPlayListTrackList(IMediaItemCollection collection);

    ArrayList<? extends IMediaElement> getGenreTrackList(IMediaItemCollection collection);


    ArrayList<? extends IMediaElement> getArtistAlbumsList(IMediaItemCollection collection);

    ArrayList<? extends IMediaElement> getGenreAlbumsList(IMediaItemCollection collection);

    ArrayList<? extends IMediaElement> getArtistAlbumsTrackList(IMediaItemCollection collection, int index);

    ArrayList<? extends IMediaElement> getGenreAlbumsTrackList(IMediaItemCollection collection, int index);

    boolean isFavoriteItem(long trackId);

    void removeItemToFavoriteList(long trackId);

    void addItemToFavoriteList(IMediaItem item);

    ArrayList<String> getArtUrlList(MediaItemCollection collection);

    void deleteBoomPlaylist(long itemId);

    void renamePlaylist(String playlistTitle, long itemId);

    void addSongToBoomPlayList(long itemId, ArrayList<? extends IMediaElement> mediaElement, boolean isUpdate) ;

    void removeSongToPlayList(long itemId, int playlistId) ;

    void removeCloudMediaItemList(@MediaType int mediaType) ;

    void createBoomPlaylist(String playlist) ;

    void addSongsToCloudItemList(@MediaType int mediaType, ArrayList<IMediaElement> fileList);

    int getRecentPlayedItemCount();

    ArrayList<? extends IMediaElement> getRecentPlayedList();

    void setRecentPlayedItem(IMediaElement recentPlayedItem);
}


