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
    private static boolean isAllSongsLoaded = false;
    private IDropboxUpdater dropboxUpdater;
    private static DropboxMediaList handler;
    private Handler postMessage;
    private Context mContext;

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

    public void addFileInDropboxList(final IMediaItemBase entry){
        if(isAllSongsLoaded)
            clearDropboxContent();

        isAllSongsLoaded = false;
        fileList.add(entry);

        postMessage.post(new Runnable() {
            @Override
            public void run() {
                if ( dropboxUpdater != null )
                    dropboxUpdater.UpdateDropboxEntryList();
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
                    dropboxUpdater.ClearList();
            }
        });

        isAllSongsLoaded = false;
    }

    public void finishDropboxLoading(){
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                if(null != dropboxUpdater)
                    dropboxUpdater.finishDropboxLoading();
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

    public void setDropboxUpdater(IDropboxUpdater dropboxUpdater){
        this.dropboxUpdater = dropboxUpdater;
    }

    public interface IDropboxUpdater {
        void UpdateDropboxEntryList();
        void finishDropboxLoading();
        void ClearList();
    }
}
