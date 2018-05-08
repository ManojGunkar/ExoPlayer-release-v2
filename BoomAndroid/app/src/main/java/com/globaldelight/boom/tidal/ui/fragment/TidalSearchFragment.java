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
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import com.globaldelight.boom.R;
import com.globaldelight.boom.tidal.tidalconnector.model.response.SearchResponse;
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
public class TidalSearchFragment extends Fragment {


    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;

    private List<NestedItemDescription> mItems=new ArrayList<>();
    private RequestChain requestChain=null;
    private NestedItemAdapter mAdapter;

    private BroadcastReceiver mUpdatePlayingItem = new BroadcastReceiver() {
        @Override
        public void onReceive(Context mActivity, Intent intent) {
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
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAYER_STATE_CHANGED);
        intentFilter.addAction(ACTION_SONG_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdatePlayingItem, intentFilter);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tidal_search, null, false);
        init(view);
        return view;
    }

    private void init(View view){
        mProgressBar=view.findViewById(R.id.progress_tidal_search);
        mRecyclerView=view.findViewById(R.id.rv_tidal_search);
        mProgressBar.setVisibility(View.GONE);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    public void getSearchQuery(String query){
        mRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setAdapter(null);
        hideKeyboard();
        loadQuery(query);
    }

    private void loadQuery(String query){
        requestChain=new RequestChain(getContext());
        setResponseType(query,R.string.tidal_album,GRID_VIEW,TidalHelper.SEARCH,TidalHelper.SEARCH_ALBUM_TYPE);
        setResponseType(query,R.string.tidal_tracks,LIST_VIEW,TidalHelper.SEARCH,TidalHelper.SEARCH_TRACK_TYPE);
        setResponseType(query,R.string.tidal_playlist,GRID_VIEW,TidalHelper.SEARCH,TidalHelper.SEARCH_PLAYLIST_TYPE);

        requestChain.submit(null,resp -> {

            mAdapter = new NestedItemAdapter(getContext(), mItems,false,true);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);

        });

    }

    private void setResponseType(String query,int resId, int type, String path,String listType){
        Call<SearchResponse> call= TidalHelper.getInstance(getContext()).searchMusic(query);
        requestChain.submit(call, new ResponseHandler(resId, type, path,listType));
    }

    private class ResponseHandler implements RequestChain.Callback<SearchResponse>{

        private int resId;
        private int type;
        private String path;
        private String listType;

        ResponseHandler(int resId, int type, String path,String listType) {
            this.resId = resId;
            this.type = type;
            this.path = path;
            this.listType=listType;
        }

        @Override
        public void onResponse(SearchResponse resp) {
                switch (listType){
                    case TidalHelper.SEARCH_ALBUM_TYPE:
                        mItems.add(new NestedItemDescription(resId,type,resp.getAlbums().getItems(),path));
                        break;
                    case TidalHelper.SEARCH_TRACK_TYPE:
                        mItems.add(new NestedItemDescription(resId,type,resp.getTracks().getItems(),path));
                        break;
                    case TidalHelper.SEARCH_PLAYLIST_TYPE:
                        mItems.add(new NestedItemDescription(resId,type,resp.getPlaylists().getItems(),path));
                        break;
                }
        }
    }

    private void hideKeyboard(){
        View hideKey = getActivity().getCurrentFocus();
        if (hideKey != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(hideKey.getWindowToken(), 0);
        }
    }

}
