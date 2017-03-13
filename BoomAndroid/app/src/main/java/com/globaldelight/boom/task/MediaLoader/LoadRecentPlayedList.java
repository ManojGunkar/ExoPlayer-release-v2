package com.globaldelight.boom.task.MediaLoader;

import android.content.Context;
import android.os.AsyncTask;

import com.globaldelight.boom.Media.MediaController;
import com.globaldelight.boom.data.MediaCallback.FavouriteMediaList;
import com.globaldelight.boom.data.MediaCallback.RecentPlayedMediaList;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 12-01-17.
 */

public class LoadRecentPlayedList extends AsyncTask<Void, Integer, ArrayList<? extends IMediaItemBase>> {
    private Context mContext;
    private RecentPlayedMediaList recentPlayedMediaList;
    public LoadRecentPlayedList(Context context){
        this.mContext = context;
        recentPlayedMediaList = RecentPlayedMediaList.getRecentPlayedListInstance(mContext);
    }
    @Override
    protected ArrayList<? extends IMediaItemBase> doInBackground(Void... params) {
        return MediaController.getInstance(mContext).getRecentPlayedList();
    }

    @Override
    protected void onPostExecute(ArrayList<? extends IMediaItemBase> iMediaItemList) {
        super.onPostExecute(iMediaItemList);
        recentPlayedMediaList.addFilesInRecentPlayedList(iMediaItemList);
        recentPlayedMediaList.finishRecentPlayedLoading();
    }
}