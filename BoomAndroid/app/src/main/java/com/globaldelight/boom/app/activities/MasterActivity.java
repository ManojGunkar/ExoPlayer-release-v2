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
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.app.analytics.UtilAnalytics;
import com.globaldelight.boom.business.client.IFBAddsUpdater;
import com.globaldelight.boom.business.client.IGoogleAddsUpdater;
import com.globaldelight.boom.business.BusinessUtils;
import com.globaldelight.boom.business.BusinessUtils.AddSource;
import com.globaldelight.boom.app.receivers.BusinessRequestReceiver;
import com.globaldelight.boom.app.fragments.MasterContentFragment;
import com.globaldelight.boom.view.slidinguppanel.SlidingUpPanelLayout;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.receivers.actions.PlayerEvents;
import com.google.android.gms.ads.NativeExpressAdView;
import java.util.Timer;
import java.util.TimerTask;
import static com.globaldelight.boom.business.BusinessUtils.AddSource.*;
import static com.globaldelight.boom.app.receivers.BusinessRequestReceiver.ACTION_BUSINESS_APP_EXPIRE;
import static com.globaldelight.boom.app.receivers.BusinessRequestReceiver.ACTION_BUSINESS_CONFIGURATION;

/**
 * Created by Rahul Agarwal on 12-01-17.
 */

public class MasterActivity extends AppCompatActivity implements SlidingUpPanelLayout.PanelSlideListener {
    private static final String TAG = "MasterActivity";

    private FrameLayout activity;
    public DrawerLayout drawerLayout;
    private LinearLayout activityContainer;
    private SlidingUpPanelLayout mSlidingPaneLayout;
    private MasterContentFragment contentFragment;
    private IPlayerSliderControl iPlayerSliderControl;
    private FragmentManager fragmentManager;
    private Handler handler;
    private static BusinessRequestReceiver businessRequestReceiver;

    private boolean isDrawerLocked = false;
    private static boolean isPlayerExpended = false, isEffectScreenExpended = false;

    private static final long FIFTEEN_MINUTES = 15 * 60 * 1000;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        activity = (FrameLayout) getLayoutInflater().inflate(R.layout.activity_master, null);
        drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        //make volume keys change multimedia volume even if music is not playing now
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        handler = new Handler();
        mSlidingPaneLayout = (SlidingUpPanelLayout) activity.findViewById(R.id.sliding_layout);
        activityContainer = (LinearLayout) activity.findViewById(R.id.activity_holder);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        fragmentManager = getSupportFragmentManager();
        contentFragment = new MasterContentFragment();
        initContainer();
        isPlayerExpended = mSlidingPaneLayout.isPanelExpanded();
        FlurryAnalyticHelper.init(this);
        super.setContentView(activity);
    }

    BroadcastReceiver mPlayerSliderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case PlayerEvents.ACTION_TOGGLE_PLAYER_SLIDE:
                    if(mSlidingPaneLayout.isPanelExpanded())
                        mSlidingPaneLayout.collapsePanel();
                    else
                        mSlidingPaneLayout.expandPanel();
                break;
            }
        }
    };

    public void registerPlayerReceiver(Context context){
        contentFragment.registerPlayerReceiver(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerEvents.ACTION_TOGGLE_PLAYER_SLIDE);
        context.registerReceiver(mPlayerSliderReceiver, intentFilter);
    }

    public void unregisterPlayerReceiver(Context context){
        contentFragment.unregisterPlayerReceiver(context);
        unregisterReceiver(mPlayerSliderReceiver);
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
    protected void onResumeFragments() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                iPlayerSliderControl.onResumeFragment(mSlidingPaneLayout.isPanelExpanded() ? 1 : 0);
            }
        });
        super.onResumeFragments();
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
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            iPlayerSliderControl.onVolumeUp();
                        }
                    });
                    return false;
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (mSlidingPaneLayout.isPanelExpanded()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            iPlayerSliderControl.onVolumeDown();
                        }
                    });
                    return false;
                }
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        isPlayerExpended = mSlidingPaneLayout.isPanelExpanded();
        try{
            App.startPlayerService();
        }catch (Exception e){

        }
        initBusinessReceiver();
        super.onResume();
    }


    @Override
    public  void onStart() {
        super.onStart();
        FlurryAnalyticHelper.flurryStartSession(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAnalyticHelper.flurryStopSession(this);
    }



    private void initBusinessReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_BUSINESS_APP_EXPIRE);
        filter.addAction(ACTION_BUSINESS_CONFIGURATION);
        registerReceiver(businessRequestReceiver, filter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(businessRequestReceiver);
        super.onPause();
    }

    public void initContainer() {
        if(!mSlidingPaneLayout.isPanelExpanded()) {

            iPlayerSliderControl = contentFragment.getPlayerSliderControl();

            fragmentManager.beginTransaction()
                    .replace(R.id.panel_holder, contentFragment).commitAllowingStateLoss();
            mSlidingPaneLayout.setPanelSlideListener(MasterActivity.this);
        }
    }

    public void setStatusBarColor(int statusBarColor) {
//        getWindow().setStatusBarColor(statusBarColor);
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
                iPlayerSliderControl.onPanelSlide(panel, slideOffset, isEffectScreenExpended);
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
                iPlayerSliderControl.onPanelCollapsed(panel);
            }
        });
        FlurryAnalyticHelper.logEvent(UtilAnalytics.PLayer_Screen_Expanded_From_Mini_Player);
    }

    @Override
    public void onPanelExpanded(final View panel) {
        isPlayerExpended = true;
        setDrawerLocked(isDrawerLocked);
        handler.post(new Runnable() {
            @Override
            public void run() {
                iPlayerSliderControl.onPanelExpanded(panel);
            }
        });
    }

    @Override
    public void onPanelAnchored(final View panel) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                iPlayerSliderControl.onPanelAnchored(panel);
            }
        });
    }

    @Override
    public void onPanelHidden(final View panel) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                iPlayerSliderControl.onPanelHidden(panel);
            }
        });
    }

    public interface IPlayerSliderControl{
        void onPanelSlide(View panel, float slideOffset, boolean isEffectOpened);
        void onPanelCollapsed(View panel);
        void onPanelExpanded(View panel);
        void onPanelAnchored(View panel);
        void onPanelHidden(View panel);
        void onResumeFragment(int alfa);
        void onVolumeUp();
        void onVolumeDown();
    }
}
