package com.globaldelight.boom.radio.ui.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.radio.ui.adapter.OnPaginationListener;
import com.globaldelight.boom.radio.ui.adapter.RadioListAdapter;
import com.globaldelight.boom.radio.webconnector.ApiRequestController;
import com.globaldelight.boom.radio.webconnector.RadioApiUtils;
import com.globaldelight.boom.radio.webconnector.responsepojo.RadioStationResponse;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Manoj Kumar on 09-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class LocalFragment extends Fragment implements RadioListAdapter.Callback {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout errorLayout;
    private Button btnRetry;
    private TextView txtError;

    private RadioListAdapter radioListAdapter;
    private List<RadioStationResponse.Content> contentList = new ArrayList<>();

    private int totalPage = 0;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private String countryCode = "in";


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
        radioListAdapter = new RadioListAdapter(getActivity(),this, contentList);
        recyclerView.setAdapter(radioListAdapter);
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
        ApiRequestController.RequestCallback requestCallback = null;
        try {
            requestCallback = ApiRequestController
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
        return requestCallback.getLocalRadio(countryCode, currentPage + "", "25");
    }

    private void getContent() {
        hideErrorView();
        requestForContent().enqueue(new Callback<RadioStationResponse>() {
            @Override
            public void onResponse(Call<RadioStationResponse> call, Response<RadioStationResponse> response) {
                if (response.isSuccessful()) {
                    hideErrorView();
                    progressBar.setVisibility(View.GONE);
                    RadioStationResponse radioResponse = response.body();
                    contentList = radioResponse.getBody().getContent();
                    totalPage = radioResponse.getBody().getTotalPages();
                    currentPage = radioResponse.getBody().getPage();
                    radioListAdapter.addAll(contentList);
                    radioListAdapter.notifyDataSetChanged();

                    if (currentPage <= totalPage) radioListAdapter.addLoadingFooter();
                    else isLastPage = true;
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<RadioStationResponse> call, Throwable t) {
                t.printStackTrace();
                progressBar.setVisibility(View.GONE);
                showErrorView(t);
            }
        });
    }

    private void getNextPageContent() {
        requestForContent().enqueue(new Callback<RadioStationResponse>() {
            @Override
            public void onResponse(Call<RadioStationResponse> call, Response<RadioStationResponse> response) {
                if (response.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    radioListAdapter.removeLoadingFooter();
                    isLoading = false;
                    RadioStationResponse radioResponse = response.body();
                    contentList = radioResponse.getBody().getContent();
                    totalPage = radioResponse.getBody().getTotalPages();
                    radioListAdapter.addAll(contentList);
                    radioListAdapter.notifyDataSetChanged();

                    if (currentPage <= totalPage) radioListAdapter.addLoadingFooter();
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

    private void showErrorView(Throwable throwable) {

        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            txtError.setText(fetchErrorMessage(throwable));
        }
    }

    private String fetchErrorMessage(Throwable throwable) {
        String errorMsg = getResources().getString(R.string.error_msg_unknown);

        if (!isNetworkConnected()) {
            errorMsg = getResources().getString(R.string.error_msg_no_internet);
        } else if (throwable instanceof TimeoutException) {
            errorMsg = getResources().getString(R.string.error_msg_timeout);
        }

        return errorMsg;
    }

    private void hideErrorView() {
        if (errorLayout.getVisibility() == View.VISIBLE) {
            errorLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void retryPageLoad() {
        getNextPageContent();
    }
}
