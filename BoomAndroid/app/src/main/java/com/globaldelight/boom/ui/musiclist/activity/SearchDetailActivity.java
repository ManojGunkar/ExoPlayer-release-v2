package com.globaldelight.boom.ui.musiclist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.globaldelight.boom.R;
import com.globaldelight.boom.ui.musiclist.fragment.SearchDetailFragment;
import com.globaldelight.boom.ui.widgets.RegularTextView;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }

        mSearchType = getIntent().getStringExtra(SearchDetailFragment.ARG_LIST_TYPE);
        mQuery = getIntent().getStringExtra(SearchDetailFragment.ARG_MEDIA_QUERY);

        ((RegularTextView) findViewById(R.id.toolbar_txt)).setText(mSearchType);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        registerPlayerReceiver(SearchDetailActivity.this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterPlayerReceiver(SearchDetailActivity.this);
        super.onPause();
    }
}

