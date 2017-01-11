package com.globaldelight.boom.task;

import android.content.Context;
import android.os.AsyncTask;

import com.globaldelight.boom.data.CloudMedia.DropboxMediaList;
import com.globaldelight.boom.utils.helpers.DropBoxUtills;

/**
 * Created by Rahul Agarwal on 06-01-17.
 */
public class LoadDropBoxList extends AsyncTask<Void, Void, Void>{

    private DropboxMediaList dropboxMediaList;
    private Context mContext;

    public LoadDropBoxList(Context context){
        this.mContext = context;
        dropboxMediaList = DropboxMediaList.getDropboxListInstance(mContext);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(null != dropboxMediaList && dropboxMediaList.getCount() > 0){
            dropboxMediaList.clearDropboxContent();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (null != DropBoxUtills.getKeys(mContext)) {
            DropBoxUtills.getFiles(DropBoxUtills.DIR, dropboxMediaList);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        dropboxMediaList.finishDropboxLoading();
    }
}
