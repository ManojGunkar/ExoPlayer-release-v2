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
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.base.IMediaItemCollection;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.app.fragments.AlbumDetailFragment;
import com.globaldelight.boom.utils.Utils;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class AlbumDetailActivity extends MasterActivity implements AlbumDetailFragment.Callback{

    IMediaItemCollection collection, currentItem;
    private int mItemIndex = -1;
    AlbumDetailFragment fragment;
    private FloatingActionButton mFloatPlayAllAlbums;

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
        collection = (MediaItemCollection) b.getParcelable("mediaItemCollection");
        mItemIndex = b.getInt("itemIndex");

        if( collection.getParentType() == ItemType.ALBUM ){
            currentItem = collection;
        } else {
            currentItem = (IMediaItemCollection) collection.getItemAt(mItemIndex);
        }

        int width = Utils.getWindowWidth(this);
        int panelSize = (int) getResources().getDimension(R.dimen.album_title_height);
        int height = Utils.getWindowHeight(this) - panelSize * 4;
        setAlbumArtSize(width, width);
    }

    private void initViews() {
        setDrawerLocked(true);
        mFloatPlayAllAlbums = (FloatingActionButton) findViewById(R.id.fab);
        mFloatPlayAllAlbums.setOnClickListener(this::onPlay);
        mFloatPlayAllAlbums.setEnabled(false);
        mFloatPlayAllAlbums.setVisibility(View.GONE);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle arguments = new Bundle();
        arguments.putParcelable("mediaItemCollection", (MediaItemCollection)collection);
        arguments.putInt("itemIndex", mItemIndex);
        fragment = new AlbumDetailFragment();
        fragment.setArguments(arguments);
        fragment.setCallback(this);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.item_detail_container, fragment)
                .commitAllowingStateLoss();
    }

    private void onPlay(View view) {
        if (null != currentItem) {
            if (currentItem.getItemType() == ItemType.GENRE) {
                FlurryAnalytics.getInstance(AlbumDetailActivity.this).setEvent(FlurryEvents.FAB_BUtton_Tapped_from_Genere_Details_Section);
            } else if (currentItem.getItemType() == ItemType.ARTIST) {
                FlurryAnalytics.getInstance(AlbumDetailActivity.this).setEvent(FlurryEvents.FAB_BUtton_Tapped_from_Artist_details_Section);
            }else if (currentItem.getItemType() == ItemType.ALBUM ) {
                FlurryAnalytics.getInstance(AlbumDetailActivity.this).setEvent(FlurryEvents.FAB_Button_Tapped_from_Album_Section);
            }
        }
        if(null != fragment){
            fragment.onFloatPlayAlbums();
            new Handler().postDelayed(this::toggleSlidingPanel, 1000);
        }
    }

    private void setAlbumArtSize(int width, int height) {
        FrameLayout container = findViewById(R.id.toolbar_layout);
        ViewGroup.LayoutParams lp = container.getLayoutParams();
        lp.height = height;
        container.setLayoutParams(lp);
    }

    public void setAlbumArt(String albumArt) {
        ImageView imageView = (ImageView) findViewById(R.id.activity_album_art);
        Glide.with(this)
                .load(albumArt)
                .placeholder(R.drawable.ic_default_art_player_header)
                .centerCrop()
                .skipMemoryCache(true)
                .into(imageView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onStart() {
        super.onStart();
        setAlbumArt(currentItem.getItemArtUrl());
    }

    @Override
    public void onStop() {
        super.onStop();
        setAlbumArt(null);
    }

    @Override
    public void onLoadingComplete() {
        mFloatPlayAllAlbums.setEnabled(true);
        mFloatPlayAllAlbums.setVisibility(View.VISIBLE);
        final Animation anim_in = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        mFloatPlayAllAlbums.startAnimation(anim_in);
    }
}

