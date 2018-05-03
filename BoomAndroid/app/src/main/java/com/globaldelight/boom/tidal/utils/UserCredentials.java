package com.globaldelight.boom.tidal.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalLoginResponse;
import com.google.android.exoplayer2.util.ParsableNalUnitBitArray;

/**
 * Created by Manoj Kumar on 01-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class UserCredentials {

    private Context mContext;

    private static UserCredentials credentials;

    private final static String USER_CREDENTIALS="USER_CREDENTIALS";
    private final static String KEY_SESSION_ID="KEY_SESSION_ID";
    private final static String KEY_USER_ID="KEY_USER_ID";
    private final static String KEY_COUNTRY_CODE="KEY_COUNTRY_CODE";

    public static final int MODE = Context.MODE_PRIVATE;
    private SharedPreferences mSharedPreferences;

    private UserCredentials(Context context){
        this.mContext=context;
        this.mSharedPreferences=context.getSharedPreferences(USER_CREDENTIALS,MODE);

    }

    public static UserCredentials getCredentials(Context context){
        if (credentials==null)credentials=new UserCredentials(context);
        return credentials;
    }

    private SharedPreferences.Editor getEditor(){
        return mSharedPreferences.edit();
    }

    public void store(TidalLoginResponse response) {
        SharedPreferences.Editor editor=getEditor();
        editor.putString(KEY_USER_ID, response.getUserId().toString());
        editor.putString(KEY_SESSION_ID, response.getSessionId());
        editor.putString(KEY_COUNTRY_CODE, response.getCountryCode());
        editor.apply();

    }

    public boolean isUserLogged(){
       return mSharedPreferences.contains(KEY_SESSION_ID)&&mSharedPreferences.contains(KEY_USER_ID)?true:false;
    }

    public void setSessionId(String sessionId){
        SharedPreferences.Editor editor=getEditor();
        editor.putString(KEY_SESSION_ID, sessionId);
        editor.apply();
    }

    public String getSessionId(){
       return mSharedPreferences.getString(KEY_SESSION_ID,null);
    }

    public String getUserId(){
        return mSharedPreferences.getString(KEY_USER_ID,null);
    }

    public String getCountry(){
        return mSharedPreferences.getString(KEY_COUNTRY_CODE,"US");
    }

}
