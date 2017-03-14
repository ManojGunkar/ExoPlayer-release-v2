package com.globaldelight.boom.ui.musiclist.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.Media.ItemType;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCallback.RecentPlayedMediaList;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.task.MediaLoader.LoadRecentPlayedList;
import com.globaldelight.boom.ui.musiclist.adapter.songAdapter.CloudItemListAdapter;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;
import static com.globaldelight.boom.task.PlayerEvents.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class RecentPlayedFragment extends Fragment implements RecentPlayedMediaList.IRecentPlayedUpdater, EasyPermissions.PermissionCallbacks  {
    private RecentPlayedMediaList recentPlayedMediaList;
    private RecyclerView rootView;
    private CloudItemListAdapter adapter;
    Activity mActivity;

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
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RecentPlayedFragment() {
    }

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
        rootView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);
        if(null == mActivity)
            mActivity = getActivity();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(null != adapter )
            listIsEmpty(adapter.getItemCount());
        else
            listIsEmpty(0);
    }

    private void initViews() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        mActivity.registerReceiver(mUpdateItemSongListReceiver, intentFilter);

        recentPlayedMediaList = RecentPlayedMediaList.getRecentPlayedListInstance(mActivity);
        recentPlayedMediaList.setRecentPlayedUpdater(this);
        recentPlayedMediaList.clearRecentPlayedContent();
        setSongListAdapter(recentPlayedMediaList.getRecentPlayedMediaList());

        if(EasyPermissions.hasPermissions(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            LoadRecentPlayedList();
        }else {
            EasyPermissions.requestPermissions(
                    RecentPlayedFragment.this, getResources().getString(R.string.storage_permission),
                    REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void setSongListAdapter(ArrayList<IMediaItemBase> recentPlayedMediaList) {
        final GridLayoutManager gridLayoutManager =
                new GridLayoutManager(mActivity, 1);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        gridLayoutManager.scrollToPosition(0);
        rootView.setLayoutManager(gridLayoutManager);
        adapter = new CloudItemListAdapter(mActivity, RecentPlayedFragment.this, recentPlayedMediaList, ItemType.RECENT_PLAYED);
        rootView.setAdapter(adapter);
        rootView.setHasFixedSize(true);
        listIsEmpty(recentPlayedMediaList.size());
    }

    private void LoadRecentPlayedList(){
        if (recentPlayedMediaList.getRecentPlayedMediaList().isEmpty()) {
            new LoadRecentPlayedList(mActivity).execute();
        } else {
            notifyAdapter(recentPlayedMediaList.getRecentPlayedMediaList());
        }
        setForAnimation();
    }


    @Override
    public void onUpdateRecentPlayedList() {
        notifyAdapter(recentPlayedMediaList.getRecentPlayedMediaList());
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if(requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            LoadRecentPlayedList();
        }
        setForAnimation();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        mActivity.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, RecentPlayedFragment.this);
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
        if(null != getActivity()) {
            if (size < 1) {
                Drawable imgResource = getResources().getDrawable(R.drawable.ic_favorites_placeholder, null);
                String placeHolderTxt = getResources().getString(R.string.favorite_empty_placeholder_txt);
//                ((MainActivity) mActivity).setEmptyPlaceHolder(imgResource, placeHolderTxt, true);
                rootView.setVisibility(View.GONE);
            } else {
//                ((MainActivity) mActivity).setEmptyPlaceHolder(null, null, false);
                rootView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity.unregisterReceiver(mUpdateItemSongListReceiver);
    }
}
