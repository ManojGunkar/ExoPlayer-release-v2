package com.globaldelight.boom.app.loaders;

import android.content.Context;
import android.os.AsyncTask;

import com.globaldelight.boom.collection.cloud.DropboxMediaList;
import com.globaldelight.boom.utils.helpers.DropBoxUtills;

/**
 * Created by Rahul Agarwal on 06-01-17.
 */
public class LoadDropBoxList extends AsyncTask<Void, Void, Void>{

    private DropboxMediaList dropboxMediaList;
    private Context mContext;

    public LoadDropBoxList(Context context){
        this.mContext = context;
        dropboxMediaList = DropboxMediaList.getInstance(mContext);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (null != DropBoxUtills.getKeys(mContext)) {
            DropBoxUtills.setItemCount(0);
            DropBoxUtills.getFiles(dropboxMediaList);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(null != dropboxMediaList && dropboxMediaList.getDropboxMediaList().size() > 0) {
            dropboxMediaList.finishDropboxLoading();
        }else if(null != dropboxMediaList){
            dropboxMediaList.EmptyDropboxList();
        }else{
            dropboxMediaList.ErrorOnLoadinfDropboxList();
        }
    }
}
