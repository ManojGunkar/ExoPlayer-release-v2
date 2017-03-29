package com.globaldelight.boom.ui.musiclist.activity;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.analytics.UtilAnalytics;
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
    private RegularTextView toolbarTitle;
    public MenuItem cloudSyncItem;
    Runnable runnable;
    ImageView emptyPlaceholderIcon;
    RegularTextView emptyPlaceholderTitle;
    LinearLayout emptyPlaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);
        initView();
        FlurryAnalyticHelper.init(this);
    }

    Runnable navigateLibrary= new Runnable() {
        public void run() {
            navigationView.getMenu().findItem(R.id.music_library).setChecked(true);
            Intent libraryIntent = new Intent(CloudListActivity.this, MainActivity.class);
            libraryIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(libraryIntent);
            overridePendingTransition(R.anim.com_mixpanel_android_fade_in, R.anim.com_mixpanel_android_fade_out);
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

        emptyPlaceholderIcon = (ImageView) findViewById(R.id.list_empty_placeholder_icon);
        emptyPlaceholderTitle = (RegularTextView) findViewById(R.id.list_empty_placeholder_txt);
        emptyPlaceHolder = (LinearLayout) findViewById(R.id.list_empty_placeholder);

        setTitle(getIntent().getStringExtra("title"));
        loadEveryThing(getIntent().getStringExtra("title"), false);
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
            if (null != toolbarTitle.getText().toString()) {
                if (toolbarTitle.getText().toString() == getResources().getString(R.string.drop_box)) {
                    FlurryAnalyticHelper.logEvent(UtilAnalytics.Sync_Button_tapped_from_Drop_BOx);

                } else if (toolbarTitle.getText().toString() == getResources().getString(R.string.google_drive))
                {
                    FlurryAnalyticHelper.logEvent(UtilAnalytics.Sync_Button_tapped_from_Google_Drive);
                }
            }
            return true;
        }
        return false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        runnable = null;
        switch (item.getItemId()){
            case R.id.music_library:
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Music_library_Opened_From_Drawer);
                runnable = navigateLibrary;
                break;
            case R.id.google_drive:
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                runnable = navigateGoogleDrive;
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Google_Drive_OPENED_FROM_DRAWER);
                break;
            case R.id.drop_box:
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                runnable = navigateDropbox;
                FlurryAnalyticHelper.logEvent(UtilAnalytics.DROP_BOX_OPENED_FROM_DRAWER);
                break;
            case R.id.nav_setting:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startCompoundActivities(R.string.title_settings);
                    }
                }, 300);
                drawerLayout.closeDrawer(GravityCompat.START);
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Settings_Page_Opened);
                return true;
            case R.id.nav_store:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startCompoundActivities(R.string.store_title);
                    }
                }, 300);
                drawerLayout.closeDrawer(GravityCompat.START);
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Store_Page_Opened_from_Drawer);
                return true;
            case R.id.nav_share:
                Utils.shareStart(CloudListActivity.this);
                drawerLayout.closeDrawer(GravityCompat.START);
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Share_Opened_from_Boom);
                return true;
        }

        if (runnable != null) {
            item.setChecked(true);
            Handler handler = new Handler();
            handler.postDelayed(runnable, 300);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startCompoundActivities(int activityName) {
        Intent intent = new Intent(this, ActivityContainer.class);
        intent.putExtra("container",activityName);
        startActivity(intent);
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
        loadEveryThing(intent.getStringExtra("title"), true);
    }

    private void loadEveryThing(String title, boolean anim){
        if(null != title && title.equals(getResources().getString(R.string.drop_box))){
            new Handler().postDelayed(navigateDropbox, anim ? 300 : 0);
        }else if(title.equals(getResources().getString(R.string.google_drive))){
            new Handler().postDelayed(navigateGoogleDrive, anim ? 300 : 0);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FlurryAnalyticHelper.flurryStartSession(this);
    }

    @Override
    public void onStop() {
            super.onStop();
            FlurryAnalyticHelper.flurryStopSession(this);
    }

    public void listIsEmpty(boolean enable, boolean isAccountConfigured) {
        if (enable) {
            emptyPlaceHolder.setVisibility(View.VISIBLE);
            Drawable imgResource = null;
            String placeHolderTxt = null;
            if(isAccountConfigured){
                imgResource = getResources().getDrawable(R.drawable.ic_no_music_placeholder, null);
                placeHolderTxt = getResources().getString(R.string.no_music_placeholder_txt);
            }else {
                imgResource = getResources().getDrawable(R.drawable.ic_cloud_placeholder, null);
                placeHolderTxt = getResources().getString(R.string.cloud_configure_placeholder_txt);
            }
            emptyPlaceholderIcon.setImageDrawable(imgResource);
            emptyPlaceholderTitle.setText(placeHolderTxt);
        } else {
            emptyPlaceHolder.setVisibility(View.GONE);
        }
    }
}