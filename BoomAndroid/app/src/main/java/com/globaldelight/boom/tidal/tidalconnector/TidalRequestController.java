package com.globaldelight.boom.tidal.tidalconnector;

import com.globaldelight.boom.tidal.tidalconnector.model.Curated;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.tidal.tidalconnector.model.response.FavoritesResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.PlaylistResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.SearchResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalLoginResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalSubscriptionInfo;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TrackPlayResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.UserMusicResponse;
import com.google.gson.JsonElement;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.DELETE;
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
        Call<TidalBaseResponse> getUserPlaylist(
                @Path(value = "path", encoded = true) String path,
                @Header("X-Tidal-SessionId") String sessionId,
                @Query("countryCode") String countryCode,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        //https://api.tidal.com/v1/users/{{userId}}/playlists

        @FormUrlEncoded
        @POST("users/{userId}/playlists")
        Call<Item> createPlaylist(
                @Header("X-Tidal-SessionId") String sessionId,
                @Path("userId") String userId,
                @Field("title") String playlistTitle,
                @Field("description") String playlistDesc);

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
                @Query("orderDirection") String orderDirection,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        @GET("{path}")
        Call<SearchResponse> getSearchResult(
                @Path(value = "path", encoded = true) String path,
                @Header("X-Tidal-Token") String token,
                @Query("query") String query,
                @Query("types") String type,
                @Query("countryCode") String countryCode ,
                @Query("offset") String offSet,
                @Query("limit") String limit);

        @GET("users/{userId}/subscription")
        Call<TidalSubscriptionInfo> getUserSubscriptionInfo(
                @Header("X-Tidal-SessionId") String sessionId,
                @Path("userId") String userId);

        @FormUrlEncoded
        @POST("users/{userId}/favorites/albums")
        Call<Void> addAlbum(
                @Header("X-Tidal-SessionId") String sessionId,
                @Path("userId") String userId,
                @Field("albumIds") String albumIds,
                @Query("countryCode") String countryCode);

        @FormUrlEncoded
        @POST("users/{userId}/favorites/playlists")
        Call<Void> addPlaylist(
                @Header("X-Tidal-SessionId") String sessionId,
                @Path("userId") String userId,
                @Field("uuids") String uuid,
                @Query("countryCode") String countryCode);

        //https://api.tidal.com/v1/playlists/c989958b-c60a-4bdc-b58f-25236c6c1e8a/items

        @FormUrlEncoded
        @POST("playlists/{playlistId}/items")
        Call<Void> addToUserPlaylist(
                @Header("X-Tidal-SessionId") String sessionId,
                @Header("If-None-Match") String eTag,
                @Path("playlistId") String playlistId,
                @Field("itemIds") String itemIds,
                @Field("toIndex") String toIndex,
                @Query("countryCode") String countryCode);

        @FormUrlEncoded
        @POST("users/{userId}/favorites/tracks")
        Call<Void> addTrack(
                @Header("X-Tidal-SessionId") String sessionId,
                @Path("userId") String userId,
                @Field("trackIds") String trackIds,
                @Query("countryCode") String countryCode);

        @FormUrlEncoded
        @POST("users/{userId}/favorites/artists")
        Call<Void> addArtist(
                @Header("X-Tidal-SessionId") String sessionId,
                @Path("userId") String userId,
                @Field("artists") String artists,
                @Query("countryCode") String countryCode);


        @DELETE("users/{userId}/favorites/playlists/{uuid}")
        Call<Void> deletePlaylist(
                @Header("X-Tidal-SessionId") String sessionId,
                @Path("userId") String userId,
                @Path("uuid") String uuid);

        @DELETE("users/{userId}/favorites/albums/{albumId}")
        Call<Void> deleteAlbum(
                @Header("X-Tidal-SessionId") String sessionId,
                @Path("userId") String userId,
                @Path("albumId") String albumId);

        @DELETE("users/{userId}/favorites/tracks/{trackId}")
        Call<Void> deleteTrack(
                @Header("X-Tidal-SessionId") String sessionId,
                @Path("userId") String userId,
                @Path("trackId") String trackId);

        @DELETE("users/{userId}/favorites/artists/{artists}")
        Call<Void> deleteArtist(
                @Header("X-Tidal-SessionId") String sessionId,
                @Path("userId") String userId,
                @Path("artists") String artists);

        @GET("users/{userId}/favorites/ids")
        Call<FavoritesResponse> getFavorites(
                @Header("X-Tidal-SessionId") String token,
                @Path("userId") String userId);
    }
}
