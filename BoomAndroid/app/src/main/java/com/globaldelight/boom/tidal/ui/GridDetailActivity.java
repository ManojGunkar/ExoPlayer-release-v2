package com.globaldelight.boom.tidal.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.activities.MasterActivity;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.tidal.tidalconnector.model.Curated;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.tidal.tidalconnector.model.response.PlaylistResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;
import com.globaldelight.boom.tidal.ui.adapter.PlaylistTrackAdapter;
import com.globaldelight.boom.tidal.ui.adapter.TrackDetailAdapter;
import com.globaldelight.boom.tidal.utils.TidalHelper;
import com.globaldelight.boom.utils.RequestChain;
import com.google.gson.Gson;

import retrofit2.Call;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_SONG_CHANGED;

/**
 * Created by Manoj Kumar on 05-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class GridDetailActivity extends MasterActivity {

    public static final String ITEM_KEY="item";
    public static final String CURATED_KEY="curated";

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private FloatingActionButton mPlayButton;

    private Item mParent = null;
    private Curated mCurated = null;

    private TrackDetailAdapter mAdapter;
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
        if (mCurated != null) {
            String moodsPath = "moods/" + mCurated.getPath() + "/playlists";
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
        String json = bundle.getString(ITEM_KEY);
        if ( json != null ) {
            mParent = new Gson().fromJson(json, Item.class);
        }

        String curatedJson = bundle.getString(CURATED_KEY);
        if ( curatedJson != null ) {
            mCurated = new Gson().fromJson(curatedJson, Curated.class);
        }

        Toolbar toolbar = findViewById(R.id.toolbar_grid_tidal);
        toolbar.setTitle((mCurated != null)? mCurated.getName() : mParent.getTitle());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ImageView imageView = findViewById(R.id.img_grid_tidal);
        Glide.with(this)
                .load((mCurated != null)? mCurated.getImageUrl() : mParent.getItemArtUrl())
                .placeholder(R.drawable.ic_default_art_player_header)
                .centerCrop()
                .skipMemoryCache(true)
                .into(imageView);

        mPlayButton = findViewById(R.id.fab_grid_tidal);
        mPlayButton.setOnClickListener(this::onPlayClicked);
        mPlayButton.setVisibility(View.GONE);

        mProgressBar = findViewById(R.id.progress_grid_tidal);
        mRecyclerView = findViewById(R.id.rv_grid_tidal);
    }

    private void loadMoods(String path) {
        RequestChain requestChain = new RequestChain(this);
        Call<TidalBaseResponse> call = TidalHelper.getInstance(this).getItemCollection(path, 0, 100);
        requestChain.submit(call, resp -> {
            mProgressBar.setVisibility(View.GONE);
            mAdapter = new TrackDetailAdapter(this, resp.getItems(), mCurated.getName());
            LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(llm);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(mAdapter);
            mPlayButton.setVisibility(View.VISIBLE);
        });
    }

    private void loadApi() {
        int limit = 999;
        Integer maxItems = mParent.getNumberOfTracks();
        if ( maxItems != null ) {
            limit = maxItems.intValue();
        }
        String title = mParent.getTitle();
        RequestChain requestChain = new RequestChain(this);
        if (mParent.getItemType() == ItemType.PLAYLIST) {
            boolean isUserCreated = mParent.getType().equals("USER");
            Call<PlaylistResponse> call = TidalHelper.getInstance(this).getPlaylistTracks(mParent.getId(), 0, limit);
            requestChain.submit(call, resp -> {
                mProgressBar.setVisibility(View.GONE);
                LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                mRecyclerView.setLayoutManager(llm);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mPlaylistAdapter = new PlaylistTrackAdapter(this, resp.getItems(), title,isUserCreated);
                mRecyclerView.setAdapter(mPlaylistAdapter);
                mPlayButton.setVisibility(View.VISIBLE);

            });
        }else if (mParent.getItemType() == ItemType.ARTIST){
            String path = "artists/" + mParent.getId() + "/toptracks";
            Call<TidalBaseResponse> call = TidalHelper.getInstance(this).getItemCollection(path, 0, limit);
            requestChain.submit(call, resp -> {
                mProgressBar.setVisibility(View.GONE);
                mAdapter = new TrackDetailAdapter(this, resp.getItems(), title);
                LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                mRecyclerView.setLayoutManager(llm);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView.setAdapter(mAdapter);
                mPlayButton.setVisibility(View.VISIBLE);

            });
        }else {
            String path = "albums/" + mParent.getId() + "/tracks";
            Call<TidalBaseResponse> call = TidalHelper.getInstance(this).getItemCollection(path, 0, limit);
            requestChain.submit(call, resp -> {
                mProgressBar.setVisibility(View.GONE);
                mAdapter = new TrackDetailAdapter(this, resp.getItems(), title);
                LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                mRecyclerView.setLayoutManager(llm);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView.setAdapter(mAdapter);
                mPlayButton.setVisibility(View.VISIBLE);
            });
        }
    }

    private void onPlayClicked(View view) {
        App.playbackManager().stop();
        if (mParent != null && mParent.getItemType() == ItemType.PLAYLIST) {
            App.playbackManager().queue().addItemListToPlay(mPlaylistAdapter.getItems(), 0, false);
        }
        else {
            App.playbackManager().queue().addItemListToPlay(mAdapter.getItems(), 0, false);
        }
    }
}
