package com.globaldelight.boom.app.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.Utils;

import static com.globaldelight.boom.app.fragments.MasterContentFragment.isUpdateUpnextDB;

/**
 * Created by adarsh on 09/03/18.
 */

public class MainActivity extends MasterActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private PermissionChecker permissionChecker;
    private ViewGroup mainContainer;
    private LibraryFragment mLibraryFragment;
    public boolean isLibraryRendered = false;
    public MusicSearchHelper musicSearchHelper;
    protected NavigationView navigationView;
    protected Toolbar mToolbar;

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
        permissionChecker = new PermissionChecker(this, this, mainContainer);
        permissionChecker.check(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getResources().getString(R.string.storage_permission),
                new PermissionChecker.OnPermissionResponse() {
                    @Override
                    public void onAccepted() {
                        isUpdateUpnextDB = true;
                        onNavigateToLibrary();
                        initSearchAndArt();
                    }

                    @Override
                    public void onDecline() {
                        finish();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initSearchAndArt(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                musicSearchHelper.getAlbumList(getApplicationContext());
                musicSearchHelper.getArtistList(getApplicationContext());
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
        mLibraryFragment.chooseCoachMarkWindow(isPlayerExpended(), isLibraryRendered);
    }

    @Override
    public void onPanelExpanded(View panel) {
        super.onPanelExpanded(panel);
        mLibraryFragment.setDismissHeadphoneCoachmark();
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
        if ( mLibraryFragment != null ) {
            mLibraryFragment.setAutoDismissBahaviour();
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
        switch (item.getItemId()){
            case R.id.music_library:
                runnable = this::onNavigateToLibrary;
                FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Music_library_Opened_From_Drawer);
                break;
            case R.id.google_drive:
                if (Utils.isOnline(this)){
                    runnable = this::onNavigateToGoogleDrive;
                    FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Google_Drive_OPENED_FROM_DRAWER);
                }else {
                    Utils.networkAlert(this);
                    return false;
                }
                break;

            case R.id.drop_box:
                if (Utils.isOnline(this)){
                    runnable = this::onNavigateToDropbox;
                    FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.DROP_BOX_OPENED_FROM_DRAWER);
                }else {
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

    private void startCompoundActivities(int activityName) {
        Intent intent = new Intent(this, ActivityContainer.class);
        intent.putExtra("container",activityName);
        startActivity(intent);
    }

    private void onNavigateToLibrary() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if ( currentFragment == mLibraryFragment && mLibraryFragment != null ) {
            return;
        }
        isLibraryRendered = true;
        navigationView.getMenu().findItem(R.id.music_library).setChecked(true);
        if ( mLibraryFragment == null ) {
            mLibraryFragment = new LibraryFragment();
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mLibraryFragment).commitAllowingStateLoss();
    }

    private void onNavigateToDropbox() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if ( currentFragment instanceof DropBoxListFragment ) {
            return;
        }

        navigationView.getMenu().findItem(R.id.drop_box).setChecked(true);
        setTitle(getResources().getString(R.string.drop_box));
        Fragment fragment = new DropBoxListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }

    private void onNavigateToGoogleDrive() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if ( currentFragment instanceof GoogleDriveListFragment ) {
            return;
        }

        navigationView.getMenu().findItem(R.id.google_drive).setChecked(true);
        setTitle(getResources().getString(R.string.google_drive));

        Fragment fragment = new GoogleDriveListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }
}
