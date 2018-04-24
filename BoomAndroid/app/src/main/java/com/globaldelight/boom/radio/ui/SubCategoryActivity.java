package com.globaldelight.boom.radio.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.activities.MasterActivity;
import com.globaldelight.boom.radio.ui.adapter.ExploreTagAdapter;
import com.globaldelight.boom.radio.ui.adapter.RadioListAdapter;
import com.globaldelight.boom.radio.ui.adapter.SubCategoryAdapter;
import com.globaldelight.boom.radio.webconnector.ApiRequestController;
import com.globaldelight.boom.radio.webconnector.RadioApiUtils;
import com.globaldelight.boom.radio.webconnector.model.CategoryResponse;
import com.globaldelight.boom.radio.webconnector.model.ExploreTag;
import com.globaldelight.boom.radio.webconnector.model.RadioStationResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Manoj Kumar on 23-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class SubCategoryActivity extends MasterActivity {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category);

        Toolbar toolbar = findViewById(R.id.toolbar_sub_category);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView = findViewById(R.id.rv_sub_category);
        mProgressBar=findViewById(R.id.progress_sub_cat);
        mProgressBar.setVisibility(View.VISIBLE);
        Bundle bundle = getIntent().getExtras();
        String permalink=bundle.getString("permalink");
        String title = bundle.getString("title");
        setTitle(title);
        boolean isTagEnable = bundle.getBoolean("isTag");

        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);

        if (isTagEnable)
            isTagEnabled(isTagEnable);
        else {
            try {
                getSubCategories(permalink);
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private void getSubCategories(String permalink) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        ApiRequestController.RequestCallback requestCallback=ApiRequestController.getClient(this, RadioApiUtils.BASE_URL);
        Call<CategoryResponse> call=requestCallback.getCategoryWiseStation(permalink);
        call.enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                if (response.isSuccessful()) {
                    mProgressBar.setVisibility(View.GONE);
                    CategoryResponse categoryResponse = response.body();
                    List<CategoryResponse.Content> contents=categoryResponse.getBody().getContent();
                    SubCategoryAdapter adapter=new SubCategoryAdapter(SubCategoryActivity.this,contents);
                    mRecyclerView.setAdapter(adapter);

                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                t.printStackTrace();
                mProgressBar.setVisibility(View.GONE);
            }
        });

    }

    private void isTagEnabled(boolean enable) {
        if (!enable) return;
        mProgressBar.setVisibility(View.GONE);
        List<ExploreTag.Tags> tags = getTags().getTags();
        ExploreTagAdapter mAdapter = new ExploreTagAdapter(this, tags);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private ExploreTag getTags() {
        ExploreTag exploreTag = null;
        try {
            String json = readJsonFile(R.raw.tags);
            System.out.println(json);
            Gson gson = new Gson();
            exploreTag = gson.fromJson(json, ExploreTag.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exploreTag;
    }


    private String readJsonFile(int id) throws IOException {
        InputStream tagStream = getResources().openRawResource(id);
        int size = tagStream.available();
        byte[] buffer = new byte[size];
        tagStream.read(buffer);
        tagStream.close();
        return new String(buffer, "UTF-8");
    }

}
