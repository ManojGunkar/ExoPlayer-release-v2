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
        if ( BuildConfig.FLAVOR.equalsIgnoreCase("inceptive") ) {
            mStrategy = new DemoModel();
        }
        else if ( BuildConfig.FLAVOR.equalsIgnoreCase("demo") ) {
            mStrategy = new DemoModel();
        }
        else {
            mStrategy = new AppStoreBusinessModel(context);
        }
    }

    public static BusinessModel getCurrentModel() {
        return mStrategy;
    }
}
