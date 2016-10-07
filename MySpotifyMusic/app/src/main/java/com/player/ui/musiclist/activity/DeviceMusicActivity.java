package com.player.ui.musiclist.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.player.myspotifymusic.R;
import com.player.ui.musiclist.fragment.MusicLibraryListFragment;
import com.audio.player.customtab.HandyTabBar;
import com.audio.player.customtab.TabBarStyle;
import com.audio.player.customtab.tablayout.CustomTabLayout;

public class DeviceMusicActivity extends BoomMasterActivity{

    private ViewPager mViewPager;
    private HandyTabBar mTabBar;
    private ListPageAdapter mPageAdapter;
    private TabBarStyle mTabBarStyle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_Main);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_music_library);

        initView();
        initHandyTabBar();
    }

    private void initView() {
        mTabBar= (HandyTabBar) findViewById(R.id.tab_bar);
        mViewPager= (ViewPager) findViewById(R.id.view_pager);

        mPageAdapter=new ListPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPageAdapter);
        mViewPager.setOffscreenPageLimit(mPageAdapter.getCount() - 1);
        mViewPager.getCurrentItem();
    }

    private void initHandyTabBar() {
        mTabBarStyle=new TabBarStyle.Builder(this)
                .setDrawIndicator(TabBarStyle.INDICATOR_LINE)
                .setDrawDivider(false)
                .setDrawLine(TabBarStyle.NONELINE)
                .setIndicatorColorResource(android.R.color.holo_green_dark)
                .setlineColorResource(android.R.color.white)
                .build();
        setCustomTab();
    }

    private void setCustomTab(){
        int[] res=new int[]{R.drawable.ic_account_balance_white_24dp,
                R.drawable.ic_account_box_white_24dp,
                R.drawable.ic_event_white_24dp,
                R.drawable.ic_alarm_add_white_24dp ,
                R.drawable.ic_alarm_add_white_24dp};
        CustomTabLayout customTabLayout=new CustomTabLayout(res);
        mTabBar.attachToViewPager(mViewPager,customTabLayout,mTabBarStyle);
    }

    private class ListPageAdapter extends FragmentPagerAdapter {

        private String[] items={"Song","Album","PlayList", "Artist","Genre"};

        public ListPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            Log.d("Item No : ", "Item_"+i);
            return MusicLibraryListFragment.getInstance(0, items[i]);
        }

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return items[position];
        }
    }

    public void killActivity() {
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }

    @Override
    protected void onPause() {
        Log.d("DeviceMusicActivity", "Pause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("DeviceMusicActivity", "Destroy");
    }
}
