package com.globaldelight.boom.app.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.app.database.MusicSearchHelper;
import com.globaldelight.boom.app.fragments.DropBoxListFragment;
import com.globaldelight.boom.app.fragments.GoogleDriveListFragment;
import com.globaldelight.boom.app.fragments.LibraryFragment;
import com.globaldelight.boom.business.BusinessModelFactory;
import com.globaldelight.boom.collection.base.IMediaElement;
import com.globaldelight.boom.playbackEvent.handler.PlaybackManager;
import com.globaldelight.boom.playbackEvent.utils.DeviceMediaLibrary;
import com.globaldelight.boom.playbackEvent.utils.MediaType;
import com.globaldelight.boom.radio.ui.fragments.RadioMainFragment;
import com.globaldelight.boom.spotify.activity.SpotifyMainFragment;
import com.globaldelight.boom.tidal.ui.fragment.TidalLoginFragment;
import com.globaldelight.boom.tidal.ui.fragment.TidalMainFragment;
import com.globaldelight.boom.tidal.utils.UserCredentials;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.Utils;

import static com.globaldelight.boom.app.fragments.MasterContentFragment.isUpdateUpnextDB;
import static com.globaldelight.boom.radio.ui.adapter.RadioFragmentStateAdapter.KEY_TYPE;

/**
 * Created by adarsh on 09/03/18.
 */

public class MainActivity extends MasterActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public boolean isLibraryRendered = false;
    public MusicSearchHelper musicSearchHelper;
    protected NavigationView navigationView;
    protected Toolbar mToolbar;
    private PermissionChecker permissionChecker;
    private ViewGroup mainContainer;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        BusinessModelFactory.getCurrentModel().addItemsToDrawer(navigationView.getMenu(), Menu.NONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        checkPermissions();
    }

    @Override
    protected void onResume() {
        App.playbackManager().isLibraryResumes = true;
        super.onResume();
    }

    private void checkPermissions() {
        permissionChecker = new PermissionChecker(this, mainContainer, PermissionChecker.STORAGE_READ_PERMISSION);
        permissionChecker.check(Manifest.permission.READ_EXTERNAL_STORAGE,
                getResources().getString(R.string.storage_permission),
                new PermissionChecker.OnPermissionResponse() {
                    @Override
                    public void onAccepted() {
                        isUpdateUpnextDB = true;
                        initSearchAndArt();
                        IMediaElement playingItem = PlaybackManager.getInstance(MainActivity.this).getPlayingItem();
                        if (playingItem != null && playingItem.getMediaType() == MediaType.RADIO) {
                            onNavigateToRadio();
                        }
                        else if (playingItem != null && playingItem.getMediaType() == MediaType.TIDAL) {
                            onNavigateToTidal();
                        }
                        else if (playingItem != null && playingItem.getMediaType() == MediaType.PODCAST) {
                            onNavigateToPodcast();
                        }
                        else {
                            onNavigateToLibrary();
                        }
                    }

                    @Override
                    public void onDecline() {
                        finish();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initSearchAndArt() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DeviceMediaLibrary.getInstance(MainActivity.this).initAlbumAndArtist();
                musicSearchHelper.setSearchContent();
            }
        }).start();
    }

    private void initView() {
        mainContainer = findViewById(R.id.coordinate_main);
        musicSearchHelper = new MusicSearchHelper(this);
    }

    @Override
    public void onPanelCollapsed(View panel) {
        super.onPanelCollapsed(panel);

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof LibraryFragment) {
            ((LibraryFragment) currentFragment).chooseCoachMarkWindow(isPlayerExpended(), isLibraryRendered);
        }

    }

    @Override
    public void onPanelExpanded(View panel) {
        super.onPanelExpanded(panel);

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof LibraryFragment) {
            ((LibraryFragment) currentFragment).setDismissHeadphoneCoachmark();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            //    String query = intent.getStringExtra(SearchManager.QUERY);
            //   searchView.setQuery(query, false);
            //    fetchAndUpdateSearchResult(query);
        }
    }

    @Override
    protected void onPause() {
        App.playbackManager().isLibraryResumes = false;
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        contentFragment.onBackPressed();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof LibraryFragment) {
            ((LibraryFragment) currentFragment).setAutoDismissBahaviour();
        }

        if (isPlayerExpended()) {
            toggleSlidingPanel();
        } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
        }
    }


    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        Runnable runnable;
        runnable = null;
        switch (item.getItemId()) {
            case R.id.music_library:
                runnable = this::onNavigateToLibrary;
                FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Music_library_Opened_From_Drawer);
                break;
            case R.id.google_drive:
                if (Utils.isOnline(this)) {
                    runnable = this::onNavigateToGoogleDrive;
                    FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Google_Drive_OPENED_FROM_DRAWER);
                } else {
                    Utils.networkAlert(this);
                    return false;
                }
                break;

            case R.id.drop_box:
                if (Utils.isOnline(this)) {
                    runnable = this::onNavigateToDropbox;
                    FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.DROP_BOX_OPENED_FROM_DRAWER);
                } else {
                    Utils.networkAlert(this);
                    return false;
                }
                break;
            case R.id.nav_setting:
                new Handler().postDelayed(() -> startCompoundActivities(R.string.title_settings), 300);
                drawerLayout.closeDrawer(GravityCompat.START);
//                FlurryAnalyticHelper.logEvent(UtilAnalytics.Settings_Page_Opened);
                FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Settings_Page_Opened);
                return true;

            case R.id.radio:
                if (Utils.isOnline(this)) {
                    runnable = this::onNavigateToRadio;
                } else {
                    Utils.networkAlert(this);
                    return false;
                }
                break;

            case R.id.podcast:
                if (Utils.isOnline(this)) {
                    runnable = this::onNavigateToPodcast;
                } else {
                    Utils.networkAlert(this);
                    return false;
                }
                break;

            case R.id.tidal:
                runnable = this::onNavigateToTidal;
                break;

            case R.id.spotify:
                runnable=this::onNavigationToSpotify;
                break;

            default:
                BusinessModelFactory.getCurrentModel().onDrawerItemClicked(item, this);
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
        }

        if (runnable != null) {
            item.setChecked(true);
            Handler handler = new Handler();
            handler.postDelayed(runnable, 300);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onNavigationToSpotify() {
        Fragment fragment = new SpotifyMainFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }


    private void startCompoundActivities(int activityName) {
        Intent intent = new Intent(this, ActivityContainer.class);
        intent.putExtra("container", activityName);
        startActivity(intent);
    }

    private void onNavigateToLibrary() {
        //   AdsBuilder.buildInterstitialGoogleAds(this).onComplete();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof LibraryFragment) {
            return;
        }

        isLibraryRendered = true;
        navigationView.getMenu().findItem(R.id.music_library).setChecked(true);
        setTitle(R.string.music_library);

        Fragment fragment = new LibraryFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }

    private void onNavigateToRadio() {
        //  AdsBuilder.buildInterstitialGoogleAds(this).onComplete();
        navigationView.getMenu().findItem(R.id.radio).setChecked(true);
        setTitle(R.string.radio);
        Fragment fragment = new RadioMainFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TYPE, "radio");
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }

    private void onNavigateToPodcast() {
        // AdsBuilder.buildInterstitialGoogleAds(this).onComplete();
        navigationView.getMenu().findItem(R.id.podcast).setChecked(true);
        setTitle(R.string.podcast);
        Fragment fragment = new RadioMainFragment();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TYPE, "podcast");
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }

    private void onNavigateToTidal() {
        //   AdsBuilder.buildInterstitialGoogleAds(this).onComplete();
        navigationView.getMenu().findItem(R.id.tidal).setChecked(true);
        setTitle(R.string.tidal);
        Fragment fragment = null;
        if (UserCredentials.getCredentials(this).isUserLogged()) {
            fragment = new TidalMainFragment();
        } else {
            fragment = new TidalLoginFragment();
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }

    private void onNavigateToDropbox() {
        //  AdsBuilder.buildInterstitialGoogleAds(this).onComplete();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof DropBoxListFragment) {
            return;
        }

        navigationView.getMenu().findItem(R.id.drop_box).setChecked(true);
        setTitle(R.string.drop_box);
        Fragment fragment = new DropBoxListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }

    private void onNavigateToGoogleDrive() {
        //  AdsBuilder.buildInterstitialGoogleAds(this).onComplete();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof GoogleDriveListFragment) {
            return;
        }

        navigationView.getMenu().findItem(R.id.google_drive).setChecked(true);
        setTitle(R.string.google_drive);

        Fragment fragment = new GoogleDriveListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }
}
