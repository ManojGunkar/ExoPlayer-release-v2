package com.globaldelight.boom.podcast.ui.fragments;

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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.business.BusinessModelFactory;
import com.globaldelight.boom.business.ads.Advertiser;
import com.globaldelight.boom.business.ads.InlineAds;
import com.globaldelight.boom.podcast.ui.Adapter.PodcastListAdapter;
import com.globaldelight.boom.podcast.utils.FavouritePodcastManager;
import com.globaldelight.boom.radio.ui.adapter.OnPaginationListener;
import com.globaldelight.boom.radio.utils.FavouriteRadioManager;
import com.globaldelight.boom.radio.webconnector.RadioApiUtils;
import com.globaldelight.boom.radio.webconnector.RadioRequestController;
import com.globaldelight.boom.radio.webconnector.model.RadioStationResponse;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.globaldelight.boom.podcast.utils.FavouritePodcastManager.FAVOURITES_PODCAST_CHANGED;

/**
 * Created by Manoj Kumar on 01-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class LocalPodcastFragment extends Fragment implements PodcastListAdapter.Callback {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout errorLayout;
    private Button btnRetry;
    private TextView txtError;
    private PodcastListAdapter podcastListAdapter;

    private List<RadioStationResponse.Content> contentList = new ArrayList<>();


    private InlineAds mAdController;


    private int totalPage = 0;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private String countryCode = Locale.getDefault().getCountry().toUpperCase();

    private BroadcastReceiver mUpdateItemSongListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case FAVOURITES_PODCAST_CHANGED:
                    if (null != podcastListAdapter)
                        podcastListAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.radio_layout, container, false);
        recyclerView = view.findViewById(R.id.rv_local_radio);
        progressBar = view.findViewById(R.id.progress_local);
        errorLayout = view.findViewById(R.id.error_layout);
        btnRetry = view.findViewById(R.id.error_btn_retry);
        txtError = view.findViewById(R.id.error_txt_cause);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        podcastListAdapter = new PodcastListAdapter(getActivity(), this, contentList);

        Advertiser factory = BusinessModelFactory.getCurrentModel().getAdFactory();
        if (factory != null) {
            mAdController = factory.createInlineAds(getActivity(), recyclerView, podcastListAdapter);
            recyclerView.setAdapter(mAdController.getAdapter());
        } else {
            recyclerView.setAdapter(podcastListAdapter);
        }
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnScrollListener(new OnPaginationListener(llm) {
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
        btnRetry.setOnClickListener(view1 -> getContent());

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
        return requestCallback.getLocalRadio(countryCode, "podcast", "popularity", String.valueOf(currentPage), "100");
    }

    private void getContent() {
        hideErrorView();
        requestForContent().enqueue(new Callback<RadioStationResponse>() {
            @Override
            public void onResponse(Call<RadioStationResponse> call, Response<RadioStationResponse> response) {
                if (response.code() == 404) {
                    showErrorView(1);
                    return;
                }
                if (response.isSuccessful()) {
                    hideErrorView();
                    progressBar.setVisibility(View.GONE);
                    RadioStationResponse radioResponse = response.body();
                    contentList = radioResponse.getBody().getContent();
                    if (contentList.size() == 0) {
                        showErrorView(1);
                    }
                    totalPage = radioResponse.getBody().getTotalPages();
                    currentPage = radioResponse.getBody().getPage();
                    podcastListAdapter.addAll(contentList);
                    podcastListAdapter.notifyDataSetChanged();

                    if (currentPage <= totalPage) podcastListAdapter.addLoadingFooter();
                    else isLastPage = true;
                } else {
                    progressBar.setVisibility(View.GONE);
                    if (response.code() == 504)
                        showErrorView(3);
                }
            }

            @Override
            public void onFailure(Call<RadioStationResponse> call, Throwable t) {
                t.printStackTrace();
                progressBar.setVisibility(View.GONE);
                showErrorView(1);
            }
        });
    }

    private void getNextPageContent() {
        requestForContent().enqueue(new Callback<RadioStationResponse>() {
            @Override
            public void onResponse(Call<RadioStationResponse> call, Response<RadioStationResponse> response) {
                if (response.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    podcastListAdapter.removeLoadingFooter();
                    isLoading = false;
                    RadioStationResponse radioResponse = response.body();
                    contentList = radioResponse.getBody().getContent();
                    totalPage = radioResponse.getBody().getTotalPages();
                    podcastListAdapter.addAll(contentList);
                    podcastListAdapter.notifyDataSetChanged();

                    if (currentPage <= totalPage) podcastListAdapter.addLoadingFooter();
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

    private void showErrorView(int errorCase) {

        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            txtError.setText(fetchErrorMessage(errorCase));
        }
    }

    private String fetchErrorMessage(int errorCase) {
        switch (errorCase) {
            case 1:
                return getResources().getString(R.string.error_msg_unknown);
            case 2:
                return getResources().getString(R.string.error_msg_no_internet);
            case 3:
                return getResources().getString(R.string.error_msg_timeout);
            default:
                return getResources().getString(R.string.error_msg_unknown);
        }

    }

    private void hideErrorView() {
        if (errorLayout.getVisibility() == View.VISIBLE) {
            errorLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void retryPageLoad() {
        getNextPageContent();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdController != null) {
            mAdController.register();
        }
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(FAVOURITES_PODCAST_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateItemSongListReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdController != null) {
            mAdController.unregister();
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateItemSongListReceiver);
    }
}
