package com.globaldelight.boom.business;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by adarsh on 07/12/17.
 */

public class DemoModel implements BusinessModel {
    @Override
    public boolean isAdsEnabled() {
        return false;
    }

    @Override
    public void addItemsToDrawer(Menu menu, int groupId) {

    }

    @Override
    public void onDrawerItemClicked(MenuItem item, Context context) {

    }

}
