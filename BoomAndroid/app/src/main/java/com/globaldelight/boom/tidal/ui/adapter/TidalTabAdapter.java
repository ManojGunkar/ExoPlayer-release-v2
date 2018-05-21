package com.globaldelight.boom.tidal.ui.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.globaldelight.boom.R;
import com.globaldelight.boom.tidal.ui.fragment.TidalCuratedFragment;
import com.globaldelight.boom.tidal.ui.fragment.TidalMyMusicFragment;
import com.globaldelight.boom.tidal.ui.fragment.TidalNewFragment;
import com.globaldelight.boom.tidal.ui.fragment.TidalRisingFragment;

/**
 * Created by Manoj Kumar on 28-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalTabAdapter extends FragmentStatePagerAdapter {

    private final int[] mTabs = new int[]{
            R.string.tidal_new_tab,
            R.string.tidal_rising_tab,
            R.string.tidal_curated_tab,
            R.string.tidal_mymusic_tab
    };

    private Fragment[] mTabFragments = {
            new TidalNewFragment(),
            new TidalRisingFragment(),
            new TidalCuratedFragment(),
            new TidalMyMusicFragment()
    };

    private Context mContext;

    public TidalTabAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        return mTabFragments[position];
    }



    @Override
    public int getCount() {
        return mTabFragments.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(mTabs[position]);
    }
}
