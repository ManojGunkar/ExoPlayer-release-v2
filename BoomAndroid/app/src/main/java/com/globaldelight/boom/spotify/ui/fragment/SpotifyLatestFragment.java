package com.globaldelight.boom.spotify.ui.fragment;

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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.tidal.ui.ContentLoadable;
import com.globaldelight.boom.utils.RequestChain;
import com.globaldelight.boom.utils.Result;

/**
 * Created by Manoj Kumar on 29-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class SpotifyLatestFragment extends Fragment implements ContentLoadable {

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;

    private View mErrorView;
    private TextView mTxtError;
    private Button mBtnRetry;
    private Result.Error mLastError = null;

    private boolean mHasResponse = false;
    private RequestChain mRequestChain = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spotify, null, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mProgressBar = view.findViewById(R.id.progress_spotify);
        mRecyclerView = view.findViewById(R.id.rv_spotify);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void resetContent() {

    }

    @Override
    public void onLoadContent() {

    }

    @Override
    public void onStopLoading() {

    }
}
