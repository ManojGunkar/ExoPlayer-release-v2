package com.globaldelight.boom.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.app.fragments.FavouriteListFragment;
import com.globaldelight.boom.app.fragments.RecentPlayedFragment;
import com.globaldelight.boom.app.fragments.AboutFragment;
import com.globaldelight.boom.app.fragments.SettingFragment;
import com.globaldelight.boom.app.fragments.UpNextListFragment;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class ActivityContainer extends MasterActivity {

    private Toolbar toolbar;
    private int container;
    private Fragment mFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        container = getIntent().getIntExtra("container", R.string.header_about);
        initViews();
        //FlurryAnalyticHelper.init(this);
    }

    private void initViews() {
        setDrawerLocked(true);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(container);
        findViewById(R.id.fab).setVisibility(View.GONE);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        addFragment();
    }

    private void addFragment() {
        toolbar.setVisibility(View.VISIBLE);
        switch (container){
            case R.string.favourite_list:
                mFragment = new FavouriteListFragment();
                break;

            case R.string.recently_played:
                mFragment = new RecentPlayedFragment();
                break;

            case R.string.up_next:
                mFragment =  new UpNextListFragment();
                contentFragment.hideUpNext();
                break;

            case R.string.title_settings:
                mFragment =  new SettingFragment();
                setVisibleMiniPlayer(false);
               // FlurryAnalyticHelper.logEvent(UtilAnalytics.Settings_Page_Opened);
                FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Settings_Page_Opened);
                break;

            case R.string.header_about:
                mFragment =  new AboutFragment();
                setVisibleMiniPlayer(false);
                break;
        }
        if (mFragment!=null)
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.item_detail_container, mFragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getSupportFragmentManager().findFragmentById(R.id.item_detail_container).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFragment = null;
    }
}

