package com.globaldelight.boom.ui.musiclist.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCallback.DropboxMediaList;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.Media.ItemType;
import com.globaldelight.boom.manager.ConnectivityReceiver;
import com.globaldelight.boom.task.MediaLoader.LoadDropBoxList;
import com.globaldelight.boom.ui.musiclist.adapter.songAdapter.CloudItemListAdapter;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.helpers.DropBoxUtills;
import java.util.ArrayList;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_CLOUD_SYNC;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_ON_NETWORK_CONNECTED;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class DropBoxListFragment extends Fragment  implements DropboxMediaList.IDropboxUpdater {

    private DropboxMediaList dropboxMediaList;
    private CloudItemListAdapter adapter;
    private RecyclerView rootView;
    private int listSize = 0;
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
                    notifyAdapter(null);
                    break;
                case ACTION_ON_NETWORK_CONNECTED:
                    LoadDropboxList();
                    break;
                case ACTION_CLOUD_SYNC:
                    try{
                        if(null != dropboxMediaList)
                            dropboxMediaList.clearDropboxContent();
                        LoadDropboxList();
                    }catch (Exception e){}
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

    private void initViews() {
        dropboxMediaList = DropboxMediaList.getDropboxListInstance(mActivity);
        dropboxMediaList.setDropboxUpdater(this);
        DropBoxUtills.checkDropboxAuthentication(mActivity);
        setSongListAdapter(dropboxMediaList.getDropboxMediaList());
    }

    @Override
    public void onResume() {
        registerReceiver();
        super.onResume();
        if(null != dropboxMediaList)
            listSize = dropboxMediaList.getDropboxMediaList().size();
        LoadDropboxList();
    }

    @Override
    public void onPause() {
        if(null != mActivity)
            mActivity.unregisterReceiver(mUpdateItemSongListReceiver);
        super.onPause();
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
        boolean isListEmpty = dropboxMediaList.getDropboxMediaList().size() <= 0;
        if(isListEmpty){
            Utils.showProgressLoader(mActivity);
        }else{
            Utils.dismissProgressLoader();
        }
        if (null != App.getDropboxAPI()
                && ConnectivityReceiver.isNetworkAvailable(mActivity, true) && isListEmpty) {
            resetAuthentication();
            new LoadDropBoxList(mActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else{
            Utils.dismissProgressLoader();
            if(null != adapter)
                adapter.notifyDataSetChanged();
        }
        setForAnimation();
    }

    private void resetAuthentication(){
        AndroidAuthSession session = App.getDropboxAPI().getSession();
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                TokenPair tokens = session.getAccessTokenPair();
                DropBoxUtills.storeKeys(mActivity, tokens.key, tokens.secret);
            } catch (IllegalStateException e) {
                Toast.makeText(mActivity,getResources().getString(R.string.dropbox_authenticate_problem)
                        + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
    }

    private void setSongListAdapter(ArrayList<? extends IMediaItemBase> iMediaItemList) {
        listSize = iMediaItemList.size();
        if(iMediaItemList.size() > 0)
            Utils.dismissProgressLoader();
        final GridLayoutManager gridLayoutManager =
                new GridLayoutManager(mActivity, 1);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        gridLayoutManager.scrollToPosition(0);
        rootView.setLayoutManager(gridLayoutManager);
        adapter = new CloudItemListAdapter(mActivity, DropBoxListFragment.this, iMediaItemList, ItemType.SONGS);
        rootView.setAdapter(adapter);
        rootView.setHasFixedSize(true);
//        listIsEmpty(iMediaItemList.size());
    }

    @Override
    public void UpdateDropboxEntryList() {
        notifyAdapter(dropboxMediaList.getDropboxMediaList());
    }

    @Override
    public void finishDropboxLoading() {
        notifyAdapter(dropboxMediaList.getDropboxMediaList());
        listSize = dropboxMediaList.getDropboxMediaList().size();
        Utils.dismissProgressLoader();
    }

    @Override
    public void ClearList() {
        notifyAdapter(dropboxMediaList.getDropboxMediaList());
    }

    private void notifyAdapter(ArrayList<IMediaItemBase> mediaList){
        if(null != adapter){
            adapter.updateMediaList(mediaList);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
}
