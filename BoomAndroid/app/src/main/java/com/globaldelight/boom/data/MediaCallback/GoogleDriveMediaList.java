package com.globaldelight.boom.data.MediaCallback;

import android.content.Context;
import android.os.Handler;

import com.globaldelight.boom.Media.MediaController;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.Media.MediaType;
import com.globaldelight.boom.utils.helpers.GoogleDriveHandler;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 11-01-17.
 */

public class GoogleDriveMediaList {

    private ArrayList<IMediaItemBase> fileList;
    private static boolean isAllSongsLoaded = false;
    private IGoogleDriveMediaUpdater googleDriveMediaUpdater;
    private static GoogleDriveMediaList handler;
    private static GoogleDriveHandler mGoogleDriveHandler;
    private Context mContext;
    private Handler postMessage;

    private GoogleDriveMediaList(Context context){
        this.mContext = context;
        fileList = new ArrayList<IMediaItemBase>();
        postMessage = new Handler();
    }

    public static GoogleDriveMediaList geGoogleDriveMediaListInstance(Context context){
        if(null == handler){
            handler = new GoogleDriveMediaList(context);
        }
        return handler;
    }

    public static void setGoogleDriveHandler(GoogleDriveHandler googleDriveHandler) {
        mGoogleDriveHandler = googleDriveHandler;
    }

    public static GoogleDriveHandler getGoogleDriveHandler() {
        return mGoogleDriveHandler;
    }

    public ArrayList<IMediaItemBase> getGoogleDriveMediaList(){
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

    public void addFileInGoogleDriveMediaList(final IMediaItemBase entry){
        if(isAllSongsLoaded)
            clearGoogleDriveMediaContent();

        isAllSongsLoaded = false;
        fileList.add(entry);
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                if(null != googleDriveMediaUpdater)
                    googleDriveMediaUpdater.onGoogleDriveMediaListUpdate();
            }
        });
    }

    public void finishGoogleDriveMediaLoading(){
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                if(null != googleDriveMediaUpdater)
                    googleDriveMediaUpdater.onFinishListLoading();
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
        if(null != googleDriveMediaUpdater) {
            this.googleDriveMediaUpdater = googleDriveMediaUpdater;
        }
    }

    public interface IGoogleDriveMediaUpdater {
        void onGoogleDriveMediaListUpdate();
        void onFinishListLoading();
        void onRequestCancelled();
        void onError(String e);
        void onEmptyList();
        void onClearList();
    }
}
