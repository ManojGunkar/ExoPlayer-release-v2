package com.globaldelight.boom.ui.musiclist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.globaldelight.boom.R;
import com.globaldelight.boom.business.inapp.IabHelper;
import com.globaldelight.boom.ui.musiclist.fragment.AboutFragment;
import com.globaldelight.boom.ui.musiclist.fragment.SettingFragment;
import com.globaldelight.boom.ui.musiclist.fragment.StoreFragment;
import com.globaldelight.boom.ui.musiclist.fragment.UpNextListFragment;
import com.globaldelight.boom.ui.widgets.RegularTextView;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class ActivityContainer extends AppCompatActivity {
    private Toolbar toolbar;
    private int container;
    private Fragment mFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        container = getIntent().getIntExtra("container", R.string.title_about);
        initViews();
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(container == R.string.store_title)
            toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);
        ((RegularTextView) findViewById(R.id.toolbar_txt)).setText(getResources().getString(container));

        findViewById(R.id.fab).setVisibility(View.GONE);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }
        addFragment();
    }

    private void addFragment() {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        switch (container){
            case R.string.store_title:
                getMenuInflater().inflate(R.menu.store_menu, menu);
                /*menu.findItem(R.id.popup_store_restore).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);*/
                break;
            default:
                break;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.popup_store_restore:
                ((StoreFragment)mFragment).restorePurchase();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != mFragment && mFragment instanceof  StoreFragment &&
                !((StoreFragment) mFragment).getPurchaseHelper().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != mFragment && mFragment instanceof  StoreFragment) {
            IabHelper mHelper = ((StoreFragment) mFragment).getPurchaseHelper();
            if (mHelper != null) try {
                mHelper.dispose();
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }
    }
}

