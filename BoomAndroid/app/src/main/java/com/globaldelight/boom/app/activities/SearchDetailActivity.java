package com.globaldelight.boom.app.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.fragments.SearchDetailFragment;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class SearchDetailActivity extends MasterActivity {

    private String mQuery, mSearchType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        initViews(savedInstanceState);
    }

    private void initViews(Bundle savedInstanceState) {
        setDrawerLocked(true);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mSearchType = getIntent().getStringExtra(SearchDetailFragment.ARG_LIST_TYPE);
        mQuery = getIntent().getStringExtra(SearchDetailFragment.ARG_MEDIA_QUERY);

        setTitle(mSearchType);
        
        findViewById(R.id.fab).setVisibility(View.GONE);


        addSearchDetailFragment(savedInstanceState);
    }

    private void addSearchDetailFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(SearchDetailFragment.ARG_LIST_TYPE, mSearchType);
            arguments.putString(SearchDetailFragment.ARG_MEDIA_QUERY, mQuery);
            SearchDetailFragment mFragment = new SearchDetailFragment();
            mFragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, mFragment)
                    .commit();
        }
    }
}

