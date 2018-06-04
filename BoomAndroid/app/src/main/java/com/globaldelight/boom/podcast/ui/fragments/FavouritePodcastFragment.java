package com.globaldelight.boom.podcast.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.business.BusinessModelFactory;
import com.globaldelight.boom.business.ads.Advertiser;
import com.globaldelight.boom.business.ads.InlineAds;
import com.globaldelight.boom.podcast.ui.Adapter.PodcastListAdapter;
import com.globaldelight.boom.podcast.utils.FavouritePodcastManager;
import com.globaldelight.boom.radio.ui.adapter.RadioListAdapter;
import com.globaldelight.boom.radio.utils.FavouriteRadioManager;
import com.globaldelight.boom.radio.webconnector.model.RadioStationResponse;

import java.util.List;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_SONG_CHANGED;

/**
 * Created by Manoj Kumar on 01-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class FavouritePodcastFragment extends Fragment {
    private RecyclerView recyclerView;
    private PodcastListAdapter mAdapter;
    private List<RadioStationResponse.Content> mContents;
    private InlineAds mAdController;

    private BroadcastReceiver mUpdateItemSongListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case FavouritePodcastManager.FAVOURITES_PODCAST_CHANGED:
                    if(null != mAdapter)
                        mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);
        return recyclerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        mContents = FavouritePodcastManager.getInstance(getContext()).getpodcast();
        mAdapter = new PodcastListAdapter(getActivity(), null, mContents);

        Advertiser factory = BusinessModelFactory.getCurrentModel().getAdFactory();
        if ( factory != null ) {
            mAdController = factory.createInlineAds(getActivity(), recyclerView, mAdapter);
            recyclerView.setAdapter(mAdController.getAdapter());
        }
        else {
            recyclerView.setAdapter(mAdapter);
        }
        recyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    @Override
    public void onStart() {
        super.onStart();
        if ( mAdController != null ) {
            mAdController.register();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FavouritePodcastManager.FAVOURITES_PODCAST_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateItemSongListReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        if ( mAdController != null ) {
            mAdController.unregister();
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateItemSongListReceiver);
    }
}
