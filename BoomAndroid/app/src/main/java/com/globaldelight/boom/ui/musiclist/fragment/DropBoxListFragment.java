package com.globaldelight.boom.ui.musiclist.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.TokenPair;
import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.MediaCallback.DropboxMediaList;
import com.globaldelight.boom.Media.ItemType;
import com.globaldelight.boom.manager.ConnectivityReceiver;
import com.globaldelight.boom.task.MediaLoader.LoadDropBoxList;
import com.globaldelight.boom.ui.musiclist.activity.CloudListActivity;
import com.globaldelight.boom.ui.musiclist.adapter.songAdapter.CloudItemListAdapter;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.helpers.DropBoxUtills;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_CLOUD_SYNC;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_ON_NETWORK_CONNECTED;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;
import static com.globaldelight.boom.utils.helpers.DropBoxUtills.ACCESS_KEY_NAME;
import static com.globaldelight.boom.utils.helpers.DropBoxUtills.ACCESS_SECRET_NAME;
import static com.globaldelight.boom.utils.helpers.DropBoxUtills.ACCOUNT_PREFS_NAME;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class DropBoxListFragment extends Fragment  implements DropboxMediaList.IDropboxUpdater {

    private DropboxMediaList dropboxMediaList;
    private boolean isDropboxAccountConfigured = true;
    private CloudItemListAdapter adapter;
    SharedPreferences prefs;
    private View rootView;
    private RecyclerView recyclerView;
    ImageView emptyPlaceholderIcon;
    RegularTextView emptyPlaceholderTitle;
    LinearLayout emptyPlaceHolder;
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
        rootView = inflater.inflate(R.layout.fragment_cloud_list, container, false);
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
        emptyPlaceholderIcon = (ImageView) rootView.findViewById(R.id.list_empty_placeholder_icon);
        emptyPlaceholderTitle = (RegularTextView) rootView.findViewById(R.id.list_empty_placeholder_txt);
        emptyPlaceHolder = (LinearLayout) rootView.findViewById(R.id.list_empty_placeholder);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.cloud_item_list);
        recyclerView.setNestedScrollingEnabled(false);
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
            if(!Utils.isProgressLoaderActive())
                Utils.showProgressLoader(mActivity);
            boolean isListEmpty = dropboxMediaList.getDropboxMediaList().size() <= 0;
            resetAuthentication();
            if (null != prefs.getString(ACCESS_KEY_NAME, null) &&
                    null != prefs.getString(ACCESS_SECRET_NAME, null)) {
                isDropboxAccountConfigured = true;
                if (null != App.getDropboxAPI()
                        && ConnectivityReceiver.isNetworkAvailable(mActivity, true) && isListEmpty) {
                    listIsEmpty(false);
                    new LoadDropBoxList(mActivity).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else if (!isListEmpty) {
                    listIsEmpty(false);
                    setSongListAdapter();
                    finishDropboxLoading();
                }else{
                    Utils.dismissProgressLoader();
                }
            } else if (null == prefs.getString(ACCESS_KEY_NAME, null) &&
                    null == prefs.getString(ACCESS_SECRET_NAME, null)) {
                isDropboxAccountConfigured = false;
                listIsEmpty(true);
                Utils.dismissProgressLoader();
            }else{
                Utils.dismissProgressLoader();
            }
            setForAnimation();
        }
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
            recyclerView.setLayoutManager(gridLayoutManager);
            adapter = new CloudItemListAdapter(mActivity, DropBoxListFragment.this, dropboxMediaList.getDropboxMediaList(), ItemType.SONGS);
            recyclerView.setAdapter(adapter);
            recyclerView.setHasFixedSize(true);
        }
    }

    private void notifyAdapter() {
        adapter.updateMediaList(dropboxMediaList.getDropboxMediaList());
    }

    @Override
    public void UpdateDropboxEntryList() {
        if(null != adapter) {
            notifyAdapter();
        }else {
            LoadDropboxList();
        }
    }

    @Override
    public void finishDropboxLoading() {
        if(null != adapter)
            notifyAdapter();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.dismissProgressLoader();
            }
        }, 3000);
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
            if (enable) {
                emptyPlaceHolder.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                Drawable imgResource = null;
                String placeHolderTxt = null;
                if(isDropboxAccountConfigured){
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
