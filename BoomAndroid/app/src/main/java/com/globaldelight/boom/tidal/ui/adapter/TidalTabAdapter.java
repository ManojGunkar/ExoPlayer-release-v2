package com.globaldelight.boom.tidal.ui.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

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

    public TidalTabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {

            case 0:
                return new TidalNewFragment();
            case 1:
                return new TidalRisingFragment();
            case 2:
                return new TidalCuratedFragment();
            case 3:
                return new TidalMyMusicFragment();

        }
        return null;
    }



    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTabs[position];
    }
}
