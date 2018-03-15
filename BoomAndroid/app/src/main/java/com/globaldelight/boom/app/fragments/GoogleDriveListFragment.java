package com.globaldelight.boom.app.fragments;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.Toast;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.collection.cloud.GoogleDriveMediaList;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.app.receivers.ConnectivityReceiver;
import com.globaldelight.boom.app.adapters.song.SongListAdapter;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.app.sharedPreferences.Preferences;
import com.globaldelight.boom.utils.helpers.GoogleDriveHandler;

import static android.app.Activity.RESULT_OK;

import com.globaldelight.boom.utils.Utils;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class GoogleDriveListFragment extends CloudFragment  implements GoogleDriveMediaList.IGoogleDriveMediaUpdater, PermissionChecker.OnPermissionResponse {

    private GoogleDriveMediaList googleDriveMediaList;
    private GoogleDriveHandler googleDriveHandler;
    private boolean isGoogleAccountConfigured = false;
    private PermissionChecker permissionChecker;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GoogleDriveListFragment() {
    }


    @Override
    public void onDetach() {
        super.onDetach();
        googleDriveMediaList.setGoogleDriveHandler(null);
        googleDriveMediaList.setGoogleDriveMediaUpdater(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
        checkPermissions();
    }

    private void initViews() {
        Preferences.writeBoolean(mActivity, Preferences.GOOGLE_DRIVE_ACCOUNT_CHANGED, false);
        mActivity.setTitle(getResources().getString(R.string.google_drive));

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                googleDriveMediaList = GoogleDriveMediaList.getInstance(mActivity);
                googleDriveMediaList.setGoogleDriveMediaUpdater(GoogleDriveListFragment.this);
                googleDriveHandler = new GoogleDriveHandler(GoogleDriveListFragment.this);
                googleDriveMediaList.setGoogleDriveHandler(googleDriveHandler);
                googleDriveHandler.getGoogleAccountCredential();
                googleDriveHandler.getGoogleApiClient();
                googleDriveHandler.connectGoogleAccount();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Preferences.readBoolean(mActivity, Preferences.GOOGLE_DRIVE_ACCOUNT_CHANGED, false)) {
            if (ConnectivityReceiver.isNetworkAvailable(mActivity, true)) {
                checkPermissions();
            }
            Preferences.writeBoolean(mActivity, Preferences.GOOGLE_DRIVE_ACCOUNT_CHANGED, false);
        }
        if( null == App.getUserPreferenceHandler().getGoogleAccountName()){
            isGoogleAccountConfigured = false;
            showEmptyList(true, isGoogleAccountConfigured);
            Utils.dismissProgressLoader();
        }
        if(null != adapter)
            adapter.notifyDataSetChanged();
    }


    @Override
    void onSync() {
        if(null != googleDriveMediaList) {
            googleDriveMediaList.clearGoogleDriveMediaContent();
        }
        checkPermissions();
        //FlurryAnalyticHelper.logEvent(UtilAnalytics.Sync_Button_tapped_from_Google_Drive);
        FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Sync_Button_tapped_from_Google_Drive);
    }

    @Override
    void loadSongList() {
        checkPermissions();
    }

    public void checkPermissions() {
        permissionChecker = new PermissionChecker(mActivity, mActivity, mListView);
        permissionChecker.check(Manifest.permission.GET_ACCOUNTS, getResources().getString(R.string.account_permission), this);
    }

    @Override
    public void onAccepted() {
        LoadGoogleDriveList();
    }

    private void dismissProgressWithDelay() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.dismissProgressLoader();
            }
        }, 3000);
    }

    private void LoadGoogleDriveList(){
        Utils.showProgressLoader(mActivity);
        if( null != App.getUserPreferenceHandler().getGoogleAccountName()){
            isGoogleAccountConfigured = true;
            showEmptyList(false, isGoogleAccountConfigured);
            if(googleDriveMediaList.getGoogleDriveMediaList().size() <= 0){
                if (ConnectivityReceiver.isNetworkAvailable(mActivity, true)) {
                    googleDriveHandler.getResultsFromApi();
                }else{
                    Utils.dismissProgressLoader();
                }
            }else if(googleDriveMediaList.getGoogleDriveMediaList().size() > 0){
                setSongListAdapter(true);
                dismissProgressWithDelay();
            }
        }else{
            Utils.dismissProgressLoader();
            if (ConnectivityReceiver.isNetworkAvailable(mActivity, true)) {
                googleDriveHandler.getResultsFromApi();
            }
        }
    }

    @Override
    public void onDecline() {
        if(null != mActivity)
            mActivity.onBackPressed();
    }

    private void setSongListAdapter(final boolean isUpdate) {
        if(null != googleDriveMediaList) {
            if (null == adapter) {
                final GridLayoutManager gridLayoutManager =
                        new GridLayoutManager(mActivity, 1);
                gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                gridLayoutManager.scrollToPosition(0);
                mListView.setLayoutManager(gridLayoutManager);
                adapter = new SongListAdapter(mActivity, GoogleDriveListFragment.this, googleDriveMediaList.getGoogleDriveMediaList(), ItemType.SONGS);
                mListView.setAdapter(adapter);
                mListView.setHasFixedSize(true);
            } else {
                adapter.updateMediaList(googleDriveMediaList.getGoogleDriveMediaList());
            }

            if (googleDriveMediaList.getGoogleDriveMediaList().size() <= 0) {
                showEmptyList(true, isGoogleAccountConfigured);
            } else {
                showEmptyList(false, isGoogleAccountConfigured);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onGoogleDriveMediaListUpdate() {
        setSongListAdapter(false);
        setForAnimation();
    }

    @Override
    public void onFinishListLoading() {
        dismissProgressWithDelay();
    }

    @Override
    public void onRequestCancelled() {
        Utils.dismissProgressLoader();
        if(null != getActivity())
            Toast.makeText(mActivity, getResources().getString(R.string.request_cancelled), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String e) {
        Utils.dismissProgressLoader();
        if(null != getActivity() && googleDriveMediaList.getGoogleDriveMediaList().size() <= 0)
            Toast.makeText(mActivity, getResources().getString(R.string.google_drive_loading_error)
                + e, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyList() {
        Utils.dismissProgressLoader();
        showEmptyList(true, isGoogleAccountConfigured);
    }

    @Override
    public void onClearList() {
        setSongListAdapter(false);
        showEmptyList(false, isGoogleAccountConfigured);
    }

    private void setForAnimation() {
        mListView.scrollTo(0, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK){
            isGoogleAccountConfigured = false;
            showEmptyList(true, isGoogleAccountConfigured);
            Utils.dismissProgressLoader();
            if(requestCode == GoogleDriveHandler.REQUEST_GOOGLE_PLAY_SERVICES)
                Toast.makeText(mActivity, getResources().getString(R.string.require_google_play_service), Toast.LENGTH_SHORT).show();
            return;
        }
        switch (requestCode) {
            case GoogleDriveHandler.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) {
                    checkPermissions();
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
                        checkPermissions();
                    }
                }else{
                    Utils.dismissProgressLoader();
                }
                break;
            case GoogleDriveHandler.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    checkPermissions();
                }
                break;
        }
    }
}
