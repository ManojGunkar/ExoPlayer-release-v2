package com.globaldelight.boom.tidal.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Adapter;
import android.widget.ProgressBar;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.activities.MasterActivity;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;
import com.globaldelight.boom.tidal.ui.adapter.TidalGridAdapter;
import com.globaldelight.boom.tidal.ui.adapter.TidalTrackAdapter;
import com.globaldelight.boom.tidal.utils.NestedItemDescription;
import com.globaldelight.boom.tidal.utils.TidalHelper;
import com.globaldelight.boom.utils.RequestChain;

import retrofit2.Call;

/**
 * Created by Manoj Kumar on 04-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class MoreItemActivity extends MasterActivity {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    private RequestChain mRequestChain;
    private String api;
    private int viewType;

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
        String title = bundle.getString("title");
        viewType = bundle.getInt("view_type");

        api = bundle.getString("api");
        setTitle(title);

        loadApi();
    }

    private void loadApi(){
        mRequestChain=new RequestChain(this);
        Call<TidalBaseResponse> call=TidalHelper.getInstance(this).getItemCollection(api,0,200);
        mRequestChain.submit(call, resp -> {
            mProgressBar.setVisibility(View.GONE);
            if (viewType == NestedItemDescription.LIST_VIEW) {
                LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                mRecyclerView.setLayoutManager(llm);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mRecyclerView.setAdapter(new TidalTrackAdapter(this, resp.getItems()));
            } else {
                GridLayoutManager  glm= new GridLayoutManager(this,2);
               mRecyclerView.setLayoutManager(glm);
               mRecyclerView.setItemAnimator(new DefaultItemAnimator());
               mRecyclerView.setAdapter(new TidalGridAdapter(this,resp.getItems()));
            }

        });
    }


}
