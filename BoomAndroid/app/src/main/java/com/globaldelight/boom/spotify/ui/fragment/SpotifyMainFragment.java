package com.globaldelight.boom.spotify.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.fragments.TabBarFragment;
import com.globaldelight.boom.spotify.ui.SpotifyLoginActivity;
import com.globaldelight.boom.spotify.ui.adapter.SpotifyTabAdapter;
import com.globaldelight.boom.tidal.ui.ContentLoadable;

/**
 * Created by Manoj Kumar on 19-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class SpotifyMainFragment extends TabBarFragment {

    private ViewPager mViewPager;
    private SpotifyTabAdapter mTabAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spotify_main, container, false);
        getActivity().startActivity(new Intent(getContext(), SpotifyLoginActivity.class));
        initComp(view);
        return view;
    }

    private void initComp(View view) {
        mTabBar = view.findViewById(R.id.tab_spotify);
        mViewPager = view.findViewById(R.id.viewpager_spotify);
        setViewPager();
    }

    private void setViewPager() {
        mTabAdapter = new SpotifyTabAdapter(getContext(), getActivity().getSupportFragmentManager());
        mViewPager.setAdapter(mTabAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mTabBar.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ((ContentLoadable) mTabAdapter.getItem(position)).onLoadContent();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
