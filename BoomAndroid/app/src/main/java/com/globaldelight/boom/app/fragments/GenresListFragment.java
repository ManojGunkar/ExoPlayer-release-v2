package com.globaldelight.boom.app.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.GridLayoutManager;

import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.collection.base.IMediaElement;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 28-02-17.
 */

public class GenresListFragment extends MediaCollectionFragment {

    @Override
    protected void loadCollection() {
        new LoadCollectionList().execute();
    }

    private class LoadCollectionList extends AsyncTask<Void, Integer, ArrayList<? extends IMediaElement>> {
        private Context context = getContext();
        GridLayoutManager gridLayoutManager;
        @Override
        protected synchronized ArrayList<? extends IMediaElement> doInBackground(Void... params) {
            return MediaController.getInstance(context).getGenreList();
        }

        @Override
        protected synchronized void onPostExecute(ArrayList<? extends IMediaElement> iMediaCollectionList) {
            super.onPostExecute(iMediaCollectionList);
            onCollectionLoaded(iMediaCollectionList);
        }
    }
}
