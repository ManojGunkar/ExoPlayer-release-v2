package com.globaldelight.boom.radio.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.globaldelight.boom.R;
import com.globaldelight.boom.radio.ui.adapter.RadioSearchAdapter;
import com.globaldelight.boom.radio.webconnector.ApiRequestController;
import com.globaldelight.boom.radio.webconnector.RadioApiUtils;
import com.globaldelight.boom.radio.webconnector.responsepojo.RadioStationResponse;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;

/**
 * Created by Manoj Kumar on 17-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class RadioSearchFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private RadioSearchAdapter mAdapter;
    private List<RadioStationResponse.Content> mContents;

    private BroadcastReceiver mUpdatePlayingItem = new BroadcastReceiver() {
        @Override
        public void onReceive(Context mActivity, Intent intent) {
            switch (intent.getAction()) {
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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdatePlayingItem, intentFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_radio_search, container, false);

        String searchQuery = getArguments().getString(RadioMainFragment.KEY_SEARCH_QUERY);
        try {
            getResult(searchQuery);
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

        init(view);
        return view;
    }

    private void init(View view) {
        mProgressBar = view.findViewById(R.id.progress_radio_search);
        mRecyclerView = view.findViewById(R.id.rv_search_radio);
        LinearLayoutManager llm = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new RadioSearchAdapter(getActivity(), mContents);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void getResult(String query) throws CertificateException, UnrecoverableKeyException,
            NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        ApiRequestController.RequestCallback requestCallback = ApiRequestController
                .getClient(getActivity(), RadioApiUtils.BASE_URL);
        Call<RadioStationResponse> call = requestCallback
                .getSearchResult("in", "radio", query, "popularity", "1", "100");
        call.enqueue(new Callback<RadioStationResponse>() {
            @Override
            public void onResponse(Call<RadioStationResponse> call, Response<RadioStationResponse> response) {
                if (response.isSuccessful()) {
                    mProgressBar.setVisibility(View.GONE);
                    mContents = response.body().getBody().getContent();
                    mAdapter=new RadioSearchAdapter(getActivity(),mContents);
                    mAdapter.notifyDataSetChanged();
                } else {
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<RadioStationResponse> call, Throwable t) {
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdatePlayingItem);
    }

}
