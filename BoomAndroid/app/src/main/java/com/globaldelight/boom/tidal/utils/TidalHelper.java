package com.globaldelight.boom.tidal.utils;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import com.globaldelight.boom.R;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.tidal.tidalconnector.TidalRequestController;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.tidal.tidalconnector.model.response.PlaylistResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.SearchResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalSubscriptionInfo;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TrackPlayResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.UserMusicResponse;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    public static final String USER_ARTISTS = "/favorites/artists";
    public static final String PLAYLIST_TRACKS = "playlists/";

    public final static String SEARCH = "search/";
    public final static String SEARCH_ALBUM_TYPE = "ALBUMS";
    public final static String SEARCH_TRACK_TYPE = "TRACKS";
    public final static String SEARCH_PLAYLIST_TYPE = "PLAYLISTS";
    public final static String SEARCH_ARTISTS_TYPE = "ARTISTS";
    private static TidalHelper instance;
    private String sessionId;
    private String userId;
    private Context context;
    private TidalRequestController.Callback client;
    private TidalSubscriptionInfo subscriptionInfo;
    private List<Item> mMyPlaylists = new ArrayList<>();

    private TidalHelper(Context context) {
        this.context = context;
        client = TidalRequestController.getTidalClient();
        this.sessionId = UserCredentials.getCredentials(context).getSessionId();
        this.userId = UserCredentials.getCredentials(context).getUserId();
    }

    public static TidalHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TidalHelper(context.getApplicationContext());
        }
        return instance;
    }

    public String getUserPath(String path) {
        return USER + userId + path;
    }

    public Call<TidalBaseResponse> getItemCollection(String path, int offset, int limit) {
        return client.getItemCollection(path,
                TidalRequestController.AUTH_TOKEN,
                Locale.getDefault().getCountry(),
                String.valueOf(offset),
                String.valueOf(limit));
    }

    public Call<PlaylistResponse> getPlaylistTracks(String uuid, int offset, int limit) {
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
    public Call<UserMusicResponse> getUserMusic(String musicType, int offset, int limit) {

        return client.getUserMusic(getUserPath(musicType),
                sessionId,
                Locale.getDefault().getCountry(),
                "NAME",
                "ASC",
                String.valueOf(offset), String.valueOf(limit));
    }

    public Call<TidalBaseResponse> getUserPlayLists(int offset, int limit ){
        return client.getUserPlaylist(getUserPath("/playlists"),sessionId,
                Locale.getDefault().getCountry(), String.valueOf(offset), String.valueOf(limit));
    }

    public Call<TrackPlayResponse> getStreamInfo(String trackId) {
        return client.playTrack(sessionId,
                trackId,
                subscriptionInfo != null ? subscriptionInfo.getHighestSoundQuality() : null);
    }

    public Call<SearchResponse> searchMusic(String query, String musicType, int offset, int limit) {
        return client.getSearchResult(
                SEARCH,
                TidalRequestController.AUTH_TOKEN,
                query,
                musicType,
                Locale.getDefault().getCountry(),
                String.valueOf(offset), String.valueOf(limit));
    }

    public Call<SearchResponse> searchMusic(String query) {
        String musicType = SEARCH_TRACK_TYPE + "," + SEARCH_ALBUM_TYPE + "," + SEARCH_PLAYLIST_TYPE+","+SEARCH_ARTISTS_TYPE;
        return client.getSearchResult(
                SEARCH,
                TidalRequestController.AUTH_TOKEN,
                query,
                musicType,
                Locale.getDefault().getCountry(),
                String.valueOf(0), String.valueOf(10));
    }

    public Call<JsonElement> addToPlaylist(String uuid) {
        return client.addToPlaylist(sessionId, userId, uuid, Locale.getDefault().getCountry());
    }

    public Call<JsonElement> addToTrack(String trackId) {
        return client.addToTrack(sessionId, userId, trackId, Locale.getDefault().getCountry());
    }

    public Call<JsonElement> addToAlbum(String albumId) {
        return client.addToAlbum(sessionId, userId, albumId, Locale.getDefault().getCountry());
    }

    public Call<JsonElement> addToArtist(String artist) {
        return client.addToArtists(sessionId, userId, artist, Locale.getDefault().getCountry());
    }

    public Call<JsonElement> removeAlbum(String albumId) {
        return client.deleteAlbum(sessionId, userId, albumId);
    }

    public Call<JsonElement> removePlaylist(String uuid) {
        return client.deletePlaylist(sessionId, userId, uuid);
    }

    public Call<JsonElement> removeTrack(String trackId) {
        return client.deleteTrack(sessionId, userId, trackId);
    }

    public Call<JsonElement> removeArtist(String artist) {
        return client.deleteArtist(sessionId, userId, artist);
    }

    public void fetchSubscriptionInfo() {
        Call<TidalSubscriptionInfo> call = client.getUserSubscriptionInfo(sessionId, userId);
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


    public void setMyPlaylists(List<Item> playlists) {
        mMyPlaylists.clear();
        mMyPlaylists.addAll(playlists);
    }

    public List<Item> getMyPlaylists() {
        return mMyPlaylists;
    }

    public void addItemToPlaylist(Item item, String playlistId) {
        Call<PlaylistResponse> call = getPlaylistTracks(playlistId,0, 1);
        call.enqueue(new Callback<PlaylistResponse>() {
            @Override
            public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {
                if (response.isSuccessful()) {
                    String etag = response.headers().get("etag");
                    new android.os.Handler(Looper.getMainLooper()).post(()->{
                        updatePlaylist(item, playlistId, etag);
                    });
                }
            }

            @Override
            public void onFailure(Call<PlaylistResponse> call, Throwable t) {

            }
        });
    }


    private void updatePlaylist(Item item, String playlistId, String etag) {
        if ( item.getItemType() == ItemType.SONGS ) {
            addItemIdsToPlaylist(Collections.singletonList(item.getId()), playlistId, etag);
        }
        else if ( item.getItemType() == ItemType.PLAYLIST ) {
            addPlaylistToPlaylist(item, playlistId, etag);
        }
        else {
            addCollectionToPlaylist(item, playlistId, etag);
        }
    }


    private void addPlaylistToPlaylist(Item item, String playlistId, String etag) {
        int limit = item.getNumberOfTracks() != null? item.getNumberOfTracks().intValue() : 10;
        Call<PlaylistResponse> call = getPlaylistTracks(item.getUuid(),0, limit );
        call.enqueue(new Callback<PlaylistResponse>() {
            @Override
            public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {
                if (response.isSuccessful()) {
                    ArrayList<String> itemIds = new ArrayList<>();
                    List<ItemWrapper> tracks = response.body().getItems();
                    for ( ItemWrapper aTrack: tracks ) {
                        itemIds.add(aTrack.getItem().getId());
                    }

                    new android.os.Handler(Looper.getMainLooper()).post(()->{
                        addItemIdsToPlaylist(itemIds, playlistId, etag);
                    });
                }
            }

            @Override
            public void onFailure(Call<PlaylistResponse> call, Throwable t) {

            }
        });
    }

    private void addCollectionToPlaylist(Item item, String playlistId, String etag) {
        String path = item.getItemType() == ItemType.ALBUM ?
                "albums/"+ item.getId() + "/tracks" :
                "artists/" + item.getId() +  "/toptracks" ;

        Call<TidalBaseResponse> call = getItemCollection(path,0, item.getNumberOfTracks().intValue());
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()) {
                    ArrayList<String> itemIds = new ArrayList<>();
                    List<Item> tracks = response.body().getItems();
                    for ( Item aTrack: tracks ) {
                        itemIds.add(aTrack.getId());
                    }

                    new android.os.Handler(Looper.getMainLooper()).post(()->{
                        addItemIdsToPlaylist(itemIds, playlistId, etag);
                    });
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }



    // returns a comma seperated String
    private static String listToCSS(List<String> itemIds) {
        StringBuilder builder = new StringBuilder();
        boolean initial = true;
        for ( String anId: itemIds ) {
            if ( !initial ) {
                builder.append(",");
            }
            builder.append(anId);
            initial = false;
        }

        return builder.toString();
    }


    private void addItemIdsToPlaylist(List<String> itemIds, String playlistId, String etag) {
        TidalRequestController.Callback client = TidalRequestController.getTidalClient();
        Call<Void> call = client.addToUserPlaylist(sessionId, etag, playlistId, listToCSS(itemIds), String.valueOf(0), getCountry());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, R.string.added_to_playlist, Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(context, "Failed to add playlist", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Failed to add playlist", Toast.LENGTH_LONG).show();
            }
        });

    }

    private String getCountry() {
        return Locale.getDefault().getCountry();
    }

}
