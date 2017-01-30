package com.globaldelight.boom.ui.musiclist.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.globaldelight.boom.R;
import com.globaldelight.boom.ui.musiclist.fragment.SearchDetailFragment;
import com.globaldelight.boom.ui.musiclist.fragment.UpNextListFragment;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class UpNextActivity extends AppCompatActivity {

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

        mSearchType = getIntent().getStringExtra(SearchDetailFragment.ARG_LIST_TYPE);
        mQuery = getIntent().getStringExtra(SearchDetailFragment.ARG_MEDIA_QUERY);

        findViewById(R.id.fab).setVisibility(View.GONE);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.title_playingque));
        }
        addUpNextFragment(savedInstanceState);
    }

    private void addUpNextFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, new UpNextListFragment())
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
    public void onBackPressed() {
        super.onBackPressed();
    }
}

