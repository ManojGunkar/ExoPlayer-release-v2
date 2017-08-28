package com.globaldelight.boom.app.activities;

import android.Manifest;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.app.receivers.actions.PlayerEvents;
import com.globaldelight.boom.app.adapters.search.SearchSuggestionAdapter;
import com.globaldelight.boom.app.fragments.LibraryFragment;
import com.globaldelight.boom.app.fragments.SearchViewFragment;
import com.globaldelight.boom.app.share.ShareAdapter;
import com.globaldelight.boom.app.share.ShareDialog;
import com.globaldelight.boom.app.share.ShareItem;
import com.globaldelight.boom.view.RegularTextView;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.app.database.MusicSearchHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_HEADSET_PLUGGED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_HOME_SCREEN_BACK_PRESSED;
import static com.globaldelight.boom.app.fragments.MasterContentFragment.isUpdateUpnextDB;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class MainActivity extends MasterActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private PermissionChecker permissionChecker;
    private CoordinatorLayout mainContainer;
    private NavigationView navigationView;
    private Fragment mSearchResult, mLibraryFragment;
    private boolean isLibraryRendered = false;
    private RegularTextView toolbarTitle;
    public SearchView searchView;
    public MenuItem searchMenuItem;
    private MusicSearchHelper musicSearchHelper;
    private SearchSuggestionAdapter searchSuggestionAdapter;
    public static String[] columns = new String[]{"_id", "FEED_TITLE"};
    Map<String, Runnable> navigationMap = new HashMap<String, Runnable>();
    Runnable runnable;
    String action;

    private BroadcastReceiver headPhoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch ( intent.getAction() ) {
                case ACTION_HEADSET_PLUGGED:
                    if( null != mLibraryFragment) {
                        ((LibraryFragment)mLibraryFragment).chooseCoachMarkWindow(isPlayerExpended(), isLibraryRendered);
                    }
                    break;

                case PlayerEvents.ACTION_PLAYER_STATE_CHANGED:
                    if ( mLibraryFragment != null ) {
                        ((LibraryFragment)mLibraryFragment).useCoachMarkWindow();
                        ((LibraryFragment)mLibraryFragment).chooseCoachMarkWindow(isPlayerExpended(), isLibraryRendered);
                    }
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        checkPermissions();
    }

    Runnable navigateLibrary = new Runnable() {
        public void run() {
            isLibraryRendered = true;
            toolbarTitle.setText(getResources().getString(R.string.music_library));
            navigationView.getMenu().findItem(R.id.music_library).setChecked(true);
            mLibraryFragment = new LibraryFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, mLibraryFragment).commitAllowingStateLoss();
        }
    };

    Runnable navigateDropbox= new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.drop_box).setChecked(true);
            Intent dropboxIntent = new Intent(MainActivity.this, CloudListActivity.class);
            dropboxIntent.putExtra("title", getResources().getString(R.string.drop_box));
            startActivity(dropboxIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    };

    Runnable navigateGoogleDrive = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.google_drive).setChecked(true);
            Intent driveIntent = new Intent(MainActivity.this, CloudListActivity.class);
            driveIntent.putExtra("title", getResources().getString(R.string.google_drive));
            startActivity(driveIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    };

    @Override
    protected void onResume() {
        if(null != navigationView)
            navigationView.getMenu().findItem(R.id.music_library).setChecked(true);
        registerHeadSetReceiver();
        App.playbackManager().isLibraryResumes = true;
        super.onResume();
    }

    private void registerHeadSetReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_HEADSET_PLUGGED);
        intentFilter.addAction(PlayerEvents.ACTION_PLAYER_STATE_CHANGED);

        registerReceiver(headPhoneReceiver, intentFilter);
    }

    private void checkPermissions() {
        permissionChecker = new PermissionChecker(this, MainActivity.this, mainContainer);
        permissionChecker.check(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getResources().getString(R.string.storage_permission),
                new PermissionChecker.OnPermissionResponse() {
                    @Override
                    public void onAccepted() {
                        isUpdateUpnextDB = true;
                        loadEverything();
                        initSearchAndArt();
                    }

                    @Override
                    public void onDecline() {
                        finish();
                    }
                });
    }

    private void loadEverything() {
        Runnable navigation = navigationMap.get(action);
        if (navigation != null) {
            navigation.run();
        } else {
            navigateLibrary.run();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initSearchAndArt(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                musicSearchHelper.getAlbumList(App.getApplication());
                musicSearchHelper.getArtistList(App.getApplication());
                musicSearchHelper.setSearchContent();
            }
        }).start();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (RegularTextView) findViewById(R.id.toolbar_txt);
        setTitle(getResources().getString(R.string.music_library));
        setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        setSupportActionBar(toolbar);

        navigationMap.put(PlayerEvents.NAVIGATE_LIBRARY, navigateLibrary);
        navigationMap.put(PlayerEvents.NAVIGATE_GOOGLE_DRIVE, navigateGoogleDrive);
        navigationMap.put(PlayerEvents.NAVIGATE_DROPBOX, navigateDropbox);

        mainContainer = (CoordinatorLayout) findViewById(R.id.coordinate_main);

        musicSearchHelper = new MusicSearchHelper(MainActivity.this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.drawer_background));
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResumeFragments() {
        sendBroadcast(new Intent(PlayerEvents.ACTION_PLAYER_SCREEN_RESUME));
        super.onResumeFragments();
    }

    @Override
    public void onPanelCollapsed(View panel) {
        super.onPanelCollapsed(panel);
        if(null != mLibraryFragment){
        }
    }

    @Override
    public void onPanelExpanded(View panel) {
        super.onPanelExpanded(panel);
        ((LibraryFragment)mLibraryFragment).setDismissHeadphoneCoachmark();
    }

    @Override
    public void onBackPressed() {
        sendBroadcast(new Intent(ACTION_HOME_SCREEN_BACK_PRESSED));

        ((LibraryFragment)mLibraryFragment).setAutoDismissBahaviour();

        if (isPlayerExpended()) {
            sendBroadcast(new Intent(PlayerEvents.ACTION_TOGGLE_PLAYER_SLIDE));
        } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.library_menu, menu);
        searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        searchView.setLayoutParams(params);
        searchView.setDrawingCacheBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
        searchView.setMaxWidth(2000);
        searchView.setIconified(true);


        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                String feedName = cursor.getString(1);
                searchView.setQuery(feedName, false);
                fetchAndUpdateSearchResult(feedName);
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                String feedName = cursor.getString(1);
                searchView.setQuery(feedName, false);
                fetchAndUpdateSearchResult(feedName);
                return true;
            }
        });
        searchSuggestionAdapter = new SearchSuggestionAdapter(MainActivity.this, R.layout.card_search_suggestion_item, null, columns,null, -1000);
        searchView.setSuggestionsAdapter(searchSuggestionAdapter);
        registerSearchListeners();

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mSearchResult = new SearchViewFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out).replace(R.id.search_container, mSearchResult).commitAllowingStateLoss();
                setVisibleLibrary(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                setVisibleLibrary(true);
                searchSuggestionAdapter.changeCursor(null);
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            return true;
        }
        return false;
    }

    private MatrixCursor convertToCursor(Cursor feedlyResults) {
        MatrixCursor cursor = new MatrixCursor(columns);
        if (feedlyResults != null && feedlyResults.moveToFirst()) {
            do{
                String[] temp = new String[2];
                temp[0] = Integer.toString(feedlyResults.getInt(0));
                temp[1] = feedlyResults.getString(1);
                cursor.addRow(temp);
            } while (feedlyResults.moveToNext());
        }
        return cursor;
    }

    private void registerSearchListeners() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchAndUpdateSearchResult(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if(query.length() >= 2) {
                    MatrixCursor matrixCursor = convertToCursor(musicSearchHelper.getSongList(query));
                    searchSuggestionAdapter.changeCursor(matrixCursor);
                }
                if(null == query || query.length() < 2){
                    searchSuggestionAdapter.changeCursor(null);
                }
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void fetchAndUpdateSearchResult(String query) {
        if(null != mSearchResult)
            ((SearchViewFragment) mSearchResult).updateSearchResult(query);
        searchView.clearFocus();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        runnable = null;
        switch (item.getItemId()){
            case R.id.music_library:
                runnable = navigateLibrary;
//                FlurryAnalyticHelper.logEvent(UtilAnalytics.Music_library_Opened_From_Drawer);
                FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Music_library_Opened_From_Drawer);
                break;
            case R.id.google_drive:
                if (Utils.isOnline(this)){
                    runnable = navigateGoogleDrive;
//                    FlurryAnalyticHelper.logEvent(UtilAnalytics.Google_Drive_OPENED_FROM_DRAWER);
                    FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Google_Drive_OPENED_FROM_DRAWER);
                }else {
                    Utils.networkAlert(this);
                    return false;
                }
                break;
            case R.id.drop_box:
                if (Utils.isOnline(this)){
                    runnable = navigateDropbox;
//                    FlurryAnalyticHelper.logEvent(UtilAnalytics.DROP_BOX_OPENED_FROM_DRAWER);
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
            case R.id.nav_store:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startCompoundActivities(R.string.store_title);
                    }
                }, 300);
                drawerLayout.closeDrawer(GravityCompat.START);
                FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Store_Page_Opened_from_Drawer);
//                FlurryAnalyticHelper.logEvent(UtilAnalytics.Store_Page_Opened_from_Drawer);
                return  true;
            case R.id.nav_share:
                Toast.makeText(this,"share",Toast.LENGTH_SHORT).show();
                new ShareDialog(this).show();
                /*new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       // startCompoundActivities(R.string.title_share);
                    }
                }, 300);*/
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
        intent.putExtra("container",activityName);
        startActivity(intent);
    }

    public void setVisibleLibrary(boolean visible){
        if(visible){
            isLibraryRendered = true;
            findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
            findViewById(R.id.search_container).setVisibility(View.GONE);
        }else{
            findViewById(R.id.fragment_container).setVisibility(View.GONE);
            findViewById(R.id.search_container).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchView.setQuery(query, false);
            fetchAndUpdateSearchResult(query);
        }
    }

    @Override
    protected void onPause() {
        unregisterReceiver(headPhoneReceiver);
        App.playbackManager().isLibraryResumes = false;
        super.onPause();
    }
}
