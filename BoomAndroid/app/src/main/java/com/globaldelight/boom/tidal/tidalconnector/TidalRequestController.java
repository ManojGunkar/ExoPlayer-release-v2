package com.globaldelight.boom.tidal.tidalconnector;

import com.globaldelight.boom.tidal.tidalconnector.model.Curated;
import com.globaldelight.boom.tidal.tidalconnector.model.response.PlaylistResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalLoginResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalSubscriptionInfo;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TrackPlayResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.UserMusicResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Manoj Kumar on 25-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalRequestController {

    public final static String AUTH_TOKEN = "xxb2MAG8HwjUFhTZ";
    private final static String BASE_URL = "https://api.tidal.com/v1/";

    /**
     * @implNote Please Suffix the size of image eg:- /80x80.jpg
     * https://resources.tidal.com/images/3e76aaa2/2dee/4f8f/bcd8/6b10a44a875a/220x146.jpg
     */
    public final static String IMAGE_BASE_URL = "https://resources.tidal.com/images/";
    private static OkHttpClient client;
    private static HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

    public static Callback getTidalClient() {

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

        @GET("tracks/{track_id}/streamurl")
        Call<TrackPlayResponse> playTrack(
                @Header("X-Tidal-SessionId") String sessionId,
                @Path("track_id") String trackId,
                @Query("soundQuality") String quality);

        @GET("{path}")
        Call<TidalBaseResponse> getItemCollection(
                @Path(value = "path", encoded = true) String path,
                @Header("X-Tidal-Token") String token,
                @Query("countryCode") String countryCode,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        @GET("{path}")
        Call<List<Curated>> getCurated(
                @Path(value = "path", encoded = true) String path,
                @Header("X-Tidal-Token") String token,
                @Query("countryCode") String countryCode);

        @GET("{path}")
        Call<PlaylistResponse> getPlayListTrack(
                @Path(value = "path", encoded = true) String path,
                @Header("X-Tidal-SessionId") String sessionId,
                @Query("countryCode") String countryCode,
                @Query("order") String order,
                @Query("orderDirection") String orderDirection,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        @GET("{path}")
        Call<UserMusicResponse> getUserMusic(
                @Path(value = "path", encoded = true) String path,
                @Header("X-Tidal-SessionId") String sessionId,
                @Query("countryCode") String countryCode,
                @Query("order") String order,
                @Query("orderDirection") String orderDirection);


        @GET("users/{userId}/subscription")
        Call<TidalSubscriptionInfo> getUserSubscriptionInfo(
                @Header("X-Tidal-SessionId") String sessionId,
                @Path("userId") String userId);



    }
}
