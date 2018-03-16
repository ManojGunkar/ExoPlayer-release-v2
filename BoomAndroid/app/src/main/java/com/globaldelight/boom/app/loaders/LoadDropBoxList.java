package com.globaldelight.boom.app.loaders;

import android.content.Context;
import android.os.AsyncTask;

import com.globaldelight.boom.collection.cloud.DropboxMediaList;
import com.globaldelight.boom.utils.helpers.DropBoxAPI;

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
        if ( DropBoxAPI.getInstance(mContext).isLoggedIn() ) {
            DropBoxAPI.getInstance(mContext).getFiles(dropboxMediaList);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(null != dropboxMediaList ) {
            dropboxMediaList.finishDropboxLoading();
        } else {
            dropboxMediaList.onLoadingError();
        }
    }
}
