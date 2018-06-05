package com.globaldelight.boom.radio.podcast;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import com.globaldelight.boom.radio.webconnector.model.RadioStationResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Manoj Kumar on 12-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class FavouritePodcastManager {

    public static final String FAVOURITES_PODCAST_CHANGED = "con.globaldelight.FAVOURITES_PODCAST_CHANGED";

    private final static String FAV_PODCAST_SHARED_PREF="FAV_PODCAST_SHARED_PREF";
    private final static String KEY_FAV_PODCAST="KEY_FAV_PODCAST";

    public static final int MODE = Context.MODE_PRIVATE;

    private static FavouritePodcastManager instance;

    private SharedPreferences mSharedPreferences;

    private Context mContext;

    private ArrayList<RadioStationResponse.Content> mContents=new ArrayList<>();
    private HashSet<String> mFavIds=new HashSet<>();

    public boolean containPodcast(RadioStationResponse.Content content){
        return mFavIds.contains(content.getId());
    }

    public void addPodcast(RadioStationResponse.Content content){

       if (containPodcast(content)){
          return;
       }
        mContents.add(content);
        mFavIds.add(content.getId());
        savePodcast(mContents);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(FAVOURITES_PODCAST_CHANGED));

    }

    public void removePodcast(RadioStationResponse.Content content){
        if (containPodcast(content)){
            mContents.remove(content);
            mFavIds.remove(content.getId());
            savePodcast(mContents);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(FAVOURITES_PODCAST_CHANGED));
        }
    }

    public List<RadioStationResponse.Content> getpodcast(){
        return mContents;
    }

    private FavouritePodcastManager(Context context){
        this.mContext=context;
        mSharedPreferences=context.getSharedPreferences(FAV_PODCAST_SHARED_PREF,MODE);
    }

    public static FavouritePodcastManager getInstance(Context context){
        if (instance==null){
            instance=new FavouritePodcastManager(context.getApplicationContext());
            instance.getFavPodcast();
        }
        return instance;
    }

    private SharedPreferences.Editor getEditor(){
        return mSharedPreferences.edit();

    }

    private void savePodcast(List<RadioStationResponse.Content> favRadioStations){
        Gson gson = new Gson();
        String jsonFav = gson.toJson(favRadioStations);
        SharedPreferences.Editor editor=getEditor();
        editor.putString(KEY_FAV_PODCAST, jsonFav);
        editor.apply();
    }

    private void getFavPodcast() {

        if (mSharedPreferences.contains(KEY_FAV_PODCAST)) {
            String jsonFavorites = mSharedPreferences.getString(KEY_FAV_PODCAST, null);
            Gson gson = new Gson();
            RadioStationResponse.Content[] favoriteItems = gson.fromJson(jsonFavorites,
                    RadioStationResponse.Content[].class);

            mContents.addAll(Arrays.asList(favoriteItems));
            for (RadioStationResponse.Content content: favoriteItems) {
                mFavIds.add(content.getId());
            }
        }


    }


}
