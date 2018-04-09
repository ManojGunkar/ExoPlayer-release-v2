package com.globaldelight.boom.radio.ui;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.activities.MainActivity;
import com.globaldelight.boom.app.adapters.utils.SectionsPagerAdapter;
import com.globaldelight.boom.radio.ui.fragments.CountryFragment;
import com.globaldelight.boom.radio.ui.fragments.ExploreFragment;
import com.globaldelight.boom.radio.ui.fragments.FavouriteFragment;
import com.globaldelight.boom.radio.ui.fragments.LocalFragment;

/**
 * Created by Manoj Kumar on 09-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class RadioActivity extends AppCompatActivity {

    private SectionsPagerAdapter mPagerAdapter;
    private TabLayout mTabBar;
    private ViewPager mViewPager;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);
        initComp();
    }

    private void initComp() {
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("Radio");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mTabBar = findViewById(R.id.tab_radio);
        mViewPager = findViewById(R.id.viewpager_radio);
        setViewPager(mViewPager);
    }

    private void setViewPager(ViewPager viewPager) {
        mPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(this, new LocalFragment(), R.string.local_radio);
        mPagerAdapter.addFragment(this, new FavouriteFragment(), R.string.favourite_radio);
        mPagerAdapter.addFragment(this, new CountryFragment(), R.string.country_radio);
        mPagerAdapter.addFragment(this, new ExploreFragment(), R.string.explore_radio);
        viewPager.setAdapter(mPagerAdapter);
        viewPager.setOffscreenPageLimit(4);
        mTabBar.setupWithViewPager(mViewPager);
        viewPager.setCurrentItem(0);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.library_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) RadioActivity.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(RadioActivity.this.getComponentName()));
        }
        return super.onCreateOptionsMenu(menu);
    }
}
