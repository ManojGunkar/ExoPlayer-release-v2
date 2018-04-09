package com.globaldelight.boom.radio.ui.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.R;
import com.globaldelight.boom.radio.ui.adapter.LocalRadioListAdapter;
import com.globaldelight.boom.radio.webconnector.ApiRequestController;
import com.globaldelight.boom.radio.webconnector.RadioApiUtils;
import com.globaldelight.boom.radio.webconnector.responsepojo.LocalRadioResponse;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Manoj Kumar on 09-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class LocalFragment extends Fragment{

    private RecyclerView recyclerView;
    private LocalRadioListAdapter radioListAdapter;
    private ProgressDialog progressDialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);

        try {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(true);
            progressDialog.show();
            getLocalRadio(getActivity());
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
        return recyclerView;
    }

    private void getLocalRadio(Context context) throws CertificateException,
            UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException, IOException {
        ApiRequestController.RequestCallback requestCallback=ApiRequestController
                .getClient(context, RadioApiUtils.BASE_URL);
        Call<LocalRadioResponse> call=requestCallback.getLocalRadio("in","1","100");
        call.enqueue(new Callback<LocalRadioResponse>() {
            @Override
            public void onResponse(Call<LocalRadioResponse> call, Response<LocalRadioResponse> response) {
                if (response.isSuccessful()){
                    if (progressDialog !=null) progressDialog.dismiss();
                    LocalRadioResponse radioResponse=response.body();
                    radioListAdapter=new LocalRadioListAdapter(getActivity(),radioResponse);
                    recyclerView.setAdapter(radioListAdapter);
                }
            }

            @Override
            public void onFailure(Call<LocalRadioResponse> call, Throwable t) {
                if (progressDialog !=null) progressDialog.dismiss();
            }
        });
    }
}
