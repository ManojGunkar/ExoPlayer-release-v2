package com.globaldelight.boom.app.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.collection.cloud.DropboxMediaList;
import com.globaldelight.boom.app.receivers.ConnectivityReceiver;
import com.globaldelight.boom.app.loaders.LoadDropBoxList;
import com.globaldelight.boom.utils.helpers.DropBoxAPI;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

import static com.globaldelight.boom.utils.helpers.DropBoxAPI.ACCOUNT_PREFS_NAME;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class DropBoxListFragment extends CloudFragment  implements DropboxMediaList.IDropboxUpdater {

    private DropboxMediaList dropboxMediaList;
    private boolean isDropboxAccountConfigured = true;
    private boolean tryLogin = true;
    private SharedPreferences prefs;


    @Override
    public void onDetach() {
        super.onDetach();
        dropboxMediaList.setDropboxUpdater(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prefs = mActivity.getSharedPreferences(
                ACCOUNT_PREFS_NAME, 0);
    }

    @Override
    public void onStart() {
        super.onStart();

        dropboxMediaList = DropboxMediaList.getInstance(mActivity);
        dropboxMediaList.setDropboxUpdater(this);

        // finish any pending authorization
        DropBoxAPI.getInstance(mActivity).finishAuthorization();
        isDropboxAccountConfigured = DropBoxAPI.getInstance(mActivity).isLoggedIn();
        if ( !isDropboxAccountConfigured ){
            listIsEmpty(true);

            // Attempt login once
            if ( tryLogin ) {
                DropBoxAPI.getInstance(mActivity).authorize();
                tryLogin = false;
            }
        }
        else if ( dropboxMediaList.isLoaded() ) {
            boolean isListEmpty = dropboxMediaList.getDropboxMediaList().size() <= 0;
            listIsEmpty(isListEmpty);
            updateSongList();
        }
        else {
            LoadDropboxList();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        dropboxMediaList.setDropboxUpdater(null);
    }

    private void LoadDropboxList(){
        if(null != getActivity()) {
            boolean isListEmpty = dropboxMediaList.getDropboxMediaList().size() <= 0;
            isDropboxAccountConfigured = DropBoxAPI.getInstance(mActivity).isLoggedIn();
            if ( isDropboxAccountConfigured ) {
                if ( ConnectivityReceiver.isNetworkAvailable(mActivity, true) && isListEmpty) {
                    listIsEmpty(false);
                    onLoadingStarted();
                    new LoadDropBoxList(mActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else if (!isListEmpty) {
                    listIsEmpty(false);
                    updateSongList();
                }
            } else {
                listIsEmpty(true);
            }
            setForAnimation();
        }
    }

    private void setForAnimation() {
        mRecyclerView.scrollTo(0, 100);
    }

    private void updateSongList() {
        boolean isEmpty = dropboxMediaList.getDropboxMediaList().size() <= 0;

        if(!isEmpty && null != mAdapter){
            notifyAdapter();
        }
    }

    private void notifyAdapter() {
        if(null != mAdapter) {
            mAdapter.updateMediaList(dropboxMediaList.getDropboxMediaList());
        }
    }

    @Override
    public void onUpdateEntry() {
        updateSongList();
    }

    @Override
    public void onFinishLoading() {
        if ( dropboxMediaList.getDropboxMediaList().size() == 0 ) {
            listIsEmpty(true);

        }
        onLoadingFinished();
    }

    @Override
    public void onLoadingError() {
        listIsEmpty(true);
        onLoadingFinished();
    }

    @Override
    public void onClearList() {
        notifyAdapter();
    }

    public void listIsEmpty(boolean enable) {
        showEmptyList(enable, isDropboxAccountConfigured);
    }

    @Override
    void onSync() {
        if ( !isDropboxAccountConfigured ) {
            DropBoxAPI.getInstance(mActivity).authorize();
            return;
        }

        if(null != dropboxMediaList) {
            dropboxMediaList.clearDropboxContent();
        }
        LoadDropboxList();
        FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Sync_Button_tapped_from_Drop_BOx);
    }

    @Override
    void loadSongList() {
        LoadDropboxList();
    }
}
