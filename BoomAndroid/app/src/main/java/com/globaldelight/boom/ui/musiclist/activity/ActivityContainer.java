package com.globaldelight.boom.ui.musiclist.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.globaldelight.boom.R;
import com.globaldelight.boom.ui.musiclist.fragment.SearchDetailFragment;
import com.globaldelight.boom.ui.musiclist.fragment.SettingFragment;
import com.globaldelight.boom.ui.musiclist.fragment.UpNextListFragment;
import com.globaldelight.boom.ui.widgets.RegularTextView;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class ActivityContainer extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        String container = getIntent().getStringExtra("container");
        initViews(savedInstanceState, container);
    }

    private void initViews(Bundle savedInstanceState, String container) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ((RegularTextView) findViewById(R.id.toolbar_txt)).setText(container.equals("upnext") ?
                getResources().getString(R.string.title_playingque) : getResources().getString(R.string.title_settings));

        findViewById(R.id.fab).setVisibility(View.GONE);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }
        addFragment(savedInstanceState, container);
    }

    private void addFragment(Bundle savedInstanceState, String container) {
        if (savedInstanceState == null) {
            if(container.equals("upnext")) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, new UpNextListFragment())
                        .commit();
            }else{
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.item_detail_container, new SettingFragment())
                        .commitAllowingStateLoss();
            }
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

