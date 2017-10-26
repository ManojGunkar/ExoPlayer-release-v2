package com.globaldelight.boom.spotify.fragment;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.globaldelight.boom.R;
import com.globaldelight.boom.spotify.activity.SpotifyActivity;
import com.globaldelight.boom.spotify.adapter.ItemClickListener;
import com.globaldelight.boom.spotify.adapter.SpotifyAlbumListAdapter;
import com.globaldelight.boom.spotify.apiconnector.ApiRequestController;
import com.globaldelight.boom.spotify.apiconnector.SpotifyApiUrls;
import com.globaldelight.boom.spotify.pojo.AlbumPlaylist;
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

import static com.globaldelight.boom.spotify.activity.SpotifyActivity.CLIENT_ID;

/**
 * Created by Manoj Kumar on 10/24/2017.
 */

public class SpotifyAlbumFragment extends Fragment implements ItemClickListener,
        Player.NotificationCallback, ConnectionStateCallback {

    private static final String TAG = SpotifyApiUrls.SPOTIFY_TAG;

    private RecyclerView recyclerView;
    private SpotifyAlbumListAdapter spotifyAlbumListAdapter;
    private ProgressDialog dialog;

    private Context context;

    private String token;
    private String albumId;
    private List<AlbumPlaylist.Item> list;

    private SpotifyPlayer spotifyPlayer;
    private Metadata metadata;
    private PlaybackState currentPlaybackState;
    private BroadcastReceiver networkStateReceiver;

    private final Player.OperationCallback operationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            Log.d(TAG, "ok");
        }

        @Override
        public void onError(Error error) {
            Log.d(TAG, "error:-"+error.name());
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);
        token = getArguments().getString(SpotifyActivity.TOKEN);
        albumId = getArguments().getString(SpotifyActivity.ALBUM_ID);
        context = getActivity();
        dialog = new ProgressDialog(getActivity());
        dialog.setTitle("loading...");
        dialog.setCancelable(false);
        dialog.show();

        ApiRequestController.RequestCallback requestCallback = ApiRequestController.getClient();
        Call<AlbumPlaylist> call = requestCallback.getAlbumPlayList(albumId, "Bearer " + token);
        call.enqueue(new Callback<AlbumPlaylist>() {
            @Override
            public void onResponse(Call<AlbumPlaylist> call, Response<AlbumPlaylist> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "success" + response.message());
                    dialog.dismiss();
                    AlbumPlaylist album = response.body();
                    list = album.getTracks().getItems();
                    Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show();
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                    spotifyAlbumListAdapter = new SpotifyAlbumListAdapter(context, list);
                    spotifyAlbumListAdapter.setClickListener(SpotifyAlbumFragment.this);
                    recyclerView.setAdapter(spotifyAlbumListAdapter);

                } else {
                    dialog.dismiss();
                    Log.d(TAG, "fail," + response.message());
                }
            }

            @Override
            public void onFailure(Call<AlbumPlaylist> call, Throwable t) {
                dialog.dismiss();
                Log.d(TAG, "GotError: " + t.getMessage());
            }
        });

        initSpotifyPlayer();

        return recyclerView;
    }

    private void initSpotifyPlayer() {
        if (spotifyPlayer == null) {
            Config playerConfig = new Config(context, token, CLIENT_ID);

            spotifyPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer spotifyPlayer) {
                    spotifyPlayer.setConnectivityStatus(operationCallback, getNetworkConnectivity(context));
                    spotifyPlayer.addNotificationCallback(SpotifyAlbumFragment.this);
                    spotifyPlayer.addConnectionStateCallback(SpotifyAlbumFragment.this);
                }

                @Override
                public void onError(Throwable throwable) {

                }
            });
        } else {
            spotifyPlayer.login(token);
        }
    }

    private Connectivity getNetworkConnectivity(Context context) {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return Connectivity.fromNetworkType(activeNetwork.getType());
        } else {
            return Connectivity.OFFLINE;
        }
    }


    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(context, "click pos " + position, Toast.LENGTH_SHORT).show();
        spotifyPlayer.playUri(operationCallback, albumId, 0, 0);
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
                    Connectivity connectivity = getNetworkConnectivity(getActivity());
                    Log.d(TAG, "Network state changed: " + connectivity.toString());
                    spotifyPlayer.setConnectivityStatus(operationCallback, connectivity);
                }
            }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(networkStateReceiver, filter);

        if (spotifyPlayer != null) {
            spotifyPlayer.addNotificationCallback(SpotifyAlbumFragment.this);
            spotifyPlayer.addConnectionStateCallback(SpotifyAlbumFragment.this);
        }
    }
}
