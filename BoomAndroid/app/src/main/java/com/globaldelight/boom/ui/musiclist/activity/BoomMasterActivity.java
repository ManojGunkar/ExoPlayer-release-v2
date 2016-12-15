package com.globaldelight.boom.ui.musiclist.activity;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.task.PlayerService;
import com.globaldelight.boom.ui.musiclist.adapter.SearchSuggestionAdapter;
import com.globaldelight.boom.ui.musiclist.fragment.SearchViewFragment;
import com.globaldelight.boom.utils.handlers.MusicSearchHelper;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Rahul Agarwal on 31-08-2016.
 */
public class BoomMasterActivity extends AppCompatActivity/* implements NavigationView.OnNavigationItemSelectedListener*/ {
    DrawerLayout drawerLayout;
    private LinearLayout mLibPanel, mListPanel, mFavPanel, mClosePanel;
    FrameLayout activityContainer;
    Fragment mSearchResult;
    Toolbar toolbar;
    ImageView toolImage;
    TextView toolTxt, toolDoneBtn;
    SearchView searchView;
    MenuItem searchItem;
    MusicSearchHelper musicSearchHelper;
    private SearchSuggestionAdapter searchSuggestionAdapter;
    public static String[] columns = new String[]{"_id", "FEED_TITLE"};
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_master, null);
        activityContainer = (FrameLayout) drawerLayout.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        super.setContentView(drawerLayout);

        mSearchResult = getSupportFragmentManager().findFragmentById(R.id.search_content);

        mLibPanel = (LinearLayout) drawerLayout.findViewById(R.id.drawer_lib_option);
        mListPanel = (LinearLayout) drawerLayout.findViewById(R.id.drawer_playlist_option);
        mFavPanel = (LinearLayout) drawerLayout.findViewById(R.id.drawer_fav_option);
        mClosePanel = (LinearLayout) drawerLayout.findViewById(R.id.drawer_close_option);

        mLibPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BoomMasterActivity.this, DeviceMusicActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });
        mListPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BoomMasterActivity.this, BoomPlaylistActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });
        mFavPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BoomMasterActivity.this, FavouriteListActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });
        mClosePanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_LIBRARY_CLOSE_BUTTON_TAPPED);
                finish();
                overridePendingTransition(R.anim.stay_out, R.anim.slide_out_left);
            }
        });

        setupToolbar();
        setupDrawer();
    }

    private void setupDrawer() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);

        if (App.getUserPreferenceHandler().isLibFromHome()) {
            navigationView.setVisibility(View.VISIBLE);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                    toolbar, 0, 0) {

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    invalidateOptionsMenu();
                    syncState();
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    invalidateOptionsMenu();
                    syncState();
                }
            };
            drawerLayout.addDrawerListener(drawerToggle);
            drawerToggle.syncState();
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            navigationView.setVisibility(View.GONE);
        }
        assert navigationView != null;
//        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolImage = (ImageView) findViewById(R.id.toolImg);
        toolTxt = (TextView) findViewById(R.id.toolTitle);
        setToolbarImage(R.drawable.ic_album_white_24dp);
        setToolbarTitle(getResources().getString(R.string.title_library));
        try {
            setSupportActionBar(toolbar);
        } catch (IllegalStateException e) {
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setTitle("Music Library"/*getIntent().getStringExtra("name")*/);
        }

        toolDoneBtn = (TextView) findViewById(R.id.toolDoneBtn);

        if (App.getUserPreferenceHandler().isLibFromHome()) {
            toolDoneBtn.setVisibility(View.GONE);
        } else {
            toolDoneBtn.setVisibility(View.VISIBLE);
        }

        toolDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaController.getInstance(BoomMasterActivity.this).addSongToBoomPlayList(App.getUserPreferenceHandler().getBoomPlayListId(), App.getUserPreferenceHandler().getItemList(), false);
                App.getUserPreferenceHandler().setLibraryStartFromHome(true);
                App.getUserPreferenceHandler().clearItemList();
                sendBroadcast(new Intent(PlayerService.ACTION_UPDATE_BOOM_PLAYLIST_LIST));
                finish();
            }
        });

        musicSearchHelper = new MusicSearchHelper(BoomMasterActivity.this);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setToolbarImage(int img) {
        toolImage.setImageDrawable(getResources().getDrawable(img, null));
    }

    public void setToolbarTitle(String title) {
        toolTxt.setText(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (App.getUserPreferenceHandler().isLibFromHome()) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.search_menu, menu);

            searchItem = menu.findItem(R.id.action_search);
            searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint(getResources().getString(R.string.search_hint));

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
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
            searchSuggestionAdapter = new SearchSuggestionAdapter(BoomMasterActivity.this, R.layout.card_search_item, null, columns,null, -1000);
            searchView.setSuggestionsAdapter(searchSuggestionAdapter);
            registerSearchListeners();

            MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    activityContainer.setVisibility(View.GONE);
                    ft.show(mSearchResult);
                    ((SearchViewFragment) mSearchResult).showEmpty(true);
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    activityContainer.setVisibility(View.VISIBLE);
                    ft.hide(mSearchResult);
                    searchSuggestionAdapter.changeCursor(null);
                    return true;
                }
            });
        }
        return true;
    }

    public void setIconified(boolean enable) {
        searchView.setIconified(enable);
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
                if(null == query || query.length() <2){
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

    private void fetchAndUpdateSearchResult(String query) {
        ((SearchViewFragment) mSearchResult).updateSearchResult(query);
        searchView.clearFocus();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("BoomMaster Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        Intent intent;
//        switch (item.getItemId()) {
//            case R.id.navigation_music:
//
//                break;
//            case R.id.navigation_spotify:
//
//                drawerLayout.closeDrawer(Gravity.LEFT);
//                break;
//            case R.id.navigation_boom_palylist:
//                intent = new Intent(BoomMasterActivity.this, BoomPlaylistActivity.class);
//                startActivity(intent);
//                drawerLayout.closeDrawer(Gravity.LEFT);
//                break;
//            case R.id.navigation_boom_favourites:
//                intent = new Intent(BoomMasterActivity.this, FavouriteListActivity.class);
//                startActivity(intent);
//                drawerLayout.closeDrawer(Gravity.LEFT);
//                break;
//            case R.id.navigation_close:
//                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_LIBRARY_CLOSE_BUTTON_TAPPED);
//                finish();
//                overridePendingTransition(R.anim.stay_out, R.anim.slide_out_left);
//                break;
//        }
//        return true;
//    }

}
