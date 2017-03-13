package com.globaldelight.boom.ui.musiclist.activity;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.globaldelight.boom.R;
import com.globaldelight.boom.task.PlayerEvents;
import com.globaldelight.boom.ui.musiclist.fragment.DropBoxListFragment;
import com.globaldelight.boom.ui.musiclist.fragment.GoogleDriveListFragment;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.Utils;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rahul Agarwal on 10-03-17.
 */

public class CloudListActivity extends MasterActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private NavigationView navigationView;
    private int fade_in = android.R.anim.fade_in;
    private int fade_out = android.R.anim.fade_out;
    private RegularTextView toolbarTitle;
    public MenuItem cloudSyncItem;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);
        initView();
    }

    Runnable navigateLibrary= new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.music_library).setChecked(true);
            Intent libraryIntent = new Intent(CloudListActivity.this, MainActivity.class);
            libraryIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(libraryIntent);
            overridePendingTransition(fade_in, fade_out);
        }
    };

    Runnable navigateDropbox= new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.drop_box).setChecked(true);
            Fragment fragment = new DropBoxListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
        }
    };

    Runnable navigateGoogleDrive = new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.google_drive).setChecked(true);
            Fragment fragment = new GoogleDriveListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
        }
    };

    public void setTitle(String title){
        toolbarTitle.setText(title);
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (RegularTextView) findViewById(R.id.toolbar_txt);
        setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.drawer_background));
        navigationView.setNavigationItemSelectedListener(this);

        loadEveryThing(getIntent().getStringExtra("title"));
    }

    @Override
    public void onBackPressed() {
        if (isPlayerExpended()) {
            sendBroadcast(new Intent(PlayerEvents.ACTION_TOGGLE_PLAYER_SLIDE));
        } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.cloud_menu, menu);

        cloudSyncItem = menu.findItem(R.id.action_cloud_sync);
        cloudSyncItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_cloud_sync){
            sendBroadcast(new Intent(PlayerEvents.ACTION_CLOUD_SYNC));
            return true;
        }
        return false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        runnable = null;
        switch (item.getItemId()){
            case R.id.music_library:
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                runnable = navigateLibrary;
                break;
            case R.id.google_drive:
                setTitle(getResources().getString(R.string.google_drive));
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                runnable = navigateGoogleDrive;
                break;
            case R.id.drop_box:
                setTitle(getResources().getString(R.string.drop_box));
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                runnable = navigateDropbox;
                break;
            case R.id.nav_setting:
                startCompoundActivities(R.string.title_settings);
                break;
            case R.id.nav_store:
                startCompoundActivities(R.string.store_title);
                break;
            case R.id.nav_share:
                Utils.shareStart(this);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);

        if (runnable != null) {
            item.setChecked(true);
            Handler handler = new Handler();
            handler.postDelayed(runnable, 100);
        }
        return true;
    }

    private void startCompoundActivities(int activityName) {
        Intent intent = new Intent(this, ActivityContainer.class);
        intent.putExtra("container",activityName);
        startActivity(intent);
        overridePendingTransition(fade_in, fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerPlayerReceiver(CloudListActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterPlayerReceiver(CloudListActivity.this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        loadEveryThing(intent.getStringExtra("title"));
    }

    private void loadEveryThing(String title){
        setTitle(title);
        if(null != title && title.equals(getResources().getString(R.string.drop_box))){
            new Handler().post(navigateDropbox);
        }else if(title.equals(getResources().getString(R.string.google_drive))){
            new Handler().post(navigateGoogleDrive);
        }
    }
}