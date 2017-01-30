package com.globaldelight.boom.ui.musiclist.activity;

/**
 * Created by Rahul Agarwal on 12-01-17.
 */

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.ui.musiclist.fragment.MusicLibraryListFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private int[] items;
    private Context mContext;
    public SectionsPagerAdapter(Context mContext, FragmentManager fm, int[] items) {
        super(fm);
        this.mContext = mContext;
        this.items = items;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a aa_PlaceholderFragment (defined as a static inner class below).
        return MusicLibraryListFragment.getInstance(0, items[position]);
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(items[position]);
    }
}
