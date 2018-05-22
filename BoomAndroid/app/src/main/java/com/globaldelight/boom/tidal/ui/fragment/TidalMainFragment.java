package com.globaldelight.boom.tidal.ui.fragment;

import android.app.SearchManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.fragments.TabBarFragment;
import com.globaldelight.boom.tidal.ui.ContentLoadable;
import com.globaldelight.boom.tidal.ui.adapter.TidalTabAdapter;
import com.globaldelight.boom.tidal.utils.TidalHelper;

import static android.content.Context.SEARCH_SERVICE;

/**
 * Created by Manoj Kumar on 28-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalMainFragment extends TabBarFragment {

    private ViewPager mViewPager;
    private TidalTabAdapter mStateAdapter;
    private SearchView searchView;
    private TidalSearchFragment tidalSearch;


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
        setViewPager();
    }

    private void setViewPager() {
        mStateAdapter = new TidalTabAdapter(getContext(), getActivity().getSupportFragmentManager());
        mViewPager.setAdapter(mStateAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mTabBar.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ((ContentLoadable)mStateAdapter.getItem(position)).onLoadContent();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.library_menu, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        searchView = (SearchView)searchMenuItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_hint_tidal));
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(SEARCH_SERVICE);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        searchView.setLayoutParams(params);
        searchView.setDrawingCacheBackgroundColor(ContextCompat.getColor(getActivity(), R.color.transparent));
        searchView.setMaxWidth(2000);
        searchView.setIconified(true);
        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                tidalSearch=new TidalSearchFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .add(R.id.fragment_container, tidalSearch)
                        .commitAllowingStateLoss();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .remove(tidalSearch)
                        .commitAllowingStateLoss();
                tidalSearch=null;
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                tidalSearch.getSearchQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

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
    public void onResume() {
        super.onResume();

        // Post is necessary. Otherwise the method is called before the fragment is ready.
        new Handler().post(()-> ((ContentLoadable)mStateAdapter.getItem(mViewPager.getCurrentItem())).onLoadContent());
        TidalHelper.getInstance(getContext()).fetchSubscriptionInfo();
        TidalHelper.getInstance(getContext()).loadUserMusic();
    }

    @Override
    public void onStop() {
        super.onStop();
        ((ContentLoadable)mStateAdapter.getItem(mViewPager.getCurrentItem())).onStopLoading();
    }
}
