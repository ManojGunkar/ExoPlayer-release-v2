package com.globaldelight.boom.ui.musiclist.activity;

import android.Manifest;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.globaldelight.boom.business.BusinessUtils;
import com.globaldelight.boom.manager.HeadPhonePlugReceiver;
import com.globaldelight.boom.manager.PlayerServiceReceiver;
import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.task.PlayerEvents;
import com.globaldelight.boom.ui.musiclist.adapter.utils.SearchSuggestionAdapter;
import com.globaldelight.boom.ui.musiclist.fragment.DropBoxListFragment;
import com.globaldelight.boom.ui.musiclist.fragment.FavouriteListFragment;
import com.globaldelight.boom.ui.musiclist.fragment.LibraryFragment;
import com.globaldelight.boom.ui.musiclist.fragment.SearchViewFragment;
import com.globaldelight.boom.ui.musiclist.fragment.BoomPlaylistFragment;
import com.globaldelight.boom.ui.musiclist.fragment.GoogleDriveListFragment;
import com.globaldelight.boom.ui.widgets.CoachMarkerWindow;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.handlers.MusicSearchHelper;
import com.globaldelight.boom.utils.handlers.Preferences;

import java.util.HashMap;
import java.util.Map;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_HEADSET_PLUGGED;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_HOME_SCREEN_BACK_PRESSED;
import static com.globaldelight.boom.ui.musiclist.fragment.MasterContentFragment.isUpdateUpnextDB;
import static com.globaldelight.boom.ui.widgets.CoachMarkerWindow.DRAW_NORMAL_BOTTOM;
import static com.globaldelight.boom.utils.handlers.Preferences.HEADPHONE_CONNECTED;
import static com.globaldelight.boom.utils.handlers.Preferences.TOLLTIP_CHOOSE_HEADPHONE_LIBRARY;
import static com.globaldelight.boom.utils.handlers.Preferences.TOLLTIP_OPEN_EFFECT_MINI_PLAYER;
import static com.globaldelight.boom.utils.handlers.Preferences.TOLLTIP_SWITCH_EFFECT_SCREEN_EFFECT;
import static com.globaldelight.boom.utils.handlers.Preferences.TOLLTIP_USE_HEADPHONE_LIBRARY;
import static com.globaldelight.boom.utils.handlers.Preferences.TOLLTIP_USE_24_HEADPHONE_LIBRARY;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class MainActivity extends MasterActivity
        implements NavigationView.OnNavigationItemSelectedListener, MasterActivity.ILibraryAddsUpdater{

    private PermissionChecker permissionChecker;
    private CoordinatorLayout mainContainer;
    private NavigationView navigationView;
    private Fragment mSearchResult, mLibraryFragment;
    private int fade_in = android.R.anim.fade_in;
    private int fade_out = android.R.anim.fade_out;
    private boolean isLibraryRendered = false;
    private RegularTextView toolbarTitle;
    private Toolbar toolbar;
    public SearchView searchView;
    public MenuItem searchMenuItem, cloudSyncItem;
    private MusicSearchHelper musicSearchHelper;
    private SearchSuggestionAdapter searchSuggestionAdapter;
    public static String[] columns = new String[]{"_id", "FEED_TITLE"};
    Map<String, Runnable> navigationMap = new HashMap<String, Runnable>();
    Runnable runnable;
    String action;
    private FloatingActionButton mFloatAddPlayList;

    private BroadcastReceiver headPhoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == ACTION_HEADSET_PLUGGED && null != mLibraryFragment){
                ((LibraryFragment)mLibraryFragment).chooseCoachMarkWindow(isPlayerExpended(), isLibraryRendered);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);
        sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_CREATE_PLAYER_SCREEN));
        setLibraryAddsUpdater(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (RegularTextView) findViewById(R.id.toolbar_txt);
        setTitle(getResources().getString(R.string.music_library));
        setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        setSupportActionBar(toolbar);
        initView();
        checkPermissions();
    }

    Runnable navigateLibrary = new Runnable() {
        public void run() {
            isLibraryRendered = true;
            setVisibleSearch(true);
            setVisibleCloudSync(false);
            setTitle(getResources().getString(R.string.music_library));
            navigationView.getMenu().findItem(R.id.music_library).setChecked(true);
            mLibraryFragment = new LibraryFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(fade_in, fade_out).replace(R.id.fragment_container, mLibraryFragment).commitAllowingStateLoss();
        }
    };

    Runnable navigateDropbox= new Runnable() {
        public void run() {
            isLibraryRendered = false;
            setTitle(getResources().getString(R.string.drop_box));
            setVisibleSearch(false);
            setVisibleCloudSync(true);
            navigationView.getMenu().findItem(R.id.drop_box).setChecked(true);
            Fragment fragment = new DropBoxListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(fade_in, fade_out).replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
        }
    };

    Runnable navigateGoogleDrive = new Runnable() {
        public void run() {
            isLibraryRendered = false;
            setTitle(getResources().getString(R.string.google_drive));
            setVisibleSearch(false);
            setVisibleCloudSync(true);
            navigationView.getMenu().findItem(R.id.google_drive).setChecked(true);
            Fragment fragment = new GoogleDriveListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(fade_in, fade_out).replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
        }
    };

    Runnable navigateBoomPlaylist = new Runnable() {
        public void run() {
            isLibraryRendered = false;
            setTitle(getResources().getString(R.string.boom_playlist));
            setVisibleSearch(false);
            setVisibleCloudSync(false);
            navigationView.getMenu().findItem(R.id.boom_palylist).setChecked(true);
            Fragment fragment = new BoomPlaylistFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(fade_in, fade_out).replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
        }
    };

    Runnable navigateFavorite = new Runnable() {
        public void run() {
            isLibraryRendered = false;
            setTitle(getResources().getString(R.string.favourite_list));
            setVisibleSearch(false);
            setVisibleCloudSync(false);
            navigationView.getMenu().findItem(R.id.favourite_list).setChecked(true);
            Fragment fragment = new FavouriteListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(fade_in, fade_out).replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
        }
    };

    @Override
    protected void onResume() {
        registerHeadSetReceiver();
        App.getPlayerEventHandler().isLibraryResumes = true;
        super.onResume();
    }

    private void registerHeadSetReceiver(){
        registerPlayerReceiver(MainActivity.this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_HEADSET_PLUGGED);
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
        navigationMap.put(PlayerEvents.NAVIGATE_LIBRARY, navigateLibrary);
        navigationMap.put(PlayerEvents.NAVIGATE_BOOM_PLAYLIST, navigateBoomPlaylist);
        navigationMap.put(PlayerEvents.NAVIGATE_FAVOURITE, navigateFavorite);
        navigationMap.put(PlayerEvents.NAVIGATE_GOOGLE_DRIVE, navigateGoogleDrive);
        navigationMap.put(PlayerEvents.NAVIGATE_DROPBOX, navigateDropbox);

        mainContainer = (CoordinatorLayout) findViewById(R.id.coordinate_main);

        musicSearchHelper = new MusicSearchHelper(MainActivity.this);

        mFloatAddPlayList = (FloatingActionButton) findViewById(R.id.fab);
        mFloatAddPlayList.setVisibility(View.GONE);
        mFloatAddPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendBroadcast(new Intent(PlayerEvents.ACTION_ADD_NEW_BOOM_PLAYLIST));
            }
        });

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
            ((LibraryFragment)mLibraryFragment).useCoachMarkWindow();
            ((LibraryFragment)mLibraryFragment).chooseCoachMarkWindow(isPlayerExpended(), isLibraryRendered);
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

        if (null != mFloatAddPlayList && mFloatAddPlayList.getVisibility() == View.VISIBLE)
            mFloatAddPlayList.setVisibility(View.GONE);

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
        getMenuInflater().inflate(R.menu.main, menu);

        cloudSyncItem = menu.findItem(R.id.action_cloud_sync);
        cloudSyncItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        setVisibleSearch(true);
        setVisibleCloudSync(false);
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
                transaction.setCustomAnimations(fade_in, fade_out).replace(R.id.search_container, mSearchResult).commitAllowingStateLoss();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }else if(id == R.id.action_cloud_sync){
            sendBroadcast(new Intent(PlayerEvents.ACTION_CLOUD_SYNC));
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
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        mFloatAddPlayList.setVisibility(View.GONE);
        runnable = null;
            switch (item.getItemId()){
                case R.id.music_library:
                    runnable = navigateLibrary;
                    break;
                case R.id.boom_palylist:
                    runnable = navigateBoomPlaylist;
                    break;
                case R.id.favourite_list:
                    runnable = navigateFavorite;
                    break;
                case R.id.google_drive:
                    runnable = navigateGoogleDrive;
                    break;
                case R.id.drop_box:
                    runnable = navigateDropbox;
                    break;
                case R.id.nav_setting:
                    isLibraryRendered = false;
                    startCompoundActivities(R.string.title_settings);
                    break;
                case R.id.nav_store:
                    isLibraryRendered = false;
                    startCompoundActivities(R.string.store_title);
                    break;
                case R.id.nav_share:
                    Utils.shareStart(this);
                    break;
            }
            drawerLayout.closeDrawer(GravityCompat.START);

        if (runnable != null) {
            item.setChecked(true);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            }, 350);
        }
        return true;
    }

    private void startCompoundActivities(int activityName) {
        Intent intent = new Intent(this, ActivityContainer.class);
        intent.putExtra("container",activityName);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void setVisibleSearch(boolean enable){
        if(null != searchMenuItem)
            searchMenuItem.setVisible(enable);
    }

    private void setVisibleCloudSync(boolean enable){
        if(null != cloudSyncItem)
            cloudSyncItem.setVisible(enable);
    }

    public void setTitle(String title){
        if(null != toolbarTitle)
            toolbarTitle.setText(title);
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
        unregisterPlayerReceiver(MainActivity.this);
        unregisterReceiver(headPhoneReceiver);
        App.getPlayerEventHandler().isLibraryResumes = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        updateUpNextDB();
        super.onDestroy();
    }

    private void updateUpNextDB() {
        sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_DESTROY_PLAYER_SCREEN));
        if(!App.getPlayerEventHandler().isPlaying() && null != App.getService()) {
            App.getService().stopSelf();
        }
    }

    @Override
    public void onAddsUpdate(BusinessUtils.AddSource addSources, boolean isAddEnable, View addContainer) {
        if(null != mLibraryFragment){
            ((LibraryFragment)mLibraryFragment).updateAdds(addSources, isAddEnable, addContainer);
        }
    }

    public void setEmptyPlaceHolder(Drawable placeHolderImg, String placeHolderTxt, boolean enable) {
        if(null != placeHolderTxt && null != placeHolderImg) {
            ((ImageView) findViewById(R.id.list_empty_placeholder_icon)).setImageDrawable(placeHolderImg);
            ((RegularTextView) findViewById(R.id.list_empty_placeholder_txt)).setText(placeHolderTxt);
        }
        findViewById(R.id.list_empty_placeholder).setVisibility(enable ? View.VISIBLE : View.GONE);
    }
}
