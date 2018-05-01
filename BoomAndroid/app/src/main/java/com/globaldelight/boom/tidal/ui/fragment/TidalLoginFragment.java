package com.globaldelight.boom.tidal.ui.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.globaldelight.boom.R;
import com.globaldelight.boom.tidal.tidalconnector.TidalRequestController;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalLoginResponse;
import com.globaldelight.boom.tidal.utils.UserCredentials;
import com.globaldelight.boom.utils.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Manoj Kumar on 25-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalLoginFragment extends Fragment implements View.OnClickListener {

    private static final String CLIENT_UNIQUE_KEY = "IME";
    private final static String TAG = "TIDAL_LOGIN_TAG";
    private EditText mEtUserId;
    private EditText mEtPassword;
    private TextInputLayout mInputUserId;
    private TextInputLayout mInputPassword;
    private TextView mTxtForget;
    private TextView mTxtNewUser;
    private Button mBtnLogin;
    private ProgressDialog mProgressDialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tidal_login, null, false);
        setHasOptionsMenu(true);
        init(view);
        return view;
    }


    private void init(View view) {

        mInputUserId=view.findViewById(R.id.input_layout_user_id);
        mInputPassword=view.findViewById(R.id.input_layout_password);

        mEtUserId = view.findViewById(R.id.et_user_id);
        mEtPassword = view.findViewById(R.id.et_password);

        mEtUserId.addTextChangedListener(new CustomTextWatcher(mEtUserId));
        mEtPassword.addTextChangedListener(new CustomTextWatcher(mEtPassword));

        mBtnLogin = view.findViewById(R.id.btn_login_tidal);
        mBtnLogin.setOnClickListener(this::onClick);

    }

    private void userLogin(String userid, String password) {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("Signing...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        TidalRequestController.Callback callback = TidalRequestController.getTidalClient();
        Call<TidalLoginResponse> call = callback.userLogin(TidalRequestController.AUTH_TOKEN, userid, password, CLIENT_UNIQUE_KEY);
        call.enqueue(new Callback<TidalLoginResponse>() {
            @Override
            public void onResponse(Call<TidalLoginResponse> call, Response<TidalLoginResponse> response) {
                if (response.isSuccessful()) {
                    if (mProgressDialog!=null)mProgressDialog.dismiss();
                    TidalLoginResponse loginResponse = response.body();
                    UserCredentials.getCredentials(getActivity()).setSessionId(loginResponse.getSessionId());
                    UserCredentials.getCredentials(getActivity()).setUserId(String.valueOf(loginResponse.getUserId()));
                    Log.d(TAG, "SESSION ID =" + loginResponse.getSessionId());
                    Log.d(TAG, "USER ID =" + loginResponse.getUserId());
                    Log.d(TAG, "COUNTRY =" + loginResponse.getCountryCode());
                    Toast.makeText(getActivity(), "Login success", Toast.LENGTH_SHORT).show();
                    jumpToTidal();
                } else {
                    Log.d(TAG, "FAILED lOGIN");
                    if (mProgressDialog!=null)mProgressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<TidalLoginResponse> call, Throwable t) {
                Log.d(TAG, "ERROR : " + t.getMessage());
                if (mProgressDialog!=null)mProgressDialog.dismiss();

            }
        });
    }

    private void jumpToTidal(){
        Fragment fragment=new TidalMainFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }

    private boolean validateUserId() {
        if (mEtUserId.getText().toString().trim().isEmpty()) {
            mInputUserId.setError("Enter Your UserId");
            reFocus(mEtUserId);
            return false;
        } else {
            mInputPassword.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validatePassword() {
        if (mEtPassword.getText().toString().trim().isEmpty()) {
            mInputPassword.setError("Enter Your Password");
            reFocus(mEtPassword);
            return false;
        } else {
            mInputPassword.setErrorEnabled(false);
        }

        return true;
    }

    private void reFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login_tidal:
                if (validateUserId()||validatePassword())
                userLogin(mEtUserId.getText().toString(), mEtPassword.getText().toString());
                break;
        }
    }

    private class CustomTextWatcher implements TextWatcher {

        private View view;

        private CustomTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
        @Override
        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.et_user_id:
                    validateUserId();
                    break;
                case R.id.et_password:
                    validatePassword();
                    break;

            }
        }
    }
}
