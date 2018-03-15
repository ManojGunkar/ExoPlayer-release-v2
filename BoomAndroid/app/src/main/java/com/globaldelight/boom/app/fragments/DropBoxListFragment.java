package com.globaldelight.boom.app.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.adapters.song.SongListAdapter;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.collection.cloud.DropboxMediaList;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.app.receivers.ConnectivityReceiver;
import com.globaldelight.boom.app.loaders.LoadDropBoxList;
import com.globaldelight.boom.app.activities.CloudListActivity;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.helpers.DropBoxAPI;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

import static com.globaldelight.boom.utils.helpers.DropBoxAPI.ACCOUNT_PREFS_NAME;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class DropBoxListFragment extends CloudFragment  implements DropboxMediaList.IDropboxUpdater {

    private DropboxMediaList dropboxMediaList;
    private boolean isDropboxAccountConfigured = true;
    SharedPreferences prefs;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DropBoxListFragment() {
    }


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
        initViews();
    }

    private void initViews() {
        ((CloudListActivity)mActivity).setTitle(getResources().getString(R.string.drop_box));
        Utils.showProgressLoader(getContext());
        if ( !DropBoxAPI.getInstance(mActivity).isLoggedIn() ){
            isDropboxAccountConfigured = false;
            listIsEmpty(true);
        }
        DropBoxAPI.getInstance(mActivity).authorize();
        dropboxMediaList = DropboxMediaList.getInstance(mActivity);
        dropboxMediaList.setDropboxUpdater(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        DropBoxAPI.getInstance(mActivity).finishAuthorization();
        LoadDropboxList();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dropboxMediaList.setDropboxUpdater(null);
    }

    private void LoadDropboxList(){
        if(null != getActivity()) {
            boolean isListEmpty = dropboxMediaList.getDropboxMediaList().size() <= 0;
            if ( DropBoxAPI.getInstance(mActivity).isLoggedIn()) {
                isDropboxAccountConfigured = true;
                if ( ConnectivityReceiver.isNetworkAvailable(mActivity, true) && isListEmpty) {
                    listIsEmpty(false);
                    if(!Utils.isProgressLoaderActive())
                        Utils.showProgressLoader(mActivity);
                    new LoadDropBoxList(mActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else if (!isListEmpty) {
                    listIsEmpty(false);
                    setSongListAdapter();
                    dismissProgressWithDelay();
                }
            } else {
                isDropboxAccountConfigured = false;
                listIsEmpty(true);
                Utils.dismissProgressLoader();
            }
            setForAnimation();
        }
    }

    private void dismissProgressWithDelay() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.dismissProgressLoader();
            }
        }, 3000);
    }

    private void setForAnimation() {
        mListView.scrollTo(0, 100);
    }

    private void setSongListAdapter() {
        boolean isEmpty = dropboxMediaList.getDropboxMediaList().size() <= 0;

        if(!isEmpty && null != adapter){
            notifyAdapter();
        }else if(!isEmpty){
            final GridLayoutManager gridLayoutManager =
                    new GridLayoutManager(mActivity, 1);
            gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            gridLayoutManager.scrollToPosition(0);
            mListView.setLayoutManager(gridLayoutManager);
            adapter = new SongListAdapter(mActivity, DropBoxListFragment.this, dropboxMediaList.getDropboxMediaList(), ItemType.SONGS);
            mListView.setAdapter(adapter);
            mListView.setHasFixedSize(true);
        }
    }

    private void notifyAdapter() {
        adapter.updateMediaList(dropboxMediaList.getDropboxMediaList());
    }

    @Override
    public void UpdateDropboxEntryList() {
        LoadDropboxList();
    }

    @Override
    public void finishDropboxLoading() {
        dismissProgressWithDelay();
    }


    @Override
    public void EmptyDropboxList() {
        listIsEmpty(true);
        Utils.dismissProgressLoader();
    }

    @Override
    public void onLoadingError() {
        listIsEmpty(true);
        Utils.dismissProgressLoader();
    }

    @Override
    public void ClearList() {
        if(null != adapter)
            notifyAdapter();
    }

    public void listIsEmpty(boolean enable) {
        showEmptyList(enable, isDropboxAccountConfigured);
    }

    @Override
    void onSync() {
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
