package com.globaldelight.boom.app.fragments;

import android.os.AsyncTask;
import android.support.v7.widget.GridLayoutManager;

import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 28-02-17.
 */

public class ArtistsListFragment extends MediaCollectionFragment {

    @Override
    protected void loadCollection() {
        new LoadCollectionList().execute();
    }

    private class LoadCollectionList extends AsyncTask<Void, Integer, ArrayList<? extends IMediaItemBase>> {
        GridLayoutManager gridLayoutManager;
        @Override
        protected synchronized ArrayList<? extends IMediaItemBase> doInBackground(Void... params) {
            return MediaController.getInstance(getContext()).getArtistsList();
        }

        @Override
        protected synchronized void onPostExecute(ArrayList<? extends IMediaItemBase> iMediaCollectionList) {
            super.onPostExecute(iMediaCollectionList);
            onCollectionLoaded(iMediaCollectionList);
        }
    }
}
