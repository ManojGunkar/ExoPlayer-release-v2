package com.globaldelight.boom.app.loaders;

import android.content.Context;
import android.os.AsyncTask;

import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.collection.local.RecentPlayedMediaList;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 12-01-17.
 */

public class LoadRecentPlayedList extends AsyncTask<Void, Integer, ArrayList<? extends IMediaItemBase>> {
    private Context mContext;
    private RecentPlayedMediaList recentPlayedMediaList;
    public LoadRecentPlayedList(Context context){
        this.mContext = context;
        recentPlayedMediaList = RecentPlayedMediaList.getInstance(mContext);
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