package com.globaldelight.boom.tidal.tidalconnector;

import com.globaldelight.boom.tidal.tidalconnector.model.TidalLoginResponse;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Manoj Kumar on 25-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalRequestController {

    private final static String BASE_URL="https://api.tidal.com/v1/";

    private final static String AUTH_TOKEN="xxb2MAG8HwjUFhTZ";

    private static OkHttpClient client;
    private static HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

    public static Callback getTidalClient(){

        if (client == null) {
            client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .addInterceptor(chain -> {
                        Request request = chain
                                .request()
                                .newBuilder()
                                .addHeader("X-Tidal-Token", AUTH_TOKEN).build();
                        return chain.proceed(request);
                    })
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(Callback.class);
    }

    public interface Callback{

        @FormUrlEncoded
        @POST("login/username")
        Call<TidalLoginResponse> userLogin(
                @Field("username") String userId,
                @Field("password") String password,
                @Field("clientUniqueKey") String clientUniqueKey);

    }
}
