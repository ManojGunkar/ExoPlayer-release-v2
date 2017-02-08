package com.globaldelight.boom.ui.musiclist.fragment;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.TokenPair;
import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCallback.DropboxMediaList;
import com.globaldelight.boom.data.MediaCallback.FavouriteMediaList;
import com.globaldelight.boom.data.MediaCallback.GoogleDriveMediaList;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.task.MediaLoader.LoadDropBoxList;
import com.globaldelight.boom.task.MediaLoader.LoadFavouriteList;
import com.globaldelight.boom.ui.musiclist.adapter.songAdapter.CloudItemListAdapter;
import com.globaldelight.boom.utils.helpers.DropBoxUtills;
import com.globaldelight.boom.utils.helpers.GoogleDriveHandler;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;
import static com.globaldelight.boom.task.PlayerEvents.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE;
import static com.globaldelight.boom.utils.helpers.GoogleDriveHandler.REQUEST_PERMISSION_GET_ACCOUNTS;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class GoogleDriveListFragment extends Fragment  implements GoogleDriveMediaList.IGoogleDriveMediaUpdater, EasyPermissions.PermissionCallbacks {

    private GoogleDriveMediaList googleDriveMediaList;
    private GoogleDriveHandler googleDriveHandler;
    private ProgressDialog progressLoader;
    private CloudItemListAdapter adapter;
    private RecyclerView rootView;

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
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);

        initViews();

        return rootView;
    }

    @Override
    public void onResume() {
        initLibrary();
        super.onResume();
    }

    private void initLibrary() {
        progressLoader.show();
        if (EasyPermissions.hasPermissions(getContext(), Manifest.permission.GET_ACCOUNTS)) {
            LoadGoogleDriveList();
        } else {
            EasyPermissions.requestPermissions(
                    GoogleDriveListFragment.this, "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
        }
    }

    private void LoadGoogleDriveList(){
        if (googleDriveMediaList.getGoogleDriveMediaList().isEmpty()) {
            googleDriveHandler.getResultsFromApi();
        } else {
            notifyAdapter(googleDriveMediaList.getGoogleDriveMediaList());
        }
    }


    private void initViews() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        getActivity().registerReceiver(mUpdateItemSongListReceiver, intentFilter);

        progressLoader = new ProgressDialog(getActivity());
        progressLoader.show();

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
        dismissLoader();
        notifyAdapter(googleDriveMediaList.getGoogleDriveMediaList());
    }

    @Override
    public void onRequestCancelled() {
        dismissLoader();
        Toast.makeText(getContext(), getResources().getString(R.string.request_cancelled), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String e) {
        dismissLoader();
        Toast.makeText(getContext(), getResources().getString(R.string.google_drive_loading_error)
                + e, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyList() {
        dismissLoader();
        Toast.makeText(getContext(), getResources().getString(R.string.empty_list), Toast.LENGTH_SHORT).show();
    }

    private void dismissLoader(){
        if(progressLoader.isShowing()){
            progressLoader.dismiss();
        }
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
    }

    private void setSongListAdapter(ArrayList<? extends IMediaItemBase> iMediaItemList) {
        final GridLayoutManager gridLayoutManager =
                new GridLayoutManager(getActivity(), 1);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        gridLayoutManager.scrollToPosition(0);
        rootView.setLayoutManager(gridLayoutManager);
        adapter = new CloudItemListAdapter(getActivity(), GoogleDriveListFragment.this, iMediaItemList, ItemType.SONGS);
        rootView.setAdapter(adapter);
        rootView.setHasFixedSize(true);
        listIsEmpty(iMediaItemList.size());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GoogleDriveHandler.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(getContext(), getResources().getString(R.string.require_google_play_service), Toast.LENGTH_SHORT).show();
                } else {
                    googleDriveHandler.getResultsFromApi();
                }
                progressLoader.dismiss();
                break;
            case GoogleDriveHandler.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(GoogleDriveHandler.PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        googleDriveHandler.setSelectedGoogleAccountName(accountName);
                        googleDriveHandler.getResultsFromApi();
                    }
                }else{
                    progressLoader.dismiss();
                }
                break;
            case GoogleDriveHandler.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    googleDriveHandler.getResultsFromApi();
                }
                progressLoader.dismiss();
                break;
        }
    }

    private void notifyAdapter(ArrayList<IMediaItemBase> mediaList){
        if(null != adapter){
            adapter.updateMediaList(mediaList);
            listIsEmpty(adapter.getItemCount());
            dismissLoader();
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
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if(requestCode == REQUEST_PERMISSION_GET_ACCOUNTS) {
            LoadGoogleDriveList();
        }
        setForAnimation();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        dismissLoader();
        getActivity().onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, GoogleDriveListFragment.this);
    }
}
