package com.globaldelight.boom.spotify.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.globaldelight.boom.R;
import com.globaldelight.boom.spotify.apiconnector.ApiRequestController;
import com.globaldelight.boom.spotify.apiconnector.SpotifyApiUrls;
import com.globaldelight.boom.spotify.pojo.NewReleaseAlbums;
import com.globaldelight.boom.spotify.ui.adapter.SpotifyAlbumAdapter;
import com.globaldelight.boom.spotify.ui.fragment.SpotifyAlbumFragment;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.globaldelight.boom.spotify.utils.Helper.CLIENT_ID;
import static com.globaldelight.boom.spotify.utils.Helper.REDIRECT_URI;
import static com.globaldelight.boom.spotify.utils.Helper.SCOPES;
import static com.globaldelight.boom.spotify.utils.Helper.TOKEN;

/**
 * Created by Manoj Kumar on 10/12/2017.
 */

public class SpotifyLoginActivity extends AppCompatActivity {

    private static final String TAG = SpotifyApiUrls.SPOTIFY_TAG;
    private static final int REQUEST_CODE = 1337;
    private static AuthenticationResponse response;

    private Context context = this;

    private List<NewReleaseAlbums.Item> list;
    private SpotifyAlbumAdapter spotifyAlbumAdapter;
    private RecyclerView recyclerView;

    public static String getToken() {
        return "Bearer " + response.getAccessToken();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_login);
        recyclerView = findViewById(R.id.grid_album_spotify);
        Toolbar toolbar = findViewById(R.id.toolbar_spotify_login);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        openLoginWindow();
    }

    private void logOut(){
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                        .setShowDialog(true)
                        .setScopes(SCOPES);
        AuthenticationRequest request=builder.build();
    }

    private void openLoginWindow() {
        final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(SCOPES)
                .build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            response = AuthenticationClient.getResponse(resultCode, intent);
            Log.d(TAG, "Token:-" + response.getAccessToken());

            switch (response.getType()) {
                case TOKEN:
                    loadData();
                    break;

                case ERROR:
                    Log.w(TAG, "Auth error: " + response.getError());
                    break;
            }
        }
    }

    private void loadData() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("loading...");
        dialog.setCancelable(false);
        dialog.show();
        ApiRequestController.RequestCallback requestCallback = ApiRequestController.getClient();
        Call<NewReleaseAlbums> call = requestCallback.getSpotifyAlbum("Bearer " + response.getAccessToken());

        call.enqueue(new Callback<NewReleaseAlbums>() {
            @Override
            public void onResponse(Call<NewReleaseAlbums> call, Response<NewReleaseAlbums> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "success");
                    dialog.dismiss();
                    NewReleaseAlbums album = response.body();
                    list = album.getAlbums().getItems();
                    Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show();
                    recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
                    spotifyAlbumAdapter = new SpotifyAlbumAdapter(context, list);
                    recyclerView.setAdapter(spotifyAlbumAdapter);
                } else {
                    dialog.dismiss();
                    Log.d(TAG, "Error");
                }
            }

            @Override
            public void onFailure(Call<NewReleaseAlbums> call, Throwable t) {
                dialog.dismiss();
                Log.d(TAG, "Error-" + t.getMessage());
            }
        });
    }

    private void jumpToTrack() {

        Bundle bundle = new Bundle();
        bundle.putString(TOKEN, response.getAccessToken());
        //  bundle.putString(ALBUM_ID, list.get(position).getId());
        SpotifyAlbumFragment albumFragment = new SpotifyAlbumFragment();
        albumFragment.setArguments(bundle);


    }


}