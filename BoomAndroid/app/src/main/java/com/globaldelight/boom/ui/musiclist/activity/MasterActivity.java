package com.globaldelight.boom.ui.musiclist.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.globaldelight.boom.ui.musiclist.fragment.MasterContentFragment;
import com.globaldelight.boom.ui.widgets.slidinguppanel.SlidingUpPanelLayout;
import com.globaldelight.boom.R;
import com.globaldelight.boom.task.PlayerEvents;
import com.surveymonkey.surveymonkeyandroidsdk.SurveyMonkey;

/**
 * Created by Rahul Agarwal on 12-01-17.
 */

public class MasterActivity extends AppCompatActivity implements SlidingUpPanelLayout.PanelSlideListener{
    private static final String TAG = "MasterActivity";
    private static final float BITMAP_SCALE = 0.4f;
    private static final float BLUR_RADIUS = 25.0f;
    final static String SURVEY_HASH = "PTXJR5S";
    final static int SURVEY_REQUEST_CODE = 2000;

    private final static long MINS = 60 * 1000;
    private final static long DAYS = 24 * 60 * MINS;
    private final static long FEEDBACK_TIME_LIMIT = 1 * 60 * MINS;
    private final static long DECLINE_TIME_LIMIT = 2 * 60 *  MINS;
    private final static long ACCEPT_TIME_LIMIT = 3 * 60 *  MINS;

    private SurveyMonkey surveyInstance = new SurveyMonkey();
    private boolean surveyInProgress = false;

    private FrameLayout activity;
    private LinearLayout activityContainer;
    private SlidingUpPanelLayout mSlidingPaneLayout;
    private MasterContentFragment contentFragment;
    private IPlayerSliderControl iPlayerSliderControl;
    private FragmentManager fragmentManager;
    private Handler handler;

    private static boolean isPlayerExpended = false;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        activity = (FrameLayout) getLayoutInflater().inflate(R.layout.activity_master, null);
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
                    return true;
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
                    return true;
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
}
