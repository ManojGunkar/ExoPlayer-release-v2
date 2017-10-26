package com.globaldelight.boom.spotify.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.globaldelight.boom.R;
import com.globaldelight.boom.spotify.adapter.ItemClickListener;
import com.globaldelight.boom.spotify.adapter.SpotifyAlbumAdapter;
import com.globaldelight.boom.spotify.apiconnector.ApiRequestController;
import com.globaldelight.boom.spotify.apiconnector.SpotifyApiUrls;
import com.globaldelight.boom.spotify.fragment.SpotifyAlbumFragment;
import com.globaldelight.boom.spotify.pojo.NewReleaseAlbums;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Manoj Kumar on 10/12/2017.
 */

public class SpotifyActivity extends AppCompatActivity implements ItemClickListener {

    private static final String TAG = SpotifyApiUrls.SPOTIFY_TAG;

    public static final String TOKEN = "token";
    public static final String ALBUM_ID = "album";


    public static final String CLIENT_ID = "0fd566b3d5d249eb83acbc3fa6cb3235";
    private static final String CLIENT_SECRET = "5947157861f44103a9b62b9d0bdb84d1";
    private static final String REDIRECT_URI = "spotify://callback";

    private static final int REQUEST_CODE = 1337;
    private static AuthenticationResponse response;

    private List<NewReleaseAlbums.Item> list;

    private SpotifyAlbumAdapter spotifyAlbumAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify);
        recyclerView = (RecyclerView) findViewById(R.id.grid_album_spotify);
        openLoginWindow();

    }

    public static String getToken() {
        return "Bearer " + response.getAccessToken();
    }

    private void openLoginWindow() {
        final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(new String[]{"user-read-private", "playlist-read", "playlist-read-private", "streaming"})
                .build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            response = AuthenticationClient.getResponse(resultCode, intent);
            Log.d(TAG, "Token:-" + response.getAccessToken());

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    final ProgressDialog dialog = new ProgressDialog(SpotifyActivity.this);
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
                                Toast.makeText(SpotifyActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                                recyclerView.setLayoutManager(new GridLayoutManager(SpotifyActivity.this, 2));
                                spotifyAlbumAdapter = new SpotifyAlbumAdapter(SpotifyActivity.this, list);
                                spotifyAlbumAdapter.setClickListener(SpotifyActivity.this);
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
                    break;

                case ERROR:
                    Log.w(TAG, "Auth error: " + response.getError());
                    break;
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "click pos " + position, Toast.LENGTH_SHORT).show();

        Bundle bundle = new Bundle();
        bundle.putString(TOKEN, response.getAccessToken());
        bundle.putString(ALBUM_ID, list.get(position).getId());
        SpotifyAlbumFragment albumFragment = new SpotifyAlbumFragment();
        albumFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame_spotify, albumFragment)
                .commit();

    }
}