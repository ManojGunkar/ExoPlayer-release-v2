package com.globaldelight.boom.radio.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.adapters.utils.SectionsPagerAdapter;
import com.globaldelight.boom.radio.ui.fragments.CountryFragment;
import com.globaldelight.boom.radio.ui.fragments.ExploreFragment;
import com.globaldelight.boom.radio.ui.fragments.FavouriteFragment;
import com.globaldelight.boom.radio.ui.fragments.LocalFragment;

/**
 * Created by Manoj Kumar on 09-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class RadioMainFragment extends Fragment {

    private SectionsPagerAdapter mPagerAdapter;
    private TabLayout mTabBar;
    private ViewPager mViewPager;
    private Toolbar mToolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_radio, null, false);
        initComp(view);
        return view;
    }

    private void initComp(View view) {
        mToolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mToolbar);
        mTabBar = view.findViewById(R.id.tab_radio);
        mViewPager = view.findViewById(R.id.viewpager_radio);
        setViewPager(mViewPager);
    }

    private void setViewPager(ViewPager viewPager) {
        mPagerAdapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager());
        mPagerAdapter.addFragment(getActivity(), new LocalFragment(), R.string.local_radio);
        mPagerAdapter.addFragment(getActivity(), new FavouriteFragment(), R.string.favourite_radio);
        mPagerAdapter.addFragment(getActivity(), new CountryFragment(), R.string.country_radio);
        mPagerAdapter.addFragment(getActivity(), new ExploreFragment(), R.string.explore_radio);
        viewPager.setAdapter(mPagerAdapter);
        viewPager.setOffscreenPageLimit(4);
        mTabBar.setupWithViewPager(mViewPager);
        viewPager.setCurrentItem(0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.library_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

    }


}
