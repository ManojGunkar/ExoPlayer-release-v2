package com.globaldelight.boom.business;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.activities.MasterActivity;
import com.globaldelight.boom.app.activities.UserVerificationActivity;
import com.globaldelight.boom.utils.DefaultActivityLifecycleCallbacks;

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
        LicenseManager.getInstance(mContext).checkLicense(new LicenseManager.Callback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int errorCode) {
                Intent intent = new Intent(mContext, UserVerificationActivity.class);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
    }

    private void validateLicense() {
        LicenseManager.getInstance(mContext).validateLicense(new LicenseManager.Callback() {
            @Override
            public void onSuccess() {
                // Nothing to do
            }

            @Override
            public void onError(int errorCode) {
                // TODO: Show a dialog to user or show the promocode page
            }
        });

    }
}
