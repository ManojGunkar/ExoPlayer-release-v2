package com.globaldelight.boom.ui.musiclist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.task.PlayerEvents;
import com.globaldelight.boom.ui.musiclist.fragment.AlbumDetailItemFragment;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class AlbumDetailItemActivity extends MasterActivity {

    IMediaItemCollection currentItem;
    AlbumDetailItemFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        initValues();

        initViews(savedInstanceState);
    }

    private void initValues() {
        currentItem = (MediaItemCollection) getIntent().getParcelableExtra("mediaItemCollection");

        int width = Utils.getWindowWidth(this);
        int panelSize = (int) getResources().getDimension(R.dimen.album_title_height);
        int height = Utils.getWindowHeight(this) - panelSize * 4;
        setAlbumArtSize(width, width);
        setAlbumArt(currentItem.getItemArtUrl(), width);
    }

    private void initViews(Bundle savedInstanceState) {
        setDrawerLocked(true);
        final FloatingActionButton mFloatPlayAlbums = (FloatingActionButton) findViewById(R.id.fab);
        mFloatPlayAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(null != fragment){
                fragment.onFloatPlayAlbums();
            }
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable("mediaItemCollection", (MediaItemCollection)currentItem);
            fragment = new AlbumDetailItemFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onResumeFragments() {
        sendBroadcast(new Intent(PlayerEvents.ACTION_PLAYER_SCREEN_RESUME));
        super.onResumeFragments();
    }

    @Override
    protected void onResume() {
        registerPlayerReceiver(AlbumDetailItemActivity.this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterPlayerReceiver(AlbumDetailItemActivity.this);
        super.onPause();
    }

    private void setAlbumArtSize(int width, int height) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height);
        findViewById(R.id.activity_album_art).setLayoutParams(lp);
    }

    public void setAlbumArt(String albumArt, int height) {
                if (PlayerUtils.isPathValid(albumArt )) {
                    Picasso.with(AlbumDetailItemActivity.this)
                            .load(new File(albumArt)).resize(height, height)
                            .error(getResources().getDrawable(R.drawable.ic_default_art_player_header, null)).noFade()
                            .into(((ImageView) findViewById(R.id.activity_album_art)));
                }else {
                    ((ImageView) findViewById(R.id.activity_album_art)).setImageDrawable(getResources().getDrawable(R.drawable.ic_default_art_player_header, null));
                }
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
}
