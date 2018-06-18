package com.globaldelight.boom.spotify.activity;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.globaldelight.boom.BuildConfig;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.activities.ActivityContainer;
import com.globaldelight.boom.app.activities.MainActivity;
import com.globaldelight.boom.app.activities.MasterActivity;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.app.fragments.DropBoxListFragment;
import com.globaldelight.boom.app.fragments.GoogleDriveListFragment;
import com.globaldelight.boom.spotify.adapter.ItemClickListener;
import com.globaldelight.boom.spotify.adapter.SpotifyAlbumAdapter;
import com.globaldelight.boom.spotify.apiconnector.ApiRequestController;
import com.globaldelight.boom.spotify.apiconnector.SpotifyApiUrls;
import com.globaldelight.boom.spotify.fragment.SpotifyAlbumFragment;
import com.globaldelight.boom.spotify.pojo.NewReleaseAlbums;
import com.globaldelight.boom.utils.Utils;
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

public class SpotifyActivity extends MasterActivity
        implements NavigationView.OnNavigationItemSelectedListener, ItemClickListener {

    public static final String TOKEN = "token";
    public static final String ALBUM_ID = "albumid";
    public static final String CLIENT_ID = "0fd566b3d5d249eb83acbc3fa6cb3235";
    private static final String TAG = SpotifyApiUrls.SPOTIFY_TAG;
    private static final String CLIENT_SECRET = "5947157861f44103a9b62b9d0bdb84d1";
    private static final String REDIRECT_URI = "spotify://callback";

    private static final int REQUEST_CODE = 1337;
    private static AuthenticationResponse response;

    private List<NewReleaseAlbums.Item> list;
    private SpotifyAlbumAdapter spotifyAlbumAdapter;
    private RecyclerView recyclerView;

    private NavigationView navigationView;
    Runnable navigateLibrary = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.music_library).setChecked(true);
            Intent libraryIntent = new Intent(SpotifyActivity.this, MainActivity.class);
            libraryIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(libraryIntent);
            overridePendingTransition(R.anim.com_mixpanel_android_fade_in, R.anim.com_mixpanel_android_fade_out);
        }
    };
    Runnable navigateDropbox = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.drop_box).setChecked(true);
            Fragment fragment = new DropBoxListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
        }
    };
    Runnable navigateGoogleDrive = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.google_drive).setChecked(true);
            Fragment fragment = new GoogleDriveListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
        }
    };
    private TextView toolbarTitle;
    private Runnable runnable;

    public static String getToken() {
        return "Bearer " + response.getAccessToken();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify);
        initView();
        recyclerView = findViewById(R.id.grid_album_spotify);
        openLoginWindow();

    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
      //  toolbarTitle = findViewById(R.id.toolbar_txt);
      //  setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.drawer_background));
        navigationView.setNavigationItemSelectedListener(this);
       /* if (!BuildConfig.BUSINESS_MODEL_ENABLED) {
            navigationView.getMenu().removeItem(R.id.nav_store);
            navigationView.getMenu().removeItem(R.id.nav_share);
        }*/

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        runnable = null;
        switch (item.getItemId()) {
            case R.id.music_library:
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//                FlurryAnalyticHelper.logEvent(UtilAnalytics.Music_library_Opened_From_Drawer);
                FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Music_library_Opened_From_Drawer);
                runnable = navigateLibrary;
                break;
            case R.id.google_drive:
                if (Utils.isOnline(this)) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    runnable = navigateGoogleDrive;
//                    FlurryAnalyticHelper.logEvent(UtilAnalytics.Google_Drive_OPENED_FROM_DRAWER);
                    FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Google_Drive_OPENED_FROM_DRAWER);

                } else {
                    Utils.networkAlert(this);
                    return false;
                }
                break;
            case R.id.drop_box:
                if (Utils.isOnline(this)) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    runnable = navigateDropbox;
//                    FlurryAnalyticHelper.logEvent(UtilAnalytics.DROP_BOX_OPENED_FROM_DRAWER);
                    FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.DROP_BOX_OPENED_FROM_DRAWER);

                } else {
                    Utils.networkAlert(this);
                    return false;
                }
                break;
            case R.id.nav_setting:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startCompoundActivities(R.string.title_settings);
                    }
                }, 300);
                drawerLayout.closeDrawer(GravityCompat.START);
//                FlurryAnalyticHelper.logEvent(UtilAnalytics.Settings_Page_Opened);
                FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Settings_Page_Opened);
                return true;
            case R.id.nav_store:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startCompoundActivities(R.string.store_title);
                    }
                }, 300);
                drawerLayout.closeDrawer(GravityCompat.START);
//                FlurryAnalyticHelper.logEvent(UtilAnalytics.Store_Page_Opened_from_Drawer);
                FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Store_Page_Opened_from_Drawer);

                return true;
            case R.id.nav_share:
             //   Utils.shareStart(SpotifyActivity.this);
                drawerLayout.closeDrawer(GravityCompat.START);
//                FlurryAnalyticHelper.logEvent(UtilAnalytics.Share_Opened_from_Boom);
                FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Share_Opened_from_Boom);
                return true;
        }

        if (runnable != null) {
            item.setChecked(true);
            Handler handler = new Handler();
            handler.postDelayed(runnable, 300);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startCompoundActivities(int activityName) {
        Intent intent = new Intent(this, ActivityContainer.class);
        intent.putExtra("container", activityName);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        loadEveryThing(intent.getStringExtra("title"), true);
    }

    private void loadEveryThing(String title, boolean anim) {
        if (null != title && title.equals(getResources().getString(R.string.drop_box))) {
            new Handler().postDelayed(navigateDropbox, anim ? 300 : 0);
        } else if (title.equals(getResources().getString(R.string.google_drive))) {
            new Handler().postDelayed(navigateGoogleDrive, anim ? 300 : 0);
        }
    }
}