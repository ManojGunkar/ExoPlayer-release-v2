package com.globaldelight.boom.radio.ui.fragments;

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
import com.globaldelight.boom.business.BusinessModelFactory;
import com.globaldelight.boom.business.ads.Advertiser;
import com.globaldelight.boom.business.ads.InlineAds;
import com.globaldelight.boom.radio.ui.adapter.ExploreCategoryAdapter;
import com.globaldelight.boom.radio.webconnector.model.ExploreCategory;
import com.globaldelight.boom.radio.webconnector.model.ExploreTag;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Manoj Kumar on 09-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class ExploreFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar mProgressBar;
    private InlineAds mAdController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.radio_layout, container, false);
        mProgressBar = view.findViewById(R.id.progress_local);
        mProgressBar.setVisibility(View.GONE);
        recyclerView = view.findViewById(R.id.rv_local_radio);
        List<ExploreCategory.Content> contents = getCategories().getBody().getContent();
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        ExploreCategoryAdapter mAdapter = new ExploreCategoryAdapter(getActivity(), contents);

        Advertiser factory = BusinessModelFactory.getCurrentModel().getAdFactory();
        if ( factory != null ) {
            mAdController = factory.createInlineAds(getActivity(), recyclerView, mAdapter);
            recyclerView.setAdapter(mAdController.getAdapter());
        }
        else {
            recyclerView.setAdapter(mAdapter);
        }
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        return view;
    }


    private ExploreCategory getCategories() {
        ExploreCategory exploreCategories = null;
        try {
            String json = readJsonFile(R.raw.categories);
            System.out.println(json);
            Gson gson = new Gson();
            exploreCategories = gson.fromJson(json, ExploreCategory.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exploreCategories;
    }

    private String readJsonFile(int id) throws IOException {
        InputStream tagStream = getActivity().getResources().openRawResource(id);
        int size = tagStream.available();
        byte[] buffer = new byte[size];
        tagStream.read(buffer);
        tagStream.close();
        return new String(buffer, "UTF-8");
    }


    @Override
    public void onStart() {
        super.onStart();
        if ( mAdController != null ) {
            mAdController.register();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if ( mAdController != null ) {
            mAdController.unregister();
        }
    }
}
