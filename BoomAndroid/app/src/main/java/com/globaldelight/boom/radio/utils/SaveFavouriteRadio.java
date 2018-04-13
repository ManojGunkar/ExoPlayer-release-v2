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
public class SaveFavouriteRadio {

    private final static String FAV_SHARED_PREF="FAV_SHARED_PREF";
    private final static String KEY_FAV_RADIO="KEY_FAV_RADIO";

    public static final int MODE = Context.MODE_PRIVATE;

    private static SaveFavouriteRadio instance;

    private SharedPreferences mSharedPreferences;

    private Context mContext;

    private  SaveFavouriteRadio(Context context){
        this.mContext=context;
        mSharedPreferences=context.getSharedPreferences(FAV_SHARED_PREF,MODE);
    }

    public static SaveFavouriteRadio getInstance(Context context){
        if (instance==null)instance=new SaveFavouriteRadio(context);
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
        editor.commit();
    }

    public void addFavRadioStation(RadioStationResponse.Content radioStation){
        List<RadioStationResponse.Content> favRadioStation = getFavRadioStation();

        if(favRadioStation == null) favRadioStation = new ArrayList<>();
        if (favRadioStation.contains(radioStation)) return;
        favRadioStation.add(radioStation);
        saveRadioStation(favRadioStation);
    }

    public void removeFavRadioStation(RadioStationResponse.Content code) {
        ArrayList<RadioStationResponse.Content> favRadioStations = getFavRadioStation();
        if (favRadioStations != null) {
            favRadioStations.remove(code);
            saveRadioStation(favRadioStations);
        }
    }


    public ArrayList<RadioStationResponse.Content> getFavRadioStation() {
        List<RadioStationResponse.Content> favRadioStations;

        if (mSharedPreferences.contains(KEY_FAV_RADIO)) {
            String jsonFavorites = mSharedPreferences.getString(KEY_FAV_RADIO, null);
            Gson gson = new Gson();
            RadioStationResponse.Content[] favoriteItems = gson.fromJson(jsonFavorites,
                    RadioStationResponse.Content[].class);

            favRadioStations = Arrays.asList(favoriteItems);
            favRadioStations = new ArrayList<>(favRadioStations);
        } else
            return null;

        HashSet<RadioStationResponse.Content> hashSet = new HashSet<>();
        hashSet.addAll(favRadioStations);
        favRadioStations.clear();
        favRadioStations.addAll(hashSet);

        return (ArrayList<RadioStationResponse.Content>) favRadioStations;
    }


}
