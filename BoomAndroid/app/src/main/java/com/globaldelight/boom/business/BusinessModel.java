package com.globaldelight.boom.business;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by adarsh on 07/12/17.
 */

// Business model interface
public interface BusinessModel {

    String ACTION_ADS_STATUS_CHANGED = "com.globaldelight.boom.ADS_STATUS_CHANGED";

    boolean isAdsEnabled();

    void addItemsToDrawer(Menu menu, int groupId);
}
