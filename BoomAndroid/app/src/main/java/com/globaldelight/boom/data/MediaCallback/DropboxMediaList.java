package com.globaldelight.boom.data.MediaCallback;

import android.content.Context;
import android.os.Handler;

import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 11-01-17.
 */

public class DropboxMediaList {

    private ArrayList<IMediaItemBase> fileList;
    private IDropboxUpdater dropboxUpdater;
    private static DropboxMediaList handler;
    private Handler postMessage;

    private DropboxMediaList(Context context){
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
        fileList.add(entry);
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                dropboxUpdater.UpdateDropboxEntryList();
            }
        });
    }

    public void addFilesInDropboxList(ArrayList<IMediaItemBase> entries){
        fileList.addAll(entries);
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                dropboxUpdater.UpdateDropboxEntryList();
            }
        });
    }

    public void removeFilesInDropboxList(int position){
        fileList.remove(position);
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                dropboxUpdater.UpdateDropboxEntryList();
            }
        });
    }

    public ArrayList<IMediaItemBase> getDropboxMediaList(){
        return fileList;
    }

    public int getCount(){
        return fileList.size();
    }

    public void clearDropboxContent(){
        fileList.clear();
    }

    public void finishDropboxLoading(){
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                dropboxUpdater.UpdateDropboxEntryList();
            }
        });
    }

    public void setDropboxUpdater(IDropboxUpdater dropboxUpdater){
        this.dropboxUpdater = dropboxUpdater;
    }

    public interface IDropboxUpdater {
        void UpdateDropboxEntryList();
    }
}
