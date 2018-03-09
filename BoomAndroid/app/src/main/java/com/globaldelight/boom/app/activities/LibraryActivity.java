package com.globaldelight.boom.app.activities;

import android.Manifest;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.app.receivers.actions.PlayerEvents;
import com.globaldelight.boom.app.adapters.search.SearchSuggestionAdapter;
import com.globaldelight.boom.app.fragments.LibraryFragment;
import com.globaldelight.boom.app.fragments.SearchViewFragment;
import com.globaldelight.boom.business.BusinessModelFactory;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.app.database.MusicSearchHelper;

import java.util.HashMap;
import java.util.Map;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_HEADSET_PLUGGED;
import static com.globaldelight.boom.app.fragments.MasterContentFragment.isUpdateUpnextDB;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class LibraryActivity extends MainActivity {

    private PermissionChecker permissionChecker;
    private ViewGroup mainContainer;
    private Fragment mSearchResult, mLibraryFragment;
    private boolean isLibraryRendered = false;
    public SearchView searchView;
    public MenuItem searchMenuItem;
    private MusicSearchHelper musicSearchHelper;
    private SearchSuggestionAdapter searchSuggestionAdapter;
    public static String[] columns = new String[]{"_id", "FEED_TITLE"};

    private BroadcastReceiver headPhoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch ( intent.getAction() ) {
                case ACTION_HEADSET_PLUGGED:
                    if( null != mLibraryFragment) {
                        ((LibraryFragment)mLibraryFragment).setDismissHeadphoneCoachmark();
                        ((LibraryFragment)mLibraryFragment).chooseCoachMarkWindow(isPlayerExpended(), isLibraryRendered);
                    }
                    break;

                case PlayerEvents.ACTION_PLAYER_STATE_CHANGED:
                    if ( mLibraryFragment != null && App.playbackManager().isPlaying() ) {
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

        LocalBroadcastManager.getInstance(this).registerReceiver(headPhoneReceiver, intentFilter);
    }

    private void checkPermissions() {
        permissionChecker = new PermissionChecker(this, LibraryActivity.this, mainContainer);
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
        setTitle(R.string.music_library);
        mainContainer = findViewById(R.id.coordinate_main);
        musicSearchHelper = new MusicSearchHelper(LibraryActivity.this);
    }

    @Override
    public void onPanelCollapsed(View panel) {
        super.onPanelCollapsed(panel);
        ((LibraryFragment)mLibraryFragment).chooseCoachMarkWindow(isPlayerExpended(), isLibraryRendered);
    }

    @Override
    public void onPanelExpanded(View panel) {
        super.onPanelExpanded(panel);
        ((LibraryFragment)mLibraryFragment).setDismissHeadphoneCoachmark();
    }

    @Override
    public void onBackPressed() {
        contentFragment.onBackPressed();
        ((LibraryFragment)mLibraryFragment).setAutoDismissBahaviour();

        if (isPlayerExpended()) {
            toggleSlidingPanel();
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
        searchSuggestionAdapter = new SearchSuggestionAdapter(LibraryActivity.this, R.layout.card_search_suggestion_item, null, columns,null, -1000);
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(headPhoneReceiver);
        App.playbackManager().isLibraryResumes = false;
        super.onPause();
    }

    @Override
    protected void onNavigateToLibrary() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if ( currentFragment == mLibraryFragment && mLibraryFragment != null ) {
            return;
        }
        isLibraryRendered = true;
        navigationView.getMenu().findItem(R.id.music_library).setChecked(true);
        mLibraryFragment = new LibraryFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mLibraryFragment).commitAllowingStateLoss();
    }

    @Override
    protected void onNavigateToDropbox() {
        navigationView.getMenu().findItem(R.id.drop_box).setChecked(true);
        Intent dropboxIntent = new Intent(LibraryActivity.this, CloudListActivity.class);
        dropboxIntent.putExtra("title", getResources().getString(R.string.drop_box));
        startActivity(dropboxIntent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onNavigateToGoogleDrive() {
        navigationView.getMenu().findItem(R.id.google_drive).setChecked(true);
        Intent driveIntent = new Intent(LibraryActivity.this, CloudListActivity.class);
        driveIntent.putExtra("title", getResources().getString(R.string.google_drive));
        startActivity(driveIntent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
