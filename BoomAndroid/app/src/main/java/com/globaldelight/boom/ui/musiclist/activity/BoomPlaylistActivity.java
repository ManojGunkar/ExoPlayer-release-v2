package com.globaldelight.boom.ui.musiclist.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.handler.search.Search;
import com.globaldelight.boom.task.PlayerService;
import com.globaldelight.boom.ui.musiclist.adapter.BoomPlayListAdapter;
import com.globaldelight.boom.ui.musiclist.adapter.SearchListAdapter;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.Logger;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.async.Action;
import com.globaldelight.boom.utils.decorations.AlbumListSpacesItemDecoration;
import com.globaldelight.boom.utils.decorations.BoomPlayListFooterItemDecoration;
import com.globaldelight.boom.utils.decorations.SearchListSpacesItemDecoration;
import com.globaldelight.boom.utils.decorations.SimpleDividerItemDecoration;

import java.io.File;
import java.util.ArrayList;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_ITEM_CLICKED;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_LAST_PLAYED_SONG;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_RECEIVE_SONG;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_TRACK_STOPPED;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_REPEAT;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_SHUFFLE;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_TRACK_SEEK;

/**
 * Created by Rahul Agarwal on 31-08-2016.
 */
public class BoomPlaylistActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RegularTextView mToolbarTitle;
    private FloatingActionButton addBoomPlaylist;
    private RecyclerView recyclerView;
    private BoomPlayListAdapter boomPlayListAdapter;
    private PermissionChecker permissionChecker;
    private LinearLayout emptyView;
    private LinearLayout mMiniPlayer, mStartPlayer;
    private ProgressBar mTrackProgress;
    private RegularTextView mTitle, mSubTitle;
    private ImageView mAlbumArt, mPlayPause;
    private GridLayoutManager gridLayoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_left, R.anim.stay_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boom_playlist);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerService.ACTION_UPDATE_BOOM_PLAYLIST);
        intentFilter.addAction(ACTION_RECEIVE_SONG);
        intentFilter.addAction(ACTION_LAST_PLAYED_SONG);
        intentFilter.addAction(ACTION_ITEM_CLICKED);
        intentFilter.addAction(ACTION_TRACK_STOPPED);
        intentFilter.addAction(ACTION_UPDATE_TRACK_SEEK);
        intentFilter.addAction(ACTION_UPDATE_SHUFFLE);
        intentFilter.addAction(ACTION_UPDATE_REPEAT);
        registerReceiver(mPlayerEventBroadcastReceiver, intentFilter);

        init();
        initMiniPlayer();
    }

    private void init() {
        toolbar = (Toolbar)findViewById(R.id.boom_playlist_list_toolbar);
        try {
            setSupportActionBar(toolbar);
        }catch (IllegalStateException e){}
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.playlistContainer);
        emptyView = (LinearLayout) findViewById(R.id.playlist_empty_view);
        addBoomPlaylist = (FloatingActionButton)findViewById(R.id.add_boom_playlist);

        addBoomPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPlaylistDialog();
            }
        });

        checkPermissions();
    }

    private void checkPermissions() {
        permissionChecker = new PermissionChecker(this, this, recyclerView);
        permissionChecker.check(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getResources().getString(R.string.storage_permission),
                new PermissionChecker.OnPermissionResponse() {
                    @Override
                    public void onAccepted() {
                        new LoadBoomPlaylist().execute();
                    }

                    @Override
                    public void onDecline() {
                        finish();
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

        if(boomPlayListAdapter != null){
            boomPlayListAdapter.updateNewList(MediaController.getInstance(BoomPlaylistActivity.this).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB));
        }
    }

    public void listIsEmpty() {
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    public void listNoMoreEmpty() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void newPlaylistDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.new_playlist)
                .backgroundColor(Color.parseColor("#171921"))
                .titleColor(Color.parseColor("#ffffff"))
                .positiveColor(Color.parseColor("#81cbc4"))
                .negativeColor(Color.parseColor("#81cbc4"))
                .widgetColor(Color.parseColor("#ffffff"))
                .contentColor(Color.parseColor("#ffffff"))
                .typeface("TitilliumWeb-SemiBold.ttf", "TitilliumWeb-Regular.ttf")
                .input(getResources().getString(R.string.new_playlist), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (!input.toString().matches("")) {
                            MediaController.getInstance(BoomPlaylistActivity.this).createBoomPlaylist(input.toString());
                            listNoMoreEmpty();
                            boomPlayListAdapter.updateNewList(MediaController.getInstance(BoomPlaylistActivity.this).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB));
//                            recyclerView.scrollToPosition(boomPlayListAdapter.getItemCount()-1);
                            Toast.makeText(BoomPlaylistActivity.this, getResources().getString(R.string.playlist_created), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != mPlayerEventBroadcastReceiver)
            unregisterReceiver(mPlayerEventBroadcastReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.stay_out, R.anim.slide_out_left);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.stay_out, R.anim.slide_out_left);
    }

    private BroadcastReceiver mPlayerEventBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MediaItem item;
            switch (intent.getAction()){
                case PlayerService.ACTION_UPDATE_BOOM_PLAYLIST:
                    if (boomPlayListAdapter != null) {
                        boomPlayListAdapter.updateNewList((ArrayList<? extends MediaItemCollection>) MediaController.getInstance(context).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB));
                    }
                    break;
                case ACTION_LAST_PLAYED_SONG:
                    item = intent.getParcelableExtra("playing_song");
                    updateMiniPlayer(item, false, intent.getBooleanExtra("last_played_song", true));
                    if(mMiniPlayer.getVisibility() != View.VISIBLE)
                        expand();
                    break;
                case ACTION_RECEIVE_SONG :
                    item = intent.getParcelableExtra("playing_song");
                    updateMiniPlayer(item, intent.getBooleanExtra("playing", false), false);
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
                                    PlayerUtils.ImageViewAnimatedChange(BoomPlaylistActivity.this, mAlbumArt, bitmap);
                                }catch (NullPointerException e){
                                    Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                                            R.drawable.ic_default_small_grid_song);
                                    PlayerUtils.ImageViewAnimatedChange(BoomPlaylistActivity.this, mAlbumArt, albumArt);
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
                PlayerUtils.ImageViewAnimatedChange(BoomPlaylistActivity.this, mAlbumArt, albumArt);
            }
        }
    }

    private void initMiniPlayer(){
        mMiniPlayer = (LinearLayout) findViewById(R.id.boom_playlist_mini_player);
        mStartPlayer = (LinearLayout) findViewById(R.id.mini_touch_panel);
        mTrackProgress = (ProgressBar) findViewById(R.id.mini_player_track_progress);
        mTitle = (RegularTextView) findViewById(R.id.mini_player_title);
        mSubTitle = (RegularTextView) findViewById(R.id.mini_player_sub_title);
        mAlbumArt = (ImageView) findViewById(R.id.mini_player_album_art);
        mPlayPause = (ImageView) findViewById(R.id.mini_player_play_pause);

        mStartPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BoomPlaylistActivity.this, BoomPlayerActivity.class);
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

    public class LoadBoomPlaylist  extends AsyncTask<Void, Integer, ArrayList<? extends IMediaItemBase>> {
        @Override
        protected ArrayList<? extends IMediaItemBase> doInBackground(Void... params) {
            return MediaController.getInstance(BoomPlaylistActivity.this).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB)/*MediaQuery.getPlayList(context)*/;
        }

        @Override
        protected void onPostExecute(ArrayList<? extends IMediaItemBase> iMediaItemList) {
            super.onPostExecute(iMediaItemList);
            boolean isPhone = Utils.isPhone(BoomPlaylistActivity.this);
            if(isPhone){
                gridLayoutManager =
                        new GridLayoutManager(BoomPlaylistActivity.this, 2);
            }else{
                gridLayoutManager =
                        new GridLayoutManager(BoomPlaylistActivity.this, 3);
            }

            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (boomPlayListAdapter.whatView(position) == BoomPlayListAdapter.ITEM_VIEW_TYPE_ITEM_LIST) {
                        return 1;
                    } else {
                        return 2;
                    }
                }
            });
            gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            gridLayoutManager.scrollToPosition(0);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.addItemDecoration(new SimpleDividerItemDecoration(BoomPlaylistActivity.this, Utils.getWindowWidth(BoomPlaylistActivity.this)));
            recyclerView.addItemDecoration(new AlbumListSpacesItemDecoration(Utils.dpToPx(BoomPlaylistActivity.this, 0)));
            boomPlayListAdapter = new BoomPlayListAdapter(BoomPlaylistActivity.this, recyclerView, iMediaItemList, isPhone);
            recyclerView.setAdapter(boomPlayListAdapter);
            recyclerView.addItemDecoration(new BoomPlayListFooterItemDecoration(2, boomPlayListAdapter));
//                        recyclerView.setHasFixedSize(true);
            if (iMediaItemList.size() < 1) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listIsEmpty();
                    }
                });
            }
        }
    }
}
