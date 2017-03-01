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
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.globaldelight.boom.Media.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.ui.musiclist.adapter.songAdapter.SongListAdapter;
import com.globaldelight.boom.ui.widgets.RegularTextView;

import java.util.ArrayList;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;

/**
 * Created by Rahul Agarwal on 28-02-17.
 */

public class SongsListFragment extends Fragment{

    private Activity mActivity;
    private View mainView;
    private RecyclerView recyclerView;
    private SongListAdapter songListAdapter;
    private ProgressBar mLibLoad;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
    }

    private BroadcastReceiver mPlayerEventBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context mActivity, Intent intent) {
            switch (intent.getAction()){
                case ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY :
                    if(null != songListAdapter){
                        songListAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if(null != songListAdapter)
            songListAdapter.notifyDataSetChanged();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        if(null != mActivity)
            mActivity.registerReceiver(mPlayerEventBroadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(null != mActivity)
            mActivity.unregisterReceiver(mPlayerEventBroadcastReceiver);
    }

    public void listIsEmpty(int size) {
        mainView.findViewById(R.id.lib_container).setVisibility(View.VISIBLE);
        if(size < 1) {
            ((ImageView) mainView.findViewById(R.id.list_empty_placeholder_icon)).setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_no_music_placeholder, null));
            ((RegularTextView) mainView.findViewById(R.id.list_empty_placeholder_txt)).setText(mActivity.getResources().getString(R.string.no_music_placeholder_txt));
        }
        mainView.findViewById(R.id.list_empty_placeholder).setVisibility(size < 1 ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(size > 0 ? View.VISIBLE : View.GONE);
        mLibLoad.setVisibility(View.GONE);
        mLibLoad.setEnabled(false);
    }

    private View getItemView(LayoutInflater inflater, ViewGroup container){
        View view = inflater.inflate(R.layout.fragment_music_library_list,
                container, false);
        return view;
    }

    private void initViews() {
        recyclerView = (RecyclerView) mainView.findViewById(R.id.albumsListContainer);
        mainView.findViewById(R.id.lib_container).setVisibility(View.GONE);
        mLibLoad = (ProgressBar)mainView.findViewById(R.id.lib_load);
        mLibLoad.setVisibility(View.VISIBLE);
        mLibLoad.setEnabled(true);
        new LoadDeviceMediaList().execute();
    }

    public void killActivity() {
        mActivity.finish();
    }

    private class LoadDeviceMediaList extends AsyncTask<Void, Integer, ArrayList<? extends IMediaItemBase>> {
        @Override
        protected ArrayList<? extends IMediaItemBase> doInBackground(Void... params) {
            return MediaController.getInstance(mActivity).getSongList();
        }

        @Override
        protected void onPostExecute(ArrayList<? extends IMediaItemBase> iMediaItemList) {
            super.onPostExecute(iMediaItemList);
            FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_SONG_COUNT);
            final LinearLayoutManager llm = new LinearLayoutManager(mActivity);
            recyclerView.setLayoutManager(llm);
            recyclerView.setHasFixedSize(true);
            songListAdapter = new SongListAdapter(mActivity, SongsListFragment.this, iMediaItemList);
            recyclerView.setAdapter(songListAdapter);
            listIsEmpty(iMediaItemList.size());
        }
    }
}
