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

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class GoogleDriveListFragment extends CloudFragment  implements GoogleDriveMediaList.IGoogleDriveMediaUpdater, PermissionChecker.OnPermissionResponse {

    private GoogleDriveMediaList googleDriveMediaList;
    private GoogleDriveHandler googleDriveHandler;
    private boolean isGoogleAccountConfigured = false;
    private PermissionChecker permissionChecker;
    private boolean shouldReload = true;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GoogleDriveListFragment() {
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
    }

    private void initViews() {
        Preferences.writeBoolean(mActivity, Preferences.GOOGLE_DRIVE_ACCOUNT_CHANGED, false);
        mActivity.setTitle(getResources().getString(R.string.google_drive));

    }


    @Override
    public void onStart() {
        super.onStart();
        googleDriveMediaList = GoogleDriveMediaList.getInstance(mActivity);
        googleDriveMediaList.setGoogleDriveMediaUpdater(GoogleDriveListFragment.this);
        googleDriveHandler = new GoogleDriveHandler(GoogleDriveListFragment.this);
        googleDriveMediaList.setGoogleDriveHandler(googleDriveHandler);

        isGoogleAccountConfigured = (App.getUserPreferenceHandler().getGoogleAccountName() != null);
        boolean accountChanged = Preferences.readBoolean(mActivity, Preferences.GOOGLE_DRIVE_ACCOUNT_CHANGED, false);
        if ( !isGoogleAccountConfigured || accountChanged || !googleDriveMediaList.isLoaded()) {
            shouldReload = shouldReload || accountChanged;
            if (ConnectivityReceiver.isNetworkAvailable(mActivity, true) && shouldReload ) {
                loadSongList();
                shouldReload = false;
            }
            Preferences.writeBoolean(mActivity, Preferences.GOOGLE_DRIVE_ACCOUNT_CHANGED, false);
        }
        else {
            setSongListAdapter(false);
        }


        if(null != adapter)
            adapter.notifyDataSetChanged();

    }

    @Override
    public void onStop() {
        googleDriveMediaList.setGoogleDriveHandler(null);
        googleDriveMediaList.setGoogleDriveMediaUpdater(null);

        super.onStop();
    }



    @Override
    void onSync() {
        if(null != googleDriveMediaList) {
            googleDriveMediaList.clearGoogleDriveMediaContent();
        }
        checkPermissions();
        FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Sync_Button_tapped_from_Google_Drive);
    }

    @Override
    void loadSongList() {
        checkPermissions();
    }

    public void checkPermissions() {
        permissionChecker = new PermissionChecker(mActivity, mListView, PermissionChecker.ACCOUNTS_PERMISSION);
        permissionChecker.check(Manifest.permission.GET_ACCOUNTS, getResources().getString(R.string.account_permission), this);
    }

    @Override
    public void onAccepted() {
        LoadGoogleDriveList();
    }


    private void LoadGoogleDriveList(){
        if (ConnectivityReceiver.isNetworkAvailable(mActivity, true)) {
            if ( googleDriveHandler.getSelectedAccountName() == null ) {
                googleDriveHandler.chooseAccount();
            }
            else {
                isGoogleAccountConfigured = true;
                showProgressView();
                googleDriveHandler.fetchMediaList();
            }
        }
    }

    @Override
    public void onDecline() {
        showEmptyList(true, isGoogleAccountConfigured);
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
    public void onMediaListUpdate() {
        setSongListAdapter(false);
        setForAnimation();
    }

    @Override
    public void onFinishLoading() {
        hideProgressView();
    }

    @Override
    public void onRequestCancelled() {
        hideProgressView();
        if(null != getActivity())
            Toast.makeText(mActivity, getResources().getString(R.string.request_cancelled), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String e) {
        hideProgressView();
        if(null != getActivity() && googleDriveMediaList.getGoogleDriveMediaList().size() <= 0)
            Toast.makeText(mActivity, getResources().getString(R.string.google_drive_loading_error)
                + e, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyList() {
        hideProgressView();
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
            hideProgressView();
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
                        new Handler().post(this::LoadGoogleDriveList);
                    }
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
