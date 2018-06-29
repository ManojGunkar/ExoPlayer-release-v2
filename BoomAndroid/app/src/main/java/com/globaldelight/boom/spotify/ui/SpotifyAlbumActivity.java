package com.globaldelight.boom.spotify.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.activities.MasterActivity;
import com.globaldelight.boom.spotify.apiconnector.ApiRequestController;
import com.globaldelight.boom.spotify.apiconnector.SpotifyApiUrls;
import com.globaldelight.boom.spotify.apiconnector.pojo.AlbumPlaylist;
import com.globaldelight.boom.spotify.ui.adapter.ItemClickListener;
import com.globaldelight.boom.spotify.ui.adapter.SpotifyAlbumListAdapter;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.globaldelight.boom.spotify.utils.Helper.ALBUM_ID;
import static com.globaldelight.boom.spotify.utils.Helper.CLIENT_ID;
import static com.globaldelight.boom.spotify.utils.Helper.TOKEN;

/**
 * Created by Manoj Kumar on 10/24/2017.
 */

public class SpotifyAlbumActivity extends MasterActivity implements ItemClickListener,
        Player.NotificationCallback, ConnectionStateCallback {

    private static final String TAG = SpotifyApiUrls.SPOTIFY_TAG;
    private final Player.OperationCallback operationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            Log.d(TAG, "ok");
        }

        @Override
        public void onError(Error error) {
            Log.d(TAG, "error:-" + error.name());
        }
    };
    private RecyclerView recyclerView;
    private SpotifyAlbumListAdapter spotifyAlbumListAdapter;
    private String token;
    private String albumId;
    private String uri;
    private List<AlbumPlaylist.Item> list;
    private SpotifyPlayer spotifyPlayer;
    private Metadata metadata;
    private PlaybackState currentPlaybackState;
    private BroadcastReceiver networkStateReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_album);
        recyclerView = findViewById(R.id.rv_spotify);
        Bundle bundle = getIntent().getExtras();
        token = bundle.getString(TOKEN);
        albumId = bundle.getString(ALBUM_ID);

        call();
    }

    private void call() {
        ApiRequestController.RequestCallback requestCallback = ApiRequestController.getClient();
        Call<AlbumPlaylist> call = requestCallback.getAlbumPlayList(albumId, "Bearer " + token);
        call.enqueue(new Callback<AlbumPlaylist>() {
            @Override
            public void onResponse(Call<AlbumPlaylist> call, Response<AlbumPlaylist> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "success" + response.message());
                    AlbumPlaylist album = response.body();
                    list = album.getTracks().getItems();

                    recyclerView.setLayoutManager(new LinearLayoutManager(SpotifyAlbumActivity.this));
                    spotifyAlbumListAdapter = new SpotifyAlbumListAdapter(SpotifyAlbumActivity.this, list);
                    recyclerView.setAdapter(spotifyAlbumListAdapter);

                } else {
                    Log.d(TAG, "fail," + response.message());
                }
            }

            @Override
            public void onFailure(Call<AlbumPlaylist> call, Throwable t) {
                Log.d(TAG, "GotError: " + t.getMessage());
            }
        });

        initSpotifyPlayer();
    }

    private void initSpotifyPlayer() {
        if (spotifyPlayer == null) {
            Config playerConfig = new Config(this, token, CLIENT_ID);

            spotifyPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer spotifyPlayer) {
                    spotifyPlayer.setConnectivityStatus(operationCallback, getNetworkConnectivity());
                    spotifyPlayer.addNotificationCallback(SpotifyAlbumActivity.this);
                    spotifyPlayer.addConnectionStateCallback(SpotifyAlbumActivity.this);
                }

                @Override
                public void onError(Throwable throwable) {

                }
            });
        } else {
            spotifyPlayer.login(token);
        }
    }

    private Connectivity getNetworkConnectivity() {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return Connectivity.fromNetworkType(activeNetwork.getType());
        } else {
            return Connectivity.OFFLINE;
        }
    }


    @Override
    public void onItemClick(View view, int position) {
        uri = list.get(position).getUri();
        spotifyPlayer.playUri(operationCallback, uri, 0, 0);
    }

    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Error error) {

    }


    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        currentPlaybackState = spotifyPlayer.getPlaybackState();
        metadata = spotifyPlayer.getMetadata();
        Log.i(TAG, "Player state: " + currentPlaybackState);
        Log.i(TAG, "Metadata: " + metadata);
    }

    @Override
    public void onPlaybackError(Error error) {

    }


    @Override
    public void onResume() {
        super.onResume();

        networkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (spotifyPlayer != null) {
                    Connectivity connectivity = getNetworkConnectivity();
                    Log.d(TAG, "Network state changed: " + connectivity.toString());
                    spotifyPlayer.setConnectivityStatus(operationCallback, connectivity);
                }
            }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStateReceiver, filter);

        if (spotifyPlayer != null) {
            spotifyPlayer.addNotificationCallback(SpotifyAlbumActivity.this);
            spotifyPlayer.addConnectionStateCallback(SpotifyAlbumActivity.this);
        }
    }
}
