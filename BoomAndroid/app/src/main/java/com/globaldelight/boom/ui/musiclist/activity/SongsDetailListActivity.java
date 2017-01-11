package com.globaldelight.boom.ui.musiclist.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.task.PlayerService;
import com.globaldelight.boom.ui.musiclist.ListDetail;
import com.globaldelight.boom.ui.musiclist.adapter.ItemSongListAdapter;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.Logger;
import com.globaldelight.boom.utils.OnStartDragListener;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.async.Action;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import static com.globaldelight.boom.data.MediaLibrary.ItemType.BOOM_PLAYLIST;
import static com.globaldelight.boom.data.MediaLibrary.ItemType.PLAYLIST;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_ITEM_CLICKED;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_LAST_PLAYED_SONG;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_RECEIVE_SONG;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_TRACK_STOPPED;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_REPEAT;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_SHUFFLE;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_TRACK_SEEK;

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
    private FloatingActionButton mPlaySongDetailList;
    private LinearLayout mMiniPlayer, mStartPlayer;
    private ProgressBar mTrackProgress;
    private RegularTextView mTitle, mSubTitle;
    private ImageView mPlayerArt, mPlayPause;
    private static boolean isMoved = false;
    private Size size;

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

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerService.ACTION_UPDATE_BOOM_PLAYLIST_LIST);
        intentFilter.addAction(ACTION_RECEIVE_SONG);
        intentFilter.addAction(ACTION_LAST_PLAYED_SONG);
        intentFilter.addAction(ACTION_ITEM_CLICKED);
        intentFilter.addAction(ACTION_TRACK_STOPPED);
        intentFilter.addAction(ACTION_UPDATE_TRACK_SEEK);
        intentFilter.addAction(ACTION_UPDATE_SHUFFLE);
        intentFilter.addAction(ACTION_UPDATE_REPEAT);
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        registerReceiver(mPlayerEventBroadcastReceiver, intentFilter);

        initView();

        initMiniPlayer();
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
        mPlaySongDetailList = (FloatingActionButton) findViewById(R.id.play_song_detail_list);

        int width = Utils.getWindowWidth(this);
        int panelSize = (int) getResources().getDimension(R.dimen.album_title_height);
        int height = Utils.getWindowHeight(this) - panelSize * 4;
        size = new Size(width, width);
        setAlbumArt();

        if (collapsingToolbarLayout != null)
            collapsingToolbarLayout.setTitle(" ");

        setDetail(collection);

        toolbar = (Toolbar) findViewById(R.id.toolbar_song_detail_list);
        try {
            setSupportActionBar(toolbar);
        } catch (IllegalStateException e) {
            Log.d(0+"", "");
        }
        if (this.getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPlaySongDetailList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                        if (collection.getMediaElement().size() > 0)
                            App.getPlayingQueueHandler().getUpNextList().addToPlay((ArrayList<IMediaItem>) collection.getMediaElement(), 0, false, true);
                    } else {
                        App.getPlayingQueueHandler().getUpNextList().addToPlay((ArrayList<IMediaItem>) ((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement(), 0, false, true);
                    }
                }catch (Exception e){}
                if(null != itemSongListAdapter)
                    itemSongListAdapter.notifyDataSetChanged();
            }
        });

        new LoadSongsDetailList().execute();
        setForAnimation();
    }

    private void setDetail(IMediaItemCollection collection) {
        StringBuilder itemCount = new StringBuilder();
        String title;
        int count = collection.getItemCount();
        if(collection.getItemType() == BOOM_PLAYLIST || collection.getItemType() == PLAYLIST){
            title = collection.getItemTitle();
            count = collection.getItemCount();

        }else{
            title = ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getItemTitle();
            count = ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getItemCount();
        }
        itemCount.append(count > 1 ? getResources().getString(R.string.songs): getResources().getString(R.string.song));
        itemCount.append(" ").append(count);
        listDetail = new ListDetail(title, itemCount.toString(), null);




    }

    @Override
    protected void onResume() {
        super.onResume();
        if(null != itemSongListAdapter){
            itemSongListAdapter.notifyDataSetChanged();
        }

        if (null != App.getPlayerEventHandler().getPlayingItem()) {
            updateMiniPlayer(App.getPlayingQueueHandler().getUpNextList().getPlayingItem() != null ?
                            (MediaItem) App.getPlayingQueueHandler().getUpNextList().getPlayingItem() :
                            null, App.getPlayerEventHandler().isPlaying(),
                       /*if last played item is set as playing item*/ (!App.getPlayerEventHandler().isPlaying() && !App.getPlayerEventHandler().isPaused() ? true : false));
            mMiniPlayer.setVisibility(View.VISIBLE);
        } else {
            mMiniPlayer.setVisibility(View.GONE);
        }
    }

    private class LoadSongsDetailList extends AsyncTask<Void, Void, IMediaItemCollection> {

        @Override
        protected IMediaItemCollection doInBackground(Void... params) {
            //ItemType.PLAYLIST, ItemType.ARTIST && ItemType.GENRE
            if (collection.getItemType() == ItemType.BOOM_PLAYLIST /*&& collection.getMediaElement().isEmpty()*/) {
                collection.setMediaElement(MediaController.getInstance(SongsDetailListActivity.this).getMediaCollectionItemDetails(collection));
            } else
                //ItemType.PLAYLIST, ItemType.ARTIST && ItemType.GENRE
                if(collection.getItemType() == ItemType.PLAYLIST && collection.getMediaElement().isEmpty()) {
                    collection.setMediaElement(MediaController.getInstance(SongsDetailListActivity.this).getMediaCollectionItemDetails(collection));
                }else if((collection.getItemType() == ItemType.ARTIST || collection.getItemType() == ItemType.GENRE) &&
                        ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().isEmpty()){ //ItemType.ARTIST && ItemType.GENRE
                    ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).setMediaElement(MediaController.getInstance(SongsDetailListActivity.this).getMediaCollectionItemDetails(collection));
                }
            setDetail(collection);
            return collection;
        }

        @Override
        protected void onPostExecute(IMediaItemCollection iMediaItemCollection) {
            super.onPostExecute(iMediaItemCollection);
            LinearLayoutManager llm = new LinearLayoutManager(SongsDetailListActivity.this);

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
            itemSongListAdapter = new ItemSongListAdapter(SongsDetailListActivity.this, iMediaItemCollection, listDetail, SongsDetailListActivity.this);
            rv.setAdapter(itemSongListAdapter);
            if (iMediaItemCollection.getItemType() == ItemType.BOOM_PLAYLIST) {
                setUpItemTouchHelper();
            }
        }
    }

    private void setAlbumArt() {
        ArrayList<String> artUrlList;
        if(collection.getItemType() == ItemType.PLAYLIST || collection.getItemType() == ItemType.BOOM_PLAYLIST){
            artUrlList = collection.getArtUrlList();
        }else{
            artUrlList = ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getArtUrlList();
        }

        if(artUrlList.size()==0 || !PlayerUtils.isPathValid(artUrlList.get(0))){
            albumArt.setVisibility(View.VISIBLE);
            setDefaultImage();
        }else{
            tblAlbumArt.setVisibility(View.VISIBLE);
            setSongsArtImage(artUrlList);
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

    private void setSongsArtImage(final ArrayList<String> Urls) {

        int count = Urls.size() > 4 ? 4 : Urls.size();
        TableRow.LayoutParams param = new TableRow.LayoutParams(size.width / 2, size.height / 2);
        artImg1.setLayoutParams(param);
        artImg2.setLayoutParams(param);
        artImg3.setLayoutParams(param);
        artImg4.setLayoutParams(param);

        switch (count){
            case 1:
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg1);
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg2);
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg3);
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg4);
                break;
            case 2:
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg1);
                Picasso.with(this).load(new File(Urls.get(1))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg2);
                Picasso.with(this).load(new File(Urls.get(1))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg3);
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg4);
                break;
            case 3:
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg1);
                Picasso.with(this).load(new File(Urls.get(1))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg2);
                Picasso.with(this).load(new File(Urls.get(2))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg3);
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg4);
                break;
            default:
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg1);
                Picasso.with(this).load(new File(Urls.get(1))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg2);
                Picasso.with(this).load(new File(Urls.get(2))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg3);
                Picasso.with(this).load(new File(Urls.get(3))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg4);
                break;
        }
    }

    private void setDefaultImage(){
        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(size.width, size.height);
        albumArt.setLayoutParams(param);
        albumArt.setImageDrawable(getResources().getDrawable(R.drawable.ic_default_album_grid));
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
                updateBoomPlayList();
                if(null != mPlaySongDetailList)
                    mPlaySongDetailList.setVisibility(View.GONE);
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
        updateBoomPlayList();
        if(null != mPlaySongDetailList)
            mPlaySongDetailList.setVisibility(View.GONE);
        super.onBackPressed();
    }

    private void updateBoomPlayList(){
        if(collection.getItemType() == BOOM_PLAYLIST && isMoved && collection.getMediaElement().size() > 0){
            MediaController.getInstance(SongsDetailListActivity.this).addSongToBoomPlayList(collection.getItemId(), collection.getMediaElement(), true);
            isMoved= false;
        }
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
                    itemSongListAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    itemSongListAdapter.notifyItemChanged(target.getAdapterPosition());
                    isMoved = true;
                }
                return true;
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

    private BroadcastReceiver mPlayerEventBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case PlayerService.ACTION_UPDATE_BOOM_PLAYLIST_LIST:
                    if (collection.getItemType() == ItemType.BOOM_PLAYLIST) {
                        int oldCount = collection.getMediaElement().size();
                        collection.getMediaElement().clear();
                        collection.setMediaElement(MediaController.getInstance(SongsDetailListActivity.this).getMediaCollectionItemDetails(collection));
                        collection.setItemCount(collection.getMediaElement().size());
                        setDetail(collection);
                        itemSongListAdapter.updateNewList(collection, listDetail, oldCount);
                        itemSongListAdapter.notifyDataSetChanged();
                        updateAlbumArt(collection);
                    }
                    break;
                case ACTION_RECEIVE_SONG :
                    MediaItem item = intent.getParcelableExtra("playing_song");
                    updateMiniPlayer(item, intent.getBooleanExtra("playing", false), false);
                    if(mMiniPlayer.getVisibility() != View.VISIBLE)
                        expand();
                    break;
                case ACTION_LAST_PLAYED_SONG:
                    item = intent.getParcelableExtra("playing_song");
                    updateMiniPlayer(item, false, intent.getBooleanExtra("last_played_song", true));
                    if(mMiniPlayer.getVisibility() != View.VISIBLE)
                        expand();
                    break;
                case ACTION_ITEM_CLICKED :
                    if(intent.getBooleanExtra("play_pause", false) == false){
                        mPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_mini_player, null));
                    }else{
                        mPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_mini_player, null));
                    }
                    break;
                case ACTION_TRACK_STOPPED :
                    mPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_mini_player, null));
                    break;
                case ACTION_UPDATE_TRACK_SEEK :
                    mTrackProgress.setProgress(intent.getIntExtra("percent", 0));
                    break;
                case ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY:
                    if(null != itemSongListAdapter)
                        itemSongListAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    public void updateAlbumArt(IMediaItemCollection collection) {
        if(collection.getMediaElement().size() > 0){
            albumArt.setVisibility(View.GONE);
            tblAlbumArt.setVisibility(View.VISIBLE);
            setSongsArtImage(MediaController.getInstance(SongsDetailListActivity.this).getArtUrlList((MediaItemCollection) collection));
        }else{
            albumArt.setVisibility(View.VISIBLE);
            tblAlbumArt.setVisibility(View.GONE);
            setDefaultImage();
        }
    }

    private void updateMiniPlayer(MediaItem item, boolean playing, boolean isLastPlayedSong) {
        if(item != null) {
            updateAlbumArt(item);
            mTitle.setText(item.getItemTitle());
            mSubTitle.setText(item.getItemArtist());
            if (playing) {
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_mini_player, null));
            } else {
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_mini_player, null));
            }
            if(isLastPlayedSong){
                mTrackProgress.setProgress(0);
            }
        }
    }

    private void updateAlbumArt(final MediaItem item){
        if (PlayerUtils.isPathValid(item.getItemArtUrl())) {
            new Action() {
                public static final String TAG = "DEVICE_MUSIC_ACTIVITY";
                private Bitmap img;

                @NonNull
                @Override
                public String id() {
                    return TAG;
                }

                @Nullable
                @Override
                protected Object run() throws InterruptedException {
                    if (PlayerUtils.isPathValid(item.getItemArtUrl())) {
                        return null;
                    } else {
                        return img = BitmapFactory.decodeResource(getBaseContext().getResources(),
                                R.drawable.ic_default_small_grid_song);
                    }
                }

                @Override
                protected void done(@Nullable final Object result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (PlayerUtils.isPathValid(item.getItemArtUrl())) {
                                Logger.LOGD("ImageLoad", "Always call --");
                                try {
                                    Bitmap bitmap = BitmapFactory.decodeFile(item.getItemArtUrl());
                                    bitmap = Bitmap.createScaledBitmap(bitmap, (int) getResources().getDimension(R.dimen.one_hundred_eighty_pt),
                                            (int) getResources().getDimension(R.dimen.one_hundred_eighty_pt), false);
                                    PlayerUtils.ImageViewAnimatedChange(SongsDetailListActivity.this, mPlayerArt, bitmap);
                                }catch (NullPointerException e){
                                    Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                                            R.drawable.ic_default_small_grid_song);
                                    PlayerUtils.ImageViewAnimatedChange(SongsDetailListActivity.this, mPlayerArt, albumArt);
                                }
                            }
                        }
                    });
                }
            }.execute();
        } else {
            if(item != null) {
                Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_default_small_grid_song);
                PlayerUtils.ImageViewAnimatedChange(SongsDetailListActivity.this, mPlayerArt, albumArt);
            }
        }
    }

    private void initMiniPlayer(){
        mMiniPlayer = (LinearLayout) findViewById(R.id.detail_song_mini_player);
        mStartPlayer = (LinearLayout) findViewById(R.id.mini_touch_panel);
        mTrackProgress = (ProgressBar) findViewById(R.id.mini_player_track_progress);
        mTitle = (RegularTextView) findViewById(R.id.mini_player_title);
        mSubTitle = (RegularTextView) findViewById(R.id.mini_player_sub_title);
        mPlayerArt = (ImageView) findViewById(R.id.mini_player_album_art);
        mPlayPause = (ImageView) findViewById(R.id.mini_player_play_pause);

        mStartPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SongsDetailListActivity.this, BoomPlayerActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });

        mPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(PlayerService.ACTION_PLAY_PAUSE_SONG));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mPlayerEventBroadcastReceiver);
    }

    private void expand() {
        mMiniPlayer.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        mMiniPlayer.measure(widthSpec, heightSpec);
        int height = mMiniPlayer.getMeasuredHeight();
        //Log.d("Height : ", ""+height);

        ValueAnimator mAnimator = slideAnimator(0, height);
        mAnimator.start();
    }

    private void collapse() {
        int finalHeight = mMiniPlayer.getHeight();

        ValueAnimator mAnimator = slideAnimator(finalHeight, 0);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //Height=0, but it set visibility to GONE
                mMiniPlayer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

        });
        mAnimator.start();
    }

    private ValueAnimator slideAnimator(int start, int end) {

        ValueAnimator animator = ValueAnimator.ofInt(start, end);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = mMiniPlayer.getLayoutParams();
                layoutParams.height = value;
                mMiniPlayer.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }
}
