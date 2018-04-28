package com.globaldelight.boom.tidal.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.globaldelight.boom.R;
import com.globaldelight.boom.tidal.tidalconnector.TidalRequestController;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalLoginResponse;
import com.globaldelight.boom.utils.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Manoj Kumar on 25-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalLoginFragment extends Fragment implements View.OnClickListener {

    private EditText mEtUserId;
    private EditText mEtPassword;
    private Button mBtnLogin;

    private static final String CLIENT_UNIQUE_KEY="IME";
    private final static String TAG="TIDAL_LOGIN_TAG";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tidal_login, null, false);
        setHasOptionsMenu(true);
        init(view);
        return view;
    }


    private void init(View view){
        mEtUserId=view.findViewById(R.id.et_user_id_tidal);
        mEtPassword=view.findViewById(R.id.et_password_tidal);
        mBtnLogin=view.findViewById(R.id.btn_login_tidal);
        mBtnLogin.setOnClickListener(this::onClick);

    }

    private void userLogin(String userid,String password){
        TidalRequestController.Callback callback=TidalRequestController.getTidalClient();
        Call<TidalLoginResponse> call=callback.userLogin(TidalRequestController.AUTH_TOKEN,userid,password,CLIENT_UNIQUE_KEY);
        call.enqueue(new Callback<TidalLoginResponse>() {
            @Override
            public void onResponse(Call<TidalLoginResponse> call, Response<TidalLoginResponse> response) {
                if (response.isSuccessful()){
                    TidalLoginResponse loginResponse=response.body();
                    Log.d(TAG,"SESSION ID ="+loginResponse.getSessionId());
                    Log.d(TAG,"USER ID ="+loginResponse.getUserId());
                    Log.d(TAG,"COUNTRY ="+loginResponse.getCountryCode());
                    Toast.makeText(getActivity(),"Login success",Toast.LENGTH_SHORT).show();
                }else {
                    Log.d(TAG,"FAILED lOGIN");
                }
            }

            @Override
            public void onFailure(Call<TidalLoginResponse> call, Throwable t) {
                Log.d(TAG,"ERROR : "+t.getMessage());

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login_tidal:
                userLogin(mEtUserId.getText().toString(),mEtPassword.getText().toString());
                break;
        }
    }
}
