package com.globaldelight.boom.collection.cloud;

import android.content.Context;
import android.os.Handler;

import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.collection.base.IMediaElement;
import com.globaldelight.boom.playbackEvent.utils.MediaType;
import com.globaldelight.boom.utils.helpers.GoogleDriveHandler;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 11-01-17.
 */

public class GoogleDriveMediaList {

    private ArrayList<IMediaElement> fileList;
    private boolean isAllSongsLoaded = false;
    private IGoogleDriveMediaUpdater googleDriveMediaUpdater;
    private static GoogleDriveMediaList sInstance;
    private GoogleDriveHandler mGoogleDriveHandler;
    private Context mContext;
    private Handler postMessage;

    private GoogleDriveMediaList(Context context){
        this.mContext = context;
        fileList = new ArrayList<IMediaElement>();
        postMessage = new Handler();
    }

    public static GoogleDriveMediaList getInstance(Context context){
        if(null == sInstance){
            sInstance = new GoogleDriveMediaList(context.getApplicationContext());
        }
        return sInstance;
    }

    public void setGoogleDriveHandler(GoogleDriveHandler googleDriveHandler) {
        mGoogleDriveHandler = googleDriveHandler;
    }

    public GoogleDriveHandler getGoogleDriveHandler() {
        return mGoogleDriveHandler;
    }


    public boolean isLoaded() {
        return isAllSongsLoaded;
    }

    public ArrayList<IMediaElement> getGoogleDriveMediaList(){
        if(null == fileList)
            fileList = new ArrayList<>();

        if(null != fileList && fileList.size() == 0){
            fileList.addAll(MediaController.getInstance(mContext).getCloudList(MediaType.GOOGLE_DRIVE));
        }

        return fileList;
    }

    public void clearGoogleDriveMediaContent(){
        if(null != fileList) {
            fileList.clear();
            MediaController.getInstance(mContext).removeCloudMediaItemList(MediaType.GOOGLE_DRIVE);
            postMessage.post(new Runnable() {
                @Override
                public void run() {
                    if(null != googleDriveMediaUpdater)
                        googleDriveMediaUpdater.onClearList();
                }
            });
        }
        isAllSongsLoaded = false;
    }

    public void addFileInGoogleDriveMediaList(final IMediaElement entry){
        if(isAllSongsLoaded)
            clearGoogleDriveMediaContent();

        isAllSongsLoaded = false;
        fileList.add(entry);
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                if(null != googleDriveMediaUpdater)
                    googleDriveMediaUpdater.onMediaListUpdate();
            }
        });
    }

    public void finishGoogleDriveMediaLoading(){
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                if(null != googleDriveMediaUpdater)
                    googleDriveMediaUpdater.onFinishLoading();
            }
        });
        if(fileList.size() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MediaController.getInstance(mContext).addSongsToCloudItemList(MediaType.GOOGLE_DRIVE, fileList);
                }
            }).start();
        }
        isAllSongsLoaded = true;
    }

    public void onErrorOccurred(final String e){
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                if(null != googleDriveMediaUpdater)
                    googleDriveMediaUpdater.onError(e);
            }
        });
    }

    public void onEmptyList(){
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                if(null != googleDriveMediaUpdater)
                    googleDriveMediaUpdater.onEmptyList();
            }
        });
    }

    public void onRequestCancelled(){
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                if(null != googleDriveMediaUpdater)
                    googleDriveMediaUpdater.onRequestCancelled();
            }
        });
    }

    public void setGoogleDriveMediaUpdater(IGoogleDriveMediaUpdater googleDriveMediaUpdater){
        this.googleDriveMediaUpdater = googleDriveMediaUpdater;
    }

    public interface IGoogleDriveMediaUpdater {
        void onMediaListUpdate();
        void onFinishLoading();
        void onRequestCancelled();
        void onError(String e);
        void onEmptyList();
        void onClearList();
    }
}
