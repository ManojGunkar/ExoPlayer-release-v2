package com.globaldelight.boom.collection.local;

import android.content.Context;
import android.os.Handler;

import com.globaldelight.boom.collection.local.callback.IMediaItemBase;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 11-01-17.
 */

public class FavouriteMediaList {

    private ArrayList<IMediaItemBase> fileList;
    private IFavouriteUpdater favouriteUpdater;
    private static FavouriteMediaList handler;
    private Handler postMessage;

    private FavouriteMediaList(Context context){
        fileList = new ArrayList<IMediaItemBase>();
        postMessage = new Handler();
    }

    public static FavouriteMediaList getFavouriteListInstance(Context context){
        if(null == handler){
            handler = new FavouriteMediaList(context);
        }
        return handler;
    }

    public void addFilesInFavouriteList(ArrayList<? extends IMediaItemBase> entries){
        fileList.addAll(entries);
    }

    public ArrayList<IMediaItemBase> getFavouriteMediaList(){
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
                favouriteUpdater.onUpdateFavouriteList();
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
