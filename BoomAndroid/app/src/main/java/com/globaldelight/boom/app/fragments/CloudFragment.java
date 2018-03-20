package com.globaldelight.boom.app.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.adapters.song.SongListAdapter;
import com.globaldelight.boom.playbackEvent.utils.ItemType;

import java.util.ArrayList;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_ON_NETWORK_CONNECTED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;

/**
 * Created by adarsh on 15/03/18.
 */

public abstract class CloudFragment extends Fragment {

    protected SongListAdapter adapter;
    protected View mRootView;
    protected RecyclerView mListView;
    protected Activity mActivity;
    private ImageView emptyPlaceholderIcon;
    private TextView emptyPlaceholderTitle;
    private LinearLayout emptyPlaceHolder;
    private View mProgressView;
    private boolean mIsLoading = false;

    private BroadcastReceiver mUpdateItemSongListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ACTION_PLAYER_STATE_CHANGED:
                    if(null != adapter)
                        adapter.notifyDataSetChanged();
                    break;
                case ACTION_ON_NETWORK_CONNECTED:
                    loadSongList();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_cloud, container, false);
        mListView = mRootView.findViewById(R.id.cloud_list);
        emptyPlaceholderIcon = mRootView.findViewById(R.id.list_empty_placeholder_icon);
        emptyPlaceholderTitle = mRootView.findViewById(R.id.list_empty_placeholder_txt);
        emptyPlaceHolder = mRootView.findViewById(R.id.list_empty_placeholder);
        mProgressView = mRootView.findViewById(R.id.progress_view);

        final GridLayoutManager gridLayoutManager =
                new GridLayoutManager(mActivity, 1);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        gridLayoutManager.scrollToPosition(0);
        mListView.setLayoutManager(gridLayoutManager);
        adapter = new SongListAdapter(mActivity, this, new ArrayList<>(), ItemType.SONGS);
        mListView.setAdapter(adapter);
        mListView.setHasFixedSize(true);

        setHasOptionsMenu(true);
        if(null == mActivity)
            mActivity = getActivity();

        return mRootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            mActivity = (Activity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateItemSongListReceiver);
    }

    @Override
    public void onResume() {
        registerReceiver();
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.cloud_menu, menu);
        MenuItem syncItem = menu.findItem(R.id.action_cloud_sync);
        syncItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_cloud_sync ){
            if ( !mIsLoading ) {
                onSync();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void registerReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAYER_STATE_CHANGED);
        intentFilter.addAction(ACTION_ON_NETWORK_CONNECTED);
        if(null != getActivity())
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateItemSongListReceiver, intentFilter);
    }

    public void showEmptyList(boolean enable, boolean isAccountConfigured) {
        if (enable) {
            mListView.setVisibility(View.GONE);
            emptyPlaceHolder.setVisibility(View.VISIBLE);
            int imgResourceId = R.drawable.ic_no_music_placeholder;
            int placeHolderTxtId = R.string.no_music_placeholder_txt;
            if(!isAccountConfigured){
                imgResourceId = R.drawable.ic_cloud_placeholder;
                placeHolderTxtId = R.string.cloud_configure_placeholder_txt;
            }
            emptyPlaceholderIcon.setImageResource(imgResourceId);
            emptyPlaceholderTitle.setText(placeHolderTxtId);
        } else {
            mListView.setVisibility(View.VISIBLE);
            emptyPlaceHolder.setVisibility(View.GONE);
        }
    }

    public void onLoadingStarted() {
        mProgressView.setVisibility(View.VISIBLE);
        mIsLoading = true;
    }

    public void onLoadingFinished() {
        mProgressView.setVisibility(View.GONE);
        mIsLoading = false;
    }


    abstract void onSync();

    abstract void loadSongList();
}
