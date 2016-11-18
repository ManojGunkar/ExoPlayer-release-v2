package com.globaldelight.boom.handler.search;

import android.content.Context;

import com.globaldelight.boom.data.DeviceMediaLibrary.DeviceMediaQuery;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 14-11-16.
 */

public class  Search {

    private ArrayList<? extends IMediaItemBase> songResult;
    private ArrayList<? extends IMediaItemBase> albumResult;
    private ArrayList<? extends IMediaItemBase> artistResult;
    private String query;
    private Context context;
    private int songCount, albumCount, artistCount;
    private boolean isPartialResult;

    public boolean getSearchResult(Context context, String query, boolean partial) {
        this.query = query;
        this.context = context;
        this.isPartialResult = partial;
        boolean result = true;

        setSongResult();
        setAlbumResult();
        setArtistResult();
        return result;
    }

    public String getQuery(){
        return query;
    }

    public ArrayList<? extends IMediaItemBase> getResultSongList(Context context, String query, boolean partial){
        this.query = query;
        this.context = context;
        this.isPartialResult = partial;
        setSongResult();
        return getSongResult();
    }

    public ArrayList<? extends IMediaItemBase> getResultAlbumList(Context context, String query, boolean partial){
        this.query = query;
        this.context = context;
        this.isPartialResult = partial;
        setAlbumResult();
        return getAlbumResult();
    }

    public ArrayList<? extends IMediaItemBase> getResultArtistList(Context context, String query, boolean partial){
        this.query = query;
        this.context = context;
        this.isPartialResult = partial;
        setArtistResult();
        return getArtistResult();
    }

    public void setSongResult() {
        SearchResult result = DeviceMediaQuery.searchSong(context, query, isPartialResult);
        this.songResult = result.getItemList();
        this.songCount = result.getCount();
    }

    public void setAlbumResult() {
        SearchResult result = DeviceMediaQuery.searchAlbum(context, query, isPartialResult);
        this.albumResult = result.getItemList();
        this.albumCount = result.getCount();
    }

    public void setArtistResult() {
        SearchResult result = DeviceMediaQuery.searchArtist(context, query, isPartialResult);
        this.artistResult = result.getItemList();
        this.artistCount = result.getCount();
    }

    public ArrayList<? extends IMediaItemBase> getSongResult() {
        return songResult;
    }

    public ArrayList<? extends IMediaItemBase> getAlbumResult() {
        return albumResult;
    }

    public ArrayList<? extends IMediaItemBase> getArtistResult() {
        return artistResult;
    }

    public int getSongCount() {
        return songCount;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    public int getAlbumCount() {
        return albumCount;
    }

    public void setAlbumCount(int albumCount) {
        this.albumCount = albumCount;
    }

    public int getArtistCount() {
        return artistCount;
    }

    public void setArtistCount(int artistCount) {
        this.artistCount = artistCount;
    }
}
