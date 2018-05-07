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
 * Created by Manoj Kumar on 06-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class CuratedDetailActivity extends MasterActivity {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private NestedItemAdapter mAdapter;
    private List<NestedItemDescription> mItemList = new ArrayList<>();
    private boolean mHasResponse = false;
    private RequestChain mRequestChain;
    private String trackPath=null;
    private String playlistPath=null;
    private String albumPath=null;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        setContentView(R.layout.activity_curated_detail);
        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString("title");
        String url = bundle.getString("imageCurated");
        trackPath = bundle.getString("trackPath");
        playlistPath = bundle.getString("playlistPath");
        albumPath = bundle.getString("albumPath");

        Toolbar toolbar = findViewById(R.id.toolbar_curated_detail);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ImageView imageView = findViewById(R.id.img_curated_detail);
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.radio_place_holder)
                .centerCrop()
                .skipMemoryCache(true)
                .into(imageView);
        imageView.setImageDrawable(getDrawable(R.drawable.ic_default_art_player_header));

        mProgressBar = findViewById(R.id.progress_curated_details);
        mRecyclerView = findViewById(R.id.rv_curated_details);

        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    private void loadAll() {
        if (mHasResponse) {
            return;
        }

        mRequestChain = new RequestChain(this);
        mProgressBar.setVisibility(View.VISIBLE);
        if (playlistPath!=null)
        mapResponse(playlistPath, R.string.tidal_playlist, GRID_VIEW);
        if (trackPath!=null)
        mapResponse(trackPath, R.string.tidal_tracks, LIST_VIEW);
        if (albumPath!=null)
        mapResponse(albumPath, R.string.tidal_album, GRID_VIEW);

        mRequestChain.submit(null, (response) -> {
            mAdapter = new NestedItemAdapter(this, mItemList,false);
            mRecyclerView.setAdapter(mAdapter);
            mProgressBar.setVisibility(View.GONE);
            mHasResponse = true;
        });
    }

    private void mapResponse(String path, int titleResId, int type) {
        Call<TidalBaseResponse> call = TidalHelper.getInstance(this).getItemCollection(path, 0, 6);
        mRequestChain.submit(call, new ResponseHandler(titleResId, type, path));
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAYER_STATE_CHANGED);
        intentFilter.addAction(ACTION_SONG_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateItemSongListReceiver, intentFilter);
        loadAll();
    }

    @Override
    public void onStop() {
        super.onStop();
        if ( mRequestChain != null ) {
            mRequestChain.cancel();
            mRequestChain = null;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateItemSongListReceiver);
    }
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
            if (tidalBaseResponse != null) {
                mItemList.add(new NestedItemDescription(resId, type, tidalBaseResponse.getItems(), path));
            }
        }
    }
}
