package com.globaldelight.boom.ui.musiclist.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.globaldelight.boom.Media.ItemType;
import com.globaldelight.boom.Media.MediaController;
import com.globaldelight.boom.Media.MediaType;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.task.PlayerEvents;
import com.globaldelight.boom.ui.musiclist.adapter.PlayListAdapter;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.decorations.AlbumListSpacesItemDecoration;
import com.globaldelight.boom.utils.decorations.SimpleDividerItemDecoration;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 28-02-17.
 */

public class PlayListsFragment extends Fragment {
    private Activity mActivity;
    private View mainView;
    private RecyclerView recyclerView;
    private PlayListAdapter playListAdapter;
    private ArrayList<? extends IMediaItemBase> defaultPlayList, mBoomPlayList;
    private IMediaItemBase mFavourite, mRecentPlayed;

    private BroadcastReceiver mUpdateItemSongListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case PlayerEvents.ACTION_UPDATE_PLAYLIST:
                    if (null != playListAdapter) {
                        new LoadCollectionList().execute();
                    }
                    break;
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayerEvents.ACTION_UPDATE_PLAYLIST);
        getActivity().registerReceiver(mUpdateItemSongListReceiver, filter);
        if (context instanceof Activity){
            mActivity = (Activity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  = getItemView(inflater, container);
        mainView = view;
        if(null == mActivity)
            mActivity = getActivity();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
        FlurryAnalyticHelper.init(mActivity);
    }

    private View getItemView(LayoutInflater inflater, ViewGroup container){
        View view = inflater.inflate(R.layout.fragment_music_library_list,
                container, false);
        return view;
    }

    @Override
    public void onDetach() {
        getActivity().unregisterReceiver(mUpdateItemSongListReceiver);
        super.onDetach();
    }

    public void listIsEmpty(int size) {
        mainView.findViewById(R.id.lib_container).setVisibility(View.VISIBLE);
        if(size < 1) {
            ((ImageView) mainView.findViewById(R.id.list_empty_placeholder_icon)).setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_no_music_placeholder, null));
            ((RegularTextView) mainView.findViewById(R.id.list_empty_placeholder_txt)).setText(mActivity.getResources().getString(R.string.no_music_placeholder_txt));
        }
        mainView.findViewById(R.id.list_empty_placeholder).setVisibility(size < 1 ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(size > 0 ? View.VISIBLE : View.GONE);
    }

    private void initViews() {
        recyclerView = (RecyclerView) mainView.findViewById(R.id.albumsListContainer);
        new LoadCollectionList().execute();
    }

    private class LoadCollectionList extends AsyncTask<Void, Void, Void> {
        GridLayoutManager gridLayoutManager;
        @Override
        protected Void doInBackground(Void... voids) {
            defaultPlayList = MediaController.getInstance(mActivity).getPlayList();
            mBoomPlayList = MediaController.getInstance(mActivity).getBoomPlayList();
            mFavourite = new MediaItemCollection(0, getResources().getString(R.string.favourite_list), null, null, MediaController.getInstance(mActivity).getFavouriteCount(),
                    0, ItemType.FAVOURITE, MediaType.DEVICE_MEDIA_LIB, ItemType.FAVOURITE);
            mRecentPlayed = new MediaItemCollection(0, getResources().getString(R.string.recently_played), null, null, MediaController.getInstance(mActivity).getRecentPlayedItemCount(),
                    0, ItemType.RECENT_PLAYED, MediaType.DEVICE_MEDIA_LIB, ItemType.RECENT_PLAYED);
            return null;
        }

        @Override
        protected synchronized void onPostExecute(Void v) {
            super.onPostExecute(v);
            if (null != mActivity) {
                boolean isPhone = Utils.isPhone(mActivity);
                if(isPhone){
                    gridLayoutManager =
                            new GridLayoutManager(mActivity, 2);
                }else{
                    gridLayoutManager =
                            new GridLayoutManager(mActivity, 3);
                }
                gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                gridLayoutManager.scrollToPosition(0);
                recyclerView.setLayoutManager(gridLayoutManager);
                recyclerView.addItemDecoration(new SimpleDividerItemDecoration(mActivity, Utils.getWindowWidth(mActivity)));
                recyclerView.addItemDecoration(new AlbumListSpacesItemDecoration(Utils.dpToPx(mActivity, 0)));
                if(null == playListAdapter) {
                    playListAdapter = new PlayListAdapter(mActivity, recyclerView, mRecentPlayed, mFavourite, mBoomPlayList, defaultPlayList, isPhone);
                    recyclerView.setAdapter(playListAdapter);
                }else{
                    playListAdapter.updateNewList(mRecentPlayed, mFavourite, mBoomPlayList);
                }
                recyclerView.setHasFixedSize(true);
            }
        }
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

}
