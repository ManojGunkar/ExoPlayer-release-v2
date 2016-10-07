package com.player.ui.musiclist.activity;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.player.data.MediaCollection.IMediaItemCollection;
import com.player.data.DeviceMediaCollection.MediaItemCollection;
import com.player.data.MediaLibrary.MediaController;
import com.player.myspotifymusic.R;
import com.player.ui.musiclist.ListDetail;
import com.player.ui.musiclist.adapter.DetailAlbumGridAdapter;
import com.player.ui.widgets.HeaderView;
import com.player.ui.widgets.MarginDecoration;
import com.player.utils.PermissionChecker;
import com.player.utils.Utils;
import com.player.utils.decorations.AlbumListSpacesItemDecoration;
import com.player.utils.decorations.SimpleDividerItemDecoration;
import com.squareup.picasso.Picasso;

import java.io.File;

public class DetailAlbumActivity extends AppCompatActivity {
    Toolbar toolbar;
    private RecyclerView recyclerView;
    private ImageView albumArt;
    private PermissionChecker permissionChecker;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appbarlayout;
    private IMediaItemCollection collection;
    private ListDetail listDetail;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_album);

        collection = (MediaItemCollection) getIntent().getParcelableExtra("mediaItemCollection");
        initView();
    }

    private void initView() {

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingtoolbarlayout_artist_album);
        appbarlayout = (AppBarLayout) findViewById(R.id.appbarlayout_artist_album);
        permissionChecker = new PermissionChecker(this, this, findViewById(R.id.base_view_artist_album));
        recyclerView = (RecyclerView) findViewById(R.id.rv_artist_album_activity);
        albumArt = (ImageView) findViewById(R.id.activity_artist_album_art);

        int width = Utils.getWindowWidth(this);
        int panelSize = (int) getResources().getDimension(R.dimen.album_title_height);
        int height = Utils.getWindowHeight(this) - panelSize * 4;
        setAlbumArtSize(width, height);
        setAlbumArt(width, height);

        if (collapsingToolbarLayout != null)
            collapsingToolbarLayout.setTitle(" ");


        StringBuilder albumCount = new StringBuilder();
        albumCount.append(collection.getItemListCount() > 1 ? getResources().getString(R.string.albums) : getResources().getString(R.string.album));
        albumCount.append(" ");
        albumCount.append(collection.getItemListCount());

        StringBuilder songCount = new StringBuilder();
        songCount.append(collection.getItemCount()>1 ? getResources().getString(R.string.songs) : getResources().getString(R.string.song));
        songCount.append(" ");
        songCount.append(collection.getItemCount());

        listDetail = new ListDetail(collection.getItemTitle(), albumCount.toString(), songCount.toString());

        toolbar = (Toolbar) findViewById(R.id.toolbar_artist_album);
        try {
            setSupportActionBar(toolbar);
        } catch (IllegalStateException e) {
        }
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addSongList();
    }

    private void setAlbumArtSize(int width, int height) {
        LinearLayout.LayoutParams lp = new LinearLayout
                .LayoutParams(width, height);
        albumArt.setLayoutParams(lp);
    }

    private void addSongList() {
        new Thread(new Runnable() {
            public void run() {

//                ItemType.ARTIST && ItemType.GENRE
                if(collection.getMediaElement().isEmpty())
                    collection.setMediaElement(MediaController.getInstance(DetailAlbumActivity.this).getMediaCollectionItemDetails(collection));

                final GridLayoutManager manager = new GridLayoutManager(DetailAlbumActivity.this, 2);
                recyclerView.setLayoutManager(manager);
                recyclerView.addItemDecoration(new MarginDecoration(DetailAlbumActivity.this));
                recyclerView.setHasFixedSize(true);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final DetailAlbumGridAdapter detailAlbumGridAdapter = new DetailAlbumGridAdapter(DetailAlbumActivity.this, recyclerView, collection, listDetail, permissionChecker);
                        recyclerView.setAdapter(detailAlbumGridAdapter);
                        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                    @Override
                                    public int getSpanSize(int position) {
                                        return detailAlbumGridAdapter.isHeader(position) ? manager.getSpanCount() : 1;
                                    }
                                });
                            }
                        });
                        if (collection.getMediaElement().size() < 1) {
                                runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listIsEmpty();
                                }
                            });
                        }
            }
        }).start();
    }

    public void listIsEmpty() {
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean fileExist(String albumArtPath) {
        File imgFile = new File(albumArtPath);
        return imgFile.exists();
    }

    public boolean isPathValid(String path) {
        return path != null && fileExist(path);
    }

    public void setAlbumArt(int width, int height) {
            try {
                if (isPathValid(collection.getItemArtUrl())) {
                    Picasso.with(DetailAlbumActivity.this).load(new File(collection.getItemArtUrl())).resize(width, height)
                            .error(getResources().getDrawable(R.drawable.default_album_art)).noFade().into(albumArt);
                    return;
                }else {
                    Utils utils = new Utils(this);
                    albumArt.setImageBitmap(utils.getBitmapOfVector(this, R.drawable.default_album_art,
                            width, height));
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                Utils utils = new Utils(this);
                albumArt.setImageBitmap(utils.getBitmapOfVector(this, R.drawable.default_album_art,
                        width, height));
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

//        ImageView v = (ImageView)searchView.findViewById(android.support.v7.appcompat.R.id.search_voice_btn);
//        searchView.setBackground(getResources().getDrawable(R.drawable.card_background));
//        getMenuInflater().inflate(R.menu.main_menu, menu);
//
//        MenuItem menuItem = menu.findItem(R.id.action_search);
//
//        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
//        setupSearchView(searchView);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d("Query : ", query);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}