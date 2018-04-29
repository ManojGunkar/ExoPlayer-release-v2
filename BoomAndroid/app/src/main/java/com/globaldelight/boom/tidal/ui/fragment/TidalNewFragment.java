package com.globaldelight.boom.tidal.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
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
import com.globaldelight.boom.tidal.ui.adapter.TidalNewAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Manoj Kumar on 28-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalNewFragment extends Fragment {
    private ProgressBar mProgressBar;
    private NestedScrollView mNestedScrollView;

    private RecyclerView mRecyclerNewPlaylist;
    private RecyclerView mRecyclerRecPlayList;
    private RecyclerView mRecyclerNewTrack;
    private RecyclerView mRecyclerRecTrack;
    private RecyclerView mRecyclerNewAlbum;
    private RecyclerView mRecyclerRecAlbum;

    private List<Item> items = new ArrayList<>();
    private TidalNewAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tidal_new, null, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mProgressBar = view.findViewById(R.id.progress_tidal_new);
        mNestedScrollView=view.findViewById(R.id.scroll_new_tidal);
        mNestedScrollView.setVisibility(View.GONE);
        mRecyclerNewPlaylist = view.findViewById(R.id.rv_new_playlist);
        mRecyclerRecPlayList = view.findViewById(R.id.rv_recommended_playlist);
        mRecyclerNewTrack = view.findViewById(R.id.rv_new_track);
        mRecyclerRecTrack = view.findViewById(R.id.rv_recommended_track);
        setAdapter(mRecyclerNewPlaylist);
        setAdapter(mRecyclerRecPlayList);
        setAdapter(mRecyclerNewTrack);
        setAdapter(mRecyclerRecTrack);
        getNewPlaylists();
        getNewTracks();
        getRecommPlaylists();
        getRecomTracks();

    }

    private void setAdapter(RecyclerView recyclerView) {
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private TidalRequestController.Callback getCallback() {
        return TidalRequestController.getTidalClient();
    }

    private void getNewPlaylists() {
        Call<TidalBaseResponse> call = getCallback().getNewPlaylists(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()) {
                    items = response.body().getItems();
                    mAdapter=new TidalNewAdapter(getActivity(),items);
                    mRecyclerNewPlaylist.setAdapter(mAdapter);
                    mProgressBar.setVisibility(View.GONE);
                    mNestedScrollView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }

    private void getRecommPlaylists() {
        Call<TidalBaseResponse> call = getCallback().getRecommendedPlaylists(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()) {
                    items = response.body().getItems();
                    mAdapter=new TidalNewAdapter(getActivity(),items);
                    mRecyclerRecPlayList.setAdapter(mAdapter);
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }

    private void getNewTracks() {
        Call<TidalBaseResponse> call = getCallback().getNewTracks(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()) {
                    items = response.body().getItems();
                    mAdapter=new TidalNewAdapter(getActivity(),items);
                    mRecyclerNewTrack.setAdapter(mAdapter);
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }

    private void getRecomTracks() {
        Call<TidalBaseResponse> call = getCallback().getRecommendedTracks(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()) {
                    items = response.body().getItems();
                    mAdapter=new TidalNewAdapter(getActivity(),items);
                    mRecyclerRecTrack.setAdapter(mAdapter);
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }


}
