package com.globaldelight.boom.app.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.TokenPair;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.adapters.song.SongListAdapter;
import com.globaldelight.boom.app.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.app.analytics.UtilAnalytics;
import com.globaldelight.boom.collection.cloud.DropboxMediaList;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.app.receivers.ConnectivityReceiver;
import com.globaldelight.boom.app.loaders.LoadDropBoxList;
import com.globaldelight.boom.app.activities.CloudListActivity;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.helpers.DropBoxUtills;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_CLOUD_SYNC;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_ON_NETWORK_CONNECTED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;
import static com.globaldelight.boom.utils.helpers.DropBoxUtills.ACCESS_KEY_NAME;
import static com.globaldelight.boom.utils.helpers.DropBoxUtills.ACCESS_SECRET_NAME;
import static com.globaldelight.boom.utils.helpers.DropBoxUtills.ACCOUNT_PREFS_NAME;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class DropBoxListFragment extends Fragment  implements DropboxMediaList.IDropboxUpdater {

    private DropboxMediaList dropboxMediaList;
    private boolean isDropboxAccountConfigured = true;
    private SongListAdapter adapter;
    SharedPreferences prefs;
    private RecyclerView rootView;
    Activity mActivity;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DropBoxListFragment() {
    }

    private BroadcastReceiver mUpdateItemSongListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY:
                    if(null != adapter)
                        adapter.notifyDataSetChanged();
                    break;
                case ACTION_ON_NETWORK_CONNECTED:
                    LoadDropboxList();
                    break;
                case ACTION_CLOUD_SYNC:
                    if(null != dropboxMediaList) {
                        dropboxMediaList.clearDropboxContent();
                    }
                    LoadDropboxList();
                    FlurryAnalyticHelper.logEvent(UtilAnalytics.Sync_Button_tapped_from_Drop_BOx);
                    break;
            }
        }
    };

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
        rootView = (RecyclerView)  inflater.inflate(R.layout.recycler_view_layout, container, false);
        if(null == mActivity)
            mActivity = getActivity();
        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prefs = mActivity.getSharedPreferences(
                ACCOUNT_PREFS_NAME, 0);
        initViews();
        FlurryAnalyticHelper.init(mActivity);
    }

    private void initViews() {
        ((CloudListActivity)mActivity).setTitle(getResources().getString(R.string.drop_box));
        Utils.showProgressLoader(getContext());
        if (null == prefs.getString(ACCESS_KEY_NAME, null) &&
                null == prefs.getString(ACCESS_SECRET_NAME, null)){
            isDropboxAccountConfigured = false;
            listIsEmpty(true);
        }
        dropboxMediaList = DropboxMediaList.getDropboxListInstance(mActivity);
        dropboxMediaList.setDropboxUpdater(this);
        DropBoxUtills.checkDropboxAuthentication(mActivity);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
        LoadDropboxList();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(null != mActivity) {
            mActivity.unregisterReceiver(mUpdateItemSongListReceiver);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dropboxMediaList.setDropboxUpdater(null);
    }

    private void registerReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        intentFilter.addAction(ACTION_ON_NETWORK_CONNECTED);
        intentFilter.addAction(ACTION_CLOUD_SYNC);
        if(null != mActivity)
            mActivity.registerReceiver(mUpdateItemSongListReceiver, intentFilter);
    }

    private void LoadDropboxList(){
        if(null != getActivity()) {
            boolean isListEmpty = dropboxMediaList.getDropboxMediaList().size() <= 0;
            resetAuthentication();
            if (null != prefs.getString(ACCESS_KEY_NAME, null) &&
                    null != prefs.getString(ACCESS_SECRET_NAME, null)) {
                isDropboxAccountConfigured = true;
                if (null != App.getDropboxAPI()
                        && ConnectivityReceiver.isNetworkAvailable(mActivity, true) && isListEmpty) {
                    listIsEmpty(false);
                    if(!Utils.isProgressLoaderActive())
                        Utils.showProgressLoader(mActivity);
                    new LoadDropBoxList(mActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else if (!isListEmpty) {
                    listIsEmpty(false);
                    setSongListAdapter();
                    dismissProgressWithDelay();
                }
            } else if (null == prefs.getString(ACCESS_KEY_NAME, null) &&
                    null == prefs.getString(ACCESS_SECRET_NAME, null)) {
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
        }else{
            if (null == prefs.getString(ACCESS_KEY_NAME, null) &&
                    null == prefs.getString(ACCESS_SECRET_NAME, null)) {
                isDropboxAccountConfigured = false;
                listIsEmpty(true);
                Utils.dismissProgressLoader();
            }else if(null != dropboxMediaList && dropboxMediaList.getDropboxMediaList().size() <= 0){
                isDropboxAccountConfigured = true;
                listIsEmpty(true);
                Utils.dismissProgressLoader();
            }
        }
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
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
            rootView.setLayoutManager(gridLayoutManager);
            adapter = new SongListAdapter(mActivity, DropBoxListFragment.this, dropboxMediaList.getDropboxMediaList(), ItemType.SONGS);
            rootView.setAdapter(adapter);
            rootView.setHasFixedSize(true);
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
    public  void onStart() {
        super.onStart();
        FlurryAnalyticHelper.flurryStartSession(mActivity);
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAnalyticHelper.flurryStopSession(mActivity);
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
        if(null != getActivity()) {
            if(enable){
                rootView.setVisibility(View.GONE);
            }else{
                rootView.setVisibility(View.VISIBLE);
            }
            ((CloudListActivity)getActivity()).listIsEmpty(enable, isDropboxAccountConfigured);
        }
    }
}
