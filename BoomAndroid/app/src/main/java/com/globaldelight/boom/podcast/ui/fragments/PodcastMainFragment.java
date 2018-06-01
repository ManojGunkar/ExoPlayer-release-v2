package com.globaldelight.boom.podcast.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.fragments.TabBarFragment;
import com.globaldelight.boom.podcast.ui.Adapter.PodcastFragmentStateAdapter;
import com.globaldelight.boom.radio.ui.fragments.RadioSearchFragment;

/**
 * Created by Manoj Kumar on 01-06-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class PodcastMainFragment extends TabBarFragment {

    private PodcastFragmentStateAdapter mStateAdapter;
    private ViewPager mViewPager;
    private Activity mActivity;
    private float mToolbarElevation;
    private SearchView searchView;

    private RadioSearchFragment radioSearchFragment;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_radio, null, false);
        setHasOptionsMenu(true);
        initComp(view);
        return view;
    }

    private void initComp(View view) {
        mTabBar = view.findViewById(R.id.tab_radio);
        mViewPager = view.findViewById(R.id.viewpager_radio);
        setViewPager(mViewPager);
    }

    private void setViewPager(ViewPager viewPager) {
        mStateAdapter = new PodcastFragmentStateAdapter(getActivity().getSupportFragmentManager());
        viewPager.setAdapter(mStateAdapter);
        viewPager.setOffscreenPageLimit(4);
        mTabBar.setupWithViewPager(mViewPager);
        viewPager.setCurrentItem(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        mActivity = null;

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.library_menu, menu);


        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
