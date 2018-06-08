package com.globaldelight.boom.radio.podcast.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
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
import com.globaldelight.boom.app.activities.MasterActivity;
import com.globaldelight.boom.business.ads.InlineAds;
import com.globaldelight.boom.radio.ui.adapter.OnPaginationListener;
import com.globaldelight.boom.radio.webconnector.RadioApiUtils;
import com.globaldelight.boom.radio.webconnector.RadioRequestController;
import com.globaldelight.boom.radio.webconnector.model.BaseResponse;
import com.globaldelight.boom.radio.webconnector.model.Chapter;
import com.globaldelight.boom.radio.webconnector.model.RadioStationResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_SONG_CHANGED;

/**
 * Created by Manoj Kumar on 07-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class PodcastDetailActitvity extends MasterActivity {

    public final static String KEY_TITLE = "KEY_TITLE";
    public final static String KEY_IMG_URL = "KEY_IMG_URL";
    public final static String KEY_PODCAST= "KEY_PODCAST";

    private int totalPage = 0;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    private List<Chapter> mContents = new ArrayList<>();

    private RadioStationResponse.Content mPodcast;

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private FloatingActionButton mPlayButton;
    private PodcastListAdapter mAdapter;

    private InlineAds mAdController;


    private BroadcastReceiver mUpdateItemSongListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_SONG_CHANGED:
                case ACTION_PLAYER_STATE_CHANGED:
                    if (null != mAdapter)
                        mAdapter.notifyDataSetChanged();
                    break;

            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        if (mAdController != null) {
            mAdController.register();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAYER_STATE_CHANGED);
        intentFilter.addAction(ACTION_SONG_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateItemSongListReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdController != null) {
            mAdController.unregister();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateItemSongListReceiver);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString(KEY_TITLE);
        String imageUrl = bundle.getString(KEY_IMG_URL);
        String json = bundle.getString(KEY_PODCAST);
        if (json != null) {
            mPodcast = new Gson().fromJson(json, RadioStationResponse.Content.class);
        }
        setContentView(R.layout.activity_grid_tidal);
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
        mPlayButton = findViewById(R.id.fab_grid_tidal);
        mProgressBar = findViewById(R.id.progress_grid_tidal);
        mRecyclerView = findViewById(R.id.rv_grid_tidal);

        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);

        mAdapter = new PodcastListAdapter(this, null, mContents);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addOnScrollListener(new OnPaginationListener(llm) {
            @Override
            protected void loadMoreContent() {
                isLoading = true;
                currentPage = currentPage + 1;

                new Handler().postDelayed(() -> getNextPageContent(), 1000);
            }

            @Override
            public int getTotalPageCount() {
                return totalPage - 1;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
    }

    private Call<BaseResponse<Chapter>> getChapters() {
        RadioRequestController.RequestCallback requestCallback = null;
        try {
            requestCallback = RadioRequestController
                    .getClient(this, RadioApiUtils.BASE_URL);
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return requestCallback.getPodcastChapters(mPodcast.getId(), String.valueOf(currentPage), "25");
    }

    private void getContent() {
        getChapters().enqueue(new Callback<BaseResponse<Chapter>>() {
            @Override
            public void onResponse(Call<BaseResponse<Chapter>> call, Response<BaseResponse<Chapter>> response) {
                if (response.isSuccessful()) {
                    mProgressBar.setVisibility(View.GONE);
                    BaseResponse<Chapter> radioResponse = response.body();
                    mContents = radioResponse.getBody().getContent();
                    for (Chapter aChapter : mContents) {
                        aChapter.setPodcast(mPodcast);
                    }
                    totalPage = radioResponse.getBody().getTotalPages();
                    currentPage = radioResponse.getBody().getPage();
                    mAdapter.addAll(mContents);
                    mAdapter.notifyDataSetChanged();

                    if (currentPage <= totalPage) mAdapter.addLoadingFooter();
                    else isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Chapter>> call, Throwable t) {
                t.printStackTrace();
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void getNextPageContent() {
        getChapters().enqueue(new Callback<BaseResponse<Chapter>>() {
            @Override
            public void onResponse(Call<BaseResponse<Chapter>> call, Response<BaseResponse<Chapter>> response) {
                mProgressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    mAdapter.removeLoadingFooter();
                    isLoading = false;
                    BaseResponse<Chapter> content = response.body();
                    mContents = content.getBody().getContent();
                    for (Chapter aChapter : mContents) {
                        aChapter.setPodcast(mPodcast);
                    }
                    totalPage = content.getBody().getTotalPages();
                    mAdapter.addAll(mContents);
                    mAdapter.notifyDataSetChanged();

                    if (currentPage <= totalPage) mAdapter.addLoadingFooter();
                    else isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Chapter>> call, Throwable t) {
            }
        });
    }
}
