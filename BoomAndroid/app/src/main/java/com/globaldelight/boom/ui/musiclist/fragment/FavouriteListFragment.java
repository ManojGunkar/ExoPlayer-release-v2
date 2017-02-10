package com.globaldelight.boom.ui.musiclist.fragment;

import android.Manifest;
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

import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCallback.FavouriteMediaList;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.task.MediaLoader.LoadFavouriteList;
import com.globaldelight.boom.ui.musiclist.adapter.songAdapter.CloudItemListAdapter;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;
import static com.globaldelight.boom.task.PlayerEvents.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class FavouriteListFragment extends Fragment implements FavouriteMediaList.IFavouriteUpdater, EasyPermissions.PermissionCallbacks  {
    private FavouriteMediaList favouriteMediaList;
    private RecyclerView rootView;
    private CloudItemListAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FavouriteListFragment() {
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

    private void initViews() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        getActivity().registerReceiver(mUpdateItemSongListReceiver, intentFilter);

        favouriteMediaList = FavouriteMediaList.getFavouriteListInstance(getActivity());
        favouriteMediaList.setFavouriteUpdater(this);
        favouriteMediaList.clearFavouriteContent();
        setSongListAdapter(favouriteMediaList.getFavouriteMediaList());

        if(EasyPermissions.hasPermissions(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            LoadFavouriteList();
        }else {
            EasyPermissions.requestPermissions(
                    FavouriteListFragment.this, getResources().getString(R.string.storage_permission),
                    REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void setSongListAdapter(ArrayList<IMediaItemBase> favouriteMediaList) {
        final GridLayoutManager gridLayoutManager =
                new GridLayoutManager(getActivity(), 1);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        gridLayoutManager.scrollToPosition(0);
        rootView.setLayoutManager(gridLayoutManager);
        adapter = new CloudItemListAdapter(getActivity(), FavouriteListFragment.this, favouriteMediaList, ItemType.FAVOURITE);
        rootView.setAdapter(adapter);
        rootView.setHasFixedSize(true);
        listIsEmpty(favouriteMediaList.size());
    }

    private void LoadFavouriteList(){
        if (favouriteMediaList.getFavouriteMediaList().isEmpty()) {
            new LoadFavouriteList(getActivity()).execute();
        } else {
            notifyAdapter(favouriteMediaList.getFavouriteMediaList());
        }
        setForAnimation();
    }


    @Override
    public void onUpdateFavouriteList() {
        notifyAdapter(favouriteMediaList.getFavouriteMediaList());
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if(requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            LoadFavouriteList();
        }
        setForAnimation();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        getActivity().onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, FavouriteListFragment.this);
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
    }

    private void notifyAdapter(ArrayList<IMediaItemBase> mediaList){
        if(null != adapter){
            adapter.updateMediaList(mediaList);
            listIsEmpty(adapter.getItemCount());
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
    public void onDestroy() {
        getActivity().unregisterReceiver(mUpdateItemSongListReceiver);
        super.onDestroy();
    }
}
