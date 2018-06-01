package com.globaldelight.boom.podcast.ui.Adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.globaldelight.boom.podcast.ui.fragments.CountryPodcastFragment;
import com.globaldelight.boom.podcast.ui.fragments.ExplorePodcastFragment;
import com.globaldelight.boom.podcast.ui.fragments.FavouritePodcastFragment;
import com.globaldelight.boom.podcast.ui.fragments.LocalPodcastFragment;
import com.globaldelight.boom.podcast.ui.fragments.PopularPodcastFragment;

/**
 * Created by Manoj Kumar on 01-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class PodcastFragmentStateAdapter extends FragmentStatePagerAdapter {

    private String mTabTitle[] = new String[]{"Local", "Favourite", "Country", "Popular", "Explore"};


    public PodcastFragmentStateAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new LocalPodcastFragment();
            case 1:
                return new FavouritePodcastFragment();
            case 2:
                return new CountryPodcastFragment();
            case 3:
                return new PopularPodcastFragment();
            case 4:
                return new ExplorePodcastFragment();

        }
        return null;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitle[position];
    }

    @Override
    public int getCount() {
        return 5;
    }
}
