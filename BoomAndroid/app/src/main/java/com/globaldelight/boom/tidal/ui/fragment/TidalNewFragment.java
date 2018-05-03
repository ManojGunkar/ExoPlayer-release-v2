package com.globaldelight.boom.tidal.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

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
    private ExecutorService executor = null;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private List<NestedItemDescription> mItemList = new ArrayList<>();
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

    class ResponseHandler implements TidalNewFragment.Callback {
        private int resId;
        private int type;

        ResponseHandler(int resId, int type) {
            this.resId = resId;
            this.type = type;
        }

        @Override
        public void onResponse(TidalBaseResponse tidalBaseResponse) {
            if ( tidalBaseResponse != null ) {
                mItemList.add(new NestedItemDescription(resId, type, tidalBaseResponse.getItems()));
            }
        }
    }

    interface APICall {
        Call<TidalBaseResponse> operation(String token,String countryCode,String offSet,String limit);
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

        executor = Executors.newSingleThreadExecutor();
        mProgressBar.setVisibility(View.VISIBLE);
        mapResponse(getCallback()::getRecommendedTracks, R.string.tidal_recommended_tracks, LIST_VIEW);
        mapResponse(getCallback()::getRecommendedAlbums, R.string.tidal_recommended_album, GRID_VIEW);
        mapResponse(getCallback()::getNewTracks, R.string.tidal_new_tracks, LIST_VIEW);
        mapResponse(getCallback()::getNewAlbums, R.string.tidal_new_albums, GRID_VIEW);
        mapResponse(getCallback()::getNewPlaylists, R.string.tidal_new_playlist, GRID_VIEW);
        mapResponse(getCallback()::getTop20Tracks, R.string.tidal_top20_tracks, LIST_VIEW);
        mapResponse(getCallback()::getTop20Albums, R.string.tidal_top20_albums, GRID_VIEW);
        mapResponse(getCallback()::getRecommendedPlaylists, R.string.tidal_recommended_playlists, GRID_VIEW);
        executeCall(getCallback()::getExclusivePlaylists, (response)->{
            new ResponseHandler(R.string.tidal_exclusive_playlists, GRID_VIEW).onResponse(response);
            mAdapter = new NestedItemAdapter(getContext(), mItemList);
            mRecyclerView.setAdapter(mAdapter);
            mProgressBar.setVisibility(View.GONE);
            mHasResponse = true;
        });
    }

    private void mapResponse(APICall api, int titleResId, int type) {
        executeCall(api, new ResponseHandler(titleResId, type));
    }

    private TidalRequestController.Callback getCallback() {
        return TidalRequestController.getTidalClient();
    }


    private interface  Callback {
        void onResponse(TidalBaseResponse tidalBaseResponse);
    }

    private void executeCall(APICall api, Callback callback) {
        Call<TidalBaseResponse> call = api.operation(TidalRequestController.AUTH_TOKEN, "US", "0", "6");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                TidalBaseResponse body = null;
                try {
                    Response<TidalBaseResponse>  resp = call.execute();
                    if ( resp.isSuccessful() ) {

                        body = resp.body();
                    }
                }
                catch (IOException e) {
                }

                final TidalBaseResponse response = body;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(response);
                    }
                });
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
        loadAll();
    }

    @Override
    public void onStop() {
        super.onStop();
        if ( executor != null ) {
            executor.shutdownNow();
            executor = null;
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateItemSongListReceiver);
    }
}
