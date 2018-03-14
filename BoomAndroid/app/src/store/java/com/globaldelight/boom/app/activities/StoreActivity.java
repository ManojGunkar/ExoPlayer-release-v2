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
import com.globaldelight.boom.app.fragments.StoreFragment;
import com.globaldelight.boom.app.fragments.UpNextListFragment;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */


public class StoreActivity extends MasterActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        initViews();
    }

    private void initViews() {
        setDrawerLocked(true);
        toolbar = findViewById(R.id.toolbar);
        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setTitle(R.string.store_title);
        findViewById(R.id.fab).setVisibility(View.GONE);

        // Show the Up button in the action bar.
        addFragment();
    }

    private void addFragment() {
        toolbar.setVisibility(View.VISIBLE);
        Fragment fragment =  new StoreFragment();
        setVisibleMiniPlayer(false);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.item_detail_container, fragment)
                .commitAllowingStateLoss();

        FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Store_Page_Opened_from_Drawer);
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

}

