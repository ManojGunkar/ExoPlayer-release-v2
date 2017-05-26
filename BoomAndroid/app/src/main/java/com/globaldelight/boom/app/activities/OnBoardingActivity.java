package com.globaldelight.boom.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.app.analytics.UtilAnalytics;
import com.globaldelight.boom.view.RegularButton;
import com.globaldelight.boom.view.RegularTextView;
import com.globaldelight.boom.view.onBoarding.PagerAdapter;
import com.globaldelight.boom.view.onBoarding.CircleIndicator;
import com.globaldelight.boom.app.sharedPreferences.Preferences;

/**
 * Created by manoj on 13/2/17.
 */

public class OnBoardingActivity extends Activity implements View.OnClickListener {

    private RegularTextView txtSkip, txtNext;
    private RegularButton startBoom;
    private ViewPager viewpager;
    private CircleIndicator indicator;
    LinearLayout bottomPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        initComp();
        FlurryAnalyticHelper.init(this);
        FlurryAnalyticHelper.logEvent(UtilAnalytics.Started_OnBoarding);
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position==2){
                    startBoom.setVisibility(View.VISIBLE);
                    bottomPanel.setVisibility(View.GONE);
                    txtSkip.setVisibility(View.INVISIBLE);
                    txtNext.setVisibility(View.INVISIBLE);
                }else {
                    startBoom.setVisibility(View.GONE);
                    bottomPanel.setVisibility(View.VISIBLE);
                    txtSkip.setVisibility(View.VISIBLE);
                    txtNext.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initComp() {
        txtSkip = (RegularTextView) findViewById(R.id.txt_skip_onboard);
        txtNext = (RegularTextView) findViewById(R.id.txt_next_onboard);
        startBoom = (RegularButton) findViewById(R.id.btn_boomin_onboard);

        viewpager = (ViewPager) findViewById(R.id.pager_home);
        indicator = (CircleIndicator) findViewById(R.id.indicator_home);
        viewpager.setAdapter(new PagerAdapter(this));
        indicator.setViewPager(viewpager);
        viewpager.setCurrentItem(0);
        txtSkip.setOnClickListener(this);
        txtNext.setOnClickListener(this);
        startBoom.setOnClickListener(this);
        bottomPanel = (LinearLayout) findViewById(R.id.onboarding_bottom) ;
    }

    private void jumpToHome() {
        Preferences.writeBoolean(OnBoardingActivity.this, Preferences.ACTION_ONBOARDING_SHOWN, false);
        Intent i = new Intent(OnBoardingActivity.this, MainActivity.class);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_boomin_onboard:
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Completed_Onboarding);
                if (Preferences.readBoolean(this, Preferences.ON_BOARDING_COMPLETED_ON_FIRST_ATTEMPT, true)) {
                    FlurryAnalyticHelper.logEvent(UtilAnalytics.OnBoarding_Completed_on_First_Attempt);
                }
            case R.id.txt_skip_onboard:
                jumpToHome();
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Completed_Onboarding_by_Tapping_Skip);
                break;

            case R.id.txt_next_onboard:
                jumpToNext();
                break;
        }
    }

    private void jumpToNext() {
        int position = viewpager.getCurrentItem();
        viewpager.setCurrentItem(position + 1, true);
        if (position>=1){
            txtSkip.setVisibility(View.INVISIBLE);
            txtNext.setVisibility(View.INVISIBLE);
        }else {
            txtSkip.setVisibility(View.VISIBLE);
            txtNext.setVisibility(View.VISIBLE);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Preferences.writeBoolean(this, Preferences.ON_BOARDING_COMPLETED_ON_FIRST_ATTEMPT, false);
    }

}