package com.globaldelight.boom.ui.musiclist.activity;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.ui.musiclist.ListDetail;
import com.globaldelight.boom.ui.musiclist.adapter.AlbumItemsListAdapter;
import com.globaldelight.boom.ui.musiclist.adapter.FavouriteListAdapter;
import com.globaldelight.boom.utils.Logger;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

public class AlbumActivity extends AppCompatActivity {
    Toolbar toolbar;
    IMediaItemCollection collection, currentItem;
    private RecyclerView rv;
    private ImageView albumArt;
    private PermissionChecker permissionChecker;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appbarlayout;
    private ListDetail listDetail;
    FloatingActionButton mPlayAlbum;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        collection = (MediaItemCollection) getIntent().getParcelableExtra("mediaItemCollection");

        if(collection.getItemType() == ItemType.ALBUM){
            currentItem = collection;
        }else {
            currentItem = (IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex());
        }
        initView();
    }

    private void initView() {

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingtoolbarlayout_album);
        appbarlayout = (AppBarLayout) findViewById(R.id.appbarlayout_album);
        permissionChecker = new PermissionChecker(this, this, findViewById(R.id.base_view_album));
        rv = (RecyclerView) findViewById(R.id.rv_album_activity);
        albumArt = (ImageView) findViewById(R.id.activity_album_art);
        mPlayAlbum = (FloatingActionButton)findViewById(R.id.play_album);

        int width = Utils.getWindowWidth(this);
        int panelSize = (int) getResources().getDimension(R.dimen.album_title_height);
        int height = Utils.getWindowHeight(this) - panelSize * 4;
        setAlbumArtSize(width, width);
        setAlbumArt(width, width);

        if (collapsingToolbarLayout != null)
            collapsingToolbarLayout.setTitle(" ");

        StringBuilder itemCount = new StringBuilder();
        itemCount.append(currentItem.getItemCount() > 1 ? getResources().getString(R.string.songs): getResources().getString(R.string.song));
        itemCount.append(" ").append(currentItem.getItemCount());

        listDetail = new ListDetail(currentItem.getItemTitle(), currentItem.getItemSubTitle(), itemCount.toString());

        toolbar = (Toolbar) findViewById(R.id.toolbar_album);

        try {
            setSupportActionBar(toolbar);
        } catch (IllegalStateException e) {
        }
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPlayAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (App.getPlayingQueueHandler().getUpNextList() != null) {
                    if (collection.getItemType() == ItemType.ALBUM) {
                        App.getPlayingQueueHandler().getUpNextList().addToPlay(collection, 0);
                    } else {
                        App.getPlayingQueueHandler().getUpNextList().addToPlay((ArrayList<MediaItem>) ((MediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement(), 0);
                    }
                }
            }
        });

        addSongList();
        setForAnimation();
    }

    private void setAlbumArtSize(int width, int height) {
        LinearLayout.LayoutParams lp = new LinearLayout
                .LayoutParams(width, height);
        albumArt.setLayoutParams(lp);
    }

    private void setForAnimation() {
        rv.scrollTo(0, 100);
    }

    private void addSongList() {
        new Thread(new Runnable() {
            public void run() {
                //ItemType.ALBUM, ItemType.ARTIST && ItemType.GENRE
                if(collection.getItemType() == ItemType.ALBUM && collection.getMediaElement().isEmpty()) {
                    collection.setMediaElement(MediaController.getInstance(AlbumActivity.this).getMediaCollectionItemDetails(collection));
                }else if((collection.getItemType() == ItemType.ARTIST || collection.getItemType() == ItemType.GENRE) &&
                        ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().isEmpty()){ //ItemType.ARTIST && ItemType.GENRE
                    ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).setMediaElement(MediaController.getInstance(AlbumActivity.this).getMediaCollectionItemDetails(collection));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rv.setLayoutManager(new LinearLayoutManager(AlbumActivity.this));
                        rv.setAdapter(new AlbumItemsListAdapter(AlbumActivity.this, collection, listDetail, permissionChecker));
                    }
                });
//                if (favList.size() < 1) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            listIsEmpty();
//                        }
//                    });
//                }
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void setAlbumArt(int width, int height) {
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums.ALBUM + "=?",
                new String[]{currentItem.getItemTitle()/*String.valueOf(itemId)*/},
                null);
        if (cursor != null && cursor.moveToFirst()) {
            String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            try {
                if (imagePath == null) {
                    Utils utils = new Utils(this);
                    albumArt.setImageBitmap(utils.getBitmapOfVector(this, R.drawable.default_art_header,
                            width, height));
                    return;
                }
                Picasso.with(AlbumActivity.this)
                        .load(new File(imagePath)).resize(width, height)
                        .error(getResources().getDrawable(R.drawable.default_art_header, null)).noFade()
                        .into(albumArt);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        cursor.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        /*MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));*/
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
            Logger.LOGD("Query : ", query);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}