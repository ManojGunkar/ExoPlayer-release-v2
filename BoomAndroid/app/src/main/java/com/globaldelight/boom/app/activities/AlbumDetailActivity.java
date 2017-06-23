package com.globaldelight.boom.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.local.callback.IMediaItemCollection;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.app.receivers.actions.PlayerEvents;
import com.globaldelight.boom.app.fragments.AlbumDetailFragment;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class AlbumDetailActivity extends MasterActivity {

    IMediaItemCollection collection, currentItem;
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
        collection = (MediaItemCollection) getIntent().getParcelableExtra("mediaItemCollection");

        if( collection.getParentType() == ItemType.ALBUM ){
            currentItem = collection;
        } else {
            currentItem = (IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex());
        }

        int width = Utils.getWindowWidth(this);
        int panelSize = (int) getResources().getDimension(R.dimen.album_title_height);
        int height = Utils.getWindowHeight(this) - panelSize * 4;
        setAlbumArtSize(width, width);
        setAlbumArt(currentItem.getItemArtUrl());
    }

    private void initViews() {
        setDrawerLocked(true);
        mFloatPlayAllAlbums = (FloatingActionButton) findViewById(R.id.fab);
        mFloatPlayAllAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != currentItem) {
                    if (currentItem.getItemType() == ItemType.GENRE) {
                     //   FlurryAnalyticHelper.logEvent(UtilAnalytics.FAB_BUtton_Tapped_from_Genere_Details_Section);
                        FlurryAnalytics.getInstance(AlbumDetailActivity.this).setEvent(FlurryEvents.FAB_BUtton_Tapped_from_Genere_Details_Section);
                    } else if (currentItem.getItemType() == ItemType.ARTIST) {
//                        FlurryAnalyticHelper.logEvent(UtilAnalytics.FAB_BUtton_Tapped_from_Artist_details_Section);
                        FlurryAnalytics.getInstance(AlbumDetailActivity.this).setEvent(FlurryEvents.FAB_BUtton_Tapped_from_Artist_details_Section);
                    }else if (currentItem.getItemType() == ItemType.ALBUM ) {
//                        FlurryAnalyticHelper.logEvent(UtilAnalytics.FAB_Button_Tapped_from_Album_Section);
                        FlurryAnalytics.getInstance(AlbumDetailActivity.this).setEvent(FlurryEvents.FAB_Button_Tapped_from_Album_Section);
                    }
                }
                if(null != fragment && !App.getPlayerEventHandler().isTrackWaitingForPlay()){
                    fragment.onFloatPlayAlbums();
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
        arguments.putParcelable("mediaItemCollection", (MediaItemCollection)collection);
        fragment = new AlbumDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.item_detail_container, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    protected void onResumeFragments() {
        sendBroadcast(new Intent(PlayerEvents.ACTION_PLAYER_SCREEN_RESUME));
        super.onResumeFragments();
    }

    @Override
    protected void onResume() {
        registerPlayerReceiver(AlbumDetailActivity.this);
        super.onResume();

        final Animation anim_in = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        mFloatPlayAllAlbums.startAnimation(anim_in);
    }

    @Override
    protected void onPause() {
        unregisterPlayerReceiver(AlbumDetailActivity.this);
        super.onPause();
    }

    private void setAlbumArtSize(int width, int height) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height);
        findViewById(R.id.activity_album_art).setLayoutParams(lp);
    }

    public void setAlbumArt(String albumArt) {
        ImageView imageView = (ImageView) findViewById(R.id.activity_album_art);
        if ( albumArt == null ) albumArt = "";
        Picasso.with(AlbumDetailActivity.this)
                .load(new File(albumArt))
                .placeholder(R.drawable.ic_default_art_player_header)
                .noFade()
                .into(imageView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onStart() {
        super.onStart();
       // FlurryAnalyticHelper.flurryStartSession(this);
        FlurryAnalytics.getInstance(this).startSession();
    }

    @Override
    public void onStop() {
        super.onStop();
//        FlurryAnalyticHelper.flurryStopSession(this);
        FlurryAnalytics.getInstance(this).endSession();
    }

}

