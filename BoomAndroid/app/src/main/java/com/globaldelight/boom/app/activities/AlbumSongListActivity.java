package com.globaldelight.boom.app.activities;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.local.callback.IMediaItemCollection;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.app.receivers.actions.PlayerEvents;
import com.globaldelight.boom.app.fragments.AlbumSongListFragment;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class AlbumSongListActivity extends MasterActivity {

    private AlbumSongListFragment fragment;
    IMediaItemCollection currentItem;
    private ImageView artImg1, artImg2, artImg3, artImg4;
    private TableLayout tblAlbumArt;
    private FloatingActionButton mFloatPlayAlbumSongs;
    private static int screenWidth= 0;

    public void updateAlbumArt() {
        final ArrayList<String> urlList = MediaController.getInstance(this).getArtUrlList((MediaItemCollection) currentItem);
        if(urlList.size() > 0){
            findViewById(R.id.activity_album_art).setVisibility(View.GONE);
            tblAlbumArt.setVisibility(View.VISIBLE);
            setSongsArtImage(urlList);
        }else{
            findViewById(R.id.activity_album_art).setVisibility(View.VISIBLE);
            tblAlbumArt.setVisibility(View.GONE);
            setDefaultImage();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_song_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();
       // FlurryAnalyticHelper.init(this);
    }

    private void initViews() {
        setDrawerLocked(true);
        Bundle b = getIntent().getBundleExtra("bundle");
        currentItem = (MediaItemCollection) b.getParcelable("mediaItemCollection");

        fragment = new AlbumSongListFragment();

        artImg1 = (ImageView)findViewById(R.id.song_detail_list_art_img1);
        artImg2 = (ImageView)findViewById(R.id.song_detail_list_art_img2);
        artImg3 = (ImageView)findViewById(R.id.song_detail_list_art_img3);
        artImg4 = (ImageView)findViewById(R.id.song_detail_list_art_img4);

        tblAlbumArt = (TableLayout)findViewById(R.id.song_detail_list_art_table);


        screenWidth = Utils.getWindowWidth(this);
        int panelSize = (int) getResources().getDimension(R.dimen.album_title_height);
        int height = Utils.getWindowHeight(this) - panelSize * 4;
        setAlbumArtSize();
        ArrayList<String> artUrlList;
        if(currentItem.getItemType() == ItemType.PLAYLIST || currentItem.getItemType() == ItemType.BOOM_PLAYLIST){
            artUrlList = currentItem.getArtUrlList();
        }else{
            artUrlList = ((IMediaItemCollection)currentItem.getItemAt(currentItem.getCurrentIndex())).getArtUrlList();
        }
        setAlbumArt(artUrlList);

        mFloatPlayAlbumSongs = (FloatingActionButton) findViewById(R.id.fab);
        mFloatPlayAlbumSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentItem.getItemType() == ItemType.BOOM_PLAYLIST) {
         //           FlurryAnalyticHelper.logEvent(UtilAnalytics.FAB_Tapped_from_Boom_playlist_section);
                    FlurryAnalytics.getInstance(AlbumSongListActivity.this).setEvent(FlurryEvents.FAB_Tapped_from_Boom_playlist_section);
                } else if (currentItem.getItemType() == ItemType.PLAYLIST) {
//                    FlurryAnalyticHelper.logEvent(UtilAnalytics.FAB_Tapped_from_playlist_section);
                    FlurryAnalytics.getInstance(AlbumSongListActivity.this).setEvent(FlurryEvents.FAB_Tapped_from_playlist_section);
                }
                if (currentItem.getItemType() == ItemType.ARTIST) {
//                    FlurryAnalyticHelper.logEvent(UtilAnalytics.FAB_Tapped_from_ARTIST_ALL_SONGS_section);
                    FlurryAnalytics.getInstance(AlbumSongListActivity.this).setEvent(FlurryEvents.FAB_Tapped_from_ARTIST_ALL_SONGS_section);
                }else if(currentItem.getItemType() == ItemType.GENRE){
//                    FlurryAnalyticHelper.logEvent(UtilAnalytics.FAB_Tapped_from_GENERE_ALL_SONGS_section);
                    FlurryAnalytics.getInstance(AlbumSongListActivity.this).setEvent(FlurryEvents.FAB_Tapped_from_GENERE_ALL_SONGS_section);
                }
                if(null != fragment && !App.playbackManager().isTrackWaitingForPlay()){
                    fragment.onFloatPlayAlbumSongs();
                    sendBroadcast(new Intent(PlayerEvents.ACTION_TOGGLE_PLAYER_SLIDE));
                }
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle arguments = new Bundle();
        arguments.putParcelable("mediaItemCollection", (MediaItemCollection)currentItem);
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.item_detail_container, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onStart() {
        super.onStart();
//        FlurryAnalyticHelper.flurryStartSession(this);
        FlurryAnalytics.getInstance(this).startSession();
    }

    @Override
    public void onStop() {
        super.onStop();
//        FlurryAnalyticHelper.flurryStopSession(this);
          FlurryAnalytics.getInstance(this).endSession();

    }

    @Override
    protected void onResumeFragments() {
        sendBroadcast(new Intent(PlayerEvents.ACTION_PLAYER_SCREEN_RESUME));
        super.onResumeFragments();
    }

    @Override
    protected void onResume() {
        fragment.updateAdapter();
        registerPlayerReceiver(AlbumSongListActivity.this);
        super.onResume();

        final Animation anim_in = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        mFloatPlayAlbumSongs.startAnimation(anim_in);
    }

    @Override
    protected void onPause() {
        unregisterPlayerReceiver(AlbumSongListActivity.this);
        super.onPause();
    }

    private void setAlbumArtSize() {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(screenWidth, screenWidth);
        findViewById(R.id.song_detail_list_img_panel).setLayoutParams(lp);
    }

    public void setAlbumArt(ArrayList<String> artUrlList) {
        if(artUrlList.size()==0 || !PlayerUtils.isPathValid(artUrlList.get(0))){
            findViewById(R.id.activity_album_art).setVisibility(View.VISIBLE);
            tblAlbumArt.setVisibility(View.GONE);
            setDefaultImage();
        }else{
            findViewById(R.id.activity_album_art).setVisibility(View.GONE);
            tblAlbumArt.setVisibility(View.VISIBLE);
            setSongsArtImage(artUrlList);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            int colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary);
            ActivityManager.TaskDescription taskDescription = new
                    ActivityManager.TaskDescription(currentItem.getItemTitle(),
                    BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher), colorPrimary);
            setTaskDescription(taskDescription);
        }
    }

    private void setDefaultImage() {
        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(screenWidth, screenWidth);
        findViewById(R.id.activity_album_art).setLayoutParams(param);
        ((ImageView) findViewById(R.id.activity_album_art)).setImageDrawable(getResources().getDrawable(R.drawable.ic_default_art_grid, null));
    }

    private void setSongsArtImage(final ArrayList<String> Urls) {
        TableRow.LayoutParams param = new TableRow.LayoutParams(screenWidth / 2, screenWidth / 2);
        artImg1.setLayoutParams(param);
        artImg2.setLayoutParams(param);
        artImg3.setLayoutParams(param);
        artImg4.setLayoutParams(param);
        PlayerUtils.setSongsArtTable(this, Urls, new ImageView[]{artImg1, artImg2, artImg3, artImg4});
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            fragment.updateBoomPlaylistIfOrderChanged();
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
        fragment.updateBoomPlaylistIfOrderChanged();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        fragment.updateBoomPlaylistIfOrderChanged();
        super.onDestroy();
    }
}
