package com.globaldelight.boom.collection.cloud;

import android.content.Context;
import android.os.Handler;

import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.playbackEvent.utils.MediaType;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 11-01-17.
 */

public class DropboxMediaList {

    private ArrayList<IMediaItemBase> fileList;
    private boolean isAllSongsLoaded = false;
    private IDropboxUpdater dropboxUpdater;
    private static DropboxMediaList handler;
    private Handler postMessage;
    private Context mContext;

    private DropboxMediaList(Context context){
        this.mContext = context;
        fileList = new ArrayList<IMediaItemBase>();
        postMessage = new Handler();
    }

    public static DropboxMediaList getInstance(Context context){
        if(null == handler){
            handler = new DropboxMediaList(context.getApplicationContext());
        }
        return handler;
    }

    public boolean isLoaded() {
        return isAllSongsLoaded;
    }

    public void addFileInDropboxList(final IMediaItemBase entry){
        if(isAllSongsLoaded)
            clearDropboxContent();

        isAllSongsLoaded = false;
        fileList.add(entry);

        postMessage.post(new Runnable() {
            @Override
            public void run() {
                if ( null != dropboxUpdater )
                    dropboxUpdater.onUpdateEntry();
            }
        });
    }

    public ArrayList<IMediaItemBase> getDropboxMediaList(){
        if(null != fileList && fileList.size() <= 0){
            fileList.addAll(MediaController.getInstance(mContext).getCloudList(MediaType.DROP_BOX));
        }
        return fileList;
    }

    public void clearDropboxContent(){
        if( fileList.size() > 0) {
            fileList.clear();
            MediaController.getInstance(mContext).removeCloudMediaItemList(MediaType.DROP_BOX);
        }

        postMessage.post(new Runnable() {
            @Override
            public void run() {
                if(null != dropboxUpdater)
                    dropboxUpdater.onClearList();
            }
        });

        isAllSongsLoaded = false;
    }

    public void finishDropboxLoading(){
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                if(null != dropboxUpdater)
                    dropboxUpdater.onFinishLoading();
            }
        });

        if(fileList.size() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MediaController.getInstance(mContext).addSongsToCloudItemList(MediaType.DROP_BOX, fileList);
                }
            }).start();
        }
        isAllSongsLoaded = true;
    }


    public void onLoadingError(){
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                if(null != dropboxUpdater)
                    dropboxUpdater.onLoadingError();
            }
        });
        isAllSongsLoaded = true;
    }

    public void setDropboxUpdater(IDropboxUpdater dropboxUpdater){
        this.dropboxUpdater = dropboxUpdater;
    }

    public interface IDropboxUpdater {
        void onUpdateEntry();
        void onFinishLoading();
        void onLoadingError();
        void onClearList();
    }
}
