package com.globaldelight.boom.app.adapters.search.utils;

import android.content.Context;

import com.globaldelight.boom.playbackEvent.utils.DeviceMediaLibrary;
import com.globaldelight.boom.collection.base.IMediaElement;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 14-11-16.
 */

public class  Search {

    private ArrayList<? extends IMediaElement> songResult;
    private ArrayList<? extends IMediaElement> albumResult;
    private ArrayList<? extends IMediaElement> artistResult;
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

    public ArrayList<? extends IMediaElement> getResultSongList(Context context, String query, boolean partial){
        this.query = query;
        this.context = context;
        this.isPartialResult = partial;
        setSongResult();
        return getSongResult();
    }

    public ArrayList<? extends IMediaElement> getResultAlbumList(Context context, String query, boolean partial){
        this.query = query;
        this.context = context;
        this.isPartialResult = partial;
        setAlbumResult();
        return getAlbumResult();
    }

    public ArrayList<? extends IMediaElement> getResultArtistList(Context context, String query, boolean partial){
        this.query = query;
        this.context = context;
        this.isPartialResult = partial;
        setArtistResult();
        return getArtistResult();
    }

    public void setSongResult() {
        SearchResult result = DeviceMediaLibrary.searchSong(context, query, isPartialResult);
        this.songResult = result.getItemList();
        this.songCount = result.getCount();
    }

    public void setAlbumResult() {
        SearchResult result = DeviceMediaLibrary.searchAlbum(context, query, isPartialResult);
        this.albumResult = result.getItemList();
        this.albumCount = result.getCount();
    }

    public void setArtistResult() {
        SearchResult result = DeviceMediaLibrary.searchArtist(context, query, isPartialResult);
        this.artistResult = result.getItemList();
        this.artistCount = result.getCount();
    }

    public ArrayList<? extends IMediaElement> getSongResult() {
        return songResult;
    }

    public ArrayList<? extends IMediaElement> getAlbumResult() {
        return albumResult;
    }

    public ArrayList<? extends IMediaElement> getArtistResult() {
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
