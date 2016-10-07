package com.player.data.MediaLibrary;

import android.content.Context;

/**
 * Created by Rahul Agarwal on 8/16/2016.
 */
public class SearchHandler {

    private static SearchHandler handler;
    private Context context;

    private SearchHandler(Context context){
        this.context = context;
    }
    public static SearchHandler getInstance(Context context) {
        if(handler == null){
            handler = new SearchHandler(context);
        }
        return handler;
    }

    public void doSearch(MediaType mediaType, String query){
        MediaController.getInstance(context).doSearch(mediaType, query);
    }
}
