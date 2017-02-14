package com.globaldelight.boom.ui.musiclist.fragment;

import android.app.ProgressDialog;
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
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.manager.ConnectivityReceiver;
import com.globaldelight.boom.task.MediaLoader.LoadDropBoxList;
import com.globaldelight.boom.ui.musiclist.adapter.songAdapter.CloudItemListAdapter;
import com.globaldelight.boom.utils.helpers.DropBoxUtills;
import java.util.ArrayList;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_CLOUD_SYNC;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_ON_NETWORK_CONNECTED;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class DropBoxListFragment extends Fragment  implements DropboxMediaList.IDropboxUpdater {

    private DropboxMediaList dropboxMediaList;
    private ProgressDialog progressLoader;
    private CloudItemListAdapter adapter;
    private RecyclerView rootView;
    private int listSize = 0;

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
                    if(null != progressLoader)
                        progressLoader.show();
                    LoadDropboxList();
                    break;
                case ACTION_CLOUD_SYNC:
                    if(null != progressLoader)
                        progressLoader.show();
                    try{
                        if(null != dropboxMediaList)
                            dropboxMediaList.clearDropboxContent();
                        LoadDropboxList();
                    }catch (Exception e){}
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);

        initViews();

        return rootView;
    }

    private void initViews() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        intentFilter.addAction(ACTION_ON_NETWORK_CONNECTED);
        intentFilter.addAction(ACTION_CLOUD_SYNC);
        getActivity().registerReceiver(mUpdateItemSongListReceiver, intentFilter);

        progressLoader = new ProgressDialog(getActivity());
        progressLoader.setCanceledOnTouchOutside(false);
        progressLoader.show();

        dropboxMediaList = DropboxMediaList.getDropboxListInstance(getActivity());
        dropboxMediaList.setDropboxUpdater(this);
        DropBoxUtills.checkDropboxAuthentication(getActivity());
        setSongListAdapter(dropboxMediaList.getDropboxMediaList());
    }

    @Override
    public void onResume() {
        super.onResume();
        if(listSize <= 0)
            progressLoader.show();
        LoadDropboxList();
    }

    private void LoadDropboxList(){
        if (null != App.getDropboxAPI()
                && ConnectivityReceiver.isNetworkAvailable(getContext())) {
            resetAuthentication();
            new LoadDropBoxList(getActivity()).execute();
        }
        setForAnimation();
    }

    private void resetAuthentication(){
        AndroidAuthSession session = App.getDropboxAPI().getSession();
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                TokenPair tokens = session.getAccessTokenPair();
                DropBoxUtills.storeKeys(getContext(), tokens.key, tokens.secret);
            } catch (IllegalStateException e) {
                Toast.makeText(getContext(),getResources().getString(R.string.dropbox_authenticate_problem)
                        + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
    }

    private void setSongListAdapter(ArrayList<? extends IMediaItemBase> iMediaItemList) {
        listSize = iMediaItemList.size();
        if(iMediaItemList.size() > 0 && progressLoader.isShowing())
            dismissLoader();
        final GridLayoutManager gridLayoutManager =
                new GridLayoutManager(getActivity(), 1);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        gridLayoutManager.scrollToPosition(0);
        rootView.setLayoutManager(gridLayoutManager);
        adapter = new CloudItemListAdapter(getActivity(), DropBoxListFragment.this, iMediaItemList, ItemType.SONGS);
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
//        listIsEmpty(adapter.getItemCount());
        listSize = dropboxMediaList.getDropboxMediaList().size();
        dismissLoader();
    }

    @Override
    public void ClearList() {
        notifyAdapter(dropboxMediaList.getDropboxMediaList());
    }

    private void dismissLoader() {
        progressLoader.dismiss();
    }

    private void notifyAdapter(ArrayList<IMediaItemBase> mediaList){
        if(null != adapter){
            adapter.updateMediaList(mediaList);
        }
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mUpdateItemSongListReceiver);
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
