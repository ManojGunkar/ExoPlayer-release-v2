package com.globaldelight.boom.data.MediaCallback;

import android.content.Context;
import android.os.Handler;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.utils.helpers.GoogleDriveHandler;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 11-01-17.
 */

public class GoogleDriveMediaList {

    private static ArrayList<IMediaItemBase> fileList;
    private IGoogleDriveMediaUpdater googleDriveMediaUpdater;
    private static GoogleDriveMediaList handler;
    private static GoogleDriveHandler mGoogleDriveHandler;
    private Handler postMessage;

    private GoogleDriveMediaList(Context context){
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

    public void addFileInGoogleDriveMediaList(IMediaItemBase entry){
        fileList.add(entry);
        if(null != googleDriveMediaUpdater) {
            postMessage.post(new Runnable() {
                @Override
                public void run() {
                    googleDriveMediaUpdater.onGoogleDriveMediaListUpdate();
                }
            });
        }
    }

    public void addFilesInGoogleDriveMediaList(ArrayList<IMediaItemBase> entries){
        fileList.addAll(entries);
        if(null != googleDriveMediaUpdater) {
            postMessage.post(new Runnable() {
                @Override
                public void run() {
                    googleDriveMediaUpdater.onGoogleDriveMediaListUpdate();
                }
            });
        }
    }

    public void removeFilesInGoogleDriveMediaList(int position){
        fileList.remove(position);
        if(null != googleDriveMediaUpdater) {
            postMessage.post(new Runnable() {
                @Override
                public void run() {
                    googleDriveMediaUpdater.onGoogleDriveMediaListUpdate();
                }
            });
        }
    }

    public ArrayList<IMediaItemBase> getGoogleDriveMediaList(){
        return fileList;
    }

    public int getCount(){
        return fileList.size();
    }

    public void clearGoogleDriveMediaContent(){
        fileList.clear();
    }

    public void finishGoogleDriveMediaLoading(){
        if(null != googleDriveMediaUpdater) {
            postMessage.post(new Runnable() {
                @Override
                public void run() {
                    googleDriveMediaUpdater.onGoogleDriveMediaListUpdate();
                }
            });
        }
    }

    public void onErrorOccurred(final String e){
        if(null != googleDriveMediaUpdater) {
            postMessage.post(new Runnable() {
                @Override
                public void run() {
                    googleDriveMediaUpdater.onError(e);
                }
            });
        }
    }

    public void onEmptyList(){
        if(null != googleDriveMediaUpdater) {
            postMessage.post(new Runnable() {
                @Override
                public void run() {
                    googleDriveMediaUpdater.onEmptyList();
                }
            });
        }
    }

    public void onRequestCancelled(){
        if(null != googleDriveMediaUpdater) {
            postMessage.post(new Runnable() {
                @Override
                public void run() {
                    googleDriveMediaUpdater.onRequestCancelled();
                }
            });
        }
    }

    public void setGoogleDriveMediaUpdater(IGoogleDriveMediaUpdater googleDriveMediaUpdater){
        if(null != googleDriveMediaUpdater) {
            this.googleDriveMediaUpdater = googleDriveMediaUpdater;
        }
    }

    public interface IGoogleDriveMediaUpdater {
        void onGoogleDriveMediaListUpdate();
        void onRequestCancelled();
        void onError(String e);
        void onEmptyList();
    }
}
