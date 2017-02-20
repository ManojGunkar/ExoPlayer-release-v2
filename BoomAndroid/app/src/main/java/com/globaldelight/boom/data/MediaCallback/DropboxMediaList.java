package com.globaldelight.boom.data.MediaCallback;

import android.content.Context;
import android.os.Handler;

import com.globaldelight.boom.Media.MediaController;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.Media.MediaType;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 11-01-17.
 */

public class DropboxMediaList {

    private ArrayList<IMediaItemBase> fileList;
    private IDropboxUpdater dropboxUpdater;
    private static DropboxMediaList handler;
    private Context mContext;
    public static boolean isAllSongsLoaded = false;
    private Handler postMessage;

    private DropboxMediaList(Context context){
        this.mContext = context;
        fileList = new ArrayList<IMediaItemBase>();
        postMessage = new Handler();
    }

    public static DropboxMediaList getDropboxListInstance(Context context){
        if(null == handler){
            handler = new DropboxMediaList(context);
        }
        return handler;
    }

    public void addFileInDropboxList(IMediaItemBase entry){
        isAllSongsLoaded = false;
        fileList.add(entry);
        if ( dropboxUpdater != null ) {
            postMessage.post(new Runnable() {
                @Override
                public void run() {
                    dropboxUpdater.UpdateDropboxEntryList();
                }
            });
        }
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
                dropboxUpdater.ClearList();
            }
        });
        isAllSongsLoaded = false;
    }

    public void finishDropboxLoading(){
        isAllSongsLoaded = true;
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                dropboxUpdater.finishDropboxLoading();
            }
        });
    }

    public void setDropboxUpdater(IDropboxUpdater dropboxUpdater){
        this.dropboxUpdater = dropboxUpdater;
    }

    public interface IDropboxUpdater {
        void UpdateDropboxEntryList();
        void finishDropboxLoading();
        void ClearList();
    }
}
