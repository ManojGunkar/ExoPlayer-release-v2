package com.globaldelight.boom.ui.musiclist.activity;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.LinearLayout;

import com.globaldelight.boom.ui.widgets.MusicListTabs.MusicTabBar;
import com.globaldelight.boom.ui.widgets.MusicListTabs.MusicTabLayout;
import com.globaldelight.boom.ui.widgets.MusicListTabs.TabBarStyle;
import com.globaldelight.boom.R;
import com.globaldelight.boom.ui.musiclist.fragment.MusicLibraryListFragment;
import com.globaldelight.boom.utils.PermissionChecker;

public class DeviceMusicActivity extends BoomMasterActivity{

    private ViewPager mViewPager;
    private PermissionChecker permissionChecker;
    private MusicTabBar mTabBar;
    private ListPageAdapter mPageAdapter;
    private TabBarStyle mTabBarStyle;
    private LinearLayout mContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_left, R.anim.stay_out);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_music_library);

        checkPermissions();
    }

    private void initView() {

        mContainer = (LinearLayout)findViewById(R.id.music_library_container);
        mTabBar= (MusicTabBar) findViewById(R.id.tab_bar);
        mViewPager= (ViewPager) findViewById(R.id.view_pager);

        mPageAdapter=new ListPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPageAdapter);
        mViewPager.setOffscreenPageLimit(mPageAdapter.getCount() - 1);
        mViewPager.getCurrentItem();

        initHandyTabBar();
    }

    private void initHandyTabBar() {
        mTabBarStyle=new TabBarStyle.Builder(this)
                .setDrawIndicator(TabBarStyle.INDICATOR_LINE)
                .setDrawDivider(false)
                .setDrawLine(TabBarStyle.NONELINE)
                .setIndicatorColorResource(android.R.color.white)
                .setlineColorResource(android.R.color.white)
                .build();
        setCustomTab();
    }

    private void setCustomTab(){
        int[] res=new int[]{R.drawable.library_songs_normal,
                R.drawable.library_albums_normal,
                R.drawable.library_artists_normal,
                R.drawable.library_playlists_normal ,
                R.drawable.library_genres_normal};

        int[] selected_res=new int[]{R.drawable.library_songs_active,
                R.drawable.library_albums_active,
                R.drawable.library_artists_active,
                R.drawable.library_playlists_active ,
                R.drawable.library_genres_active};

        MusicTabLayout customTabLayout=new MusicTabLayout(res, selected_res);
        mTabBar.attachToViewPager(mViewPager,customTabLayout,mTabBarStyle);
    }

    private class ListPageAdapter extends FragmentPagerAdapter {

        private int[] items={R.string.songs, R.string.albums, R.string.artists, R.string.playlists, R.string.genres};

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
            return getResources().getString(items[position]);
        }
    }

    public void killActivity() {
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.stay_out, R.anim.slide_out_left);
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

    private void checkPermissions() {
        permissionChecker = new PermissionChecker(this, DeviceMusicActivity.this, mContainer);
        permissionChecker.check(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getResources().getString(R.string.storage_permission),
                new PermissionChecker.OnPermissionResponse() {
                    @Override
                    public void onAccepted() {
                        initView();
                    }

                    @Override
                    public void onDecline() {
                        finish();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void setPermissionChecker(PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
    }
}
