package com.globaldelight.boom.tidal.ui.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.globaldelight.boom.tidal.ui.fragment.TidalGenresFragment;
import com.globaldelight.boom.tidal.ui.fragment.TidalMyMusicFragment;
import com.globaldelight.boom.tidal.ui.fragment.TidalNewFragment;
import com.globaldelight.boom.tidal.ui.fragment.TidalPlaylistFragment;
import com.globaldelight.boom.tidal.ui.fragment.TidalRisingFragment;

/**
 * Created by Manoj Kumar on 28-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalTabAdapter extends FragmentStatePagerAdapter {

    private final String[] mTabs=new String[]{"NEW","TIDAL Rising","PLAYLISTS","GENRES","MY MUSIC"};

    public TidalTabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){

            case 0:
                return new TidalNewFragment();
            case 1:
                return  new TidalRisingFragment();
            case 2:
                return new TidalPlaylistFragment();
            case 3:
                return new TidalGenresFragment();
            case 4:
                return new TidalMyMusicFragment();

        }
        return null;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTabs[position];
    }
}
