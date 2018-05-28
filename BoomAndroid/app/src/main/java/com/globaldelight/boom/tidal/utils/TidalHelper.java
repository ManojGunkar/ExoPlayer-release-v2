package com.globaldelight.boom.tidal.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import com.globaldelight.boom.R;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.tidal.tidalconnector.TidalRequestController;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.tidal.tidalconnector.model.ItemWrapper;
import com.globaldelight.boom.tidal.tidalconnector.model.response.PlaylistResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.SearchResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalSubscriptionInfo;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TrackPlayResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.UserMusicResponse;
import com.globaldelight.boom.tidal.ui.GridDetailActivity;
import com.globaldelight.boom.tidal.ui.adapter.PlaylistTrackAdapter;
import com.globaldelight.boom.tidal.ui.adapter.TrackDetailAdapter;
import com.globaldelight.boom.utils.Log;
import com.globaldelight.boom.utils.Result;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by adarsh on 03/05/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class TidalHelper {

    public interface CompletionHandler <T> {
         void onComplete(Result<T> result);
    }

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
    public static final String USER_PLAYLISTS = "/favor";
    public static final String USER_TRACKS = "/favor";
    public static final String USER_ABLUMS = "/favo";
    public static final String USER_ARTISTS = "/favori";
    public static final String PLAYLIST_TRACKS = "playlists/";

    public final static String SEARCH = "search/";
    public final static String SEARCH_ALBUM_TYPE = "ALBUMS";
    public final static String SEARCH_TRACK_TYPE = "TRACKS";
    public final static String SEARCH_PLAYLIST_TYPE = "PLAYLISTS";
    public final static String SEARCH_ARTISTS_TYPE = "ARTISTS";

    private String sessionId;
    private String userId;
    private Context context;
    private TidalRequestController.Callback client;
    private TidalSubscriptionInfo subscriptionInfo;
    private List<Item> mMyPlaylists = new ArrayList<>();
    private FavoritesManager mFavoriteManager;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    private static TidalHelper instance = null;
    public static TidalHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TidalHelper(context.getApplicationContext());
        }
        return instance;
    }


    private TidalHelper(Context context) {
        this.context = context;
        client = TidalRequestController.getTidalClient();
        mFavoriteManager = new FavoritesManager(context, client);
    }


    public void loadUserData() {
        this.sessionId = UserCredentials.getCredentials(context).getSessionId();
        this.userId = UserCredentials.getCredentials(context).getUserId();
        mMyPlaylists.clear();
        fetchSubscriptionInfo();
        loadUserPlaylist();
        mFavoriteManager.load();
    }


    public FavoritesManager getFavoriteManager() {
        return mFavoriteManager;
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


    public void getTracks(Item parent, CompletionHandler<List<Item>> completionHandler) {
        Integer trackCount = parent.getNumberOfTracks();
        if ( trackCount == null ) {
            trackCount = new Integer(999);
        }

        if (parent.getItemType() == ItemType.PLAYLIST) {
            Call<PlaylistResponse> call = getPlaylistTracks(parent.getId(), 0, trackCount);
            call.enqueue(new Callback<PlaylistResponse>() {
                @Override
                public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {
                    if (response.isSuccessful()) {
                        List<ItemWrapper> wrappedItems = response.body().getItems();
                        ArrayList<Item> items = new ArrayList<>();
                        for (ItemWrapper wrapped : wrappedItems) {
                            items.add(wrapped.getItem());
                        }
                        mMainHandler.post(() -> completionHandler.onComplete(Result.success(items)));
                    } else {
                        mMainHandler.post(() -> completionHandler.onComplete(Result.error(response.code(), response.message())));
                    }
                }

                @Override
                public void onFailure(Call<PlaylistResponse> call, Throwable t) {
                    mMainHandler.post(()->completionHandler.onComplete(Result.error(-1, null)));
                }
            });

        }
        else {
            String path;
            switch (parent.getItemType()) {
                case ItemType.ARTIST:
                    path = "artists/" + parent.getId() + "/toptracks";
                    break;

                default:
                case ItemType.ALBUM:
                    path = "albums/" + parent.getId() + "/tracks";
                    break;
            }
            Call<TidalBaseResponse> call = getItemCollection(path, 0, trackCount);
            call.enqueue(new Callback<TidalBaseResponse>() {
                @Override
                public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                    if (response.isSuccessful()) {
                        mMainHandler.post(() -> completionHandler.onComplete(Result.success(response.body().getItems())));
                    } else {
                        mMainHandler.post(() -> completionHandler.onComplete(Result.error(response.code(), response.message())));
                    }
                }

                @Override
                public void onFailure(Call<TidalBaseResponse> call, Throwable t) {
                    mMainHandler.post(()->completionHandler.onComplete(Result.error(-1, null)));
                }
            });
        }
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

    public Call<Void> addToFavorites(Item item) {
        switch (item.getItemType()) {
            default:
            case ItemType.SONGS:
                return client.addTrack(sessionId, userId, item.getId(), Locale.getDefault().getCountry());

            case ItemType.ALBUM:
                return client.addAlbum(sessionId, userId, item.getId(), Locale.getDefault().getCountry());

            case ItemType.ARTIST:
                return client.addArtist(sessionId, userId, item.getId(), Locale.getDefault().getCountry());

            case ItemType.PLAYLIST:
                return client.addPlaylist(sessionId, userId, item.getId(), Locale.getDefault().getCountry());
        }
    }

    public Call<Void> removeFromFavorites(Item item) {
        switch (item.getItemType()) {
            default:
            case ItemType.SONGS:
                return client.deleteTrack(sessionId, userId, item.getId());

            case ItemType.ALBUM:
                return client.deleteAlbum(sessionId, userId, item.getId());

            case ItemType.ARTIST:
                return client.deleteArtist(sessionId, userId, item.getId());

            case ItemType.PLAYLIST:
                return client.deletePlaylist(sessionId, userId, item.getId());
        }
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

    public void loadUserPlaylist() {
        Call<TidalBaseResponse> call = TidalHelper.getInstance(context).getUserPlayLists(0,1);
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()) {
                    loadFullUserPlaylists(response.body().getTotalNumberOfItems());
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });
    }

    public void createPlaylist(String name, String description, CompletionHandler<Item> completion) {
        Call<Item> call = client.createPlaylist(sessionId, userId, name, description);
        call.enqueue(new ResponseAdapter<Item>(completion) {
            @Override
            public void onProcess(Item body) {
                super.onProcess(body);
                addToUserPlaylist(body);
            }
        });
    }

    public void renamePlaylist(Item playlist, String newName, CompletionHandler<Void> completion) {
        Call<Void> call = client.renamePlaylist(sessionId, playlist.getId(), newName, "my playlist");
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if ( response.isSuccessful() ) {
                    mMainHandler.post(()->{
                        for ( Item aPlaylist: mMyPlaylists ) {
                            if ( aPlaylist.equalTo(playlist) ) {
                                aPlaylist.setTitle(newName);
                                break;
                            }
                        }
                        mMainHandler.post(()->completion.onComplete(Result.success(null)));
                    });
                }
                else {
                    mMainHandler.post(()->{
                        completion.onComplete(Result.error(response.code(), response.message()));
                    });
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mMainHandler.post(()->{
                    completion.onComplete(Result.error(-1, "Failed"));
                });
            }
        });
    }

    public void deletePlaylist(Item playlist, CompletionHandler<Void> completion) {
        Call<Void> call = client.deletePlaylist(sessionId, playlist.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if ( response.isSuccessful() ) {
                    mMainHandler.post(()->{
                        int count = mMyPlaylists.size();
                        int index = -1;
                        for ( int i = 0; i < count; i++ ) {
                            if ( mMyPlaylists.get(i).equalTo(playlist) ) {
                                index = i;
                                break;
                            }
                        }
                        if ( index != -1 ) {
                            mMyPlaylists.remove(index);
                        }
                        mMainHandler.post(()->completion.onComplete(Result.success(null)));
                    });
                }
                else {
                    mMainHandler.post(()->{
                        completion.onComplete(Result.error(response.code(), response.message()));
                    });
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mMainHandler.post(()->{
                    completion.onComplete(Result.error(-1, "Failed"));
                });
            }
        });

    }


    private void loadFullUserPlaylists(int itemCount) {
        Call<TidalBaseResponse> call = TidalHelper.getInstance(context).getUserPlayLists(0,itemCount);
        call.enqueue(new Callback<TidalBaseResponse>() {
            @Override
            public void onResponse(Call<TidalBaseResponse> call, Response<TidalBaseResponse> response) {
                if (response.isSuccessful()) {
                    mMyPlaylists.clear();
                    mMyPlaylists.addAll(response.body().getItems());
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {

            }
        });

    }

    public List<Item> getUserPlaylists() {
        return mMyPlaylists;
    }


    public void addToUserPlaylist(Item item) {
        mMyPlaylists.add(item);
    }


    public void playlistMoveItem(Item playlist, int fromIndex, int toIndex, CompletionHandler<Void> completion) {
        TidalRequestController.Callback client = TidalRequestController.getTidalClient();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            String path = PLAYLIST_TRACKS + playlist.getId() + "/items";
            Call<PlaylistResponse> call = client.getPlayListTrack(path,
                    sessionId,
                    getCountry(),
                    "INDEX",
                    "ASC",
                    String.valueOf(0),
                    String.valueOf(1));
            try {
                Response<PlaylistResponse> response = call.execute();
                if (response.isSuccessful()) {
                    String etag = response.headers().get("etag");
                    mMainHandler.post(()->movedItem(playlist.getId(), etag, fromIndex, toIndex, completion));
                }
            } catch (IOException e) {
                mMainHandler.post(()->completion.onComplete(Result.error(-1, "")));
            }
        });


    }

    private void movedItem(String uuid, String etag, int fromIndex, int toIndex, CompletionHandler<Void> completion) {
        TidalRequestController.Callback client = TidalRequestController.getTidalClient();
        Call<Void> call = client.moveItem(sessionId, etag, uuid, String.valueOf(fromIndex), String.valueOf(toIndex));
        call.enqueue(new ResponseAdapter<>(completion));
    }



    public void addItemToPlaylist(Item item, String playlistId, final CompletionHandler completion) {
        Call<PlaylistResponse> call = getPlaylistTracks(playlistId,0, 1);
        call.enqueue(new Callback<PlaylistResponse>() {
            @Override
            public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {
                if (response.isSuccessful()) {
                    String etag = response.headers().get("etag");
                    new android.os.Handler(Looper.getMainLooper()).post(()->{
                        updatePlaylist(item, playlistId, etag, completion);
                    });
                }
            }

            @Override
            public void onFailure(Call<PlaylistResponse> call, Throwable t) {
                mMainHandler.post(()->completion.onComplete(Result.error(-1, "")));
            }
        });
    }


    private void updatePlaylist(Item item, String playlistId, String etag, CompletionHandler completion) {
        if ( item.getItemType() == ItemType.SONGS ) {
            addItemIdsToPlaylist(Collections.singletonList(item.getId()), playlistId, etag, completion);
        }
        else if ( item.getItemType() == ItemType.PLAYLIST ) {
            addPlaylistToPlaylist(item, playlistId, etag, completion);
        }
        else {
            addCollectionToPlaylist(item, playlistId, etag, completion);
        }
    }


    private void addPlaylistToPlaylist(Item item, String playlistId, String etag, CompletionHandler completion) {
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
                        addItemIdsToPlaylist(itemIds, playlistId, etag, completion);
                    });
                }
            }

            @Override
            public void onFailure(Call<PlaylistResponse> call, Throwable t) {
                mMainHandler.post(()->completion.onComplete(Result.error(-1, "")));
            }
        });
    }

    private void addCollectionToPlaylist(Item item, String playlistId, String etag, CompletionHandler completion) {
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
                        addItemIdsToPlaylist(itemIds, playlistId, etag, completion);
                    });
                }
            }

            @Override
            public void onFailure(Call<TidalBaseResponse> call, Throwable t) {
                mMainHandler.post(()->completion.onComplete(Result.error(-1, "")));
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


    private void addItemIdsToPlaylist(List<String> itemIds, String playlistId, String etag, CompletionHandler completion) {
        TidalRequestController.Callback client = TidalRequestController.getTidalClient();
        Call<Void> call = client.addToUserPlaylist(sessionId, etag, playlistId, listToCSS(itemIds), String.valueOf(0), getCountry());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    completion.onComplete(Result.success(null));
                }
                else {
                    completion.onComplete(Result.error(-1, "failed"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                completion.onComplete(Result.error(-1, "failed"));
            }
        });

    }

    private String getCountry() {
        return Locale.getDefault().getCountry();
    }

    private class ResponseAdapter<T> implements Callback<T> {

        private CompletionHandler<T> completionHandler;

        public ResponseAdapter(CompletionHandler<T> completion) {
            completionHandler = completion;
        }

        @Override
        public void onResponse(Call<T> call, Response<T> response) {
            if ( response.isSuccessful() ) {
                T body = response.body();
                onProcess(body);
                mMainHandler.post(()->completionHandler.onComplete(Result.success(body)));
            }
            else {
                mMainHandler.post(()->completionHandler.onComplete(Result.error(response.code(), response.message())));
            }
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            mMainHandler.post(()->completionHandler.onComplete(Result.error(-1, "Failed")));
        }

        public void onProcess(T body) {

        }
    }
}
