package com.globaldelight.boom.collection.local;

import android.content.Context;
import android.os.Handler;

import com.globaldelight.boom.collection.base.IMediaElement;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 11-01-17.
 */

public class FavouriteMediaList {

    private ArrayList<IMediaElement> fileList;
    private IFavouriteUpdater favouriteUpdater;
    private static FavouriteMediaList instance;
    private Handler postMessage;

    private FavouriteMediaList(Context context){
        fileList = new ArrayList<IMediaElement>();
        postMessage = new Handler();
    }

    public static FavouriteMediaList getInstance(Context context){
        if(null == instance){
            instance = new FavouriteMediaList(context.getApplicationContext());
        }
        return instance;
    }

    public void addFilesInFavouriteList(ArrayList<? extends IMediaElement> entries){
        fileList.addAll(entries);
    }

    public ArrayList<IMediaElement> getFavouriteMediaList(){
        return fileList;
    }

    public int getCount(){
        return fileList.size();
    }

    public void clearFavouriteContent(){
        fileList.clear();
    }

    public void finishFavouriteLoading(){
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                if (favouriteUpdater != null ) {
                    favouriteUpdater.onUpdateFavouriteList();
                }
            }
        });
    }

    public void setFavouriteUpdater(IFavouriteUpdater favouriteUpdater){
        this.favouriteUpdater = favouriteUpdater;
    }

    public interface IFavouriteUpdater {
        void onUpdateFavouriteList();
    }
}
