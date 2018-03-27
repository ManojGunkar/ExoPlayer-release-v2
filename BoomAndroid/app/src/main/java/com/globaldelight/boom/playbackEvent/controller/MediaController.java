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
import com.globaldelight.boom.collection.local.callback.IMediaItem;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.collection.local.callback.IMediaItemCollection;
import com.globaldelight.boom.playbackEvent.controller.callbacks.IMediaController;
import com.globaldelight.boom.app.receivers.actions.PlayerEvents;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 18-02-17.
 */

public class MediaController implements IMediaController {

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

    @Override
    public ArrayList<? extends IMediaItemBase> getPlayList() {
        return DeviceMediaLibrary.getPlayList(context);
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getPlayListTrackList(IMediaItemCollection collection) {
        return DeviceMediaLibrary.getPlaylistSongs(context, collection.getItemId(), collection.getItemTitle());
    }

    @Override
    public void createBoomPlaylist(String playlist) {
        App.getBoomPlayListHelper().createPlaylist(playlist);
//        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_CREATED_NEW_PLAYLIST);
        FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.EVENT_CREATED_NEW_PLAYLIST);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getBoomPlayList() {
        return App.getBoomPlayListHelper().getAllPlaylist();
    }

    @Override
    public IMediaItemCollection getBoomPlayListItem(long itemId) {
        return App.getBoomPlayListHelper().gePlaylist(itemId);
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getBoomPlayListTrackList(long id) {
        return App.getBoomPlayListHelper().getPlaylistSongs(id);
    }

    public boolean isAlreadyAdded(long playlistId, long trackId) {
        return App.getBoomPlayListHelper().isAlreadyAddedToPlaylist(playlistId, trackId);
    }

    @Override
    public void addSongToBoomPlayList(long itemId, ArrayList<? extends IMediaItemBase> mediaElement, boolean isUpdate) {
        App.getBoomPlayListHelper().addSongs(mediaElement, itemId, isUpdate);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }

    @Override
    public void removeSongToPlayList(long itemId, int playlistId) {
        App.getBoomPlayListHelper().removeSong(itemId, playlistId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_BOOM_ITEM_LIST));
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }

    @Override
    public void deleteBoomPlaylist(long itemId) {
        App.getBoomPlayListHelper().deletePlaylist(itemId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }

    @Override
    public void renamePlaylist(String playlistTitle, long itemId) {
        App.getBoomPlayListHelper().renamePlaylist(playlistTitle, itemId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_BOOM_ITEM_LIST));
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getAlbumList() {
        return DeviceMediaLibrary.getAlbumList(context);
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getSongList() {
        return DeviceMediaLibrary.getSongList(context);
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getArtistsList() {
        return DeviceMediaLibrary.getArtistList(context);
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getGenreList() {
        return DeviceMediaLibrary.getGenreList(context);
    }

    @Override
    public int getFavouriteCount(){
        return App.getFavoriteDBHelper().getFavouriteItemCount();
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getFavoriteList() {
        return App.getFavoriteDBHelper().getFavouriteItemList();
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getCloudList(@MediaType int mediaType) {
        return App.getCloudMediaItemDBHelper().getSongList(mediaType);
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getAlbumTrackList(IMediaItemCollection collection) {
        return DeviceMediaLibrary.getAlbumDetail(context, collection.getItemId(), collection.getItemTitle());
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getArtistTrackList(IMediaItemCollection collection) {
        return DeviceMediaLibrary.getSongListOfArtist(context, collection.getItemId(), collection.getItemTitle());
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getGenreTrackList(IMediaItemCollection collection) {
        return DeviceMediaLibrary.getSongListOfGenre(context, collection.getItemId(), collection.getItemTitle());
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getArtistAlbumsList(IMediaItemCollection collection) {
        return DeviceMediaLibrary.getArtistsAlbumDetails(context, collection.getItemId(), collection.getItemTitle(), collection.getItemCount());
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getGenreAlbumsList(IMediaItemCollection collection) {
        return DeviceMediaLibrary.getGenresAlbumDetails(context, collection.getItemId(), collection.getItemTitle(), collection.getItemCount());
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getArtistAlbumsTrackList(IMediaItemCollection collection, int index) {
        return DeviceMediaLibrary.getSongListOfArtistsAlbum(context, collection.getItemId(), collection.getItemAt(index).getItemId());
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getGenreAlbumsTrackList(IMediaItemCollection collection, int index) {
        return DeviceMediaLibrary.getSongListOfGenreAlbum(context, collection.getItemId(), collection.getItemTitle(), collection.getItemAt(index).getItemId(), collection.getItemAt(index).getItemTitle());
    }

    @Override
    public boolean isFavoriteItem(long trackId){
        return App.getFavoriteDBHelper().isFavouriteItems(trackId);
    }

    @Override
    public void removeItemToFavoriteList(long trackId){
        App.getFavoriteDBHelper().removeSong(trackId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
//        FlurryAnalyticHelper.logEvent(UtilAnalytics.Remove_Favorites);
        FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.Remove_Favorites);
    }

    @Override
    public void addItemToFavoriteList(IMediaItem item){
        App.getFavoriteDBHelper().addSong(item);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
//        FlurryAnalyticHelper.logEvent(UtilAnalytics.Add_To_Favorites);
        FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.Add_To_Favorites);

    }

    @Override
    public ArrayList<String> getArtUrlList(MediaItemCollection collection) {
        switch (collection.getItemType()){
            case ItemType.ARTIST:
                return DeviceMediaLibrary.getArtistsArtList(context, collection.getItemId(), collection.getItemTitle());
            case ItemType.PLAYLIST:
                return DeviceMediaLibrary.getPlaylistArtList(context, collection.getItemId(), collection.getItemTitle());
            case ItemType.GENRE:
                return DeviceMediaLibrary.getGenreArtList(context, collection.getItemId(), collection.getItemTitle());
            case ItemType.BOOM_PLAYLIST:
                return App.getBoomPlayListHelper().getBoomPlayListArtList(collection.getItemId());
            case ItemType.RECENT_PLAYED:
                return App.getUPNEXTDBHelper().getRecentArtList();
            case ItemType.FAVOURITE:
                return App.getFavoriteDBHelper().getFavouriteArtList();
            default:
                break;
        }
        return null;
    }

    @Override
    public void removeCloudMediaItemList(@MediaType int mediaType) {
        App.getCloudMediaItemDBHelper().clearList(mediaType);
    }

    @Override
    public void addSongsToCloudItemList(@MediaType int mediaType, ArrayList<IMediaItemBase> fileList) {
        App.getCloudMediaItemDBHelper().addSongs(mediaType, fileList);
    }

    @Override
    public int getRecentPlayedItemCount(){
        return App.getUPNEXTDBHelper().getRecentPlayedCount();
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getRecentPlayedList() {
        return App.getUPNEXTDBHelper().getRecentPlayedItemList();
    }

    @Override
    public void setRecentPlayedItem(IMediaItemBase recentPlayedItem) {
        App.getUPNEXTDBHelper().addItemsToRecentPlayedList(recentPlayedItem);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }
}
