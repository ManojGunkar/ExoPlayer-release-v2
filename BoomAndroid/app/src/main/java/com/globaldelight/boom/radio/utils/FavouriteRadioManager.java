package com.globaldelight.boom.radio.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.globaldelight.boom.radio.webconnector.responsepojo.RadioStationResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Manoj Kumar on 12-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class FavouriteRadioManager {

    private final static String FAV_SHARED_PREF="FAV_SHARED_PREF";
    private final static String KEY_FAV_RADIO="KEY_FAV_RADIO";

    public static final int MODE = Context.MODE_PRIVATE;

    private static FavouriteRadioManager instance;

    private SharedPreferences mSharedPreferences;

    private Context mContext;

    private ArrayList<RadioStationResponse.Content> mContents=new ArrayList<>();
    private HashSet<String> mFavIds=new HashSet<>();

    public boolean containsRadioStation(RadioStationResponse.Content content){
        return mFavIds.contains(content.getId());
    }

    public void addRadioStation(RadioStationResponse.Content content){

       if (containsRadioStation(content)){
          return;
       }
        mContents.add(content);
        mFavIds.add(content.getId());
        saveRadioStation(mContents);

    }

    public void removeRadioSation(RadioStationResponse.Content content){
        if (containsRadioStation(content)){
            mContents.remove(content);
            mFavIds.remove(content.getId());
            saveRadioStation(mContents);
        }
    }

    public List<RadioStationResponse.Content> getRadioStations(){
        return mContents;
    }

    private FavouriteRadioManager(Context context){
        this.mContext=context;
        mSharedPreferences=context.getSharedPreferences(FAV_SHARED_PREF,MODE);
    }

    public static FavouriteRadioManager getInstance(Context context){
        if (instance==null){
            instance=new FavouriteRadioManager(context.getApplicationContext());
            instance.getFavRadioStation();
        }
        return instance;
    }

    private SharedPreferences.Editor getEditor(){
        return mSharedPreferences.edit();

    }

    private void saveRadioStation(List<RadioStationResponse.Content> favRadioStations){
        Gson gson = new Gson();
        String jsonFav = gson.toJson(favRadioStations);
        SharedPreferences.Editor editor=getEditor();
        editor.putString(KEY_FAV_RADIO, jsonFav);
        editor.apply();
    }

    private void getFavRadioStation() {

        if (mSharedPreferences.contains(KEY_FAV_RADIO)) {
            String jsonFavorites = mSharedPreferences.getString(KEY_FAV_RADIO, null);
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
