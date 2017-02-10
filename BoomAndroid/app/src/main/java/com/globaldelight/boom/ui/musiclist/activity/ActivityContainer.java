package com.globaldelight.boom.ui.musiclist.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.globaldelight.boom.R;
import com.globaldelight.boom.ui.musiclist.fragment.AboutFragment;
import com.globaldelight.boom.ui.musiclist.fragment.SettingFragment;
import com.globaldelight.boom.ui.musiclist.fragment.StoreFragment;
import com.globaldelight.boom.ui.musiclist.fragment.UpNextListFragment;
import com.globaldelight.boom.ui.widgets.RegularTextView;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class ActivityContainer extends AppCompatActivity {
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        int container = getIntent().getIntExtra("container", R.string.title_about);
        initViews(savedInstanceState, container);
    }

    private void initViews(Bundle savedInstanceState, int container) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ((RegularTextView) findViewById(R.id.toolbar_txt)).setText(getResources().getString(container));

        findViewById(R.id.fab).setVisibility(View.GONE);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }
        addFragment(container);
    }

    private void addFragment(int container) {
        Fragment mFragment = null;
        toolbar.setVisibility(View.VISIBLE);
        switch (container){
            case R.string.title_upnext:
                mFragment =  new UpNextListFragment();
                break;
            case R.string.title_settings:
                mFragment =  new SettingFragment();
                break;
            case R.string.store_title:
                mFragment =  new StoreFragment();
                break;
            case R.string.header_about:
                mFragment =  new AboutFragment();
                break;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.item_detail_container, mFragment)
                .commitAllowingStateLoss();
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

