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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.TokenPair;
import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DropboxMedia.DropboxFileList;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.task.LoadDropBoxList;
import com.globaldelight.boom.task.PlayerService;
import com.globaldelight.boom.ui.musiclist.adapter.CloudItemListAdapter;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.Logger;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.async.Action;
import com.globaldelight.boom.utils.helpers.DropBoxUtills;

import java.util.ArrayList;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_ITEM_CLICKED;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_LAST_PLAYED_SONG;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_RECEIVE_SONG;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_TRACK_STOPPED;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_REPEAT;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_SHUFFLE;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_TRACK_SEEK;

/**
 * Created by Rahul Agarwal on 20-11-16.
 */

public class CloudItemListActivity extends AppCompatActivity implements DropboxFileList.IDropboxUpdater {

    private DropboxFileList dropboxFileList;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private CloudItemListAdapter adapter;
    private PermissionChecker permissionChecker;
    private View emptyView;
    private LinearLayout mMiniPlayer, mStartPlayer;
    private ProgressBar mTrackProgress;
    private RegularTextView mTitle, mSubTitle, mToolTitle;
    private ImageView mPlayerArt, mPlayPause;
    private MediaType mediaType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_left, R.anim.stay_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        mediaType = MediaType.fromOrdinal(getIntent().getIntExtra("SONG_LIST_TYPE", MediaType.DEVICE_MEDIA_LIB.ordinal()));

        IntentFilter intentFilter = new IntentFilter();
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
        toolbar = (Toolbar)findViewById(R.id.favourite_list_toolbar);
        try {
            setSupportActionBar(toolbar);
        }catch (IllegalStateException e){}
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mToolTitle = (RegularTextView) findViewById(R.id.favourite_list_toolbar_title);

        if(mediaType == MediaType.DEVICE_MEDIA_LIB){
            mToolTitle.setText(getResources().getString(R.string.title_favourite_list));
        }else if(mediaType == MediaType.DROP_BOX){
            mToolTitle.setText(getResources().getString(R.string.title_dropbox_list));
        }else if(mediaType == MediaType.GOOGLE_DRIVE){
            mToolTitle.setText(getResources().getString(R.string.title_google_drive_list));
        }

        recyclerView = (RecyclerView) findViewById(R.id.rv_favourite_list);

        emptyView = findViewById(R.id.fav_empty_view);

        if(mediaType == MediaType.DROP_BOX){
            dropboxFileList = DropboxFileList.getDropboxListInstance(this);
            dropboxFileList.setDropboxUpdater(this);
            dropboxFileList.clearDropboxContent();

            DropBoxUtills.checkDropboxAuthentication(this);

            setSongListAdapter(dropboxFileList.getFileList());
        }

        checkPermissions();
    }

    @Override
    public void UpdateDropboxEntryList() {
        adapter.notifyDataSetChanged();
        listIsEmpty(adapter.getItemCount());
    }

    private class LoadFavouriteList extends AsyncTask<Void, Integer, ArrayList<? extends IMediaItemBase>> {

        @Override
        protected ArrayList<? extends IMediaItemBase> doInBackground(Void... params) {
            return MediaController.getInstance(CloudItemListActivity.this).getFavouriteListItems();
        }

        @Override
        protected void onPostExecute(ArrayList<? extends IMediaItemBase> iMediaItemList) {
            super.onPostExecute(iMediaItemList);
            setSongListAdapter(iMediaItemList);
        }
    }

    private void setSongListAdapter(ArrayList<? extends IMediaItemBase> iMediaItemList) {
        final GridLayoutManager gridLayoutManager =
                new GridLayoutManager(CloudItemListActivity.this, 1);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        gridLayoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new CloudItemListAdapter(CloudItemListActivity.this, recyclerView, iMediaItemList);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        if(mediaType == MediaType.DROP_BOX)
            listIsEmpty(iMediaItemList.size());
    }

    public void listIsEmpty(int size) {
        if (size < 1) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else{
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

    }

    private void checkPermissions() {
        permissionChecker = new PermissionChecker(this, this, recyclerView);
        permissionChecker.check(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getResources().getString(R.string.storage_permission),
                new PermissionChecker.OnPermissionResponse() {
                    @Override
                    public void onAccepted() {
                        if(mediaType == MediaType.DEVICE_MEDIA_LIB)
                            new LoadFavouriteList().execute();
                        else if(mediaType == MediaType.DROP_BOX)
                            new LoadDropBoxList(CloudItemListActivity.this).execute();
                        /*else if(mediaType == MediaType.GOOGLE_DRIVE)
                            new LoadGoogleDriveList().execute();*/
                    }

                    @Override
                    public void onDecline() {
                        finish();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
            IMediaItem item;
            switch (intent.getAction()){
                case ACTION_RECEIVE_SONG :
                    item = intent.getParcelableExtra("playing_song");
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
                    if(null != adapter)
                        adapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    private void updateMiniPlayer(IMediaItem item, boolean playing, boolean isLastPlayedSong) {
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

    private void updateAlbumArt(final IMediaItem item){
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
                                    PlayerUtils.ImageViewAnimatedChange(CloudItemListActivity.this, mPlayerArt, bitmap);
                                }catch (NullPointerException e){
                                    Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                                            R.drawable.ic_default_small_grid_song);
                                    PlayerUtils.ImageViewAnimatedChange(CloudItemListActivity.this, mPlayerArt, albumArt);
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
                PlayerUtils.ImageViewAnimatedChange(CloudItemListActivity.this, mPlayerArt, albumArt);
            }
        }
    }

    private void initMiniPlayer(){
        mMiniPlayer = (LinearLayout) findViewById(R.id.favourite_mini_player);
        mStartPlayer = (LinearLayout) findViewById(R.id.mini_touch_panel);
        mTrackProgress = (ProgressBar) findViewById(R.id.mini_player_track_progress);
        mTitle = (RegularTextView) findViewById(R.id.mini_player_title);
        mSubTitle = (RegularTextView) findViewById(R.id.mini_player_sub_title);
        mPlayerArt = (ImageView) findViewById(R.id.mini_player_album_art);
        mPlayPause = (ImageView) findViewById(R.id.mini_player_play_pause);

        mStartPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CloudItemListActivity.this, BoomPlayerActivity.class);
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
        if(null != adapter){
            adapter.notifyDataSetChanged();
        }
        if (null != App.getPlayerEventHandler().getPlayingItem()) {
            updateMiniPlayer(App.getPlayingQueueHandler().getUpNextList().getPlayingItem() != null ?
                            (IMediaItem) App.getPlayingQueueHandler().getUpNextList().getPlayingItem() :
                            null, App.getPlayerEventHandler().isPlaying(),
                       /*if last played item is set as playing item*/ (!App.getPlayerEventHandler().isPlaying() && !App.getPlayerEventHandler().isPaused() ? true : false));
            mMiniPlayer.setVisibility(View.VISIBLE);
        } else {
            mMiniPlayer.setVisibility(View.GONE);
        }

        if(mediaType == MediaType.DROP_BOX && null != App.getDropboxAPI()) {
            AndroidAuthSession session = App.getDropboxAPI().getSession();
            if (session.authenticationSuccessful()) {
                try {
                    session.finishAuthentication();
                    TokenPair tokens = session.getAccessTokenPair();
                    DropBoxUtills.storeKeys(this, tokens.key, tokens.secret);
                } catch (IllegalStateException e) {
                    Toast.makeText(this,"Couldn't authenticate with Dropbox:"
                            + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != mPlayerEventBroadcastReceiver)
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
