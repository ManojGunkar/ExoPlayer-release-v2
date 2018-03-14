package com.globaldelight.boom.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.business.BusinessModelFactory;
import com.globaldelight.boom.utils.Utils;

/**
 * Created by adarsh on 09/03/18.
 */

abstract public class MainActivity extends MasterActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected NavigationView navigationView;
    protected Toolbar mToolbar;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        BusinessModelFactory.getCurrentModel().addItemsToDrawer(navigationView.getMenu(), Menu.NONE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        Runnable runnable;
        runnable = null;
        switch (item.getItemId()){
            case R.id.music_library:
                runnable = this::onNavigateToLibrary;
                FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Music_library_Opened_From_Drawer);
                break;
            case R.id.google_drive:
                if (Utils.isOnline(this)){
                    runnable = this::onNavigateToGoogleDrive;
                    FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.Google_Drive_OPENED_FROM_DRAWER);
                }else {
                    Utils.networkAlert(this);
                    return false;
                }
                break;

            case R.id.drop_box:
                if (Utils.isOnline(this)){
                    runnable = this::onNavigateToDropbox;
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

            default:
                BusinessModelFactory.getCurrentModel().onDrawerItemClicked(item, this);
                drawerLayout.closeDrawer(GravityCompat.START);
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


    abstract protected void onNavigateToLibrary();

    abstract protected void onNavigateToGoogleDrive();

    abstract protected void onNavigateToDropbox();
}
