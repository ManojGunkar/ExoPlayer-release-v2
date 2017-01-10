package com.globaldelight.boom.data.DropboxMedia;

import android.content.Context;
import android.os.Handler;
import com.dropbox.client2.DropboxAPI;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import java.util.ArrayList;

/**
 * Created by Venkata N M on 12/29/2016.
 */

public class DropboxFileList {

    private ArrayList<IMediaItemBase> fileList;
    private IDropboxUpdater dropboxUpdater;
    private static DropboxFileList handler;
    private Handler postMessage;

    private DropboxFileList(Context context){
        fileList = new ArrayList<IMediaItemBase>();
        postMessage = new Handler();
    }

    public static DropboxFileList getDropboxListInstance(Context context){
        if(null == handler){
            handler = new DropboxFileList(context);
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

    public ArrayList<IMediaItemBase> getFileList(){
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
