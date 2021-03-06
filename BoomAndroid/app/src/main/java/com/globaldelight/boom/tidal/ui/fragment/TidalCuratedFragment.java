package com.globaldelight.boom.tidal.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.tidal.tidalconnector.TidalRequestController;
import com.globaldelight.boom.tidal.tidalconnector.model.Curated;
import com.globaldelight.boom.tidal.ui.ContentLoadable;
import com.globaldelight.boom.tidal.ui.adapter.CuratedAdapter;
import com.globaldelight.boom.utils.RequestChain;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;

/**
 * Created by Manoj Kumar on 06-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalCuratedFragment extends Fragment implements ContentLoadable {

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerGenres;
    private RecyclerView mRecyclerMoods;
    private TextView mTxtGenres;
    private TextView mTxtMoods;
    private boolean mHasData = false;
    private RequestChain mRequestChain = null;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tidal_genres, null, false);
        init(view);
        return view;
    }

    private void init(View view){
        mProgressBar=view.findViewById(R.id.progress_tidal_curated);
        mRecyclerMoods=view.findViewById(R.id.rv_tidal_moods);
        mRecyclerGenres=view.findViewById(R.id.rv_tidal_genres);
        mTxtGenres=view.findViewById(R.id.txt_header_genres_curated);
        mTxtMoods=view.findViewById(R.id.txt_header_moods_curated);
    }

    public void onLoadContent() {
        if ( mHasData && mRequestChain != null ) {
            return;
        }
        mRequestChain = new RequestChain(getContext());
        load("genres",false);
        load("moods",true);
    }

    @Override
    public void onStopLoading() {
        if ( mRequestChain != null ) {
            mRequestChain.cancel();
            mRequestChain = null;
        }
    }

    @Override
    public void resetContent() {
        mHasData = false;
    }



    private void load(String path,boolean isMoods){
        TidalRequestController.Callback callback=TidalRequestController.getTidalClient();

        Call<List<Curated>> call=callback.getCurated(path,
                TidalRequestController.AUTH_TOKEN,
                Locale.getDefault().getCountry());

        mRequestChain.submit(call,resp->{
            mProgressBar.setVisibility(View.GONE);
            GridLayoutManager glm= new GridLayoutManager(getContext(), 2, GridLayoutManager.HORIZONTAL, false);

            if (isMoods){
                mRecyclerMoods.setLayoutManager(glm);
                mRecyclerMoods.setItemAnimator(new DefaultItemAnimator());
                mTxtMoods.setVisibility(View.VISIBLE);
                mRecyclerMoods.setAdapter(new CuratedAdapter(getContext(),resp,true));
            }
            else {
                mRecyclerGenres.setLayoutManager(glm);
                mRecyclerGenres.setItemAnimator(new DefaultItemAnimator());
                mTxtGenres.setVisibility(View.VISIBLE);
                mRecyclerGenres.setAdapter(new CuratedAdapter(getContext(),resp,false));
            }
            mHasData = true;
        });
    }

}
