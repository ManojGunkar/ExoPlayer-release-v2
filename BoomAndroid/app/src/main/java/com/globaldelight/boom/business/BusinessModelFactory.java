package com.globaldelight.boom.business;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.lang.reflect.Constructor;

/**
 * Created by adarsh on 07/12/17.
 */

// Factory that instatiates a business model
public class BusinessModelFactory {

    private static BusinessModel mStrategy = null;

    public static void initModel(Context context) {
        try {
            Context appContext = context.getApplicationContext();
            ApplicationInfo info = appContext.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            String className = info.metaData.getString("business-model");
            if ( className != null ) {
                Class cls = Class.forName(className);
                Constructor businessConstructor =  cls.getConstructor(Context.class);
                mStrategy = (BusinessModel)businessConstructor.newInstance(context.getApplicationContext());
            }
        }
        catch (Exception e) {

        }

        if ( mStrategy == null ) {
            mStrategy = new DefaultModel();
        }
    }

    public static BusinessModel getCurrentModel() {
        return mStrategy;
    }
}
