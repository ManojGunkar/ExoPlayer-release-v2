package com.player.ui.musiclist.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
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
import android.widget.TextView;

import com.player.myspotifymusic.R;

/**
 * Created by Rahul Agarwal on 31-08-2016.
 */
public class BoomMasterActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    DrawerLayout drawerLayout;
    FrameLayout activityContainer;
    Toolbar toolbar;
    ImageView toolImage;
    TextView toolTxt;
    SearchView searchView;
    MenuItem searchItem;
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_master, null);
        activityContainer = (FrameLayout) drawerLayout.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        super.setContentView(drawerLayout);

        setupToolbar();
        setupDrawer();
    }

    private void setupDrawer() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);

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

        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setupToolbar() {
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        toolImage = (ImageView)findViewById(R.id.toolImg);
        toolTxt = (TextView) findViewById(R.id.toolTitle);
        setToolbarImage(R.drawable.ic_album_white_24dp);
        setToolbarTitle("Music Library");
        toolTxt.setTextSize(18);
        try {
            setSupportActionBar(toolbar);
        }catch (IllegalStateException e){}
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setTitle("Music Library"/*getIntent().getStringExtra("name")*/);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setToolbarImage(int img){
        toolImage.setImageDrawable(getResources().getDrawable(img, null));
    }

    public void setToolbarTitle(String title){
        toolTxt.setText(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_button_menu, menu);

        searchItem = menu.findItem(R.id.action_search);

        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {


//                Intent i = new Intent(BoomMasterActivity.this, SearchResult.class);
//                startActivity(i);

                return false;
            }
        });
/* *
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        registerSearchListeners();

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {

                return true;
            }
        });
* */
        return true;
    }

    public void setIconified(boolean enable){
        searchView.setIconified(enable);
    }

    private void registerSearchListeners() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
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
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.navigation_music:
                intent = new Intent(BoomMasterActivity.this, DeviceMusicActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;
            case R.id.navigation_spotify:

                drawerLayout.closeDrawer(Gravity.LEFT);
                break;
            case R.id.navigation_boom_palylist:
//                intent = new Intent(BoomMasterActivity.this, BoomPlaylistActivity.class);
//                startActivity(intent);
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;
            case R.id.navigation_boom_favourites:
//                intent = new Intent(BoomMasterActivity.this, PlayingQueueActivity.class);
//                startActivity(intent);
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;
            case R.id.navigation_close:
                finish();
                break;
        }
        return true;
    }
}
