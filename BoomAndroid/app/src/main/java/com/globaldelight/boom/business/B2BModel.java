package com.globaldelight.boom.business;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;

import com.globaldelight.boom.BuildConfig;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.activities.MasterActivity;
import com.globaldelight.boom.app.activities.UserVerificationActivity;
import com.globaldelight.boom.utils.DefaultActivityLifecycleCallbacks;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by adarsh on 08/12/17.
 */

public class B2BModel implements BusinessModel {

    private Context mContext;

    private Application.ActivityLifecycleCallbacks mLifecycle = new DefaultActivityLifecycleCallbacks() {
        @Override
        public void onActivityStarted(Activity activity) {
            if ( activity instanceof MasterActivity ) {
                verify();
            }
        }
    };

    public B2BModel(Context context) {
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

    private void verify() {

        SharedPreferences prefs = mContext.getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
        if (!prefs.getBoolean("IsUnlocked", false) ) {
            Intent intent = new Intent(mContext, UserVerificationActivity.class);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            // if app has not verified the promo code show screen to enter promo code
            mContext.startActivity(intent);
        }
    }
}
