package com.globaldelight.boom.tidal.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.activities.MasterActivity;
import com.globaldelight.boom.tidal.tidalconnector.TidalRequestController;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.tidal.tidalconnector.model.response.SearchResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.UserMusicResponse;
import com.globaldelight.boom.tidal.ui.adapter.GridAdapter;
import com.globaldelight.boom.tidal.ui.adapter.TrackAdapter;
import com.globaldelight.boom.tidal.utils.NestedItemDescription;
import com.globaldelight.boom.tidal.utils.TidalHelper;
import com.globaldelight.boom.tidal.utils.UserCredentials;
import com.globaldelight.boom.utils.RequestChain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_SONG_CHANGED;
import static com.globaldelight.boom.tidal.utils.TidalHelper.SEARCH_ALBUM_TYPE;
import static com.globaldelight.boom.tidal.utils.TidalHelper.SEARCH_ARTISTS_TYPE;
import static com.globaldelight.boom.tidal.utils.TidalHelper.SEARCH_PLAYLIST_TYPE;
import static com.globaldelight.boom.tidal.utils.TidalHelper.SEARCH_TRACK_TYPE;

/**
 * Created by Manoj Kumar on 04-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class MoreItemActivity extends MasterActivity {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    private RequestChain mRequestChain = null;
    private String api;
    private int viewType;
    private boolean isUserMode = false;
    private boolean isSearchMode = false;
    private boolean isUserPlaylist = false;
    private String title;
    private TrackAdapter mAdapter;
    private GridAdapter mGridAdapter;
    private BroadcastReceiver mUpdateItemSongListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_SONG_CHANGED:
                case ACTION_PLAYER_STATE_CHANGED:
                    if (null != mAdapter)
                        mAdapter.notifyDataSetChanged();
                    if (mGridAdapter != null)
                        mGridAdapter.notifyDataSetChanged();
                    break;

            }
        }
    };
    private String searchQuery;
    private boolean isArtists = false;

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAYER_STATE_CHANGED);
        intentFilter.addAction(ACTION_SONG_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateItemSongListReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateItemSongListReceiver);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_sub_category);
        Toolbar toolbar = findViewById(R.id.toolbar_sub_category);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView = findViewById(R.id.rv_sub_category);
        mProgressBar = findViewById(R.id.progress_sub_cat);
        mProgressBar.setVisibility(View.VISIBLE);
        Bundle bundle = getIntent().getExtras();
        title = bundle.getString("title");
        searchQuery = bundle.getString("query");
        viewType = bundle.getInt("view_type");
        isUserMode = bundle.getBoolean("isUserMode");
        isSearchMode = bundle.getBoolean("isSearchMode");
        isUserPlaylist = bundle.getBoolean("isUserPlaylist");
        api = bundle.getString("api");
        isArtists = title.equalsIgnoreCase("artists");

        setTitle(title);
        if (isUserMode) {
            isArtists = api.contains("artists");
            if (!isArtists && isUserPlaylist)
                loadUserPlaylists();
            else
                loadUserMusic();
        } else if (isSearchMode) {
            loadSearch(searchQuery, title);
        } else {
            loadApi();
        }
    }

    private void loadApi() {
        mRequestChain = new RequestChain(this);
        Call<TidalBaseResponse> call = TidalHelper.getInstance(this).getItemCollection(api, 0, 200);
        mRequestChain.submit(call, resp -> {
            setDataInAdapter(resp.getItems());
        });
    }

    private void loadUserPlaylists() {
        mRequestChain = new RequestChain(this);
        Call<TidalBaseResponse> call = TidalHelper.getInstance(this).getUserPlayLists(0, 200);
        mRequestChain.submit(call, resp -> {
            setDataInAdapter(resp.getItems());
        });
    }


    private void loadUserMusic() {
        mRequestChain = new RequestChain(this);
        TidalRequestController.Callback callback = TidalRequestController.getTidalClient();
        Call<UserMusicResponse> call = callback.getUserMusic(api, UserCredentials.getCredentials(this).getSessionId(),
                Locale.getDefault().getCountry(), "NAME", "ASC", "0", "100");
        mRequestChain.submit(call, resp -> {
            List<Item> itemList = new ArrayList<>();
            for (int i = 0; i < resp.getItems().size(); i++) {
                itemList.add(resp.getItems().get(i).getItem());
            }
            setDataInAdapter(itemList);
        });
    }

    private void loadSearch(String query, String searchType) {
        String type = null;
        if (searchType.equalsIgnoreCase(SEARCH_ALBUM_TYPE)) {
            type = SEARCH_ALBUM_TYPE;
        } else if (searchType.equalsIgnoreCase(SEARCH_PLAYLIST_TYPE)) {
            type = SEARCH_PLAYLIST_TYPE;
        } else if (searchType.equalsIgnoreCase(SEARCH_TRACK_TYPE)) {
            type = SEARCH_TRACK_TYPE;
        } else if (searchType.equalsIgnoreCase(SEARCH_ARTISTS_TYPE)) {
            type = SEARCH_ARTISTS_TYPE;
        }
        mRequestChain = new RequestChain(this);
        Call<SearchResponse> call = TidalHelper.getInstance(this).searchMusic(query, type, 0, 100);
        String finalType = type;
        mRequestChain.submit(call, resp -> {
            switch (finalType) {
                case SEARCH_ALBUM_TYPE:
                    setDataInAdapter(resp.getAlbums().getItems());
                    break;
                case SEARCH_PLAYLIST_TYPE:
                    setDataInAdapter(resp.getPlaylists().getItems());
                    break;
                case SEARCH_TRACK_TYPE:
                    setDataInAdapter(resp.getTracks().getItems());
                    break;
                case SEARCH_ARTISTS_TYPE:
                    setDataInAdapter(resp.getArtists().getItems());
                    break;
            }
        });
    }

    private void setDataInAdapter(List<Item> items) {
        mProgressBar.setVisibility(View.GONE);
        if (viewType == NestedItemDescription.LIST_VIEW) {
            LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(llm);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mAdapter = new TrackAdapter(this, items, isUserMode);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            GridLayoutManager glm = new GridLayoutManager(this, 2);
            mRecyclerView.setLayoutManager(glm);
            mGridAdapter = new GridAdapter(this, items, isUserMode, isArtists);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(mGridAdapter);
        }
    }

}
