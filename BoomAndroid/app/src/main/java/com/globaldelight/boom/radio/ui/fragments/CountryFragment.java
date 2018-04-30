package com.globaldelight.boom.radio.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.radio.ui.adapter.CountryListAdapter;
import com.globaldelight.boom.radio.ui.adapter.OnPaginationListener;
import com.globaldelight.boom.radio.webconnector.RadioRequestController;
import com.globaldelight.boom.radio.webconnector.RadioApiUtils;
import com.globaldelight.boom.radio.webconnector.model.CategoryResponse;

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

/**
 * Created by Manoj Kumar on 09-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class CountryFragment extends Fragment {

    public final static String KEY_COUNTRY_CODE = "CODE";
    public final static String KEY_COUNTRY_NAME = "NAME";
    public final static String KEY_COUNTRY_URL = "URL";
    private RecyclerView recyclerView;
    private TextView txtResCode;
    private CountryListAdapter countryListAdapter;
    private ProgressBar progressBar;
    private List<CategoryResponse.Content> mContents = new ArrayList<>();
    private int totalPage = 0;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.radio_layout, container, false);
        recyclerView = view.findViewById(R.id.rv_local_radio);
        txtResCode = view.findViewById(R.id.txt_log);
        progressBar = view.findViewById(R.id.progress_local);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        countryListAdapter = new CountryListAdapter(getActivity(), mContents);
        recyclerView.setAdapter(countryListAdapter);
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
        return view;
    }

    private Call<CategoryResponse> requestForContent() {
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
        return requestCallback.getCountry(String.valueOf(currentPage), "100");
    }

    private void getContent() {
        requestForContent().enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                if (response.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    CategoryResponse radioResponse = response.body();
                    mContents = radioResponse.getBody().getContent();
                    totalPage = radioResponse.getBody().getTotalPages();
                    currentPage = radioResponse.getBody().getPage();
                    countryListAdapter.addAll(mContents);
                    countryListAdapter.notifyDataSetChanged();

                    if (currentPage <= totalPage) countryListAdapter.addLoadingFooter();
                    else isLastPage = true;
                } else {
                    txtResCode.setText("Error Code = " + response.code());
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                t.printStackTrace();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void getNextPageContent() {
        requestForContent().enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                if (response.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    countryListAdapter.removeLoadingFooter();
                    isLoading = false;
                    CategoryResponse radioResponse = response.body();
                    mContents = radioResponse.getBody().getContent();
                    totalPage = radioResponse.getBody().getTotalPages();
                    countryListAdapter.addAll(mContents);
                    countryListAdapter.notifyDataSetChanged();

                    if (currentPage <= totalPage) countryListAdapter.addLoadingFooter();
                    else isLastPage = true;
                } else {
                    txtResCode.setText("Error Code = " + response.code());
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }


}