package com.globaldelight.boom.tidal.ui.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.globaldelight.boom.tidal.ui.ContentLoadable;
import com.globaldelight.boom.tidal.ui.fragment.TidalCuratedFragment;
import com.globaldelight.boom.tidal.ui.fragment.TidalMyMusicFragment;
import com.globaldelight.boom.tidal.ui.fragment.TidalNewFragment;
import com.globaldelight.boom.tidal.ui.fragment.TidalRisingFragment;

/**
 * Created by Manoj Kumar on 28-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalTabAdapter extends FragmentStatePagerAdapter {

    private final String[] mTabs = new String[]{"NEW", "TIDAL RISING", "CURATED", "MY MUSIC"};
    private Fragment[] tabFragments = {
            new TidalNewFragment(),
            new TidalRisingFragment(),
            new TidalCuratedFragment(),
            new TidalMyMusicFragment()
    };

    public TidalTabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return tabFragments[position];
    }



    @Override
    public int getCount() {
        return tabFragments.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTabs[position];
    }
}
