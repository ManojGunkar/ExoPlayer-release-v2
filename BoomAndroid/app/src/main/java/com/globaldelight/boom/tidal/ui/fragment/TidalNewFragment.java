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
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;
import com.globaldelight.boom.tidal.ui.adapter.NestedItemAdapter;
import com.globaldelight.boom.tidal.utils.NestedItemDescription;
import com.globaldelight.boom.tidal.utils.TidalHelper;
import com.globaldelight.boom.utils.RequestChain;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_SONG_CHANGED;
import static com.globaldelight.boom.tidal.utils.NestedItemDescription.GRID_VIEW;
import static com.globaldelight.boom.tidal.utils.NestedItemDescription.LIST_VIEW;

/**
 * Created by Manoj Kumar on 28-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalNewFragment extends Fragment {
    private ProgressBar mProgressBar;

    private RecyclerView mRecyclerView;
    private NestedItemAdapter mAdapter;

    private boolean mHasResponse = false;
    private RequestChain mRequestChain = null;

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

    private List<NestedItemDescription> mItemList = new ArrayList<>();
    class ResponseHandler implements RequestChain.Callback<TidalBaseResponse> {
        private int resId;
        private int type;
        private String path;

        ResponseHandler(int resId, int type, String path) {
            this.resId = resId;
            this.type = type;
            this.path = path;
        }

        @Override
        public void onResponse(TidalBaseResponse tidalBaseResponse) {
            if ( tidalBaseResponse != null ) {
                mItemList.add(new NestedItemDescription(resId, type, tidalBaseResponse.getItems(), path));
            }
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tidal_new, null, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mProgressBar = view.findViewById(R.id.progress_tidal_new);
        mRecyclerView = view.findViewById(R.id.rv_tidal_new);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    private void loadAll() {
        if ( mHasResponse ) {
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);

        mItemList.clear();
        mRequestChain = new RequestChain(getContext());
        mapResponse(TidalHelper.EXCLUSIVE_PLAYLISTS, R.string.tidal_exclusive_playlists, GRID_VIEW);
        mapResponse(TidalHelper.RECOMMENDED_TRACKS, R.string.tidal_recommended_tracks, LIST_VIEW);
        mapResponse(TidalHelper.RECOMMENDED_ALBUMS, R.string.tidal_recommended_album, GRID_VIEW);
        mapResponse(TidalHelper.RECOMMENDED_PLAYLISTS, R.string.tidal_recommended_playlists, GRID_VIEW);
        mapResponse(TidalHelper.NEW_TRACKS, R.string.tidal_new_tracks, LIST_VIEW);
        mapResponse(TidalHelper.NEW_ALBUMS, R.string.tidal_new_albums, GRID_VIEW);
        mapResponse(TidalHelper.NEW_PLAYLISTS, R.string.tidal_new_playlist, GRID_VIEW);
        mapResponse(TidalHelper.TOP_TRACKS, R.string.tidal_top20_tracks, LIST_VIEW);
        mapResponse(TidalHelper.TOP_ALBUMS, R.string.tidal_top20_albums, GRID_VIEW);
        mapResponse(TidalHelper.LOCAL_TRACKS, R.string.tidal_local_tracks, LIST_VIEW);
        mapResponse(TidalHelper.LOCAL_ALBUMS, R.string.tidal_local_albums, GRID_VIEW);
        mapResponse(TidalHelper.LOCAL_PLAYLISTS, R.string.tidal_local_playlists, GRID_VIEW);
        mRequestChain.submit(null, (response)->{
            mAdapter = new NestedItemAdapter(getContext(), mItemList,false,false);
            mRecyclerView.setAdapter(mAdapter);
            mProgressBar.setVisibility(View.GONE);
            mHasResponse = true;
        });
    }

    private void mapResponse(String path, int titleResId, int type) {
        Call<TidalBaseResponse> call = TidalHelper.getInstance(getContext()).getItemCollection(path, 0 , 6);
        mRequestChain.submit(call, new ResponseHandler(titleResId, type,path));
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAYER_STATE_CHANGED);
        intentFilter.addAction(ACTION_SONG_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateItemSongListReceiver, intentFilter);
        loadAll();
    }

    @Override
    public void onStop() {
        super.onStop();
        if ( mRequestChain != null ) {
            mRequestChain.cancel();
            mRequestChain = null;
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateItemSongListReceiver);
    }
}
