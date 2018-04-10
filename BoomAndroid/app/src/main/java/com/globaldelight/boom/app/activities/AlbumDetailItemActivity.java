package com.globaldelight.boom.app.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.base.IMediaItemCollection;
import com.globaldelight.boom.app.fragments.AlbumDetailItemFragment;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.utils.Utils;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class AlbumDetailItemActivity extends MasterActivity implements AlbumDetailItemFragment.LoadingCallback {

    IMediaItemCollection currentItem;
    AlbumDetailItemFragment fragment;
    private FloatingActionButton mFloatPlayAlbums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        initValues();

        initViews();
        //FlurryAnalyticHelper.init(this);
    }

    private void initValues() {
        Bundle b = getIntent().getBundleExtra("bundle");
        currentItem = (MediaItemCollection) b.getParcelable("mediaItemCollection");

        int width = Utils.getWindowWidth(this);
        int panelSize = (int) getResources().getDimension(R.dimen.album_title_height);
        int height = Utils.getWindowHeight(this) - panelSize * 4;
        setAlbumArtSize(width, width);
        setAlbumArt(currentItem.getItemArtUrl(), width);
    }

    private void initViews() {
        setDrawerLocked(true);
        mFloatPlayAlbums = (FloatingActionButton) findViewById(R.id.fab);
        mFloatPlayAlbums.setOnClickListener(this::onPlay);
        mFloatPlayAlbums.setEnabled(false);
        mFloatPlayAlbums.setVisibility(View.GONE);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle arguments = new Bundle();
        arguments.putParcelable("mediaItemCollection", (MediaItemCollection)currentItem);
        fragment = new AlbumDetailItemFragment();
        fragment.setArguments(arguments);
        fragment.setLoadingCallback(this);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.item_detail_container, fragment)
                .commit();
    }

    private void onPlay(View v) {
        if (null != currentItem) {
            if (currentItem.getItemType() == ItemType.GENRE) {
//                        FlurryAnalyticHelper.logEvent(UtilAnalytics.FAB_BUtton_Tapped_from_Genere_Section);
                FlurryAnalytics.getInstance(AlbumDetailItemActivity.this).setEvent(FlurryEvents.FAB_BUtton_Tapped_from_Genere_Section);
            } else if (currentItem.getItemType() == ItemType.ARTIST) {
//                        FlurryAnalyticHelper.logEvent(UtilAnalytics.FAB_BUtton_Tapped_from_Artist_Section);
                FlurryAnalytics.getInstance(AlbumDetailItemActivity.this).setEvent(FlurryEvents.FAB_BUtton_Tapped_from_Artist_Section);
            }
        }
        if( null != fragment ){
            fragment.onFloatPlayAlbums();
            new Handler().postDelayed(this::toggleSlidingPanel, 1000);
        }
    }

    private void setAlbumArtSize(int width, int height) {
        ViewGroup container = findViewById(R.id.toolbar_layout);
        ViewGroup.LayoutParams lp = container.getLayoutParams();
        lp.height = height;
        container.setLayoutParams(lp);
    }

    public void setAlbumArt(String albumArt, int height) {
        if ( albumArt == null ) albumArt = "";
        Glide.with(AlbumDetailItemActivity.this)
                .load(albumArt)
                .placeholder(R.drawable.ic_default_art_player_header)
                .centerCrop()
                .skipMemoryCache(true)
                .into(((ImageView) findViewById(R.id.activity_album_art)));
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onLoadingComplete() {
        mFloatPlayAlbums.setVisibility(View.VISIBLE);
        mFloatPlayAlbums.setEnabled(true);
        final Animation anim_in = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        mFloatPlayAlbums.startAnimation(anim_in);
    }
}
