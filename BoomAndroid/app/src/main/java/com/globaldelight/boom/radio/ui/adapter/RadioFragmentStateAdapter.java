package com.globaldelight.boom.radio.ui.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.globaldelight.boom.radio.ui.fragments.CountryFragment;
import com.globaldelight.boom.radio.ui.fragments.ExploreFragment;
import com.globaldelight.boom.radio.ui.fragments.FavouriteFragment;
import com.globaldelight.boom.radio.ui.fragments.LocalFragment;

/**
 * Created by Manoj Kumar on 16-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class RadioFragmentStateAdapter extends FragmentStatePagerAdapter {

    private String mTabTitle[] = new String[]{"Local", "Favourite", "Country","Explore"};


    public RadioFragmentStateAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new LocalFragment();
            case 1:
                return new FavouriteFragment();
                case 2:
                return new CountryFragment();
            case 3:
                return new ExploreFragment();

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
        return 4;
    }
}
