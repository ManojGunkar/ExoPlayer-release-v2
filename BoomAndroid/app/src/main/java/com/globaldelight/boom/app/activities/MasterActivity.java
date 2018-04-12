package com.globaldelight.boom.app.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.fragments.MasterContentFragment;
import com.globaldelight.boom.view.slidinguppanel.SlidingUpPanelLayout;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.receivers.actions.PlayerEvents;

/**
 * Created by Rahul Agarwal on 12-01-17.
 */

public class MasterActivity extends AppCompatActivity implements SlidingUpPanelLayout.PanelSlideListener {
    private static final String TAG = "MasterActivity";
    private static boolean isPlayerExpended = false, isEffectScreenExpended = false;

    protected DrawerLayout drawerLayout;
    private SlidingUpPanelLayout mSlidingPaneLayout;
    protected MasterContentFragment contentFragment;
    private Handler handler;
    private boolean isDrawerLocked = false;


    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(R.layout.activity_master);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        //make volume keys change multimedia volume even if music is not playing now
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        handler = new Handler();
        mSlidingPaneLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        LinearLayout activityContainer = (LinearLayout)findViewById(R.id.activity_holder);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        contentFragment = new MasterContentFragment();
        contentFragment.setSlidingPanel(mSlidingPaneLayout);
        initContainer();
        isPlayerExpended = mSlidingPaneLayout.isPanelExpanded();
    }


    public void setDrawerUnlocked(){
        drawerLayout.setDrawerLockMode(isDrawerLocked ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public void setDrawerLocked(boolean locked){
        isDrawerLocked = locked;
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void setVisibleMiniPlayer(boolean visible){
        if(null != mSlidingPaneLayout) {
            if (visible) {
                mSlidingPaneLayout.showPanel();
            } else {
                mSlidingPaneLayout.hidePanel();

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if(mSlidingPaneLayout.isPanelExpanded()){
            mSlidingPaneLayout.collapsePanel();
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (mSlidingPaneLayout.isPanelExpanded()) {
                    return false;
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (mSlidingPaneLayout.isPanelExpanded()) {
                    return false;
                }
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        isPlayerExpended = mSlidingPaneLayout.isPanelExpanded();
        super.onResume();
    }


    @Override
    public  void onStart() {
        super.onStart();
        FlurryAnalytics.getInstance(this).startSession(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAnalytics.getInstance(this).endSession(this);
    }

    public void initContainer() {
        if(!mSlidingPaneLayout.isPanelExpanded()) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.panel_holder, contentFragment).commitAllowingStateLoss();
            mSlidingPaneLayout.setPanelSlideListener(MasterActivity.this);
        }
    }

    public void toggleSlidingPanel() {
        contentFragment.toggleSlidingPanel();
    }

    public static boolean isPlayerExpended(){
        return isPlayerExpended;
    }

    @Override
    public void onPanelSlide(final View panel, final float slideOffset) {
        if(slideOffset > .92){
            isEffectScreenExpended = true;
        }else{
            isEffectScreenExpended = false;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                contentFragment.onPanelSlide(panel, slideOffset, isEffectScreenExpended);
            }
        });
    }

    @Override
    public void onPanelCollapsed(final View panel) {
        isPlayerExpended = false;
        setDrawerUnlocked();
        handler.post(new Runnable() {
            @Override
            public void run() {
                contentFragment.onPanelCollapsed(panel);
            }
        });
    }

    @Override
    public void onPanelExpanded(final View panel) {
        isPlayerExpended = true;
        setDrawerLocked(isDrawerLocked);
        handler.post(new Runnable() {
            @Override
            public void run() {
                contentFragment.onPanelExpanded(panel);
            }
        });
    }

    @Override
    public void onPanelAnchored(final View panel) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                contentFragment.onPanelAnchored(panel);
            }
        });
    }

    @Override
    public void onPanelHidden(final View panel) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                contentFragment.onPanelHidden(panel);
            }
        });
    }
}
