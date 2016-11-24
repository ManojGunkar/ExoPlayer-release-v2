package com.globaldelight.boom.ui.musiclist.activity;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.ui.musiclist.ListDetail;
import com.globaldelight.boom.ui.musiclist.adapter.ItemSongListAdapter;
import com.globaldelight.boom.utils.OnStartDragListener;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Rahul Agarwal on 8/1/2016.
 */

public class SongsDetailListActivity extends AppCompatActivity implements OnStartDragListener {
    Toolbar toolbar;
    private RecyclerView rv;
    private ImageView albumArt, artImg1, artImg2, artImg3, artImg4;
    private PermissionChecker permissionChecker;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appbarlayout;
    private TableLayout tblAlbumArt;
    private ItemSongListAdapter itemSongListAdapter;
    private IMediaItemCollection collection;
    private ListDetail listDetail;
    private ItemTouchHelper mItemTouchHelper;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this,android.R.color.transparent));
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_detail_list);

        collection = (MediaItemCollection) getIntent().getParcelableExtra("mediaItemCollection");
        initView();
    }

    private void initView() {
        artImg1 = (ImageView)findViewById(R.id.song_detail_list_art_img1);
        artImg2 = (ImageView)findViewById(R.id.song_detail_list_art_img2);
        artImg3 = (ImageView)findViewById(R.id.song_detail_list_art_img3);
        artImg4 = (ImageView)findViewById(R.id.song_detail_list_art_img4);

        tblAlbumArt = (TableLayout)findViewById(R.id.song_detail_list_art_table);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingtoolbarlayout_song_detail_list);
        appbarlayout = (AppBarLayout) findViewById(R.id.appbarlayout_song_detail_list);
        permissionChecker = new PermissionChecker(this, this, findViewById(R.id.song_detail_list_base_view));
        rv = (RecyclerView) findViewById(R.id.rv_song_detail_list);
        albumArt = (ImageView) findViewById(R.id.song_detail_list_default_img);

        int width = Utils.getWindowWidth(this);
        int panelSize = (int) getResources().getDimension(R.dimen.album_title_height);
        int height = Utils.getWindowHeight(this) - panelSize * 4;
        Size size = new Size(width, width);
        setAlbumArt(size);

        if (collapsingToolbarLayout != null)
            collapsingToolbarLayout.setTitle(" ");

        StringBuilder itemCount = new StringBuilder();
        itemCount.append(collection.getItemCount() > 1 ? getResources().getString(R.string.songs): getResources().getString(R.string.song));
        itemCount.append(" ").append(collection.getItemCount());

        listDetail = new ListDetail(collection.getItemTitle(), itemCount.toString(), null);

        toolbar = (Toolbar) findViewById(R.id.toolbar_song_detail_list);
        try {
            setSupportActionBar(toolbar);
        } catch (IllegalStateException e) {
            Log.d(0+"", "");
        }
        if (this.getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setSongList();
        setForAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(itemSongListAdapter != null){
            getCollectionData();
            itemSongListAdapter.updateNewList(collection);
        }
    }

    public void getCollectionData(){
        //              ItemType.PLAYLIST, ItemType.ARTIST && ItemType.GENRE
        if (collection.getItemType() == ItemType.BOOM_PLAYLIST && collection.getMediaElement().isEmpty()) {
            collection.setMediaElement(MediaController.getInstance(SongsDetailListActivity.this).getMediaCollectionItemDetails(collection));
        } else
        //ItemType.PLAYLIST, ItemType.ARTIST && ItemType.GENRE
        if(collection.getItemType() == ItemType.PLAYLIST && collection.getMediaElement().isEmpty()) {
            collection.setMediaElement(MediaController.getInstance(SongsDetailListActivity.this).getMediaCollectionItemDetails(collection));
        }else if((collection.getItemType() == ItemType.ARTIST || collection.getItemType() == ItemType.GENRE) &&
                ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().isEmpty()){ //ItemType.ARTIST && ItemType.GENRE
            ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).setMediaElement(MediaController.getInstance(SongsDetailListActivity.this).getMediaCollectionItemDetails(collection));
        }
    }

    private void setSongList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getCollectionData();
                final LinearLayoutManager llm = new LinearLayoutManager(SongsDetailListActivity.this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rv.setLayoutManager(llm);
//                        rv.addItemDecoration(new SimpleDividerItemDecoration(SongsDetailListActivity.this, 0));
                        rv.setHasFixedSize(true);
                        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);
                                itemSongListAdapter.recyclerScrolled();
                            }

                            @Override
                            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);

                                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                                    // Do something
                                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                    // Do something
                                } else {
                                    // Do something
                                }
                            }
                        });
                        itemSongListAdapter = new ItemSongListAdapter(SongsDetailListActivity.this, collection, listDetail, permissionChecker, SongsDetailListActivity.this);
                        rv.setAdapter(itemSongListAdapter);
                        if (collection.getItemType() == ItemType.BOOM_PLAYLIST) {
                            setUpItemTouchHelper();
                        }

                    }
                });
            }
        }).start();
    }

    private void setAlbumArt(Size size) {
        ArrayList<String> artUrlList;
        if(collection.getItemType() == ItemType.PLAYLIST || collection.getItemType() == ItemType.BOOM_PLAYLIST){
            artUrlList = collection.getArtUrlList();
        }else{
            artUrlList = ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getArtUrlList();
        }

        if(artUrlList.size()==0){
            albumArt.setVisibility(View.VISIBLE);
            setDefaultImage(size);
        }else{
            tblAlbumArt.setVisibility(View.VISIBLE);
            setSongsArtImage(size, artUrlList);
        }

        int colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary);
        collapsingToolbarLayout.setBackgroundColor(colorPrimary);
        collapsingToolbarLayout.setContentScrimColor(colorPrimary);
        collapsingToolbarLayout.setStatusBarScrimColor(getAutoStatColor(colorPrimary));
        if (Build.VERSION.SDK_INT >= 21) {
            ActivityManager.TaskDescription taskDescription = new
                    ActivityManager.TaskDescription(collection.getItemTitle(),
                    BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher), colorPrimary);
            setTaskDescription(taskDescription);
        }
    }

    public int getAutoStatColor(int baseColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[2] *= 1.4f;
        return Color.HSVToColor(hsv);
    }

    private void setAlbumArtSize(int width, int height) {
    }

    private void setSongsArtImage(final Size size, final ArrayList<String> Urls) {

        int count = Urls.size() > 4 ? 4 : Urls.size();
        TableRow.LayoutParams param = new TableRow.LayoutParams(size.width / 2, size.height / 2);
        artImg1.setLayoutParams(param);
        artImg2.setLayoutParams(param);
        artImg3.setLayoutParams(param);
        artImg4.setLayoutParams(param);


        switch (count){
            case 1:
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg1);
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg2);
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg3);
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg4);
                break;
            case 2:
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg1);
                Picasso.with(this).load(new File(Urls.get(1))).error(getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg2);
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg3);
                Picasso.with(this).load(new File(Urls.get(1))).error(getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg4);
                break;
            case 3:
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg1);
                Picasso.with(this).load(new File(Urls.get(1))).error(getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg2);
                Picasso.with(this).load(new File(Urls.get(2))).error(getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg3);
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg4);
                break;
            default:
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg1);
                Picasso.with(this).load(new File(Urls.get(1))).error(getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg2);
                Picasso.with(this).load(new File(Urls.get(2))).error(getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg3);
                Picasso.with(this).load(new File(Urls.get(3))).error(getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg4);
                break;
        }
    }

    private void setDefaultImage(Size size){
        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(size.width, size.height);
        albumArt.setLayoutParams(param);
        albumArt.setImageDrawable(getResources().getDrawable(R.drawable.default_art_header));
    }

    private void setForAnimation() {
        rv.scrollTo(0, 100);
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
            Log.d("Query : ", query);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN
                , 0) {





            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {


                if (collection.getMediaElement().size() > 0 && target.getAdapterPosition() > 0) {
                    Collections.swap(collection.getMediaElement(), viewHolder.getAdapterPosition() - 1, target.getAdapterPosition() - 1);
                    itemSongListAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                }
                return false;
            }


            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {


            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }





        };
        mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(rv);
    }

    public class Size {
        int width;
        int height;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

}
