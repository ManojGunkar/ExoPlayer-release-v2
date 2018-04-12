package com.globaldelight.boom.collection.local;

import android.content.Context;
import android.os.Handler;

import com.globaldelight.boom.collection.base.IMediaElement;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 11-01-17.
 */

public class RecentPlayedMediaList {

    private ArrayList<IMediaElement> fileList;
    private IRecentPlayedUpdater recentPlayedUpdater;
    private static RecentPlayedMediaList instance;
    private Handler postMessage;

    private RecentPlayedMediaList(Context context){
        fileList = new ArrayList<IMediaElement>();
        postMessage = new Handler();
    }

    public static RecentPlayedMediaList getInstance(Context context){
        if(null == instance){
            instance = new RecentPlayedMediaList(context.getApplicationContext());
        }
        return instance;
    }

    public void addFilesInRecentPlayedList(ArrayList<? extends IMediaElement> entries){
        fileList.addAll(entries);
    }

    public ArrayList<IMediaElement> getRecentPlayedMediaList(){
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
                if ( recentPlayedUpdater != null ) {
                    recentPlayedUpdater.onUpdateRecentPlayedList();
                }
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
