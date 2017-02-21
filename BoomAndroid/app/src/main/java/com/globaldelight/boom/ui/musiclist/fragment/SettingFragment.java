package com.globaldelight.boom.ui.musiclist.fragment;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.TokenPair;
import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.manager.ConnectivityReceiver;
import com.globaldelight.boom.ui.musiclist.activity.ActivityContainer;
import com.globaldelight.boom.ui.musiclist.adapter.utils.HeadPhoneItemAdapter;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.handlers.Preferences;
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
    private GoogleDriveHandler googleDriveHandler;
    private PermissionChecker permissionChecker;
    Activity mActivity;

    public SettingFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ScrollView) inflater.inflate(R.layout.fragment_settings, container, false);
        mActivity = getActivity();
        initViews();

        TimerUtils.resumeTimerState(mActivity, sleepTimerTxt);
        return rootView;
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

        setHeadsetList(recyclerView);
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
                if(ConnectivityReceiver.isNetworkAvailable(mActivity)) {
                    App.getDropboxAPI().getSession().unlink();
                    DropBoxUtills.clearKeys(mActivity);
                    DropBoxUtills.checkAppKeySetup(App.getApplication());
                    DropBoxUtills.checkDropboxAuthentication(mActivity);
                }
                break;
            case R.id.setting_google_drive_panel:
                checkPermissions();
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
                break;
            case R.id.feedback_panel:

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
        if(ConnectivityReceiver.isNetworkAvailable(mActivity)) {
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
}
