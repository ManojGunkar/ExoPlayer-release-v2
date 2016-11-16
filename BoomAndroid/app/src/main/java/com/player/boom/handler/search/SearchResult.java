package com.player.boom.handler.search;

import com.player.boom.data.MediaCollection.IMediaItemBase;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 14-11-16.
 */

public class SearchResult {
    private ArrayList<? extends IMediaItemBase> itemList;
    private int count;
    public static String SONGS = "Songs";
    public static String ALBUMS = "Albums";
    public static String ARTISTS = "Artists";

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
