package com.globaldelight.boom.app.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.app.receivers.actions.PlayerEvents;
import com.globaldelight.boom.app.fragments.DropBoxListFragment;
import com.globaldelight.boom.app.fragments.GoogleDriveListFragment;
import com.globaldelight.boom.business.BusinessModelFactory;
import com.globaldelight.boom.utils.Utils;

/**
 * Created by Rahul Agarwal on 10-03-17.
 */

public class CloudListActivity extends  MainActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);
        initView();
    }

    private void initView() {
        setTitle(getIntent().getStringExtra("title"));
        loadEveryThing(getIntent().getStringExtra("title"), false);
    }


    @Override
    protected void onNavigateToLibrary() {
        navigationView.getMenu().findItem(R.id.music_library).setChecked(true);
        Intent libraryIntent = new Intent(CloudListActivity.this, LibraryActivity.class);
        libraryIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(libraryIntent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onNavigateToGoogleDrive() {
        navigationView.getMenu().findItem(R.id.google_drive).setChecked(true);
        Fragment fragment = new GoogleDriveListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }

    @Override
    protected void onNavigateToDropbox() {
        navigationView.getMenu().findItem(R.id.drop_box).setChecked(true);
        Fragment fragment = new DropBoxListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        loadEveryThing(intent.getStringExtra("title"), true);
    }

    private void loadEveryThing(String title, boolean anim){
        if(null != title && title.equals(getResources().getString(R.string.drop_box))){
            new Handler().postDelayed(this::onNavigateToDropbox, anim ? 300 : 0);
        }else if(title.equals(getResources().getString(R.string.google_drive))){
            new Handler().postDelayed(this::onNavigateToGoogleDrive, anim ? 300 : 0);
        }
    }
}