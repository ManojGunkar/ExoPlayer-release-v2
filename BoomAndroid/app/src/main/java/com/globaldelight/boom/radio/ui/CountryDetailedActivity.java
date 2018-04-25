package com.globaldelight.boom.radio.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
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
import com.globaldelight.boom.radio.ui.adapter.OnPaginationListener;
import com.globaldelight.boom.radio.ui.adapter.RadioListAdapter;
import com.globaldelight.boom.radio.ui.fragments.CountryFragment;
import com.globaldelight.boom.radio.webconnector.ApiRequestController;
import com.globaldelight.boom.radio.webconnector.RadioApiUtils;
import com.globaldelight.boom.radio.webconnector.model.RadioStationResponse;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_SONG_CHANGED;

/**
 * Created by Manoj Kumar on 18-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class CountryDetailedActivity extends MasterActivity implements RadioListAdapter.Callback {

    private RecyclerView mRecyclerView;
    private RadioListAdapter mAdapter;
    private ProgressBar mProgressBar;
    private List<RadioStationResponse.Content> mContents = new ArrayList<>();

    private int totalPage = 0;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

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
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAYER_STATE_CHANGED);
        intentFilter.addAction(ACTION_SONG_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateItemSongListReceiver, intentFilter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateItemSongListReceiver);
    }

    private void init() {
        setContentView(R.layout.activity_country_detail);
        Bundle bundle = getIntent().getExtras();
        String country = bundle.getString(CountryFragment.KEY_COUNTRY_NAME);
        String code = bundle.getString(CountryFragment.KEY_COUNTRY_CODE);
        String url = bundle.getString(CountryFragment.KEY_COUNTRY_URL);

        Toolbar toolbar = findViewById(R.id.toolbar_country_detail);
        toolbar.setTitle(country);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ImageView imageView = findViewById(R.id.img_country_detail);
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_default_art_player_header)
                .centerCrop()
                .skipMemoryCache(true)
                .into(imageView);

        mProgressBar = findViewById(R.id.progress_country_details);

        mRecyclerView = findViewById(R.id.rv_country_details);
        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new RadioListAdapter(this, this::retryPageLoad, mContents);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addOnScrollListener(new OnPaginationListener(llm) {
            @Override
            protected void loadMoreContent() {
                isLoading = true;
                currentPage = currentPage + 1;

                new Handler().postDelayed(() -> getNextPageContent(code), 1000);
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

        getContent(code);

    }

    private Call<RadioStationResponse> requestForContent(String countryCode) {
        ApiRequestController.RequestCallback requestCallback = null;
        try {
            requestCallback = ApiRequestController
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
        return requestCallback.getLocalRadio(countryCode, "radio", "popularity", String.valueOf(currentPage), "25");
    }

    private void getContent(String countryCode) {

        requestForContent(countryCode).enqueue(new Callback<RadioStationResponse>() {
            @Override
            public void onResponse(Call<RadioStationResponse> call, Response<RadioStationResponse> response) {
                if (response.isSuccessful()) {
                    mProgressBar.setVisibility(View.GONE);
                    RadioStationResponse radioResponse = response.body();
                    mContents = radioResponse.getBody().getContent();
                    totalPage = radioResponse.getBody().getTotalPages();
                    currentPage = radioResponse.getBody().getPage();
                    mAdapter.addAll(mContents);
                    mAdapter.notifyDataSetChanged();

                    if (currentPage <= totalPage) mAdapter.addLoadingFooter();
                    else isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<RadioStationResponse> call, Throwable t) {
                t.printStackTrace();
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void getNextPageContent(String countryCode) {
        requestForContent(countryCode).enqueue(new Callback<RadioStationResponse>() {
            @Override
            public void onResponse(Call<RadioStationResponse> call, Response<RadioStationResponse> response) {
                if (response.isSuccessful()) {
                    mAdapter.removeLoadingFooter();
                    isLoading = false;
                    RadioStationResponse radioResponse = response.body();
                    mContents = radioResponse.getBody().getContent();
                    totalPage = radioResponse.getBody().getTotalPages();
                    mAdapter.addAll(mContents);
                    mAdapter.notifyDataSetChanged();

                    if (currentPage <= totalPage) mAdapter.addLoadingFooter();
                    else isLastPage = true;
                }
            }

            @Override
            public void onFailure(Call<RadioStationResponse> call, Throwable t) {
            }
        });
    }

    @Override
    public void retryPageLoad() {

    }
}
