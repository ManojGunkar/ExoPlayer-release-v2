package com.globaldelight.boom.data.MediaCallback;

import android.content.Context;
import android.os.Handler;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 11-01-17.
 */

public class GoogleDriveMediaList {

    private ArrayList<IMediaItemBase> fileList;
    private IGoogleDriveMediaUpdater googleDriveMediaUpdater;
    private static GoogleDriveMediaList handler;
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

    public void addFileInGoogleDriveMediaList(IMediaItemBase entry){
        fileList.add(entry);
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                googleDriveMediaUpdater.onGoogleDriveMediaListUpdate();
            }
        });
    }

    public void addFilesInGoogleDriveMediaList(ArrayList<IMediaItemBase> entries){
        fileList.addAll(entries);
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                googleDriveMediaUpdater.onGoogleDriveMediaListUpdate();
            }
        });
    }

    public void removeFilesInGoogleDriveMediaList(int position){
        fileList.remove(position);
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                googleDriveMediaUpdater.onGoogleDriveMediaListUpdate();
            }
        });
    }

    public ArrayList<IMediaItemBase> getFileList(){
        return fileList;
    }

    public int getCount(){
        return fileList.size();
    }

    public void clearGoogleDriveMediaContent(){
        fileList.clear();
    }

    public void finishGoogleDriveMediaLoading(){
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                googleDriveMediaUpdater.onGoogleDriveMediaListUpdate();
            }
        });
    }

    public void onErrorOccurred(final String e){
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                googleDriveMediaUpdater.onError(e);
            }
        });
    }

    public void onEmptyList(){
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                googleDriveMediaUpdater.onEmptyList();
            }
        });
    }

    public void onRequestCancelled(){
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                googleDriveMediaUpdater.onRequestCancelled();
            }
        });
    }

    public void setGoogleDriveMediaUpdater(IGoogleDriveMediaUpdater googleDriveMediaUpdater){
        this.googleDriveMediaUpdater = googleDriveMediaUpdater;
    }

    public interface IGoogleDriveMediaUpdater {
        void onGoogleDriveMediaListUpdate();
        void onRequestCancelled();
        void onError(String e);
        void onEmptyList();
    }
}
