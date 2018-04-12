package com.globaldelight.boom.app.loaders;

import android.content.Context;
import android.os.AsyncTask;

import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.collection.local.FavouriteMediaList;
import com.globaldelight.boom.collection.base.IMediaElement;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 12-01-17.
 */

public class LoadFavouriteList extends AsyncTask<Void, Integer, ArrayList<? extends IMediaElement>> {
    private Context mContext;
    private FavouriteMediaList favouriteMediaList;
    public LoadFavouriteList (Context context){
        this.mContext = context;
        favouriteMediaList = FavouriteMediaList.getInstance(mContext);
    }
    @Override
    protected ArrayList<? extends IMediaElement> doInBackground(Void... params) {
        return MediaController.getInstance(mContext).getFavoriteList();
    }

    @Override
    protected void onPostExecute(ArrayList<? extends IMediaElement> iMediaItemList) {
        super.onPostExecute(iMediaItemList);
        favouriteMediaList.addFilesInFavouriteList(iMediaItemList);
        favouriteMediaList.finishFavouriteLoading();
    }
}