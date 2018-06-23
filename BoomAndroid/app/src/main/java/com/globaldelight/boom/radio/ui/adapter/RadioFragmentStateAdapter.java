package com.globaldelight.boom.radio.ui.adapter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.globaldelight.boom.radio.ui.fragments.CountryFragment;
import com.globaldelight.boom.radio.ui.fragments.ExploreFragment;
import com.globaldelight.boom.radio.ui.fragments.FavouriteFragment;
import com.globaldelight.boom.radio.ui.fragments.LocalFragment;
import com.globaldelight.boom.radio.ui.fragments.PopularFragment;

/**
 * Created by Manoj Kumar on 16-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class RadioFragmentStateAdapter extends FragmentStatePagerAdapter {

    public static final String KEY_TYPE = "KEY_TYPE";
    private String mTabTitle[] = new String[]{"Local", "Favourite", "Country", "Popular", "Explore"};
    private String mType;

    public RadioFragmentStateAdapter(FragmentManager fm, String type) {
        super(fm);
        this.mType = type;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TYPE, mType);

        switch (position) {
            case 0:
                fragment = new LocalFragment();
                fragment.setArguments(bundle);
                return fragment;
            case 1:
                fragment = new FavouriteFragment();
                fragment.setArguments(bundle);
                return fragment;
            case 2:
                fragment = new CountryFragment();
                fragment.setArguments(bundle);
                return fragment;
            case 3:
                fragment = new PopularFragment();
                fragment.setArguments(bundle);
                return fragment;
            case 4:
                fragment = new ExploreFragment();
                fragment.setArguments(bundle);
                return fragment;


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
        if (mType.equalsIgnoreCase("podcast"))
            return 4;
        return 5;
    }
}
