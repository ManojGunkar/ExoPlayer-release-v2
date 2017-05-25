package com.globaldelight.boom.app.fragments;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.app.adapters.song.SongListAdapter;

import java.util.ArrayList;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class MediaItemListFragment extends Fragment {

    private RecyclerView rootView;
    private SongListAdapter songListAdapter;
    Activity mActivity;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MediaItemListFragment() {
    }

    private BroadcastReceiver mUpdatePlayingItem = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY:
                    if(null != songListAdapter)
                        songListAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            mActivity = (Activity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);
        if(null == mActivity)
            mActivity = getActivity();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        new LoadSongsList().execute();
        setForAnimation();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        mActivity.registerReceiver(mUpdatePlayingItem, intentFilter);
        FlurryAnalyticHelper.init(mActivity);
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
    }

    public void killActivity() {
        mActivity.finish();
    }


    private class LoadSongsList extends AsyncTask<Void, Void, ArrayList<? extends IMediaItemBase>> {

        @Override
        protected ArrayList<? extends IMediaItemBase> doInBackground(Void... params) {
            return MediaController.getInstance(mActivity).getSongList();
        }

        @Override
        protected void onPostExecute(ArrayList<? extends IMediaItemBase> itemList) {
            super.onPostExecute(itemList);
            rootView.setLayoutManager(new LinearLayoutManager(mActivity));
            songListAdapter = new SongListAdapter(mActivity, MediaItemListFragment.this, itemList);
            rootView.setAdapter(songListAdapter);
            rootView.setHasFixedSize(true);
            if (itemList.size() < 1) {
//                listIsEmpty();
            }
        }
    }

    public void listIsEmpty() {
        rootView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        mActivity.unregisterReceiver(mUpdatePlayingItem);
        super.onDestroy();
    }
    @Override
    public  void onStart() {
        super.onStart();
        FlurryAnalyticHelper.flurryStartSession(mActivity);
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAnalyticHelper.flurryStopSession(mActivity);
    }
}
