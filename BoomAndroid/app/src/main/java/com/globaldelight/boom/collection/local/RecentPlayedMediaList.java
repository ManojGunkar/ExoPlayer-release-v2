package com.globaldelight.boom.collection.local;

import android.content.Context;
import android.os.Handler;

import com.globaldelight.boom.collection.local.callback.IMediaItemBase;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 11-01-17.
 */

public class RecentPlayedMediaList {

    private ArrayList<IMediaItemBase> fileList;
    private IRecentPlayedUpdater recentPlayedUpdater;
    private static RecentPlayedMediaList handler;
    private Handler postMessage;

    private RecentPlayedMediaList(Context context){
        fileList = new ArrayList<IMediaItemBase>();
        postMessage = new Handler();
    }

    public static RecentPlayedMediaList getRecentPlayedListInstance(Context context){
        if(null == handler){
            handler = new RecentPlayedMediaList(context);
        }
        return handler;
    }

    public void addFilesInRecentPlayedList(ArrayList<? extends IMediaItemBase> entries){
        fileList.addAll(entries);
    }

    public ArrayList<IMediaItemBase> getRecentPlayedMediaList(){
        return fileList;
    }

    public int getCount(){
        return fileList.size();
    }

    public void clearRecentPlayedContent(){
        fileList.clear();
    }

    public void finishRecentPlayedLoading(){
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                recentPlayedUpdater.onUpdateRecentPlayedList();
            }
        });
    }

    public void setRecentPlayedUpdater(IRecentPlayedUpdater recentPlayedUpdater){
        this.recentPlayedUpdater = recentPlayedUpdater;
    }

    public interface IRecentPlayedUpdater {
        void onUpdateRecentPlayedList();
    }
}
