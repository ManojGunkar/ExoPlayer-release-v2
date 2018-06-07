package com.globaldelight.boom.radio.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
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
import com.globaldelight.boom.radio.ui.adapter.OnPaginationListener;
import com.globaldelight.boom.radio.ui.adapter.RadioFragmentStateAdapter;
import com.globaldelight.boom.radio.ui.adapter.RadioListAdapter;
import com.globaldelight.boom.radio.webconnector.RadioRequestController;
import com.globaldelight.boom.radio.webconnector.RadioApiUtils;
import com.globaldelight.boom.radio.webconnector.model.RadioStationResponse;

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
 * Created by Manoj Kumar on 20-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class PopularFragment extends Fragment {

    private RecyclerView recyclerView;

    private RadioListAdapter mAdapter;
    private ProgressBar progressBar;
    private List<RadioStationResponse.Content> mContents = new ArrayList<>();
    private int totalPage = 0;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private InlineAds mAdController;

    private LinearLayoutManager llm;
    private GridLayoutManager glm;

    private BroadcastReceiver mUpdatePlayingItem = new BroadcastReceiver() {
        @Override
        public void onReceive(Context mActivity, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_PLAYER_STATE_CHANGED:
                case ACTION_SONG_CHANGED:
                    if (null != mAdapter)
                        mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    private String type;

    @Override
    public void onStart() {
        super.onStart();
        if (mAdController != null) {
            mAdController.register();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAYER_STATE_CHANGED);
        intentFilter.addAction(ACTION_SONG_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdatePlayingItem, intentFilter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.radio_layout, container, false);
        type = getArguments().getString(RadioFragmentStateAdapter.KEY_TYPE);
        recyclerView = view.findViewById(R.id.rv_local_radio);
        progressBar = view.findViewById(R.id.progress_local);

        if (type.equalsIgnoreCase("podcast")){
            glm = new GridLayoutManager(getContext(), 2);
            recyclerView.setLayoutManager(glm);
        }else {
            llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(llm);
        }

        mAdapter = new RadioListAdapter(getActivity(),null, mContents,type.equalsIgnoreCase("podcast")?true:false);

        Advertiser factory = BusinessModelFactory.getCurrentModel().getAdFactory();
        if ( factory != null ) {
            mAdController = factory.createInlineAds(getActivity(), recyclerView, mAdapter);
            recyclerView.setAdapter(mAdController.getAdapter());
        }
        else {
            recyclerView.setAdapter(mAdapter);
        }
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnScrollListener(new OnPaginationListener(llm!=null?llm:glm) {
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
        getContent();
        return view;
    }

    private Call<RadioStationResponse> requestForContent() {
        RadioRequestController.RequestCallback requestCallback = null;
        try {
            requestCallback = RadioRequestController
                    .getClient(getActivity(), RadioApiUtils.BASE_URL);
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
        return requestCallback.getPopularStation(type,"popularity",String.valueOf(currentPage), "25");
    }

    private void getContent() {
       // hideErrorView();
        requestForContent().enqueue(new Callback<RadioStationResponse>() {
            @Override
            public void onResponse(Call<RadioStationResponse> call, Response<RadioStationResponse> response) {
                if (response.code()==404){
             //       showErrorView(1);
                    return;
                }
                if (response.isSuccessful()) {
               //     hideErrorView();
                    progressBar.setVisibility(View.GONE);
                    RadioStationResponse radioResponse = response.body();
                    mContents = radioResponse.getBody().getContent();
                    if (mContents.size() == 0) {
                       // showErrorView(1);
                    }
                    totalPage = radioResponse.getBody().getTotalPages();
                    currentPage = radioResponse.getBody().getPage();
                    mAdapter.addAll(mContents);
                    mAdapter.notifyDataSetChanged();

                    if (currentPage <= totalPage) mAdapter.addLoadingFooter();
                    else isLastPage = true;
                } else {
                    progressBar.setVisibility(View.GONE);
                    if (response.code()==504){

                    }
                }
            }

            @Override
            public void onFailure(Call<RadioStationResponse> call, Throwable t) {
                t.printStackTrace();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void getNextPageContent() {
        requestForContent().enqueue(new Callback<RadioStationResponse>() {
            @Override
            public void onResponse(Call<RadioStationResponse> call, Response<RadioStationResponse> response) {
                if (response.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    mAdapter.removeLoadingFooter();
                    isLoading = false;
                    RadioStationResponse radioResponse = response.body();
                    mContents = radioResponse.getBody().getContent();
                    totalPage = radioResponse.getBody().getTotalPages();
                    mAdapter.addAll(mContents);
                    mAdapter.notifyDataSetChanged();

                    if (currentPage <= totalPage) mAdapter.addLoadingFooter();
                    else isLastPage = true;
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<RadioStationResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdController != null) {
            mAdController.unregister();
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdatePlayingItem);
    }
}