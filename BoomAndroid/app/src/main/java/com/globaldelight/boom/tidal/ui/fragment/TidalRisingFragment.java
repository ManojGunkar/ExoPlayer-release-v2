package com.globaldelight.boom.tidal.ui.fragment;

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
import android.widget.ProgressBar;

import com.globaldelight.boom.R;
import com.globaldelight.boom.tidal.tidalconnector.TidalRequestController;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;
import com.globaldelight.boom.tidal.ui.adapter.TidalRisingAdapter;
import com.globaldelight.boom.tidal.ui.adapter.TidalTrackAdapter;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_SONG_CHANGED;

/**
 * Created by Manoj Kumar on 28-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalRisingFragment extends Fragment {

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;

    private TidalRisingAdapter mAdapter;

    private HashMap<String,List<Item>> mItemsMap = new HashMap<>();

    private BroadcastReceiver mUpdateItemSongListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
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
        View view = inflater.inflate(R.layout.fragment_tidal_new, null, false);
        initView(view);
        getAlbums();
        getTracks();
        return view;
    }

    private void initView(View view) {
        mProgressBar = view.findViewById(R.id.progress_tidal_new);
        mRecyclerView= view.findViewById(R.id.rv_tidal_new);


        LinearLayoutManager vertical = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(vertical);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

    }


    private TidalRequestController.Callback getCallback() {
        return TidalRequestController.getTidalClient();
    }

    private void getAlbums() {
        Call<TidalBaseResponse> call = getCallback()
                .getRisingAlbums(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()) {
                    mItemsMap.put("Albums",response.body().getItems());
                    mAdapter=new TidalRisingAdapter(getActivity(), mItemsMap);
                    mRecyclerView.setAdapter(mAdapter);
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }

    private void getTracks() {
        Call<TidalBaseResponse> call = getCallback()
                .getRisingTracks(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()) {
                    mItemsMap.put("Tracks",response.body().getItems());
                    mAdapter=new TidalRisingAdapter(getActivity(), mItemsMap);
                    mRecyclerView.setAdapter(mAdapter);
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAYER_STATE_CHANGED);
        intentFilter.addAction(ACTION_SONG_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateItemSongListReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateItemSongListReceiver);
    }

}
