package com.globaldelight.boom.tidal.tidalconnector;

import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalLoginResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TrackPlayResponse;

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

        // Play track http://api.tidal.com/v1/tracks/87002321/streamurl

        @GET("tracks/{track_id}/streamurl")
        Call<TrackPlayResponse> playTrack(
                @Header("X-Tidal-SessionId") String sessionId,
                @Path("track_id") String trackId,
                @Query("soundQuality") String quality);

        /*
        Playlist
            – New – GET https://api.tidal.com/v1/featured/new/playlists?US&limit=1&offset=1}
            – Recommended – GET https://api.tidal.com/v1/featured/recommended/playlists?US&limit=1&offset=1
            – Local (not applicable all regions)
                – GET https://api.tidal.com/v1/featured/local/playlists?US&limit=1&offset=1
            – Exclusive – GET https://api.tidal.com/v1/featured/exclusive/playlists?US&limit=1&offset=1
        */

        @GET("featured/new/playlists")
        Call<TidalBaseResponse> getNewPlaylists(
                @Header("X-Tidal-Token") String token,
                @Query("countryCode") String countryCode,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        @GET("/featured/recommended/playlists")
        Call<TidalBaseResponse> getRecommendedPlaylists(
                @Header("X-Tidal-Token") String token,
                @Query("countryCode") String countryCode,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        @GET("featured/local/playlists")
        Call<TidalBaseResponse> getLocalPlaylists(
                @Header("X-Tidal-Token") String token,
                @Query("countryCode") String countryCode,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        @GET("featured/exclusive/playlists")
        Call<TidalBaseResponse> getExclusivePlaylists(
                @Header("X-Tidal-Token") String token,
                @Query("countryCode") String countryCode,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        /*
        Album
            – New – GET https://api.tidal.com/v1/featured/new/albums?US&limit=1&offset=1
            – Recommended – GET https://api.tidal.com/v1/featured/recommended/albums?US&limit=1&offset=1
            – Top20 – GET https://api.tidal.com/v1/featured/top/albums?US&limit=1&offset=1
            – Local (not applicable all regions)
                – GET https://api.tidal.com/v1/featured/local/albums?US&limit=1&offset=1
        */

        @GET("featured/new/albums")
        Call<TidalBaseResponse> getNewAlbums(
                @Header("X-Tidal-Token") String token,
                @Query("countryCode") String countryCode,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        @GET("featured/recommended/albums")
        Call<TidalBaseResponse> getRecommendedAlbums(
                @Header("X-Tidal-Token") String token,
                @Query("countryCode") String countryCode,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        @GET("featured/top/albums")
        Call<TidalBaseResponse> getTop20Albums(
                @Header("X-Tidal-Token") String token,
                @Query("countryCode") String countryCode,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        @GET("featured/local/albums")
        Call<TidalBaseResponse> getLocalAlbums(
                @Header("X-Tidal-Token") String token,
                @Query("countryCode") String countryCode,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        /*
        Tracks
            – New – GET https://api.tidal.com/v1/featured/new/tracks?US&limit=1&offset=1
            – Recommended – GET https://api.tidal.com/v1/featured/recommended/tracks?US&limit=1&offset=1
            – Top20 – GET https://api.tidal.com/v1/featured/top/tracks?US&limit=1&offset=1
            – Local (not applicable all regions)
                – GET https://api.tidal.com/v1/featured/local/tracks?US&limit=1&offset=1
        */

        @GET("featured/new/tracks")
        Call<TidalBaseResponse> getNewTracks(
                @Header("X-Tidal-Token") String token,
                @Query("countryCode") String countryCode,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        @GET("featured/recommended/tracks")
        Call<TidalBaseResponse> getRecommendedTracks(
                @Header("X-Tidal-Token") String token,
                @Query("countryCode") String countryCode,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        @GET("featured/top/tracks")
        Call<TidalBaseResponse> getTop20Tracks(
                @Header("X-Tidal-Token") String token,
                @Query("countryCode") String countryCode,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        @GET("featured/local/tracks")
        Call<TidalBaseResponse> getLocalTracks(
                @Header("X-Tidal-Token") String token,
                @Query("countryCode") String countryCode,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        @GET("rising/new/albums")
        Call<TidalBaseResponse> getRisingAlbums(
                @Header("X-Tidal-Token") String token,
                @Query("countryCode") String countryCode,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        @GET("rising/new/tracks")
        Call<TidalBaseResponse> getRisingTracks(
                @Header("X-Tidal-Token") String token,
                @Query("countryCode") String countryCode,
                @Query("offset") String offSet,
                @Query("limit") String limit);

    }
}
