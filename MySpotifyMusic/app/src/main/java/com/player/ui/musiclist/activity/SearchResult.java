package com.player.ui.musiclist.activity;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.player.myspotifymusic.R;
import com.player.ui.musiclist.fragment.SearchResultFragment;
import com.audio.player.customtab.HandyTabBar;
import com.audio.player.customtab.TabBarStyle;
import com.audio.player.customtab.tablayout.CustomTabLayout;

/**
 * Created by Rahul Agarwal on 14-09-16.
 */
public class SearchResult extends BoomMasterActivity {

    private ViewPager mViewPager;
    private HandyTabBar mTabBar;
    private ListPageAdapter mPageAdapter;
    private TabBarStyle mTabBarStyle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        setToolbarTitle("Search Result");

        initView();
        initHandyTabBar();
    }

    private void initView() {
        mTabBar= (HandyTabBar) findViewById(R.id.search_result_tab_bar);
        mViewPager= (ViewPager) findViewById(R.id.search_result_view_pager);

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

        private String[] items={"Music", "GMusic", "Spotify", "Favourites", "PlayList"};

        public ListPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            Log.d("Item No : ", "Item_"+i);
            return SearchResultFragment.getInstance(0, items[i]);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        searchItem = menu.findItem(R.id.action_search);
        searchItem.expandActionView();

        searchItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                return false;
            }
        });

        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        registerSearchListeners();

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                SearchResult.this.finish();
                return true;
            }
        });

        return true;
    }

    private void registerSearchListeners() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
