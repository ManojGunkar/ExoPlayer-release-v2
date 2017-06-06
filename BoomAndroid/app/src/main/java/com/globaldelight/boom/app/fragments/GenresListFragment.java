package com.globaldelight.boom.app.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.globaldelight.boom.app.adapters.media.MediaGridAdapter;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.view.RegularTextView;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.decorations.AlbumListSpacesItemDecoration;
import com.globaldelight.boom.utils.decorations.SimpleDividerItemDecoration;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 28-02-17.
 */

public class GenresListFragment extends MediaCollectionFragment {

    @Override
    protected void loadCollection() {
        new LoadCollectionList().execute();
    }

    private class LoadCollectionList extends AsyncTask<Void, Integer, ArrayList<? extends IMediaItemBase>> {
        GridLayoutManager gridLayoutManager;
        @Override
        protected synchronized ArrayList<? extends IMediaItemBase> doInBackground(Void... params) {
            return MediaController.getInstance(getContext()).getGenreList();
        }

        @Override
        protected synchronized void onPostExecute(ArrayList<? extends IMediaItemBase> iMediaCollectionList) {
            super.onPostExecute(iMediaCollectionList);
            onCollectionLoaded(iMediaCollectionList);
        }
    }
}
