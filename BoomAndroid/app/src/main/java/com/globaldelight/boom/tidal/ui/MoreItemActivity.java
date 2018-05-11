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

/**
 * Created by Manoj Kumar on 04-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class MoreItemActivity extends MasterActivity {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    private RequestChain mRequestChain=null;
    private String api;
    private int viewType;
    private boolean isUserMode=false;
    private boolean isSearchMode=false;
    private String title;

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
    private TrackAdapter mAdapter;
    private GridAdapter mGridAdapter;

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

    private void initView(){
        setContentView(R.layout.activity_sub_category);
        Toolbar toolbar = findViewById(R.id.toolbar_sub_category);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView = findViewById(R.id.rv_sub_category);
        mProgressBar = findViewById(R.id.progress_sub_cat);
        mProgressBar.setVisibility(View.VISIBLE);
        Bundle bundle = getIntent().getExtras();
        title = bundle.getString("title");
        viewType = bundle.getInt("view_type");
        isUserMode=bundle.getBoolean("isUserMode");
        isSearchMode=bundle.getBoolean("isSearchMode");
        api = bundle.getString("api");
        setTitle(title);
        if (isUserMode){
            loadUserMusic();
        }else if (isSearchMode){
           // loadSearch();
        }else {
            loadApi();
        }
    }

    private void loadApi(){
        mRequestChain=new RequestChain(this);
        Call<TidalBaseResponse> call=TidalHelper.getInstance(this).getItemCollection(api,0,200);
        mRequestChain.submit(call, resp -> {
           setDataInAdapter(resp.getItems());
        });
    }

    private void loadUserMusic(){
        mRequestChain=new RequestChain(this);
        TidalRequestController.Callback callback=TidalRequestController.getTidalClient();
        Call<UserMusicResponse> call=callback.getUserMusic(api, UserCredentials.getCredentials(this).getSessionId(),
                Locale.getDefault().getCountry(), "NAME", "ASC","0","100");
        mRequestChain.submit(call,resp ->{
            List<Item> itemList=new ArrayList<>();
            for (int i=0;i<resp.getItems().size();i++){
                itemList.add(resp.getItems().get(i).getItem());
            }
            setDataInAdapter(itemList);
        });
    }

    private void loadSearch(String query,String searchType){
        mRequestChain=new RequestChain(this);
        Call<SearchResponse> call= TidalHelper.getInstance(this).searchMusic(query,searchType,0,100);
        mRequestChain.submit(call,resp -> {

        });
    }

    private void setDataInAdapter(List<Item> items){
        mProgressBar.setVisibility(View.GONE);
        if (viewType == NestedItemDescription.LIST_VIEW) {
            LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(llm);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mAdapter=new TrackAdapter(this,items);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            GridLayoutManager  glm= new GridLayoutManager(this,2);
            mRecyclerView.setLayoutManager(glm);
            mGridAdapter=new GridAdapter(this,items);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(mGridAdapter);
        }
    }

}
