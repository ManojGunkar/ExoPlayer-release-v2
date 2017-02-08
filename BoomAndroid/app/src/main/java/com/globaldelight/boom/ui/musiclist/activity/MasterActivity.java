package com.globaldelight.boom.ui.musiclist.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.globaldelight.boom.App;
import com.globaldelight.boom.business.client.IFBAddsUpdater;
import com.globaldelight.boom.business.client.IGoogleAddsUpdater;
import com.globaldelight.boom.business.BusinessUtils;
import com.globaldelight.boom.business.BusinessUtils.AddSource;
import com.globaldelight.boom.manager.BusinessRequestReceiver;
import com.globaldelight.boom.ui.musiclist.fragment.MasterContentFragment;
import com.globaldelight.boom.ui.widgets.slidinguppanel.SlidingUpPanelLayout;
import com.globaldelight.boom.R;
import com.globaldelight.boom.task.PlayerEvents;
import com.google.android.gms.ads.NativeExpressAdView;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.globaldelight.boom.business.BusinessUtils.AddSource.*;
import static com.globaldelight.boom.manager.BusinessRequestReceiver.ACTION_BUSINESS_CONFIGURATION;

/**
 * Created by Rahul Agarwal on 12-01-17.
 */

public class MasterActivity extends AppCompatActivity implements SlidingUpPanelLayout.PanelSlideListener, BusinessRequestReceiver.IUpdateBusinessRequest, IFBAddsUpdater, IGoogleAddsUpdater {
    private static final String TAG = "MasterActivity";

    private CoordinatorLayout activity;
    private LinearLayout activityContainer;
    private SlidingUpPanelLayout mSlidingPaneLayout;
    private MasterContentFragment contentFragment;
    private IPlayerSliderControl iPlayerSliderControl;
    private FragmentManager fragmentManager;
    private Handler handler;
    private static BusinessRequestReceiver businessRequestReceiver;
    private static ILibraryAddsUpdater iLibraryAddsUpdater;

    private static boolean isPlayerExpended = false;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        setTheme(R.style.MyTheme);
        activity = (CoordinatorLayout) getLayoutInflater().inflate(R.layout.activity_master, null);
        handler = new Handler();
        mSlidingPaneLayout = (SlidingUpPanelLayout) activity.findViewById(R.id.sliding_layout);
        activityContainer = (LinearLayout) activity.findViewById(R.id.activity_holder);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        fragmentManager = getSupportFragmentManager();
        contentFragment = new MasterContentFragment();
        initContainer();
        isPlayerExpended = mSlidingPaneLayout.isPanelExpanded();
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
        App.getBusinessHandler().setFBNativeAddListener(this);
        App.getBusinessHandler().setGoogleNativeAddListener(this);
        businessRequestReceiver = new BusinessRequestReceiver(this, businessRequestReceiver);
        registerReceiver(businessRequestReceiver, new IntentFilter(ACTION_BUSINESS_CONFIGURATION));
        super.onResume();
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
        handler.post(new Runnable() {
            @Override
            public void run() {
                iPlayerSliderControl.onPanelSlide(panel, slideOffset);
            }
        });
    }

    @Override
    public void onPanelCollapsed(final View panel) {
        isPlayerExpended = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                iPlayerSliderControl.onPanelCollapsed(panel);
            }
        });
    }

    @Override
    public void onPanelExpanded(final View panel) {
        isPlayerExpended = true;
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

    public static void setLibraryAddsUpdater(ILibraryAddsUpdater libraryAddsUpdater){
        iLibraryAddsUpdater = libraryAddsUpdater;
    }

    @Override
    public void onBusinessRequest(AddSource addSources, boolean libraryBannerEnable, boolean libraryVideoEnable) {
        if(libraryBannerEnable) {
            if (addSources == google) {
                App.getBusinessHandler().loadGoogleNativeAdd(addSources, libraryBannerEnable);
            } else {
                App.getBusinessHandler().loadFbNativeAdds(addSources, libraryBannerEnable);
            }
        }
        if(libraryVideoEnable){
            FacebookSdk.sdkInitialize(getApplicationContext());
            AppEventsLogger.activateApp(getApplicationContext());
            if (addSources == google) {
                App.getBusinessHandler().loadGoogleFullScreenAdds();
            } else {
                App.getBusinessHandler().loadFullScreenFbAdds();
            }
        }
    }

    @Override
    public void onLoadFBNativeAdds(BusinessUtils.AddSource addSources, boolean libraryBannerEnable, final LinearLayout fbNativeAddContainer) {
        LoadAdds(addSources, libraryBannerEnable, fbNativeAddContainer);
    }

    @Override
    public void onLoadGoogleNativeAdds(BusinessUtils.AddSource addSources, boolean libraryBannerEnable, final NativeExpressAdView googleAddView) {
        LoadAdds(addSources, libraryBannerEnable, googleAddView);
    }

    private void LoadAdds(final AddSource addSources, final boolean libraryBannerEnable, final View addView){
        handler.post(new Runnable() {
            @Override
            public void run() {
                iLibraryAddsUpdater.onAddsUpdate(addSources, libraryBannerEnable, addView);
            }
        });
    }

    public interface IPlayerSliderControl{
        void onPanelSlide(View panel, float slideOffset);
        void onPanelCollapsed(View panel);
        void onPanelExpanded(View panel);
        void onPanelAnchored(View panel);
        void onPanelHidden(View panel);
        void onResumeFragment(int alfa);
        void onVolumeUp();
        void onVolumeDown();
    }

    public interface ILibraryAddsUpdater{
        void onAddsUpdate(AddSource addSources, boolean isLibraryAddsEnable, View addContainer);
    }
}
