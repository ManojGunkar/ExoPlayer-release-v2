package com.globaldelight.boom.tidal.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.activities.MasterActivity;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;
import com.globaldelight.boom.tidal.ui.adapter.GridAdapter;
import com.globaldelight.boom.tidal.utils.TidalHelper;
import com.globaldelight.boom.utils.RequestChain;

import retrofit2.Call;

/**
 * Created by Manoj Kumar on 06-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class CuratedDetailActivity extends MasterActivity {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

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
        String path = bundle.getString("path");

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
        loadApi(path);

    }

    private void loadApi(String path) {
        RequestChain requestChain = new RequestChain(this);
        Call<TidalBaseResponse> call = TidalHelper.getInstance(this).getItemCollection(path, 0, 50);
        requestChain.submit(call, resp -> {
            mProgressBar.setVisibility(View.GONE);
            GridLayoutManager glm = new GridLayoutManager(this, 2);
            mRecyclerView.setLayoutManager(glm);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(new GridAdapter(this, resp.getItems()));

        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
