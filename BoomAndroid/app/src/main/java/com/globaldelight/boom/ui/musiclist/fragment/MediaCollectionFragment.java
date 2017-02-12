package com.globaldelight.boom.ui.musiclist.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.ui.musiclist.ListDetail;
import com.globaldelight.boom.ui.musiclist.adapter.CollectionItemListAdapter;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class MediaCollectionFragment extends Fragment {

    private IMediaItemCollection collection;
    private ListDetail listDetail;
    private RecyclerView rootView;
    private CollectionItemListAdapter collectionItemListAdapter;

    public static final String ARG_ITEM_COLLECTION = "item_collection";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MediaCollectionFragment() {
    }

    private BroadcastReceiver mUpdatePlayingItem = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY:
                    if(null != collectionItemListAdapter)
                        collectionItemListAdapter.notifyDataSetChanged();
                break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments().containsKey(ARG_ITEM_COLLECTION)) {
            collection = (MediaItemCollection) getArguments().getParcelable(ARG_ITEM_COLLECTION);
        }
        listDetail = getListDetail();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        getActivity().registerReceiver(mUpdatePlayingItem, intentFilter);
    }

    public ListDetail getListDetail() {
        StringBuilder itemCount = new StringBuilder();
        itemCount.append(collection.getItemCount() > 1 ? getResources().getString(R.string.songs): getResources().getString(R.string.song));
        itemCount.append(" ").append(collection.getItemCount());

        return new ListDetail(collection.getItemTitle(), collection.getItemSubTitle(), itemCount.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);

        LoadAlbumSongs();
        setForAnimation();
        return rootView;
    }

    private void LoadAlbumSongs() {
        rootView.setLayoutManager(new LinearLayoutManager(getActivity()));
        collectionItemListAdapter = new CollectionItemListAdapter(getActivity(), collection, listDetail);
        rootView.setAdapter(collectionItemListAdapter);
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
    }


    public void listIsEmpty() {
        rootView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mUpdatePlayingItem);
        super.onDestroy();
    }

    public void onFloatActionPlaySong() {
        try {
            if (App.getPlayingQueueHandler().getUpNextList() != null) {
                App.getPlayingQueueHandler().getUpNextList().addToPlay(collection, 0, false, true);
                collectionItemListAdapter.notifyDataSetChanged();
            }
        }catch (Exception e){

        }
    }
}
