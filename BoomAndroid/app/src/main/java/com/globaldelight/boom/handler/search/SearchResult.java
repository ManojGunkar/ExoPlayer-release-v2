package com.globaldelight.boom.handler.search;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 14-11-16.
 */

public class SearchResult {
    private ArrayList<? extends IMediaItemBase> itemList;
    private int count;
    public static String SONGS = App.getApplication().getString(R.string.song);
    public static String ALBUMS = App.getApplication().getString(R.string.albums);
    public static String ARTISTS = App.getApplication().getString(R.string.artists);

    public SearchResult(ArrayList<? extends IMediaItemBase> itemList, int count){
        this.itemList = itemList;
        this.count = count;
    }

    public ArrayList<? extends IMediaItemBase> getItemList() {
        return itemList;
    }

    public int getCount() {
        return count;
    }
}
