package com.globaldelight.boom.business;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.BuildConfig;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.activities.MasterActivity;
import com.globaldelight.boom.app.activities.UserVerificationActivity;
import com.globaldelight.boom.app.activities.WebViewActivity;
import com.globaldelight.boom.utils.DefaultActivityLifecycleCallbacks;
import com.globaldelight.boom.utils.Result;
import com.globaldelight.boom.utils.Utils;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by adarsh on 08/12/17.
 */
@Keep
public class B2BModel implements BusinessModel {

    private static final long UPDATE_CHECK_INTERVAL = (24 * 60 * 60 * 1000); // 24Hrs
    private static final String AVAILABLE_VERSION = "available_version";


    private Context mContext;
    private Activity mCurrentActivity;
    private MenuItem mUpdateMenuItem;

    private Application.ActivityLifecycleCallbacks mLifecycle = new DefaultActivityLifecycleCallbacks() {
        @Override
        public void onActivityStarted(Activity activity) {
            mCurrentActivity = activity;
            if ( activity instanceof MasterActivity ) {
                verify();
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            super.onActivityResumed(activity);
            mCurrentActivity = activity;
        }

        @Override
        public void onActivityStopped(Activity activity) {
            super.onActivityStopped(activity);
            if ( mCurrentActivity == activity ) {
                mCurrentActivity = null;
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
        mUpdateMenuItem = menu.add(groupId, R.id.update, Menu.NONE, R.string.check_update_drawer_title).setIcon(R.drawable.ic_update);
        menu.add(groupId, R.id.feedback, Menu.NONE, R.string.title_feedback).setIcon(R.drawable.ic_feedback);
        updateDrawer();
    }

    @Override
    public void onDrawerItemClicked(MenuItem item, Context context) {
        switch ( item.getItemId() ) {
            case R.id.feedback:
                showFeedback();
                break;
            case R.id.update:
                showStorePage();
                break;
        }
    }

    private void verify() {
        LicenseManager.getInstance(mContext).checkLicense(new LicenseManager.Callback() {
            @Override
            public void onSuccess() {
                checkForUpdate();
            }

            @Override
            public void onError(int errorCode) {
                Intent intent = new Intent(mContext, UserVerificationActivity.class);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
    }

    private void checkForUpdate() {
        final String LAST_CHECK_KEY = "last_update_check";

        final SharedPreferences prefs = mContext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        long lastCheck = prefs.getLong(LAST_CHECK_KEY, 0);
        if ( System.currentTimeMillis() - lastCheck <  UPDATE_CHECK_INTERVAL ) {
            updateDrawer();
            return;
        }

        new AsyncTask<Void, Void, Result<String>>() {
            @Override
            protected Result<String> doInBackground(Void... voids) {
                return B2BApi.getInstance(mContext).checkForUpdate();
            }

            @Override
            protected void onPostExecute(Result<String> result) {
                super.onPostExecute(result);
                if ( result.isSuccess() && isNewerVersion(result.get()) ) {
                    showUpdateDialog();
                    prefs.edit().putLong(LAST_CHECK_KEY, System.currentTimeMillis()).apply();
                    prefs.edit().putString(AVAILABLE_VERSION, result.get()).apply();
                }

                updateDrawer();
            }
        }.execute();
    }

    private boolean isNewerVersion(String version) {
        String currentVersion = BuildConfig.VERSION_NAME;
        String[] partsNV = version.split("\\.");
        String[] partsCV = currentVersion.split("\\.");

        boolean isNewer = false;
        int partCount = Math.min(partsNV.length, partsCV.length);
        for ( int i = 0; i < partCount; i++ ) {
            int diff = Integer.parseInt(partsNV[i]) - Integer.parseInt(partsCV[i]);
            isNewer = (diff > 0);
            if ( diff != 0 ) { // only if current parts are same compare next parts
                break;
            }
        }

        return isNewer;
    }

    private void updateDrawer() {
        final SharedPreferences prefs = mContext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        String availableVersion = prefs.getString(AVAILABLE_VERSION, null);
        if ( availableVersion != null && isNewerVersion(availableVersion) ) {
            mUpdateMenuItem.setTitle(R.string.update_drawer_title);
            mUpdateMenuItem.setIcon(R.drawable.ic_new_update);
        }
        else {
            mUpdateMenuItem.setTitle(R.string.check_update_drawer_title);
            mUpdateMenuItem.setIcon(R.drawable.ic_update);
        }
    }

    private void showUpdateDialog() {
        Utils.createDialogBuilder(mCurrentActivity)
                .title(R.string.update_dialog_title)
                .content(R.string.update_dialog_message)
                .positiveText(R.string.update_now)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        showStorePage();
                    }
                })
                .negativeText(R.string.remind_button_title)
                .show();
    }

    private void showStorePage() {
        String storePageURL = B2BApi.STORE_PAGE_URL + BuildConfig.VERSION_NAME;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(storePageURL));
        mCurrentActivity.startActivity(browserIntent);
    }

    private void showFeedback() {
        Intent intent = new Intent(mCurrentActivity, WebViewActivity.class);
        mCurrentActivity.startActivity(intent);
    }
}

