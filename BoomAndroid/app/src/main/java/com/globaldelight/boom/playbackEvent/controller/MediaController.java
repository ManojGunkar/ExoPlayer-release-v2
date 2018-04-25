package com.globaldelight.boom.playbackEvent.controller;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.playbackEvent.utils.DeviceMediaLibrary;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.playbackEvent.utils.MediaType;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.base.IMediaItem;
import com.globaldelight.boom.collection.base.IMediaElement;
import com.globaldelight.boom.collection.base.IMediaItemCollection;
import com.globaldelight.boom.app.receivers.actions.PlayerEvents;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 18-02-17.
 */

public class MediaController {

    private static MediaController handler;
    private Context context;

    private MediaController(Context context){
        this.context = context;
    }
    public static MediaController getInstance(Context context) {
        if(handler == null){
            handler = new MediaController(context.getApplicationContext());
        }
        return handler;
    }

    public ArrayList<? extends IMediaElement> getPlayList() {
        return DeviceMediaLibrary.getPlayList(context);
    }

    public ArrayList<? extends IMediaElement> getPlayListTrackList(IMediaItemCollection collection) {
        return DeviceMediaLibrary.getPlaylistSongs(context, collection.getId(), collection.getTitle());
    }

    public void createBoomPlaylist(String playlist) {
        App.getBoomPlayListHelper().createPlaylist(playlist);
//        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_CREATED_NEW_PLAYLIST);
        FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.EVENT_CREATED_NEW_PLAYLIST);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }

    public ArrayList<? extends IMediaElement> getBoomPlayList() {
        return App.getBoomPlayListHelper().getAllPlaylist();
    }

    public IMediaItemCollection getBoomPlayListItem(IMediaElement item) {
        return App.getBoomPlayListHelper().gePlaylist(item.getId());
    }

    public ArrayList<? extends IMediaElement> getBoomPlayListTrackList(IMediaElement item) {
        return App.getBoomPlayListHelper().getPlaylistSongs(item.getId());
    }

    public boolean isAlreadyAdded(IMediaElement playlist, IMediaElement track) {
        return App.getBoomPlayListHelper().isAlreadyAddedToPlaylist(playlist.getId(), track.getId());
    }

    public void addSongToBoomPlayList(IMediaElement item, ArrayList<? extends IMediaElement> mediaElement, boolean isUpdate) {
        App.getBoomPlayListHelper().addSongs(mediaElement, item.getId(), isUpdate);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }

    public void removeSongToPlayList(IMediaElement item, IMediaElement playlist) {
        App.getBoomPlayListHelper().removeSong(item.getId(), playlist.getId());
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_BOOM_ITEM_LIST));
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }

    public void deleteBoomPlaylist(IMediaElement item) {
        App.getBoomPlayListHelper().deletePlaylist(item.getId());
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }

    public void renamePlaylist(String playlistTitle, IMediaElement item) {
        App.getBoomPlayListHelper().renamePlaylist(playlistTitle, item.getId());
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_BOOM_ITEM_LIST));
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }

    public ArrayList<? extends IMediaElement> getAlbumList() {
        return DeviceMediaLibrary.getAlbumList(context);
    }

    public ArrayList<? extends IMediaElement> getSongList() {
        return DeviceMediaLibrary.getSongList(context);
    }

    public ArrayList<? extends IMediaElement> getArtistsList() {
        return DeviceMediaLibrary.getArtistList(context);
    }

    public ArrayList<? extends IMediaElement> getGenreList() {
        return DeviceMediaLibrary.getGenreList(context);
    }

    public int getFavouriteCount(){
        return App.getFavoriteDBHelper().getFavouriteItemCount();
    }

    public ArrayList<? extends IMediaElement> getFavoriteList() {
        return App.getFavoriteDBHelper().getFavouriteItemList();
    }

    public ArrayList<? extends IMediaElement> getCloudList(@MediaType int mediaType) {
        return App.getCloudMediaItemDBHelper().getSongList(mediaType);
    }

    public ArrayList<? extends IMediaElement> getAlbumTrackList(IMediaItemCollection collection) {
        return DeviceMediaLibrary.getAlbumDetail(context, collection.getId(), collection.getTitle());
    }

    public ArrayList<? extends IMediaElement> getArtistTrackList(IMediaItemCollection collection) {
        return DeviceMediaLibrary.getSongListOfArtist(context, collection.getId(), collection.getTitle());
    }

    public ArrayList<? extends IMediaElement> getGenreTrackList(IMediaItemCollection collection) {
        return DeviceMediaLibrary.getSongListOfGenre(context, collection.getId(), collection.getTitle());
    }

    public ArrayList<? extends IMediaElement> getArtistAlbumsList(IMediaItemCollection collection) {
        return DeviceMediaLibrary.getArtistsAlbumDetails(context, collection.getId(), collection.getTitle(), collection.getItemCount());
    }

    public ArrayList<? extends IMediaElement> getGenreAlbumsList(IMediaItemCollection collection) {
        return DeviceMediaLibrary.getGenresAlbumDetails(context, collection.getId(), collection.getTitle(), collection.getItemCount());
    }

    public ArrayList<? extends IMediaElement> getArtistAlbumsTrackList(IMediaItemCollection collection, int index) {
        return DeviceMediaLibrary.getSongListOfArtistsAlbum(context, collection.getId(), collection.getItemAt(index).getId());
    }

    public ArrayList<? extends IMediaElement> getGenreAlbumsTrackList(IMediaItemCollection collection, int index) {
        return DeviceMediaLibrary.getSongListOfGenreAlbum(context, collection.getId(), collection.getTitle(), collection.getItemAt(index).getId(), collection.getItemAt(index).getTitle());
    }

    public boolean isFavoriteItem(IMediaElement item){
        return App.getFavoriteDBHelper().isFavouriteItems(item.getId());
    }

    public void removeItemToFavoriteList(IMediaElement item){
        App.getFavoriteDBHelper().removeSong(item.getId());
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
//        FlurryAnalyticHelper.logEvent(UtilAnalytics.Remove_Favorites);
        FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.Remove_Favorites);
    }

    public void addItemToFavoriteList(IMediaItem item){
        App.getFavoriteDBHelper().addSong(item);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
//        FlurryAnalyticHelper.logEvent(UtilAnalytics.Add_To_Favorites);
        FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.Add_To_Favorites);

    }

    public ArrayList<String> getArtUrlList(MediaItemCollection collection) {
        switch (collection.getItemType()){
            case ItemType.ARTIST:
                return DeviceMediaLibrary.getArtistsArtList(context, collection.getId(), collection.getTitle());
            case ItemType.PLAYLIST:
                return DeviceMediaLibrary.getPlaylistArtList(context, collection.getId(), collection.getTitle());
            case ItemType.GENRE:
                return DeviceMediaLibrary.getGenreArtList(context, collection.getId(), collection.getTitle());
            case ItemType.BOOM_PLAYLIST:
                return App.getBoomPlayListHelper().getBoomPlayListArtList(collection.getId());
            case ItemType.RECENT_PLAYED:
                return App.getUPNEXTDBHelper().getRecentArtList();
            case ItemType.FAVOURITE:
                return App.getFavoriteDBHelper().getFavouriteArtList();
            default:
                break;
        }
        return null;
    }

    public void removeCloudMediaItemList(@MediaType int mediaType) {
        App.getCloudMediaItemDBHelper().clearList(mediaType);
    }

    public void addSongsToCloudItemList(@MediaType int mediaType, ArrayList<IMediaElement> fileList) {
        App.getCloudMediaItemDBHelper().addSongs(mediaType, fileList);
    }

    public int getRecentPlayedItemCount(){
        return App.getUPNEXTDBHelper().getRecentPlayedCount();
    }

    public ArrayList<? extends IMediaElement> getRecentPlayedList() {
        return App.getUPNEXTDBHelper().getRecentPlayedItemList();
    }

    public void setRecentPlayedItem(IMediaElement recentPlayedItem) {
        if ( recentPlayedItem.getMediaType() != MediaType.RADIO ) {
            App.getUPNEXTDBHelper().addItemsToRecentPlayedList(recentPlayedItem);
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
        }
    }
}
