package com.globaldelight.boom.radio.ui.fragments;

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

import com.globaldelight.boom.R;
import com.globaldelight.boom.business.BusinessModelFactory;
import com.globaldelight.boom.business.ads.Advertiser;
import com.globaldelight.boom.business.ads.InlineAds;
import com.globaldelight.boom.radio.podcast.FavouritePodcastManager;
import com.globaldelight.boom.radio.ui.adapter.RadioFragmentStateAdapter;
import com.globaldelight.boom.radio.ui.adapter.RadioListAdapter;
import com.globaldelight.boom.radio.utils.FavouriteRadioManager;
import com.globaldelight.boom.radio.webconnector.model.RadioStationResponse;

import java.util.List;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_SONG_CHANGED;

/**
 * Created by Manoj Kumar on 09-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class FavouriteFragment extends Fragment {

    private RecyclerView recyclerView;
    private RadioListAdapter mAdapter;
    private List<RadioStationResponse.Content> mContents;
    private InlineAds mAdController;
    private String type;
    private boolean isPodcastType = false;

    private BroadcastReceiver mUpdateItemSongListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case FavouriteRadioManager.FAVOURITES_RADIO_CHANGED:
                case ACTION_PLAYER_STATE_CHANGED:
                case ACTION_SONG_CHANGED:

                    if (null != mAdapter)
                        mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);
        type = getArguments().getString(RadioFragmentStateAdapter.KEY_TYPE);
        isPodcastType = type.equalsIgnoreCase("podcast") ? true : false;
        return recyclerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        if (!isPodcastType)
            mContents = FavouriteRadioManager.getInstance(getContext()).getRadioStations();
        else
            mContents = FavouritePodcastManager.getInstance(getContext()).getpodcast();
        mAdapter = new RadioListAdapter(getActivity(), null, mContents, isPodcastType);

        Advertiser factory = BusinessModelFactory.getCurrentModel().getAdFactory();
        if (factory != null) {
            mAdController = factory.createInlineAds(getActivity(), recyclerView, mAdapter);
            recyclerView.setAdapter(mAdController.getAdapter());
        } else {
            recyclerView.setAdapter(mAdapter);
        }
        recyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdController != null) {
            mAdController.register();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAYER_STATE_CHANGED);
        intentFilter.addAction(ACTION_SONG_CHANGED);
        intentFilter.addAction(FavouriteRadioManager.FAVOURITES_RADIO_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateItemSongListReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdController != null) {
            mAdController.unregister();
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateItemSongListReceiver);
    }
}
