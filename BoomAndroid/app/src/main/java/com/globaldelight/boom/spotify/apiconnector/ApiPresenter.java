package com.globaldelight.boom.spotify.apiconnector;

import android.content.Context;

import com.globaldelight.boom.utils.Result;

import retrofit2.Call;

/**
 * Created by Manoj Kumar on 26-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class ApiPresenter {

    private static ApiPresenter instance;
    private Context mContext;

    private ApiRequestController.RequestCallback mClientCallback;

    private ApiPresenter(Context context) {
        this.mContext = context.getApplicationContext();
        mClientCallback = ApiRequestController.getClient();
    }

    public static ApiPresenter getInstance(Context context) {
        if (instance == null) instance = new ApiPresenter(context);
        return instance;
    }

    public <T> Call<T> getSpotifyReponse(String token,String path){
        return mClientCallback.getSpotifyResponse(path,token);
    }



}
