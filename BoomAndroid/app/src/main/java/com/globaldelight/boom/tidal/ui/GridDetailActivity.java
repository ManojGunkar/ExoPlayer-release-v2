package com.globaldelight.boom.tidal.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.activities.MasterActivity;
import com.globaldelight.boom.tidal.tidalconnector.model.response.PlaylistResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;
import com.globaldelight.boom.tidal.ui.adapter.PlaylistTrackAdapter;
import com.globaldelight.boom.tidal.ui.adapter.TrackDetailAdapter;
import com.globaldelight.boom.tidal.utils.TidalHelper;
import com.globaldelight.boom.utils.RequestChain;

import retrofit2.Call;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_SONG_CHANGED;

/**
 * Created by Manoj Kumar on 05-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class GridDetailActivity extends MasterActivity {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    private String id;
    private boolean isPlaylist = false;
    private boolean isArtists = false;
    private boolean isMoods = false;
    private String moodsPath = null;
    private boolean isUserMode = false;

    private TrackDetailAdapter mAdapter;
    private String title;
    private PlaylistTrackAdapter mPlaylistAdapter;
    private BroadcastReceiver mUpdateItemSongListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_SONG_CHANGED:
                case ACTION_PLAYER_STATE_CHANGED:
                    if (null != mAdapter)
                        mAdapter.notifyDataSetChanged();
                    if (mPlaylistAdapter != null)
                        mPlaylistAdapter.notifyDataSetChanged();
                    break;

            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAYER_STATE_CHANGED);
        intentFilter.addAction(ACTION_SONG_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateItemSongListReceiver, intentFilter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        if (isMoods) {
            loadMoods(moodsPath);
        } else {
            loadApi();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateItemSongListReceiver);
    }

    private void init() {
        setContentView(R.layout.activity_grid_tidal);
        Bundle bundle = getIntent().getExtras();
        title = bundle.getString("title");
        id = bundle.getString("id");
        isPlaylist = bundle.getBoolean("isPlaylist");
        isArtists = bundle.getBoolean("isArtists");
        isMoods = bundle.getBoolean("isMoods");
        isUserMode = bundle.getBoolean("isUserMode");
        String imageUrl = bundle.getString("imageurl");
        moodsPath = bundle.getString("path");

        Toolbar toolbar = findViewById(R.id.toolbar_grid_tidal);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ImageView imageView = findViewById(R.id.img_grid_tidal);
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_default_art_player_header)
                .centerCrop()
                .skipMemoryCache(true)
                .into(imageView);
        imageView.setImageDrawable(getDrawable(R.drawable.ic_default_art_player_header));

        mProgressBar = findViewById(R.id.progress_grid_tidal);
        mRecyclerView = findViewById(R.id.rv_grid_tidal);

    }

    private void loadMoods(String path) {
        RequestChain requestChain = new RequestChain(this);
        Call<TidalBaseResponse> call = TidalHelper.getInstance(this).getItemCollection(path, 0, 100);
        requestChain.submit(call, resp -> {
            mProgressBar.setVisibility(View.GONE);
            mAdapter = new TrackDetailAdapter(this, resp.getItems(), title,isUserMode);
            LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(llm);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(mAdapter);
        });
    }

    private void loadApi() {
        RequestChain requestChain = new RequestChain(this);
        if (isPlaylist) {
            Call<PlaylistResponse> call = TidalHelper.getInstance(this).getPlaylistTracks(id, 0, 100);
            requestChain.submit(call, resp -> {
                mProgressBar.setVisibility(View.GONE);
                LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                mRecyclerView.setLayoutManager(llm);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mPlaylistAdapter = new PlaylistTrackAdapter(this, resp.getItems(), title,isUserMode);
                mRecyclerView.setAdapter(mPlaylistAdapter);
            });
        }else if (isArtists){
            String path = "artists/" + id + "/toptracks";
            Call<TidalBaseResponse> call = TidalHelper.getInstance(this).getItemCollection(path, 0, 999);
            requestChain.submit(call, resp -> {
                mProgressBar.setVisibility(View.GONE);
                mAdapter = new TrackDetailAdapter(this, resp.getItems(), title,isUserMode);
                LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                mRecyclerView.setLayoutManager(llm);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView.setAdapter(mAdapter);
            });
        }else {
            String path = "albums/" + id + "/tracks";
            Call<TidalBaseResponse> call = TidalHelper.getInstance(this).getItemCollection(path, 0, 200);
            requestChain.submit(call, resp -> {
                mProgressBar.setVisibility(View.GONE);
                mAdapter = new TrackDetailAdapter(this, resp.getItems(), title,isUserMode);
                LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                mRecyclerView.setLayoutManager(llm);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView.setAdapter(mAdapter);
            });
        }
    }
}
