package com.globaldelight.boom.ui.musiclist.fragment;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCallback.GoogleDriveMediaList;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.Media.ItemType;
import com.globaldelight.boom.manager.ConnectivityReceiver;
import com.globaldelight.boom.ui.musiclist.adapter.songAdapter.CloudItemListAdapter;
import com.globaldelight.boom.ui.widgets.BoomDialogView;
import com.globaldelight.boom.utils.PermissionChecker;
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
    private CloudItemListAdapter adapter;
    private RecyclerView rootView;
    private PermissionChecker permissionChecker;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GoogleDriveListFragment() {
    }

    private BroadcastReceiver mUpdateItemSongListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY:
                    notifyAdapter(null);
                    break;
                case ACTION_ON_NETWORK_CONNECTED:
                    Utils.showProgressLoader(getContext());
                    checkPermissions();
                case ACTION_CLOUD_SYNC:
                    Utils.showProgressLoader(getContext());
                    if(null != googleDriveMediaList) {
                        googleDriveMediaList.clearGoogleDriveMediaContent();
                        checkPermissions();
                    }
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);

        initViews();

        checkPermissions();

        return rootView;
    }

    @Override
    public void onResume() {
        notifyAdapter(googleDriveMediaList.getGoogleDriveMediaList());
        super.onResume();
        registerReceiver();
    }

    @Override
    public void onPause() {
        if(null != getActivity())
            getActivity().unregisterReceiver(mUpdateItemSongListReceiver);
        super.onPause();
    }

    public void checkPermissions() {
        permissionChecker = new PermissionChecker(getContext(), getActivity(), rootView);
        permissionChecker.check(Manifest.permission.GET_ACCOUNTS, getResources().getString(R.string.account_permission), this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void LoadGoogleDriveList(){
        if (googleDriveMediaList.getGoogleDriveMediaList().size() <= 0 &&
                ConnectivityReceiver.isNetworkAvailable(getContext())) {
            googleDriveHandler.getResultsFromApi();
        }else{
            Utils.dismissProgressLoader();
        }
        setForAnimation();
    }

    private void registerReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        intentFilter.addAction(ACTION_ON_NETWORK_CONNECTED);
        intentFilter.addAction(ACTION_CLOUD_SYNC);
        if(null != getActivity())
            getActivity().registerReceiver(mUpdateItemSongListReceiver, intentFilter);
    }

    private void initViews() {
        Utils.showProgressLoader(getContext());
        googleDriveMediaList = GoogleDriveMediaList.geGoogleDriveMediaListInstance(getActivity());
        googleDriveMediaList.setGoogleDriveMediaUpdater(this);
        googleDriveHandler = new GoogleDriveHandler(GoogleDriveListFragment.this);
        googleDriveMediaList.setGoogleDriveHandler(googleDriveHandler);
        googleDriveHandler.getGoogleAccountCredential();
        googleDriveHandler.getGoogleApiClient();
        googleDriveHandler.connectGoogleAccount();
        setSongListAdapter(googleDriveMediaList.getGoogleDriveMediaList());
    }

    @Override
    public void onGoogleDriveMediaListUpdate() {
        notifyAdapter(googleDriveMediaList.getGoogleDriveMediaList());
        setForAnimation();
    }

    @Override
    public void onFinishListLoading() {
        Utils.dismissProgressLoader();
    }

    @Override
    public void onRequestCancelled() {
        Utils.dismissProgressLoader();
        Toast.makeText(getContext(), getResources().getString(R.string.request_cancelled), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String e) {
        Utils.dismissProgressLoader();
        Toast.makeText(getContext(), getResources().getString(R.string.google_drive_loading_error)
                + e, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyList() {
        Utils.dismissProgressLoader();
        Toast.makeText(getContext(), getResources().getString(R.string.empty_list), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClearList() {
        notifyAdapter(googleDriveMediaList.getGoogleDriveMediaList());
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
    }

    private void setSongListAdapter(ArrayList<? extends IMediaItemBase> iMediaItemList) {
        if(iMediaItemList.size() > 0)
            Utils.dismissProgressLoader();

        final GridLayoutManager gridLayoutManager =
                new GridLayoutManager(getActivity(), 1);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        gridLayoutManager.scrollToPosition(0);
        rootView.setLayoutManager(gridLayoutManager);
        adapter = new CloudItemListAdapter(getActivity(), GoogleDriveListFragment.this, iMediaItemList, ItemType.SONGS);
        rootView.setAdapter(adapter);
        rootView.setHasFixedSize(true);
//        listIsEmpty(iMediaItemList.size());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GoogleDriveHandler.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(getContext(), getResources().getString(R.string.require_google_play_service), Toast.LENGTH_SHORT).show();
//                    dismissLoader();
                } else {
                    LoadGoogleDriveList();
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
                        LoadGoogleDriveList();
                    }
                }else{
                    Utils.dismissProgressLoader();
                }
                break;
            case GoogleDriveHandler.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    LoadGoogleDriveList();
                }else {
                    Utils.dismissProgressLoader();
                }
                break;
        }
    }

    private void notifyAdapter(ArrayList<IMediaItemBase> mediaList){
        if(null != adapter){
            adapter.updateMediaList(mediaList);
        }
    }

    public void listIsEmpty(int size) {
        if (size < 1) {
//                emptyView.setVisibility(View.VISIBLE);
            rootView.setVisibility(View.GONE);
        }else{
//            emptyView.setVisibility(View.GONE);
            rootView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAccepted() {
        LoadGoogleDriveList();
    }

    @Override
    public void onDecline() {
        Utils.dismissProgressLoader();
        if(null != getActivity())
            getActivity().onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
