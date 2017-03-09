package com.globaldelight.boom.Media;

import android.content.Context;
import android.content.Intent;

import com.globaldelight.boom.App;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.task.PlayerEvents;

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
            handler = new MediaController(context);
        }
        return handler;
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getPlayList() {
        return DeviceMediaQuery.getPlayList(context);
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getPlayListTrackList(IMediaItemCollection collection) {
        return DeviceMediaQuery.getPlaylistSongs(context, collection.getItemId(), collection.getItemTitle());
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getPlayListTrackList(long parentId, String parentTitle) {
        return DeviceMediaQuery.getPlaylistSongs(context, parentId, parentTitle);
    }

    @Override
    public void createBoomPlaylist(String playlist) {
        App.getBoomPlayListHelper().createPlaylist(playlist);
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

    @Override
    public void addSongToBoomPlayList(long itemId, ArrayList<? extends IMediaItemBase> mediaElement, boolean isUpdate) {
        App.getBoomPlayListHelper().addSongs(mediaElement, itemId, isUpdate);
        context.sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }

    @Override
    public void removeSongToPlayList(long itemId, int playlistId) {
        App.getBoomPlayListHelper().removeSong(itemId, playlistId);
        context.sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_BOOM_ITEM_LIST));
        context.sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }

    @Override
    public void deleteBoomPlaylist(long itemId) {
        App.getBoomPlayListHelper().deletePlaylist(itemId);
        context.sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }

    @Override
    public void renamePlaylist(String playlistTitle, long itemId) {
        App.getBoomPlayListHelper().renamePlaylist(playlistTitle, itemId);
        context.sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_BOOM_ITEM_LIST));
        context.sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getAlbumList() {
        return DeviceMediaQuery.getAlbumList(context);
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getSongList() {
        return DeviceMediaQuery.getSongList(context);
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getArtistsList() {
        return DeviceMediaQuery.getArtistList(context);
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getGenreList() {
        return DeviceMediaQuery.getGenreList(context);
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
    public ArrayList<? extends IMediaItemBase> getCloudList(MediaType mediaType) {
        return App.getCloudMediaItemDBHelper().getSongList(mediaType);
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getAlbumTrackList(IMediaItemCollection collection) {
        return DeviceMediaQuery.getAlbumDetail(context, collection.getItemId(), collection.getItemTitle());
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getArtistTrackList(IMediaItemCollection collection) {
        return DeviceMediaQuery.getSongListOfArtist(context, collection.getItemId(), collection.getItemTitle());
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getGenreTrackList(IMediaItemCollection collection) {
        return DeviceMediaQuery.getSongListOfGenre(context, collection.getItemId(), collection.getItemTitle());
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getArtistAlbumsList(IMediaItemCollection collection) {
        return DeviceMediaQuery.getArtistsAlbumDetails(context, collection.getItemId(), collection.getItemTitle(), collection.getItemCount());
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getGenreAlbumsList(IMediaItemCollection collection) {
        return DeviceMediaQuery.getGenresAlbumDetails(context, collection.getItemId(), collection.getItemTitle(), collection.getItemCount());
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getArtistAlbumsTrackList(IMediaItemCollection collection) {
        return DeviceMediaQuery.getSongListOfArtistsAlbum(context, collection.getItemId(), collection.getItemTitle(), collection.getMediaElement().get(collection.getCurrentIndex()).getItemId(), collection.getMediaElement().get(collection.getCurrentIndex()).getItemTitle());
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getGenreAlbumsTrackList(IMediaItemCollection collection) {
        return DeviceMediaQuery.getSongListOfGenreAlbum(context, collection.getItemId(), collection.getItemTitle(), collection.getMediaElement().get(collection.getCurrentIndex()).getItemId(), collection.getMediaElement().get(collection.getCurrentIndex()).getItemTitle());
    }

    @Override
    public boolean isFavoriteItem(long trackId){
        return App.getFavoriteDBHelper().isFavouriteItems(trackId);
    }

    @Override
    public void removeItemToFavoriteList(long trackId){
        App.getFavoriteDBHelper().removeSong(trackId);
        context.sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }

    @Override
    public void addItemToFavoriteList(IMediaItem item){
        App.getFavoriteDBHelper().addSong(item);
        context.sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }

    @Override
    public ArrayList<String> getArtUrlList(MediaItemCollection collection) {
        switch (collection.getItemType()){
            case ARTIST:
                return DeviceMediaQuery.getArtistsArtList(context, collection.getItemId(), collection.getItemTitle());
            case PLAYLIST:
                return DeviceMediaQuery.getPlaylistArtList(context, collection.getItemId(), collection.getItemTitle());
            case GENRE:
                return DeviceMediaQuery.getGenreArtList(context, collection.getItemId(), collection.getItemTitle());
            case BOOM_PLAYLIST:
                return App.getBoomPlayListHelper().getBoomPlayListArtList(collection.getItemId());
            case RECENT_PLAYED:
                return App.getUPNEXTDBHelper().getRecentArtList();
            case FAVOURITE:
                return App.getFavoriteDBHelper().getFavouriteArtList();
            default:
                break;
        }
        return null;
    }

    @Override
    public void removeCloudMediaItemList(MediaType mediaType) {
        App.getCloudMediaItemDBHelper().clearList(mediaType);
    }

    @Override
    public void addSongsToCloudItemList(MediaType mediaType, ArrayList<IMediaItemBase> fileList) {
        App.getCloudMediaItemDBHelper().addSongs(mediaType, fileList);
    }

    @Override
    public ArrayList<? extends IMediaItem> getAlbumTrackList(long itemId, String itemTitle) {
        return DeviceMediaQuery.getAlbumDetail(context, itemId, itemTitle);
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getArtistAlbumsTrackList(long parentId, String parentTitle, long itemId, String itemTitle) {
        return DeviceMediaQuery.getSongListOfArtistsAlbum(context, itemId, itemTitle, parentId, parentTitle);
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getArtistTrackList(long parentId, String parentTitle) {
        return DeviceMediaQuery.getSongListOfArtist(context, parentId, parentTitle);
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getGenreAlbumsTrackList(long parentId, String parentTitle, long itemId, String itemTitle) {
        return DeviceMediaQuery.getSongListOfGenreAlbum(context, parentId, parentTitle, itemId, itemTitle);
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getGenreTrackList(long parentId, String parentTitle) {
        return DeviceMediaQuery.getSongListOfGenre(context, parentId, parentTitle);
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
        context.sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_PLAYLIST));
    }
}
