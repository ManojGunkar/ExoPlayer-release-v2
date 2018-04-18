package com.globaldelight.boom.radio.ui.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.R;
import com.globaldelight.boom.radio.ui.adapter.RadioFragmentStateAdapter;

import static android.content.Context.SEARCH_SERVICE;

/**
 * Created by Manoj Kumar on 09-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class RadioMainFragment extends Fragment {

    private RadioFragmentStateAdapter mStateAdapter;
    private TabLayout mTabBar;
    private ViewPager mViewPager;
    private Toolbar mToolbar;
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
        mStateAdapter = new RadioFragmentStateAdapter(getActivity().getSupportFragmentManager());
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
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        searchView = (SearchView)searchMenuItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
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
                 radioSearchFragment=new RadioSearchFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .add(R.id.fragment_container, radioSearchFragment)
                        .commitAllowingStateLoss();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .remove(radioSearchFragment)
                        .commitAllowingStateLoss();
                radioSearchFragment=null;
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                radioSearchFragment.updateResult(query);
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
