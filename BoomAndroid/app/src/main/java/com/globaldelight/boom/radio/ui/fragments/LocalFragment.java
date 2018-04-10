package com.globaldelight.boom.radio.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.radio.ui.adapter.LocalRadioListAdapter;
import com.globaldelight.boom.radio.ui.adapter.OnPaginationListener;
import com.globaldelight.boom.radio.webconnector.ApiRequestController;
import com.globaldelight.boom.radio.webconnector.RadioApiUtils;
import com.globaldelight.boom.radio.webconnector.responsepojo.LocalRadioResponse;

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
public class LocalFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabLoad;
    private TextView txtResCode;
    private LocalRadioListAdapter radioListAdapter;
    private ProgressDialog progressDialog;
    private List<LocalRadioResponse.Content> contentList=new ArrayList<>();

    private int totalPage = 0;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private String countryCode = "in";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.radio_layout, container, false);
        recyclerView=view.findViewById(R.id.rv_local_radio);
        fabLoad=view.findViewById(R.id.fab_load);
        txtResCode=view.findViewById(R.id.txt_log);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        radioListAdapter=new LocalRadioListAdapter(getActivity(),contentList);
        recyclerView.setAdapter(radioListAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnScrollListener(new OnPaginationListener(llm) {
            @Override
            protected void loadMoreContent() {
                isLoading = true;
                currentPage =currentPage+ 1;

                new Handler().postDelayed(() -> getNextPageContent(), 1000);
            }

            @Override
            public int getTotalPageCount() {
                return totalPage-1;
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
        fabLoad.setOnClickListener(v -> getContent());
        return view;
    }

    private Call<LocalRadioResponse> requestForContent() {
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
        return requestCallback.getLocalRadio(countryCode, currentPage + "", "10");
    }

    private void getContent(){
        requestForContent().enqueue(new Callback<LocalRadioResponse>() {
            @Override
            public void onResponse(Call<LocalRadioResponse> call, Response<LocalRadioResponse> response) {
                if (response.isSuccessful()){
                    LocalRadioResponse radioResponse=response.body();
                    contentList=radioResponse.getBody().getContent();
                    totalPage=radioResponse.getBody().getTotalPages();
                    currentPage=radioResponse.getBody().getPage();
                    radioListAdapter.addAll(contentList);
                    radioListAdapter.notifyDataSetChanged();

                    if (currentPage <= totalPage) radioListAdapter.addLoadingFooter();
                    else isLastPage = true;
                }else {
                    txtResCode.setText("Error Code = "+response.code());
                }
            }

            @Override
            public void onFailure(Call<LocalRadioResponse> call, Throwable t) {

            }
        });
    }

    private void getNextPageContent() {
        requestForContent().enqueue(new Callback<LocalRadioResponse>() {
            @Override
            public void onResponse(Call<LocalRadioResponse> call, Response<LocalRadioResponse> response) {
                if (response.isSuccessful()){
                    radioListAdapter.removeLoadingFooter();
                    isLoading = false;
                    LocalRadioResponse radioResponse=response.body();
                    contentList=radioResponse.getBody().getContent();
                    totalPage=radioResponse.getBody().getTotalPages();
                    radioListAdapter.addAll(contentList);
                    radioListAdapter.notifyDataSetChanged();

                    if (currentPage <= totalPage) radioListAdapter.addLoadingFooter();
                    else isLastPage = true;
                }else {
                    txtResCode.setText("Error Code = "+response.code());
                }
            }

            @Override
            public void onFailure(Call<LocalRadioResponse> call, Throwable t) {

            }
        });
    }


}
