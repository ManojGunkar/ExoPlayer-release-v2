package com.globaldelight.boom.app.adapters.search.utils;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.base.IMediaElement;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 14-11-16.
 */

public class SearchResult {
    private ArrayList<? extends IMediaElement> itemList;
    private int count;
    public static String SONGS = App.getApplication().getString(R.string.songs);
    public static String ALBUMS = App.getApplication().getString(R.string.albums);
    public static String ARTISTS = App.getApplication().getString(R.string.artists);

    public SearchResult(ArrayList<? extends IMediaElement> itemList, int count){
        this.itemList = itemList;
        this.count = count;
    }

    public ArrayList<? extends IMediaElement> getItemList() {
        return itemList;
    }

    public int getCount() {
        return count;
    }
}
