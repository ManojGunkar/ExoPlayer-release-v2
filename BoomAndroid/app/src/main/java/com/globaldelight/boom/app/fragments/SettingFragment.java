package com.globaldelight.boom.app.fragments;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.TokenPair;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.app.analytics.UtilAnalytics;
import com.globaldelight.boom.app.receivers.ConnectivityReceiver;
import com.globaldelight.boom.app.activities.ActivityContainer;
import com.globaldelight.boom.app.activities.WebViewActivity;
import com.globaldelight.boom.app.adapters.utils.HeadPhoneItemAdapter;
import com.globaldelight.boom.view.RegularTextView;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.app.sharedPreferences.Preferences;
import com.globaldelight.boom.utils.helpers.DropBoxUtills;
import com.globaldelight.boom.utils.helpers.GoogleDriveHandler;
import com.globaldelight.boom.utils.sleepTimerUtils.TimerUtils;
import static android.app.Activity.RESULT_OK;

/**
 * Created by Rahul Agarwal on 03-02-17.
 */

public class SettingFragment extends Fragment implements View.OnClickListener {
    ScrollView rootView;
    RegularTextView sleepTimerTxt;
    private TextView googleDriveSettingsTitle;
    private TextView googleDriveSettingsDescription;
    private TextView dropboxSettingsTitle;
    private TextView dropboxSettingsDescription;

    private GoogleDriveHandler googleDriveHandler;
    private PermissionChecker permissionChecker;
    Activity mActivity;



    private class DropboxAccountLoader extends AsyncTask<Void, Void, DropboxAPI.Account> {
        @Override
        protected DropboxAPI.Account doInBackground(Void... params) {
            try {
                return App.getDropboxAPI().accountInfo();
            } catch (DropboxException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(DropboxAPI.Account account) {
            DropBoxUtills.setAccountName(mActivity, account.email);
            updateDropboxPanel();
        }
    };

    private DropboxAccountLoader mAccountLoader = null;


    public SettingFragment(){}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            mActivity = (Activity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ScrollView) inflater.inflate(R.layout.fragment_settings, container, false);
        mActivity = getActivity();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
        FlurryAnalyticHelper.init(mActivity);
        TimerUtils.resumeTimerState(mActivity, sleepTimerTxt);
    }
    @Override
    public void onStart() {
        super.onStart();
        FlurryAnalyticHelper.flurryStartSession(mActivity);
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAnalyticHelper.flurryStopSession(mActivity);
        if ( mAccountLoader != null && mAccountLoader.getStatus() != AsyncTask.Status.FINISHED ) {
            mAccountLoader.cancel(false);
        }
    }

    private void initViews() {
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.headset_recycler) ;
        LinearLayout dropbox = (LinearLayout) rootView.findViewById(R.id.setting_dropbox_panel);
        dropbox.setOnClickListener(this);
        LinearLayout googleDrive = (LinearLayout) rootView.findViewById(R.id.setting_google_drive_panel);
        googleDrive.setOnClickListener(this);
        sleepTimerTxt = (RegularTextView) rootView.findViewById(R.id.seeting_sleep_timer);

        sleepTimerTxt.setOnClickListener(this);
        LinearLayout sleepTimerPanel = (LinearLayout) rootView.findViewById(R.id.seeting_sleep_timer_panel);
        sleepTimerPanel.setOnClickListener(this);
        LinearLayout aboutPanel = (LinearLayout) rootView.findViewById(R.id.about_panel);
        aboutPanel.setOnClickListener(this);
        LinearLayout feedbackPanel = (LinearLayout) rootView.findViewById(R.id.feedback_panel);
        feedbackPanel.setOnClickListener(this);


        googleDriveSettingsTitle = (TextView)rootView.findViewById(R.id.google_drive_settings_title);
        googleDriveSettingsDescription = (TextView)rootView.findViewById(R.id.google_drive_settings_description);
        dropboxSettingsTitle = (TextView)rootView.findViewById(R.id.dropbox_settings_title);
        dropboxSettingsDescription = (TextView)rootView.findViewById(R.id.dropbox_settings_description);

        setHeadsetList(recyclerView);
    }


    private void updateAccountDetails() {
        updateDropboxPanel();
        updateGoogleDrivePanel();
    }

    private void updateDropboxPanel() {
        if ( DropBoxUtills.isLoggedIn(mActivity) ) {
            String accountName = DropBoxUtills.getAccountName(mActivity);
            if ( accountName == null ) {
                mAccountLoader = new DropboxAccountLoader();
                mAccountLoader.execute();
            }
            else {
                String title = mActivity.getString(R.string.drop_box) + " (" + accountName + ")";
                dropboxSettingsTitle.setText(title);
                dropboxSettingsDescription.setText(R.string.cloud_setting_subtitle_change_account);
            }
        }
        else {
            dropboxSettingsTitle.setText(R.string.drop_box);
            dropboxSettingsDescription.setText(R.string.cloud_setting_dropbox_subtitle);
        }
    }


    private void updateGoogleDrivePanel() {
        String googleDriveAccName = App.getUserPreferenceHandler().getGoogleAccountName();
        if ( null != googleDriveAccName) {
            String title = mActivity.getString(R.string.google_drive) + " (" + googleDriveAccName + ")";
            googleDriveSettingsTitle.setText(title);
            googleDriveSettingsDescription.setText(R.string.cloud_setting_subtitle_change_account);
        }
        else {
            googleDriveSettingsTitle.setText(R.string.google_drive);
            googleDriveSettingsDescription.setText(R.string.cloud_setting_google_subtitle);
        }
    }

    private void setHeadsetList(RecyclerView recyclerView) {
        TypedArray activeHeadPhoneList = getResources().obtainTypedArray(R.array.headphone_active);
        TypedArray inactiveHeadPhoneList = getResources().obtainTypedArray(R.array.headphone_inactive);
        TypedArray HeadPhoneList = getResources().obtainTypedArray(R.array.headphone_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new HeadPhoneItemAdapter(activeHeadPhoneList, inactiveHeadPhoneList, HeadPhoneList, mActivity, recyclerView));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.setting_dropbox_panel:
                onDropboxClicked();
                break;
            case R.id.setting_google_drive_panel:
                onGoogleDriveClicked();
                break;
            case R.id.seeting_sleep_timer:
            case R.id.seeting_sleep_timer_panel:
                boolean sleepTimerEnabled = Preferences.readBoolean(mActivity, Preferences.SLEEP_TIMER_ENABLED, false);
                TimerUtils.customMaterialTimepicker(mActivity, sleepTimerTxt, sleepTimerEnabled);
                if (sleepTimerEnabled) {
                    try {
                        TimerUtils.cancelTimer(sleepTimerTxt);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.about_panel:
                startCompoundActivities(R.string.header_about);
                FlurryAnalyticHelper.logEvent(UtilAnalytics.About_Page_opened);
                break;
            case R.id.feedback_panel:
                FlurryAnalyticHelper.logEvent(UtilAnalytics.FeedBack_Page_opened);
                if (ConnectivityReceiver.isNetworkAvailable(mActivity, true)) {
                    Intent intent = new Intent(mActivity, WebViewActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }

    private void startCompoundActivities(int activityName) {
        Intent intent = new Intent(mActivity, ActivityContainer.class);
        intent.putExtra("container",activityName);
        mActivity.startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GoogleDriveHandler.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(mActivity, getResources().getString(R.string.require_google_play_service), Toast.LENGTH_SHORT).show();
                }
                break;
            case GoogleDriveHandler.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        App.getUserPreferenceHandler().setGoogleAccountName(accountName);
                        googleDriveHandler.setSelectedGoogleAccountName(accountName);
                    }
                    Preferences.writeBoolean(getContext(), Preferences.GOOGLE_DRIVE_ACCOUNT_CHANGED, true);
                }
                break;
            case GoogleDriveHandler.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
//                    googleDriveHandler.getResultsFromApi();
                }
                break;
        }
    }

    private void resetGoogleDriveAuth() {
        if(ConnectivityReceiver.isNetworkAvailable(mActivity, true)) {
            try {
                googleDriveHandler = new GoogleDriveHandler(SettingFragment.this);
                googleDriveHandler.getGoogleAccountCredential();
                googleDriveHandler.getGoogleApiClient();
                googleDriveHandler.resetKeys(mActivity);
            }catch (Exception e){}
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resetAuthentication();
        updateAccountDetails();
    }

    private void resetAuthentication(){
        if(null != App.getDropboxAPI()) {
            AndroidAuthSession session = App.getDropboxAPI().getSession();
            if (session.authenticationSuccessful()) {
                try {
                    session.finishAuthentication();
                    TokenPair tokens = session.getAccessTokenPair();
                    DropBoxUtills.storeKeys(mActivity, tokens.key, tokens.secret);
                } catch (IllegalStateException e) {
                    Toast.makeText(mActivity, getResources().getString(R.string.dropbox_authenticate_problem)
                            + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void checkPermissions() {
        permissionChecker = new PermissionChecker(mActivity, mActivity, rootView);
        permissionChecker.check(Manifest.permission.GET_ACCOUNTS,
                getResources().getString(R.string.account_permission),
                new PermissionChecker.OnPermissionResponse() {
                    @Override
                    public void onAccepted() {
                        resetGoogleDriveAuth();
                    }

                    @Override
                    public void onDecline() {

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public Context getFragmentContext() {
        return mActivity;
    }


    private void onDropboxClicked() {
        Runnable action = new Runnable() {
            @Override
            public void run() {
                if(ConnectivityReceiver.isNetworkAvailable(mActivity, true)) {
                    App.getDropboxAPI().getSession().unlink();
                    DropBoxUtills.clearKeys(mActivity);
                    DropBoxUtills.checkAppKeySetup(App.getApplication());
                    DropBoxUtills.checkDropboxAuthentication(mActivity);
                }
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Drop_Box_Tapped_From_Setting_Page);
            }
        };

        if ( DropBoxUtills.isLoggedIn(mActivity) ) {
            showAccountChangeDialog(action);
        }
        else {
            action.run();
        }
    }


    private void onGoogleDriveClicked() {
        Runnable action = new Runnable() {
            @Override
            public void run() {
                checkPermissions();
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Google_Drive_Tapped_From_Setting_Page);
            }
        };

        if ( App.getUserPreferenceHandler().getGoogleAccountName() != null ) {
            showAccountChangeDialog(action);
        }
        else {
            action.run();
        }

    }


    private void showAccountChangeDialog(final Runnable action) {
        new MaterialDialog.Builder(mActivity)
                .backgroundColor(ContextCompat.getColor(mActivity, R.color.dialog_background))
                .titleColor(ContextCompat.getColor(mActivity, R.color.dialog_title))
                .positiveColor(ContextCompat.getColor(mActivity, R.color.dialog_submit_positive))
                .negativeColor(ContextCompat.getColor(mActivity, R.color.dialog_submit_negative))
                .widgetColor(ContextCompat.getColor(mActivity, R.color.dialog_widget))
                .content(R.string.settings_change_account_message)
                .contentColor(ContextCompat.getColor(mActivity, R.color.dialog_content))
                .negativeText(R.string.dialog_txt_cancel)
                .positiveText(R.string.conitnue_with_acc_change)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        action.run();
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    }
                })
                .show();
    }
}
