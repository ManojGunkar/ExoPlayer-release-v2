package com.globaldelight.boom.app.adapters.utils;

/**
 * Created by Rahul Agarwal on 12-01-17.
 */

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragments = new ArrayList<>();
    private final List<Integer> mFragmentTitles = new ArrayList<>();
    private Context mContext;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Activity mActivity, Fragment fragment, int title) {
        mFragments.add(fragment);
        mFragmentTitles.add(title);
        mContext = mActivity;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(mFragmentTitles.get(position));
    }


}
