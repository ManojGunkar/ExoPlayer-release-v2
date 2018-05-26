package com.globaldelight.boom.app.fragments;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.sharedPreferences.Preferences;

import static com.globaldelight.boom.app.sharedPreferences.Preferences.LIBRARY_CURRENT_TAB;


/**
 * Created by adarsh on 11/05/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class TabBarFragment extends Fragment {
    private float mToolbarElevation;
    protected TabLayout mTabBar;

    @Override
    public void onStart() {
        super.onStart();

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        mToolbarElevation = toolbar.getElevation();
        mTabBar.setElevation(mToolbarElevation);
        toolbar.setElevation(0);
    }

    @Override
    public void onStop() {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setElevation(mToolbarElevation);

        super.onStop();
    }

}
