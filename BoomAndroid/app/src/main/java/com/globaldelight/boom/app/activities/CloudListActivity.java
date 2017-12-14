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

import com.globaldelight.boom.BuildConfig;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.app.receivers.actions.PlayerEvents;
import com.globaldelight.boom.app.fragments.DropBoxListFragment;
import com.globaldelight.boom.app.fragments.GoogleDriveListFragment;
import com.globaldelight.boom.app.share.ShareDialog;
import com.globaldelight.boom.business.BusinessModelFactory;
import com.globaldelight.boom.utils.Utils;

/**
 * Created by Rahul Agarwal on 10-03-17.
 */

public class CloudListActivity extends MasterActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private NavigationView navigationView;
    private TextView toolbarTitle;
    public MenuItem cloudSyncItem;
    Runnable runnable;
    ImageView emptyPlaceholderIcon;
    TextView emptyPlaceholderTitle;
    LinearLayout emptyPlaceHolder;

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
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_txt);
        setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.drawer_background));
        navigationView.setNavigationItemSelectedListener(this);
        BusinessModelFactory.getCurrentModel().addItemsToDrawer(navigationView.getMenu(), Menu.NONE);

        emptyPlaceholderIcon = findViewById(R.id.list_empty_placeholder_icon);
        emptyPlaceholderTitle = findViewById(R.id.list_empty_placeholder_txt);
        emptyPlaceHolder = findViewById(R.id.list_empty_placeholder);

        setTitle(getIntent().getStringExtra("title"));
        loadEveryThing(getIntent().getStringExtra("title"), false);
    }

    @Override
    public void onBackPressed() {
        if (isPlayerExpended()) {
            toggleSlidingPanel();
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
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(PlayerEvents.ACTION_CLOUD_SYNC));
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
//                FlurryAnalyticHelper.logEvent(UtilAnalytics.Music_library_Opened_From_Drawer);
                FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Music_library_Opened_From_Drawer);
                runnable = navigateLibrary;
                break;
            case R.id.google_drive:
                if (Utils.isOnline(this)){
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    runnable = navigateGoogleDrive;
//                    FlurryAnalyticHelper.logEvent(UtilAnalytics.Google_Drive_OPENED_FROM_DRAWER);
                    FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Google_Drive_OPENED_FROM_DRAWER);

                }else {
                    Utils.networkAlert(this);
                    return false;
                }
                break;
            case R.id.drop_box:
                if (Utils.isOnline(this)){
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    runnable = navigateDropbox;
//                    FlurryAnalyticHelper.logEvent(UtilAnalytics.DROP_BOX_OPENED_FROM_DRAWER);
                    FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.DROP_BOX_OPENED_FROM_DRAWER);

                }else {
                    Utils.networkAlert(this);
                    return false;
                }
                break;
            case R.id.nav_setting:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startCompoundActivities(R.string.title_settings);
                    }
                }, 300);
                drawerLayout.closeDrawer(GravityCompat.START);
//                FlurryAnalyticHelper.logEvent(UtilAnalytics.Settings_Page_Opened);
                FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Settings_Page_Opened);
                return true;
            case R.id.nav_store:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startCompoundActivities(R.string.store_title);
                    }
                }, 300);
                drawerLayout.closeDrawer(GravityCompat.START);
//                FlurryAnalyticHelper.logEvent(UtilAnalytics.Store_Page_Opened_from_Drawer);
                FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Store_Page_Opened_from_Drawer);

                return true;
            case R.id.nav_share:
//                Utils.shareStart(CloudListActivity.this);
                new ShareDialog(this).show();
                drawerLayout.closeDrawer(GravityCompat.START);
//                FlurryAnalyticHelper.logEvent(UtilAnalytics.Share_Opened_from_Boom);
                FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Share_Opened_from_Boom);
                return true;
            default:
                BusinessModelFactory.getCurrentModel().onDrawerItemClicked(item, this);
                break;
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