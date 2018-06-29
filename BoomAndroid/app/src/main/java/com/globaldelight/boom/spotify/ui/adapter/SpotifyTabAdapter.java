package com.globaldelight.boom.spotify.ui.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.globaldelight.boom.R;
import com.globaldelight.boom.spotify.ui.fragment.SpotifyCuratedFragment;
import com.globaldelight.boom.spotify.ui.fragment.SpotifyLatestFragment;
import com.globaldelight.boom.spotify.ui.fragment.SpotifyMyMusicFragment;

/**
 * Created by Manoj Kumar on 29-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class SpotifyTabAdapter extends FragmentStatePagerAdapter {

    private Context mContext;

    private final int[] mTabs = new int[]{
            R.string.spotify_new_tab,
            R.string.spotify_curated_tab,
            R.string.spotify_mymusic_tab
    };

    private Fragment[] mTabFragments = {
            new SpotifyLatestFragment(),
            new SpotifyCuratedFragment(),
            new SpotifyMyMusicFragment()
    };


    public SpotifyTabAdapter(Context context, FragmentManager fm) {
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
