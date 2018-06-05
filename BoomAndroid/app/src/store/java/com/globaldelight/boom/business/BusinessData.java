package com.globaldelight.boom.business;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import com.globaldelight.boom.BuildConfig;
import com.globaldelight.boom.utils.Log;

import java.util.Date;

/**
 * Created by adarsh on 13/07/17.
 */

public class BusinessData {
    private static final String APP_STATE_KEY = "BusinessData.state";
    private static final String SHARED_DATE_KEY = "BusinessData.shared_date";
    private static final String REWARD_DATE_KEY = "BusinessData.reward_date";
    private static final String START_DATE_KEY = "BusinessData.start_date";
    private static final String LAST_POPUP_DATE = "BusinessData.last_popup";
    private static final String INITIAL_POPUP_SHOWN_KEY = "BusinessData.initial_popup_shown";
    private static final String LAST_SHARE_REMINDER_KEY = "BusinessData.LAST_SHARE_REMINDER";
    private static final String LAST_PURCHASE_REMINDER_KEY = "BusinessData.LAST_PURCHASE_REMINDER";


    private boolean mShowAds = false;
    private Context mContext;
    private SharedPreferences mPrefs;
    private Date mInstallDate;

    public BusinessData(Context context) {
        mContext = context;
        mPrefs = mContext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        try {
            mInstallDate = new Date(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).firstInstallTime);
            Log.d("BusinessData", "Install date " + mInstallDate.toString() );
        }
        catch (PackageManager.NameNotFoundException e) {

        }
    }


    public void setState(int state) {
        mPrefs.edit().putInt(APP_STATE_KEY, state).apply();
    }

    public int getState() {
        return mPrefs.getInt(APP_STATE_KEY, -1);
    }

    public void enableAds(boolean enable) {
        mShowAds = enable;
    }


    public boolean showAds() {
        return mShowAds;
    }


    public Date installDate() {
        return mInstallDate;
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

    public void setLastShareReminder(Date date) {
        mPrefs.edit().putLong(LAST_SHARE_REMINDER_KEY, date.getTime()).apply();
    }

    public Date getLastShareReminder() {
        long time = mPrefs.getLong(LAST_SHARE_REMINDER_KEY,0);
        return time > 0 ? new Date(time) : null;
    }
    public void setLastPurchaseReminder(Date date) {
        mPrefs.edit().putLong(LAST_PURCHASE_REMINDER_KEY, date.getTime()).apply();
    }

    public Date getLastPurchaseReminder() {
        long time = mPrefs.getLong(LAST_PURCHASE_REMINDER_KEY,0);
        return time > 0 ? new Date(time) : null;
    }

}
