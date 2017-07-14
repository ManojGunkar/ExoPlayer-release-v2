package com.globaldelight.boom.business;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import java.util.Date;

/**
 * Created by adarsh on 13/07/17.
 */

public class BusinessData {
    public static final int STATE_UNDEFINED = -1;
    public static final int STATE_TRIAL = 0;
    public static final int STATE_SHARED = 1;
    public static final int STATE_LOCKED = 2;
    public static final int STATE_VIDOE_REWARD = 3;
    public static final int STATE_PURCHASED = 4;

    private static final String APP_STATE_KEY = "BusinessData.state";
    private static final String SHARED_DATE_KEY = "BusinessData.shared_date";
    private static final String REWARD_DATE_KEY = "BusinessData.reward_date";
    private static final String START_DATE_KEY = "BusinessData.start_date";
    private static final String LAST_POPUP_DATE = "BusinessData.last_popup";
    private static final String INITIAL_POPUP_SHOWN_KEY = "BusinessData.initial_popup_shown";


    private boolean mShowAds = false;
    private Context mContext;
    private SharedPreferences mPrefs;

    public BusinessData(Context context) {
        mContext = context;
        mPrefs = mContext.getSharedPreferences("com.globaldelight.boom", Context.MODE_PRIVATE);
        if ( mPrefs.getLong("install_date", 0) == 0 ) {
            mPrefs.edit().putLong("install_date", System.currentTimeMillis()).apply();
        }
    }


    public void setState(int state) {
        mPrefs.edit().putInt(APP_STATE_KEY, state).apply();
    }

    public int getState() {
        return mPrefs.getInt(APP_STATE_KEY, STATE_UNDEFINED);
    }

    public void enableAds(boolean enable) {
        mShowAds = enable;
    }


    public boolean showAds() {
        return mShowAds;
    }


    public Date installDate() {
        long time = mPrefs.getLong("install_date",0);
        return time > 0 ? new Date(time) : null;

//        try {
//            return new Date(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).firstInstallTime);
//        }
//        catch (PackageManager.NameNotFoundException e) {
//            return new Date();
//        }
    }


    public Date getSharedDate() {
        long time = mPrefs.getLong(SHARED_DATE_KEY,0);
        return time > 0 ? new Date(time) : null;
    }


    public void setSharedDate(Date date) {
        mPrefs.edit().putLong(SHARED_DATE_KEY, date.getTime()).apply();
    }


    public void setVideoRewardDate(Date date) {
        mPrefs.edit().putLong(REWARD_DATE_KEY, date.getTime()).apply();
    }

    public Date getVideoRewardDate() {
        long time = mPrefs.getLong(REWARD_DATE_KEY,0);
        return time > 0 ? new Date(time) : null;
    }

    public void setStartDate(Date date) {
        mPrefs.edit().putLong(START_DATE_KEY, date.getTime()).apply();
    }

    public Date getStartDate() {
        long time = mPrefs.getLong(START_DATE_KEY,0);
        return time > 0 ? new Date(time) : null;
    }

    public void setLastPopupDate(Date date) {
        mPrefs.edit().putLong(LAST_POPUP_DATE, date.getTime()).apply();
    }

    public Date getLastPopupDate() {
        long time = mPrefs.getLong(LAST_POPUP_DATE,0);
        return time > 0 ? new Date(time) : null;
    }

    public boolean isInitialPopupShown() {
        return mPrefs.getBoolean(INITIAL_POPUP_SHOWN_KEY, false);
    }

    public void setInitialPopupShown() {
        mPrefs.edit().putBoolean(INITIAL_POPUP_SHOWN_KEY, true).apply();
    }
}
