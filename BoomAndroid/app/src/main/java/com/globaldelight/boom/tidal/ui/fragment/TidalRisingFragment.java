package com.globaldelight.boom.tidal.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;
import com.globaldelight.boom.tidal.ui.ContentLoadable;
import com.globaldelight.boom.tidal.ui.adapter.NestedItemAdapter;
import com.globaldelight.boom.tidal.utils.NestedItemDescription;
import com.globaldelight.boom.tidal.utils.TidalHelper;
import com.globaldelight.boom.utils.RequestChain;
import com.globaldelight.boom.utils.Result;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_SONG_CHANGED;
import static com.globaldelight.boom.tidal.utils.NestedItemDescription.GRID_VIEW;
import static com.globaldelight.boom.tidal.utils.NestedItemDescription.LIST_VIEW;

/**
 * Created by Manoj Kumar on 28-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalRisingFragment extends Fragment implements ContentLoadable {

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;

    private boolean mHasResponse = false;
    private RequestChain mRequestChain = null;
    private NestedItemAdapter mAdapter;

    private View mErrorView;
    private TextView mTxtError;
    private Button mBtnRetry;
    private Result.Error mLastError = null;

    private List<NestedItemDescription> mItemList = new ArrayList<>();
    private BroadcastReceiver mUpdateItemSongListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_PLAYER_STATE_CHANGED:
                case ACTION_SONG_CHANGED:
                    if (null != mAdapter)
                        mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

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

        mErrorView = view.findViewById(R.id.layout_error);
        mTxtError = view.findViewById(R.id.txt_cause);
        mBtnRetry = view.findViewById(R.id.btn_retry);

        LinearLayoutManager vertical = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(vertical);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onLoadContent() {
        if (mHasResponse || mRequestChain != null) {
            return;
        }
        mLastError = null;
        mItemList.clear();
        mRequestChain = new RequestChain(getContext());
        mProgressBar.setVisibility(View.VISIBLE);
        mapResponse(TidalHelper.RISING_ALBUMS, R.string.tidal_album, GRID_VIEW);
        mapResponse(TidalHelper.RISING_TRACKS, R.string.tidal_tracks, LIST_VIEW);
        mRequestChain.submit(resp -> {
            mProgressBar.setVisibility(View.VISIBLE);
            if (mLastError == null) {
                mAdapter = new NestedItemAdapter(getActivity(), mItemList, false, false);
                mRecyclerView.setAdapter(mAdapter);
                mProgressBar.setVisibility(View.GONE);
                mHasResponse = true;
            } else {
                mErrorView.setVisibility(View.VISIBLE);
                mTxtError.setText(mLastError.getReason());
                mBtnRetry.setOnClickListener(view -> onLoadContent());
            }

        });
    }

    @Override
    public void onStopLoading() {
        if (mRequestChain != null) {
            mRequestChain.cancel();
            mRequestChain = null;
        }
    }

    @Override
    public void resetContent() {
        mHasResponse = false;
        mItemList.clear();
    }

    private void mapResponse(String path, int titleResId, int type) {
        Call<TidalBaseResponse> call = TidalHelper.getInstance(getContext()).getItemCollection(path, 0, 10);
        mRequestChain.submit(new RequestChain.CallAdapter<>(call), new ResponseHandler(titleResId, type, path));
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAYER_STATE_CHANGED);
        intentFilter.addAction(ACTION_SONG_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateItemSongListReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateItemSongListReceiver);
    }

    class ResponseHandler implements RequestChain.Callback<Result<TidalBaseResponse>> {
        private int resId;
        private int type;
        private String path;

        ResponseHandler(int resId, int type, String path) {
            this.resId = resId;
            this.path = path;
            this.type = type;
        }

        @Override
        public void onResponse(Result<TidalBaseResponse> result) {
            if (result.isSuccess() || result.getError().getCode() > 0) {
                TidalBaseResponse baseResponse = result.get();
                if (baseResponse != null)
                    mItemList.add(new NestedItemDescription(resId, type, baseResponse.getItems(), path));

            } else {
                mLastError = result.getError();
            }

        }
    }

}
