package com.globaldelight.boom.business;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import com.globaldelight.boom.business.ads.Advertiser;

/**
 * Created by adarsh on 07/12/17.
 */

// Business model interface
public interface BusinessModel {

    String ACTION_ADS_STATUS_CHANGED = "com.globaldelight.boom.ADS_STATUS_CHANGED";

    default boolean isAdsEnabled() {
        return false;
    }

    default Advertiser getAdFactory() {
        return null;
    }


    default void addItemsToDrawer(Menu menu, int groupId) {

    }

    default void onDrawerItemClicked(MenuItem item, Context context) {

    }

    default EffectsScreenPolicy createEffectsScreenPolicy() {
        return null;
    }
}
