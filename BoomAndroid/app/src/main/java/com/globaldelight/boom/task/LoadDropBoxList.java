package com.globaldelight.boom.task;

import android.content.Context;
import android.os.AsyncTask;

import com.globaldelight.boom.data.DropboxMedia.DropboxFileList;
import com.globaldelight.boom.utils.helpers.DropBoxUtills;

/**
 * Created by Rahul Agarwal on 06-01-17.
 */
public class LoadDropBoxList extends AsyncTask<Void, Void, Void>{

    private DropboxFileList dropboxFileList;
    private Context mContext;

    public LoadDropBoxList(Context context){
        this.mContext = context;
        dropboxFileList = DropboxFileList.getDropboxListInstance(mContext);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(null != dropboxFileList && dropboxFileList.getCount() > 0){
            dropboxFileList.clearDropboxContent();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (null != DropBoxUtills.getKeys(mContext)) {
            DropBoxUtills.getFiles(DropBoxUtills.DIR, dropboxFileList);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        dropboxFileList.finishDropboxLoading();
    }
}
