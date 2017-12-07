package com.globaldelight.boom.business;

import android.view.Menu;

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
}
