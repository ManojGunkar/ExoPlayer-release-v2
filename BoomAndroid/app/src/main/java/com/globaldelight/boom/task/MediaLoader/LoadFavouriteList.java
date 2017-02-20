package com.globaldelight.boom.task.MediaLoader;

import android.content.Context;
import android.os.AsyncTask;

import com.globaldelight.boom.Media.MediaController;
import com.globaldelight.boom.data.MediaCallback.FavouriteMediaList;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 12-01-17.
 */

public class LoadFavouriteList extends AsyncTask<Void, Integer, ArrayList<? extends IMediaItemBase>> {
    private Context mContext;
    private FavouriteMediaList favouriteMediaList;
    public LoadFavouriteList (Context context){
        this.mContext = context;
        favouriteMediaList = FavouriteMediaList.getFavouriteListInstance(mContext);
    }
    @Override
    protected ArrayList<? extends IMediaItemBase> doInBackground(Void... params) {
        return MediaController.getInstance(mContext).getFavoriteList();
    }

    @Override
    protected void onPostExecute(ArrayList<? extends IMediaItemBase> iMediaItemList) {
        super.onPostExecute(iMediaItemList);
        favouriteMediaList.addFilesInFavouriteList(iMediaItemList);
        favouriteMediaList.finishFavouriteLoading();
    }
}