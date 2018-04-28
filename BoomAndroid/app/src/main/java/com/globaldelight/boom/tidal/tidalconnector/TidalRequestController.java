package com.globaldelight.boom.tidal.tidalconnector;

import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalLoginResponse;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by Manoj Kumar on 25-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalRequestController {

    public final static String AUTH_TOKEN = "xxb2MAG8HwjUFhTZ";
    private final static String BASE_URL = "https://api.tidal.com/v1/";
    private static OkHttpClient client;
    private static HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

    public static Callback getTidalClient() {

        if (client == null) {
            client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    /* .addInterceptor(chain -> {
                         Request request = chain
                                 .request()
                                 .newBuilder()
                                 .addHeader("X-Tidal-Token", AUTH_TOKEN).build();
                         return chain.proceed(request);
                     })*/
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

    public interface Callback {

        @FormUrlEncoded
        @POST("login/username")
        Call<TidalLoginResponse> userLogin(
                @Header("X-Tidal-Token") String token,
                @Field("username") String userId,
                @Field("password") String password,
                @Field("clientUniqueKey") String clientUniqueKey);

        /*
        TidalBaseResponse
            – New – GET https://api.tidal.com/v1/featured/new/playlists?US&limit=1&offset=1}
            – Recommended – GET https://api.tidal.com/v1/featured/recommended/playlists?US&limit=1&offset=1
            – Local (not applicable all regions)
                – GET https://api.tidal.com/v1/featured/local/playlists?US&limit=1&offset=1
            – Exclusive – GET https://api.tidal.com/v1/featured/exclusive/playlists?US&limit=1&offset=1
        */

        /*
        Album
            – New – GET https://api.tidal.com/v1/featured/new/albums?US&limit=1&offset=1
            – Recommended – GET https://api.tidal.com/v1/featured/recommended/albums?US&limit=1&offset=1
            – Top20 – GET https://api.tidal.com/v1/featured/top/albums?US&limit=1&offset=1
            – Local (not applicable all regions)
                – GET https://api.tidal.com/v1/featured/local/albums?US&limit=1&offset=1
        */


        /*
        Tracks
            – New – GET https://api.tidal.com/v1/featured/new/tracks?US&limit=1&offset=1
            – Recommended – GET https://api.tidal.com/v1/featured/recommended/tracks?US&limit=1&offset=1
            – Top20 – GET https://api.tidal.com/v1/featured/top/tracks?US&limit=1&offset=1
            – Local (not applicable all regions)
                – GET https://api.tidal.com/v1/featured/local/tracks?US&limit=1&offset=1
        */

    }
}
