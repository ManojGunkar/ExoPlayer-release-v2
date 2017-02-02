package com.globaldelight.boom.ui.musiclist.activity;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.globaldelight.boom.business.BusinessHandler;
import com.globaldelight.boom.business.IFBAddsUpdater;
import com.globaldelight.boom.business.IGoogleAddsUpdater;
import com.globaldelight.boom.data.MediaCallback.DropboxMediaList;
import com.globaldelight.boom.data.MediaCallback.FavouriteMediaList;
import com.globaldelight.boom.data.MediaCallback.GoogleDriveMediaList;
import com.globaldelight.boom.manager.PlayerServiceReceiver;
import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.task.PlayerEvents;
import com.globaldelight.boom.ui.musiclist.adapter.SearchSuggestionAdapter;
import com.globaldelight.boom.ui.musiclist.adapter.SectionsPagerAdapter;
import com.globaldelight.boom.ui.musiclist.fragment.SearchViewFragment;
import com.globaldelight.boom.ui.musiclist.fragment.BoomPlaylistFragment;
import com.globaldelight.boom.ui.musiclist.fragment.ItemSongListFragment;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.handlers.MusicSearchHelper;
import com.google.android.gms.ads.NativeExpressAdView;

import static android.support.design.widget.TabLayout.MODE_SCROLLABLE;
import static com.globaldelight.boom.ui.musiclist.fragment.MasterContentFragment.isUpdateUpnextDB;

public class MainActivity extends MasterActivity
        implements NavigationView.OnNavigationItemSelectedListener, IFBAddsUpdater, IGoogleAddsUpdater{

    private PermissionChecker permissionChecker;
    private DrawerLayout drawerLayout;
    private Fragment mSearchResult, mFragment;
    private FrameLayout fragmentContainer;
    private int currentItem = 0;
    private int fade_in = android.R.anim.fade_in;
    private int fade_out = android.R.anim.fade_out;
    private FragmentManager fragmentManager;
    private Toolbar toolbar;
    public SearchView searchView;
    public MenuItem searchMenuItem;
    private MusicSearchHelper musicSearchHelper;
    private SearchSuggestionAdapter searchSuggestionAdapter;
    public static String[] columns = new String[]{"_id", "FEED_TITLE"};

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private FloatingActionButton mFloatAddPlayList;
    private TabLayout tabLayout;

    private LinearLayout mAddsContainer;
    private BusinessHandler mBusinessHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_CREATE_PLAYER_SCREEN));
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.music_library));
        setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        setSupportActionBar(toolbar);
        checkPermissions();
    }

    @Override
    protected void onResume() {
        registerPlayerReceiver(MainActivity.this);
        super.onResume();
    }

    private void checkPermissions() {
        permissionChecker = new PermissionChecker(this, MainActivity.this, drawerLayout);
        permissionChecker.check(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getResources().getString(R.string.storage_permission),
                new PermissionChecker.OnPermissionResponse() {
                    @Override
                    public void onAccepted() {
                        initView();
                        initSearchAndArt();
                    }

                    @Override
                    public void onDecline() {
                        finish();
                    }
                });
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
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mAddsContainer = (LinearLayout) findViewById(R.id.lib_add_container);
        mBusinessHandler = BusinessHandler.getBusinessHandlerInstance(MainActivity.this);

        musicSearchHelper = new MusicSearchHelper(MainActivity.this);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        mFloatAddPlayList = (FloatingActionButton) findViewById(R.id.fab);
        mFloatAddPlayList.setVisibility(View.GONE);
        mFloatAddPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mFragment instanceof BoomPlaylistFragment)
                    ((BoomPlaylistFragment)mFragment).newPlaylistDialog();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        int[] items = {R.string.artists, R.string.albums, R.string.songs, R.string.playlists, R.string.genres};
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), items);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setTabMode(MODE_SCROLLABLE);
        tabLayout.setTabGravity(MODE_SCROLLABLE);
        for (int i = 0 ; i < 5; i++){
            ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(i);
            View tabViewChild = vgTab.getChildAt(1);
            if (tabViewChild instanceof TextView) {
//                ((TextView) tabViewChild).setTextColor(Color.RED);
            }
        }

        fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);

        fragmentManager = getSupportFragmentManager();

        isUpdateUpnextDB = true;
    }

    @Override
    protected void onResumeFragments() {
        sendBroadcast(new Intent(PlayerEvents.ACTION_PLAYER_SCREEN_RESUME));
        super.onResumeFragments();
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        mFloatAddPlayList.setVisibility(View.GONE);
        if(!FavouriteMediaList.getFavouriteListInstance(this).getFavouriteMediaList().isEmpty())
            FavouriteMediaList.getFavouriteListInstance(this).clearFavouriteContent();
        if(!DropboxMediaList.getDropboxListInstance(this).getDropboxMediaList().isEmpty())
            DropboxMediaList.getDropboxListInstance(this).clearDropboxContent();
        if(!GoogleDriveMediaList.geGoogleDriveMediaListInstance(this).getGoogleDriveMediaList().isEmpty())
            GoogleDriveMediaList.geGoogleDriveMediaListInstance(this).clearGoogleDriveMediaContent();
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else /*if(fragmentManager.getBackStackEntryCount() >= 0) {
            Log.d("fragmentManager : ", "backstack");
            super.onBackPressed();
            int count;
            if ((count = fragmentManager.getBackStackEntryCount()) >= 0){
                if(count > 0)
                    setTitle(fragmentManager.getBackStackEntryAt(count -1 ).getName());
                if(count == 0) {
                    setTitle(getResources().getString(R.string.music_library));
                    setVisiblePager(true);
                }
            }
        }*/ if(fragmentContainer.getVisibility() == View.VISIBLE){
            Log.d("fragmentManager : ", "VISIBLE");
            setTitle(getResources().getString(R.string.music_library));
            setVisiblePager(true);
            removeFragment();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        searchMenuItem = menu.findItem(R.id.action_search);

        searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        searchView.setLayoutParams(params);
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

        mSearchResult = new SearchViewFragment();
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                fragmentSwitcher(mSearchResult,  -1, getResources().getString(R.string.search_hint), fade_in, fade_out);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                setVisiblePager(true);
                getSupportFragmentManager().popBackStack();
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
        }

        return super.onOptionsItemSelected(item);
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
        int id = item.getItemId();
        Bundle arguments = new Bundle();
        if(null != mSectionsPagerAdapter) {
            if (id == R.id.music_library) {
                fragmentSwitcher(null,  0, getResources().getString(R.string.music_library), fade_in, fade_out);
            } else if (id == R.id.boom_palylist) {
                mFragment = new BoomPlaylistFragment();
                fragmentSwitcher(mFragment,  1, getResources().getString(R.string.boom_playlist), fade_in, fade_out);
            } else if (id == R.id.favourite_list) {
                arguments.putInt(ItemSongListFragment.ARG_ITEM_TYPE, ItemType.FAVOURITE.ordinal());
                arguments.putInt(ItemSongListFragment.ARG_MEDIA_TYPE, MediaType.DEVICE_MEDIA_LIB.ordinal());
                mFragment = new ItemSongListFragment();
                mFragment.setArguments(arguments);
                fragmentSwitcher(mFragment,  2, getResources().getString(R.string.favourite_list), fade_in, fade_out);
            }else if (id == R.id.google_drive) {
                arguments.putInt(ItemSongListFragment.ARG_ITEM_TYPE, ItemType.SONGS.ordinal());
                arguments.putInt(ItemSongListFragment.ARG_MEDIA_TYPE, MediaType.GOOGLE_DRIVE.ordinal());
                mFragment = new ItemSongListFragment();
                mFragment.setArguments(arguments);
                fragmentSwitcher(mFragment,  3, getResources().getString(R.string.google_drive), fade_in, fade_out);
            } else if (id == R.id.drop_box) {
                arguments.putInt(ItemSongListFragment.ARG_ITEM_TYPE, ItemType.SONGS.ordinal());
                arguments.putInt(ItemSongListFragment.ARG_MEDIA_TYPE, MediaType.DROP_BOX.ordinal());
                mFragment = new ItemSongListFragment();
                mFragment.setArguments(arguments);
                fragmentSwitcher(mFragment,  4, getResources().getString(R.string.drop_box), fade_in, fade_out);
            } else if (id == R.id.nav_share) {

            } else if (id == R.id.nav_send) {

            }

            if(id == R.id.boom_palylist){
                mFloatAddPlayList.setVisibility(View.VISIBLE);
            }else{
                mFloatAddPlayList.setVisibility(View.GONE);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void removeFragment() {
        fragmentContainer.removeAllViews();
    }

    public void fragmentSwitcher(Fragment fragment, int itemId,
                                 String fname, @AnimRes int animationEnter,
                                 @AnimRes int animationExit) {
        if (currentItem == itemId) {
            // Don't allow re-selection of the currently active item
            return;
        }
        currentItem = itemId;

        if(currentItem < 0){
            setVisiblePager(false);
        }else if(currentItem == 0){
            setVisiblePager(true);
        }else {
            setTitle(String.valueOf(fname));
            setVisiblePager(false);
        }

        if(currentItem != 0) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(animationEnter, animationExit)
                    .replace(R.id.fragment_container, fragment)
                   /* .addToBackStack(String.valueOf(fname))*/
                    .commitAllowingStateLoss();
        }else {
            removeFragment();
        }
    }

    public void setTitle(String title){
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);
    }

    public void setVisiblePager(boolean visible){
        if(visible){
            currentItem = 0;
            findViewById(R.id.library_tab_panel).setVisibility(View.VISIBLE);
            fragmentContainer.setVisibility(View.GONE);
        }else{
            findViewById(R.id.library_tab_panel).setVisibility(View.GONE);
            fragmentContainer.setVisibility(View.VISIBLE);
        }
    }

    public void setStatusBarColor(Activity activity, int color) {
        activity.getWindow().setStatusBarColor(color);
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
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        updateUpNextDB();
        super.onDestroy();
    }

    private void updateUpNextDB() {
        sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_DESTROY_PLAYER_SCREEN));
        if(!App.getPlayerEventHandler().isPlaying()) {
            App.getService().stopSelf();
        }
    }

    @Override
    public void onLoadFBNativeAdds(LinearLayout fbNativeAddContainer) {
        mAddsContainer.addView(fbNativeAddContainer);
    }

    @Override
    public void onLoadGoogleNativeAdds(NativeExpressAdView googleAddView) {
        mAddsContainer.addView(googleAddView);
    }
}
