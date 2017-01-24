package com.globaldelight.boom.ui.musiclist.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.task.PlayerService;
import com.globaldelight.boom.ui.musiclist.ListDetail;
import com.globaldelight.boom.ui.musiclist.adapter.DetailAlbumGridAdapter;
import com.globaldelight.boom.ui.widgets.MarginDecoration;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.Logger;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.async.Action;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_ITEM_CLICKED;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_LAST_PLAYED_SONG;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_RECEIVE_SONG;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_TRACK_STOPPED;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_REPEAT;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_SHUFFLE;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_TRACK_SEEK;

public class DetailAlbumActivity extends AppCompatActivity {
    Toolbar toolbar;
    private RecyclerView recyclerView;
    private ImageView albumArt;
    private PermissionChecker permissionChecker;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appbarlayout;
    private IMediaItemCollection collection;
    private ListDetail listDetail;
    private FloatingActionButton mPlayArtistGenreBtn;
    private LinearLayout mMiniPlayer, mStartPlayer;
    private ProgressBar mTrackProgress;
    private RegularTextView mTitle, mSubTitle;
    private ImageView mPlayerArt, mPlayPause;
    private static boolean isExpended = false;
    private GridLayoutManager gridLayoutManager;

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

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_RECEIVE_SONG);
        intentFilter.addAction(ACTION_LAST_PLAYED_SONG);
        intentFilter.addAction(ACTION_ITEM_CLICKED);
        intentFilter.addAction(ACTION_TRACK_STOPPED);
        intentFilter.addAction(ACTION_UPDATE_TRACK_SEEK);
        intentFilter.addAction(ACTION_UPDATE_SHUFFLE);
        intentFilter.addAction(ACTION_UPDATE_REPEAT);
        registerReceiver(mPlayerEventBroadcastReceiver, intentFilter);

        initView();
        initMiniPlayer();
    }

    private void initView() {

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingtoolbarlayout_artist_album);
        appbarlayout = (AppBarLayout) findViewById(R.id.appbarlayout_artist_album);
        permissionChecker = new PermissionChecker(this, this, findViewById(R.id.base_view_artist_album));
        recyclerView = (RecyclerView) findViewById(R.id.rv_artist_album_activity);
        albumArt = (ImageView) findViewById(R.id.activity_artist_album_art);
        mPlayArtistGenreBtn = (FloatingActionButton)findViewById(R.id.play_artist_album);
        int width = Utils.getWindowWidth(this);
        int panelSize = (int) getResources().getDimension(R.dimen.album_title_height);
        int height = Utils.getWindowHeight(this) - panelSize * 4;
        setAlbumArtSize(width, width);
        setAlbumArt(width, width);

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

        mPlayArtistGenreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().isEmpty())
                        ((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).setMediaElement(MediaController.getInstance(DetailAlbumActivity.this).getMediaCollectionItemDetails(collection));
                    App.getPlayingQueueHandler().getUpNextList().addToPlay((ArrayList<IMediaItem>) MediaController.getInstance(DetailAlbumActivity.this).getMediaCollectionItemDetails(collection), 0, false, true);
                }catch (Exception e){}
            }
        });

        new LoadDetailAlbumList().execute();
    }

    private BroadcastReceiver mPlayerEventBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
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
            }
        }
    };

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
                    if (item.getItemArtUrl() != null && (new File(item.getItemArtUrl())).exists()) {
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
                            if (item.getItemArtUrl() != null && (new File(item.getItemArtUrl())).exists()) {
                                Logger.LOGD("ImageLoad", "Always call --");
                                try {
                                    Bitmap bitmap = BitmapFactory.decodeFile(item.getItemArtUrl());
                                    bitmap = Bitmap.createScaledBitmap(bitmap, (int) getResources().getDimension(R.dimen.one_hundred_eighty_pt),
                                            (int) getResources().getDimension(R.dimen.one_hundred_eighty_pt), false);
                                    PlayerUtils.ImageViewAnimatedChange(DetailAlbumActivity.this, mPlayerArt, bitmap);
                                }catch (NullPointerException e){
                                    Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                                            R.drawable.ic_default_small_grid_song);
                                    PlayerUtils.ImageViewAnimatedChange(DetailAlbumActivity.this, mPlayerArt, albumArt);
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
                PlayerUtils.ImageViewAnimatedChange(DetailAlbumActivity.this, mPlayerArt, albumArt);
            }
        }
    }

    private void initMiniPlayer(){
        mMiniPlayer = (LinearLayout) findViewById(R.id.detail_album_mini_player);
        mStartPlayer = (LinearLayout) findViewById(R.id.mini_touch_panel);
        mTrackProgress = (ProgressBar) findViewById(R.id.mini_player_track_progress);
        mTitle = (RegularTextView) findViewById(R.id.mini_player_title);
        mSubTitle = (RegularTextView) findViewById(R.id.mini_player_sub_title);
        mPlayerArt = (ImageView) findViewById(R.id.mini_player_album_art);
        mPlayPause = (ImageView) findViewById(R.id.mini_player_play_pause);

        mStartPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailAlbumActivity.this, BoomPlayerActivity.class);
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
    protected void onResume() {
        super.onResume();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != mPlayerEventBroadcastReceiver)
            unregisterReceiver(mPlayerEventBroadcastReceiver);
    }

    private void setAlbumArtSize(int width, int height) {
        LinearLayout.LayoutParams lp = new LinearLayout
                .LayoutParams(width, height);
        albumArt.setLayoutParams(lp);
    }

    private class LoadDetailAlbumList extends AsyncTask<Void, Integer, IMediaItemBase> {

        @Override
        protected IMediaItemBase doInBackground(Void... params) {
//            ItemType.ARTIST && ItemType.GENRE
            if(collection.getMediaElement().isEmpty())
                collection.setMediaElement(MediaController.getInstance(DetailAlbumActivity.this).getMediaCollectionItemDetails(collection));
            return collection;
        }

        @Override
        protected void onPostExecute(IMediaItemBase iMediaItemBase) {
            super.onPostExecute(iMediaItemBase);
            boolean isPhone = Utils.isPhone(DetailAlbumActivity.this);
            if(isPhone){
                gridLayoutManager =
                        new GridLayoutManager(DetailAlbumActivity.this, 2);
            }else{
                gridLayoutManager =
                        new GridLayoutManager(DetailAlbumActivity.this, 3);
            }
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.addItemDecoration(new MarginDecoration(DetailAlbumActivity.this));
            recyclerView.setHasFixedSize(true);
            final DetailAlbumGridAdapter detailAlbumGridAdapter = new DetailAlbumGridAdapter(DetailAlbumActivity.this, recyclerView, (IMediaItemCollection) iMediaItemBase, listDetail, isPhone);
            recyclerView.setAdapter(detailAlbumGridAdapter);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return detailAlbumGridAdapter.isHeader(position) ? gridLayoutManager.getSpanCount() : 1;
                }
            });
            if (((IMediaItemCollection) iMediaItemBase).getMediaElement().size() < 1) {
                listIsEmpty();
            }
        }
    }

    public void listIsEmpty() {
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void setAlbumArt(int width, int height) {
            try {
                if (PlayerUtils.isPathValid(collection.getItemArtUrl())) {
                    Picasso.with(DetailAlbumActivity.this).load(new File(collection.getItemArtUrl())).resize(width, height)
                            .error(getResources().getDrawable(R.drawable.ic_default_album_header)).noFade().into(albumArt);
                    return;
                }else {
                    albumArt.setImageDrawable(getResources().getDrawable(R.drawable.ic_default_album_header));
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                albumArt.setImageDrawable(getResources().getDrawable(R.drawable.ic_default_album_header));
            }
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
                if(null != mPlayArtistGenreBtn)
                    mPlayArtistGenreBtn.setVisibility(View.GONE);
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
        if(null != mPlayArtistGenreBtn)
            mPlayArtistGenreBtn.setVisibility(View.GONE);
        super.onBackPressed();
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
        isExpended = true;
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
        isExpended = false;
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