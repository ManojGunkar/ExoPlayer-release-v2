package com.globaldelight.boom.spotify.apiconnector;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.globaldelight.boom.utils.Result;

/**
 * Created by Manoj Kumar on 26-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class ApiPresenter {

    public interface CompletionHandler <T> {
        void onComplete(Result<T> result);
    }

    private Context mContext;

    private ApiRequestController.RequestCallback mClientCallback;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    private static ApiPresenter instance;

    private ApiPresenter(Context context){
        this.mContext=context.getApplicationContext();
        mClientCallback=ApiRequestController.getClient();
    }

    public static ApiPresenter getInstance(Context context){
        if (instance==null)instance=new ApiPresenter(context);
        return instance;
    }

}
