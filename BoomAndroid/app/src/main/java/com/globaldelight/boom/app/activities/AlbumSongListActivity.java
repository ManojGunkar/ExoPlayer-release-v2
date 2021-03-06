package com.globaldelight.boom.app.activities;

import android.app.ActivityManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;

import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.base.IMediaItemCollection;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.app.fragments.AlbumSongListFragment;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class AlbumSongListActivity extends MasterActivity implements AlbumSongListFragment.Callback {

    private AlbumSongListFragment fragment;
    IMediaItemCollection currentItem;
    private ImageView artImg1, artImg2, artImg3, artImg4;
    private TableLayout tblAlbumArt;
    private FloatingActionButton mFloatPlayAlbumSongs;
    private static int screenWidth= 0;
    private int mItemIndex = 0;

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
        mItemIndex = b.getInt("itemIndex");

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
            artUrlList = ((IMediaItemCollection)currentItem.getItemAt(mItemIndex)).getArtUrlList();
        }
        setAlbumArt(artUrlList);

        mFloatPlayAlbumSongs = (FloatingActionButton) findViewById(R.id.fab);
        mFloatPlayAlbumSongs.setOnClickListener(this::onPlay);
        mFloatPlayAlbumSongs.setEnabled(false);
        mFloatPlayAlbumSongs.setVisibility(View.GONE);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle arguments = new Bundle();
        arguments.putParcelable("mediaItemCollection", (MediaItemCollection)currentItem);
        arguments.putInt("itemIndex", mItemIndex);
        fragment.setArguments(arguments);
        fragment.setCallback(this);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.item_detail_container, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onStart() {
        super.onStart();
        fragment.updateAdapter();
    }

    private void onPlay(View v) {
        if (currentItem.getItemType() == ItemType.BOOM_PLAYLIST) {
            FlurryAnalytics.getInstance(AlbumSongListActivity.this).setEvent(FlurryEvents.FAB_Tapped_from_Boom_playlist_section);
        } else if (currentItem.getItemType() == ItemType.PLAYLIST) {
            FlurryAnalytics.getInstance(AlbumSongListActivity.this).setEvent(FlurryEvents.FAB_Tapped_from_playlist_section);
        }
        if (currentItem.getItemType() == ItemType.ARTIST) {
            FlurryAnalytics.getInstance(AlbumSongListActivity.this).setEvent(FlurryEvents.FAB_Tapped_from_ARTIST_ALL_SONGS_section);
        }else if(currentItem.getItemType() == ItemType.GENRE){
            FlurryAnalytics.getInstance(AlbumSongListActivity.this).setEvent(FlurryEvents.FAB_Tapped_from_GENERE_ALL_SONGS_section);
        }
        if(null != fragment ){
            fragment.onFloatPlayAlbumSongs();
            new Handler().postDelayed(this::toggleSlidingPanel, 1000);
        }
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
                    ActivityManager.TaskDescription(currentItem.getTitle(),
                    BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher), colorPrimary);
            setTaskDescription(taskDescription);
        }
    }

    private void setDefaultImage() {
        ViewGroup container = findViewById(R.id.toolbar_layout);
        ViewGroup.LayoutParams lp = container.getLayoutParams();
        lp.height = screenWidth;
        container.setLayoutParams(lp);
        ((ImageView) findViewById(R.id.activity_album_art)).setImageDrawable(getResources().getDrawable(R.drawable.ic_default_art_grid, null));
    }

    private void setSongsArtImage(final ArrayList<String> Urls) {
        ViewGroup container = findViewById(R.id.toolbar_layout);
        ViewGroup.LayoutParams lp = container.getLayoutParams();
        lp.height = screenWidth;
        container.setLayoutParams(lp);

        PlayerUtils.setSongsArtTable(this, Urls, new ImageView[]{artImg1, artImg2, artImg3, artImg4});
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
        fragment.updateBoomPlaylistIfOrderChanged();
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onLoadingComplete() {
        mFloatPlayAlbumSongs.setEnabled(true);
        mFloatPlayAlbumSongs.setVisibility(View.VISIBLE);
        final Animation anim_in = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        mFloatPlayAlbumSongs.startAnimation(anim_in);
    }
}
