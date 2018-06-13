package com.globaldelight.boom.login.api;

import com.globaldelight.boom.login.api.request.SocialRequestBody;
import com.google.gson.JsonElement;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by Manoj Kumar on 11-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class LoginApiController {

    public static final String APP_AUTH_BASE_URL = "http://devuser.globaldelight.net/";
    public static final String BASE_URL = "https://login.globaldelight.net/";
    public static final String REGISTER_ENDPOINT = "register/";
    public static final String AUTH_URL = "appauthentication/";
    public static final String SOCIAL_LOGIN_URL = "sociallogin/";

    public static final String SECRET_KEY = "4301aad4464554d3245ba5fd6fc1bf9fa6c47b4fb5292e13da6ed7c1ac5ef6fd";

    private static OkHttpClient client;
    private static HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();


    public static Callback getClient(String url) {

        if (client == null) {
            client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
        }

        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(Callback.class);

    }

    public interface Callback {

        @POST(AUTH_URL)
        Call<JsonElement> getToken(
                @Body RequestBody requestBody);

        @POST(SOCIAL_LOGIN_URL)
        Call<JsonElement> sendSocialInfo(@Body SocialRequestBody requestBody);

    }

}
