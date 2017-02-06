package com.globaldelight.boom.ui.musiclist.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.ui.musiclist.adapter.songAdapter.SongListAdapter;

import java.util.ArrayList;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class MediaItemListFragment extends Fragment {

    private RecyclerView rootView;
    private SongListAdapter songListAdapter;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);

        new LoadSongsList().execute();
        setForAnimation();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        getActivity().registerReceiver(mUpdatePlayingItem, intentFilter);

        return rootView;
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
    }

    public void killActivity() {
        getActivity().finish();
    }


    private class LoadSongsList extends AsyncTask<Void, Void, ArrayList<? extends IMediaItemBase>> {

        @Override
        protected ArrayList<? extends IMediaItemBase> doInBackground(Void... params) {
            return MediaController.getInstance(getActivity()).getMediaCollectionItemList(ItemType.SONGS, MediaType.DEVICE_MEDIA_LIB) /*MediaQuery.getUpNextSongs(context)*/;
        }

        @Override
        protected void onPostExecute(ArrayList<? extends IMediaItemBase> itemList) {
            super.onPostExecute(itemList);
            rootView.setLayoutManager(new LinearLayoutManager(getActivity()));
            songListAdapter = new SongListAdapter(getActivity(), MediaItemListFragment.this, itemList);
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
        getActivity().unregisterReceiver(mUpdatePlayingItem);
        super.onDestroy();
    }
}
