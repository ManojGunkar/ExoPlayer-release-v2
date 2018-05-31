package com.globaldelight.boom.tidal.utils;

import android.content.Context;
import android.os.Looper;
import android.os.Handler;

import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.tidal.tidalconnector.TidalRequestController;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.tidal.tidalconnector.model.response.FavoritesResponse;

import java.util.HashSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by adarsh on 22/05/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class FavoritesManager {

    private Context mContext;

    private HashSet<String> mTracks = new HashSet<>();
    private HashSet<String> mAlbums = new HashSet<>();
    private HashSet<String> mArtists = new HashSet<>();
    private HashSet<String> mPlaylists = new HashSet<>();

    private TidalRequestController.Callback mClient;

    public FavoritesManager(Context context, TidalRequestController.Callback client) {
        mContext = context;
        mClient = client;
    }

    public void load() {

        mTracks.clear();
        mAlbums.clear();
        mArtists.clear();
        mPlaylists.clear();

        final UserCredentials credentials = UserCredentials.getCredentials(mContext);
        Call<FavoritesResponse> call = mClient.getFavorites(credentials.getSessionId(), credentials.getUserId());
        call.enqueue(new Callback<FavoritesResponse>() {

            @Override
            public void onResponse(Call<FavoritesResponse> call, Response<FavoritesResponse> response) {
                if ( response.isSuccessful() ) {
                    FavoritesResponse body = response.body();
                    new Handler(Looper.getMainLooper()).post(()->{
                        mTracks.addAll(body.getTracks());
                        mAlbums.addAll(body.getAlbums());
                        mArtists.addAll(body.getArtists());
                        mPlaylists.addAll(body.getPlaylists());
                    });
                }
            }

            @Override
            public void onFailure(Call<FavoritesResponse> call, Throwable t) {

            }
        });
    }

    public boolean isFavorite(Item item) {
        return setForItem(item).contains(item.getId());
    }

    public void addToFavorites(Item item) {
        setForItem(item).add(item.getId());
    }

    public void removeFromFavorites(Item item) {
        setForItem(item).remove(item.getId());
    }


    private HashSet<String> setForItem(Item item) {
        switch (item.getItemType()) {
            default:
            case ItemType.SONGS:
                return mTracks;

            case ItemType.ALBUM:
                return mAlbums;

            case ItemType.ARTIST:
                return mArtists;

            case ItemType.PLAYLIST:
                return mPlaylists;
        }
    }
}
