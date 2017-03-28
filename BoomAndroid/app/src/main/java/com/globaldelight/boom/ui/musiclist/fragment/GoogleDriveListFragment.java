package com.globaldelight.boom.ui.musiclist.fragment;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.MediaCallback.GoogleDriveMediaList;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.Media.ItemType;
import com.globaldelight.boom.manager.ConnectivityReceiver;
import com.globaldelight.boom.ui.musiclist.activity.CloudListActivity;
import com.globaldelight.boom.ui.musiclist.adapter.songAdapter.CloudItemListAdapter;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.handlers.Preferences;
import com.globaldelight.boom.utils.helpers.GoogleDriveHandler;
import java.util.ArrayList;
import static android.app.Activity.RESULT_OK;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_CLOUD_SYNC;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_ON_NETWORK_CONNECTED;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;
import com.globaldelight.boom.utils.Utils;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class GoogleDriveListFragment extends Fragment  implements GoogleDriveMediaList.IGoogleDriveMediaUpdater, PermissionChecker.OnPermissionResponse {

    private GoogleDriveMediaList googleDriveMediaList;
    private GoogleDriveHandler googleDriveHandler;
    private boolean isGoogleAccountConfigured = false;
    private CloudItemListAdapter adapter;
    private View rootView;
    private RecyclerView recyclerView;
    private PermissionChecker permissionChecker;
    ImageView emptyPlaceholderIcon;
    RegularTextView emptyPlaceholderTitle;
    LinearLayout emptyPlaceHolder;
    Activity mActivity;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GoogleDriveListFragment() {
    }

    public Context getFragmentContext(){
        return mActivity;
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
                    checkPermissions();
                    break;
                case ACTION_CLOUD_SYNC:
                    if(null != googleDriveMediaList) {
                        googleDriveMediaList.clearGoogleDriveMediaContent();
                    }
                    checkPermissions();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_cloud_list, container, false);
        if(null == mActivity)
            mActivity = getActivity();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
        checkPermissions();
        FlurryAnalyticHelper.init(mActivity);
    }

    private void initViews() {
        Preferences.writeBoolean(mActivity, Preferences.GOOGLE_DRIVE_ACCOUNT_CHANGED, false);
        ((CloudListActivity)mActivity).setTitle(getResources().getString(R.string.google_drive));
        emptyPlaceholderIcon = (ImageView) rootView.findViewById(R.id.list_empty_placeholder_icon);
        emptyPlaceholderTitle = (RegularTextView) rootView.findViewById(R.id.list_empty_placeholder_txt);
        emptyPlaceHolder = (LinearLayout) rootView.findViewById(R.id.list_empty_placeholder);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.cloud_item_list);
        recyclerView.setNestedScrollingEnabled(false);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                googleDriveMediaList = GoogleDriveMediaList.geGoogleDriveMediaListInstance(mActivity);
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
        registerReceiver();
        if (Preferences.readBoolean(mActivity, Preferences.GOOGLE_DRIVE_ACCOUNT_CHANGED, false)) {
            if (ConnectivityReceiver.isNetworkAvailable(mActivity, true)) {
                checkPermissions();
            }
            Preferences.writeBoolean(mActivity, Preferences.GOOGLE_DRIVE_ACCOUNT_CHANGED, false);
        }
        if( null == App.getUserPreferenceHandler().getGoogleAccountName()){
            isGoogleAccountConfigured = false;
            listIsEmpty(true);
            Utils.dismissProgressLoader();
        }

        if(null != adapter)
            adapter.notifyDataSetChanged();
    }

    public void checkPermissions() {
        permissionChecker = new PermissionChecker(mActivity, mActivity, rootView);
        permissionChecker.check(Manifest.permission.GET_ACCOUNTS, getResources().getString(R.string.account_permission), this);
    }

    @Override
    public void onAccepted() {
        LoadGoogleDriveList();
    }

    private void LoadGoogleDriveList(){
        Utils.showProgressLoader(mActivity);
        if( null != App.getUserPreferenceHandler().getGoogleAccountName()){
            isGoogleAccountConfigured = true;
            listIsEmpty(false);
            if(googleDriveMediaList.getGoogleDriveMediaList().size() <= 0){
                if (ConnectivityReceiver.isNetworkAvailable(mActivity, true)) {
                    googleDriveHandler.getResultsFromApi();
                }else{
                    Utils.dismissProgressLoader();
                }
            }else if(googleDriveMediaList.getGoogleDriveMediaList().size() > 0){
                setSongListAdapter(true);
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
                recyclerView.setLayoutManager(gridLayoutManager);
                adapter = new CloudItemListAdapter(mActivity, GoogleDriveListFragment.this, googleDriveMediaList.getGoogleDriveMediaList(), ItemType.SONGS);
                recyclerView.setAdapter(adapter);
                recyclerView.setHasFixedSize(true);
            } else {
                adapter.updateMediaList(googleDriveMediaList.getGoogleDriveMediaList());
            }

            if (googleDriveMediaList.getGoogleDriveMediaList().size() <= 0) {
                listIsEmpty(true);
            } else {
                listIsEmpty(false);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isUpdate)
                        Utils.dismissProgressLoader();
                }
            }, 3000);
        }
    }

    @Override
    public void onPause() {
        if(null != mActivity)
            mActivity.unregisterReceiver(mUpdateItemSongListReceiver);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        googleDriveMediaList.setGoogleDriveMediaUpdater(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void registerReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        intentFilter.addAction(ACTION_ON_NETWORK_CONNECTED);
        intentFilter.addAction(ACTION_CLOUD_SYNC);
        if(null != mActivity)
            mActivity.registerReceiver(mUpdateItemSongListReceiver, intentFilter);
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
    public void onGoogleDriveMediaListUpdate() {
        setSongListAdapter(false);
        setForAnimation();
    }

    @Override
    public void onFinishListLoading() {
        if(null != adapter)
            adapter.notifyDataSetChanged();
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
        if(null != getActivity())
            Toast.makeText(mActivity, getResources().getString(R.string.google_drive_loading_error)
                + e, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyList() {
        Utils.dismissProgressLoader();
        listIsEmpty(true);
    }

    @Override
    public void onClearList() {
        setSongListAdapter(false);
        listIsEmpty(false);
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK){
            isGoogleAccountConfigured = false;
            listIsEmpty(true);
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

    public void listIsEmpty(boolean enable) {
        if(null != getActivity()) {
            if (enable) {
                emptyPlaceHolder.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                Drawable imgResource = null;
                String placeHolderTxt = null;
                if(isGoogleAccountConfigured){
                    imgResource = getResources().getDrawable(R.drawable.ic_no_music_placeholder, null);
                    placeHolderTxt = getResources().getString(R.string.no_music_placeholder_txt);
                }else {
                    imgResource = getResources().getDrawable(R.drawable.ic_cloud_placeholder, null);
                    placeHolderTxt = getResources().getString(R.string.cloud_configure_placeholder_txt);
                }
                emptyPlaceholderIcon.setImageDrawable(imgResource);
                emptyPlaceholderTitle.setText(placeHolderTxt);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyPlaceHolder.setVisibility(View.GONE);
            }
        }
    }
}
