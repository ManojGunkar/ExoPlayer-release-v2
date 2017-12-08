package com.globaldelight.boom.business;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.Menu;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.activities.MasterActivity;
import com.globaldelight.boom.utils.DefaultActivityLifecycleCallbacks;

/**
 * Created by adarsh on 08/12/17.
 */

public class GDPLStoreModel implements BusinessModel {

    private Context mContext;

    private Application.ActivityLifecycleCallbacks mLifecycle = new DefaultActivityLifecycleCallbacks() {
        @Override
        public void onActivityStarted(Activity activity) {
            if ( activity instanceof MasterActivity ) {

            }
        }
    };

    public GDPLStoreModel(Context context) {
        mContext = context;
        App.getApplication().registerActivityLifecycleCallbacks(mLifecycle);
    }

    @Override
    public boolean isAdsEnabled() {
        return false;
    }

    @Override
    public void addItemsToDrawer(Menu menu, int groupId) {

    }
}
