package com.globaldelight.boom.business;

import android.content.Context;

import com.globaldelight.boom.BuildConfig;

/**
 * Created by adarsh on 07/12/17.
 */

// Factory that instatiates a business model
public class BusinessModelFactory {

    private static BusinessModel mStrategy = null;

    public static void initModel(Context context) {
        if ( BuildConfig.BUSINESS_MODEL_ENABLED ) {
            mStrategy = new AppStoreBusinessModel(context);
        }
        else {
            mStrategy = new DemoModel();
        }
    }

    public static BusinessModel getCurrentModel() {
        return mStrategy;
    }
}
