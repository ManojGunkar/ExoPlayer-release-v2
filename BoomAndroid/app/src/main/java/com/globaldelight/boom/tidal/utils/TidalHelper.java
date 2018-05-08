package com.globaldelight.boom.tidal.utils;

import android.content.Context;

import com.globaldelight.boom.tidal.tidalconnector.TidalRequestController;
import com.globaldelight.boom.tidal.tidalconnector.model.response.PlaylistResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.SearchResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalSubscriptionInfo;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TrackPlayResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.UserMusicResponse;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by adarsh on 03/05/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class TidalHelper {

    public static final String NEW_PLAYLISTS = "featured/new/playlists";
    public static final String RECOMMENDED_PLAYLISTS = "/featured/recommended/playlists";
    public static final String LOCAL_PLAYLISTS = "featured/local/playlists";
    public static final String EXCLUSIVE_PLAYLISTS = "featured/exclusive/playlists";
    public static final String NEW_ALBUMS = "featured/new/albums";
    public static final String RECOMMENDED_ALBUMS = "featured/recommended/albums";
    public static final String TOP_ALBUMS = "featured/top/albums";
    public static final String LOCAL_ALBUMS = "featured/local/albums";
    public static final String NEW_TRACKS = "featured/new/tracks";
    public static final String RECOMMENDED_TRACKS = "featured/recommended/tracks";
    public static final String TOP_TRACKS = "featured/top/tracks";
    public static final String LOCAL_TRACKS = "featured/local/tracks";
    public static final String RISING_ALBUMS = "rising/new/albums";
    public static final String RISING_TRACKS = "rising/new/tracks";

    public static final String USER = "users/";
    public static final String USER_PLAYLISTS = "/favorites/playlists";
    public static final String USER_TRACKS = "/favorites/tracks";
    public static final String USER_ABLUMS = "/favorites/albums";
    public static final String PLAYLIST_TRACKS = "playlists/";

    public final static String SEARCH="search/";
    public final static String SEARCH_ALBUM_TYPE="ALBUMS";
    public final static String SEARCH_TRACK_TYPE="TRACKS";
    public final static String SEARCH_PLAYLIST_TYPE="PLAYLISTS";

    private static TidalHelper instance;
    private Context context;
    private TidalRequestController.Callback client;
    private TidalSubscriptionInfo subscriptionInfo;

    private TidalHelper(Context context) {
        this.context = context;
        client = TidalRequestController.getTidalClient();
    }

    public static TidalHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TidalHelper(context.getApplicationContext());
        }
        return instance;
    }

    public Call<TidalBaseResponse> getItemCollection(String path, int offset, int limit) {
        return client.getItemCollection(path,
                TidalRequestController.AUTH_TOKEN,
                Locale.getDefault().getCountry(),
                String.valueOf(offset),
                String.valueOf(limit));
    }

    public Call<PlaylistResponse> getPlaylistTracks(String uuid, int offset, int limit) {
        String sessionId = UserCredentials.getCredentials(context).getSessionId();
        String path = PLAYLIST_TRACKS + uuid + "/items";
        return client.getPlayListTrack(path,
                sessionId,
                Locale.getDefault().getCountry(),
                "INDEX",
                "ASC",
                String.valueOf(offset),
                String.valueOf(limit));
    }

    /**
     * @param musicType @implNote Specify the music type eg:- Album, Track or Playlists.
     */
    public Call<UserMusicResponse> getUserMusic(String musicType) {
        String sessionId = UserCredentials.getCredentials(context).getSessionId();
        String path = USER + UserCredentials.getCredentials(context).getUserId() + musicType;
        return client.getUserMusic(path,
                sessionId,
                Locale.getDefault().getCountry(),
                "NAME",
                "ASC");
    }

    public Call<TrackPlayResponse> getStreamInfo(String trackId) {
        return client.playTrack(UserCredentials.getCredentials(context).getSessionId(),
                trackId,
                subscriptionInfo != null ? subscriptionInfo.getHighestSoundQuality() : null);
    }

    public Call<SearchResponse> searchMusic(String query,String musicType){
        return client.getSearchResult(
                SEARCH,
                TidalRequestController.AUTH_TOKEN,
                query,
                Locale.getDefault().getCountry(),
                musicType);
    }

    public Call<SearchResponse> searchMusic(String query){
        String musicType=SEARCH_TRACK_TYPE+","+SEARCH_ALBUM_TYPE+","+SEARCH_PLAYLIST_TYPE;
        return client.getSearchResult(
                SEARCH,
                TidalRequestController.AUTH_TOKEN,
                query,
                musicType,
                Locale.getDefault().getCountry());
    }

    public void fetchSubscriptionInfo() {
        Call<TidalSubscriptionInfo> call = client.getUserSubscriptionInfo(UserCredentials.getCredentials(context).getSessionId(),
                UserCredentials.getCredentials(context).getUserId());
        call.enqueue(new Callback<TidalSubscriptionInfo>() {
            @Override
            public void onResponse(Call<TidalSubscriptionInfo> call, Response<TidalSubscriptionInfo> response) {
                subscriptionInfo = response.body();
            }

            @Override
            public void onFailure(Call<TidalSubscriptionInfo> call, Throwable t) {

            }
        });
    }


}
