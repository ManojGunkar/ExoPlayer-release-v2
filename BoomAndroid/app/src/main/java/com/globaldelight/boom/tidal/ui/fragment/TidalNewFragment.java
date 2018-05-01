package com.globaldelight.boom.tidal.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import java.util.HashMap;
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

    private RecyclerView mRecyclerView;
    private TidalNewAdapter mAdapter;
    private HashMap<String, List<Item>> mItemsMap = new HashMap<>();

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

        mProgressBar.setVisibility(View.VISIBLE);

        getNewPlaylists();
        getRecommPlaylists();
        getExclusivePlaylists();
        getLocalPlaylists();

        getNewAlbums();
        getRecommAlbums();
        getTop20Albums();
        getLocalAlbums();

        getNewTracks();
        getRecommTracks();
        getTop20Tracks();
        getLocalTracks();

    }

    private TidalRequestController.Callback getCallback() {
        return TidalRequestController.getTidalClient();
    }

    private void getNewPlaylists() {
        Call<TidalBaseResponse> call = getCallback()
                .getNewPlaylists(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()){
                    mItemsMap.put("New PlayLists",response.body().getItems());
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });

    }

    private void getRecommPlaylists() {
        Call<TidalBaseResponse> call = getCallback()
                .getRecommendedPlaylists(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()){
                    mItemsMap.put("Recomm PlayLists",response.body().getItems());
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }

    private void getLocalPlaylists() {
        Call<TidalBaseResponse> call = getCallback()
                .getLocalPlaylists(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()){
                    mItemsMap.put("Local PlayLists",response.body().getItems());
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }

    private void getExclusivePlaylists() {
        Call<TidalBaseResponse> call = getCallback()
                .getExclusivePlaylists(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()){
                    mItemsMap.put("Exclusive PlayLists",response.body().getItems());
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }

    private void getNewAlbums() {
        Call<TidalBaseResponse> call = getCallback()
                .getNewAlbums(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()){
                    mItemsMap.put("New Albums",response.body().getItems());
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }

    private void getRecommAlbums() {
        Call<TidalBaseResponse> call = getCallback()
                .getRecommendedAlbums(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()){
                    mItemsMap.put("Recomm Albums",response.body().getItems());
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }

    private void getTop20Albums() {
        Call<TidalBaseResponse> call = getCallback()
                .getTop20Albums(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()){
                    mItemsMap.put("Top20s Albums",response.body().getItems());
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }

    private void getLocalAlbums() {
        Call<TidalBaseResponse> call = getCallback()
                .getLocalAlbums(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()){
                    mItemsMap.put("Local Albums",response.body().getItems());
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }

    private void getNewTracks() {
        Call<TidalBaseResponse> call = getCallback()
                .getNewTracks(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()){
                    mItemsMap.put("New Tracks",response.body().getItems());
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }

    private void getRecommTracks() {
        Call<TidalBaseResponse> call = getCallback()
                .getRecommendedTracks(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()){
                    mItemsMap.put("Recomm Tracks",response.body().getItems());
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }

    private void getTop20Tracks() {
        Call<TidalBaseResponse> call = getCallback()
                .getTop20Tracks(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()){
                    mItemsMap.put("Top20 Tracks",response.body().getItems());
                    mAdapter = new TidalNewAdapter(getActivity(), mItemsMap);
                    mRecyclerView.setAdapter(mAdapter);
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }

    private void getLocalTracks() {
        Call<TidalBaseResponse> call = getCallback()
                .getLocalTracks(TidalRequestController.AUTH_TOKEN, "US", "0", "20");
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()){
                    mItemsMap.put("Local Tracks",response.body().getItems());
                    mAdapter = new TidalNewAdapter(getActivity(), mItemsMap);
                    mRecyclerView.setAdapter(mAdapter);
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }
}
