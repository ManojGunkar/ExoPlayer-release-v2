package com.globaldelight.boom.tidal.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.globaldelight.boom.R;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.response.UserMusicResponse;
import com.globaldelight.boom.tidal.ui.adapter.NestedItemAdapter;
import com.globaldelight.boom.tidal.utils.NestedItemDescription;
import com.globaldelight.boom.tidal.utils.TidalHelper;
import com.globaldelight.boom.tidal.utils.TidalPopupMenu;
import com.globaldelight.boom.utils.RequestChain;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_REFRESH_LIST;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_SONG_CHANGED;
import static com.globaldelight.boom.tidal.utils.NestedItemDescription.GRID_VIEW;
import static com.globaldelight.boom.tidal.utils.NestedItemDescription.LIST_VIEW;

/**
 * Created by Manoj Kumar on 28-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalMyMusicFragment extends Fragment {

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;

    private boolean mHasResponse = false;
    private RequestChain mRequestChain = null;
    private NestedItemAdapter mAdapter;

    private List<NestedItemDescription> mItemList = new ArrayList<>();

    private BroadcastReceiver mUpdateItemSongListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_PLAYER_STATE_CHANGED:
                case ACTION_SONG_CHANGED:
                    if (null != mAdapter)
                        mAdapter.notifyDataSetChanged();
                    break;

                case ACTION_REFRESH_LIST:
                    mItemList.clear();
                    mItemList.addAll(TidalPopupMenu.newInstance(getActivity()).getNestedItems());
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tidal_my_music, null, false);
        init(view);
        return view;
    }

    private void init(View view) {
        mProgressBar = view.findViewById(R.id.progress_tidal_my_music);
        mRecyclerView = view.findViewById(R.id.rv_tidal_my_music);

        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void loadAll() {
        if (mHasResponse) {
            return;
        }

        mRequestChain = new RequestChain(getContext());
        mProgressBar.setVisibility(View.VISIBLE);
        mapResponse(TidalHelper.USER_PLAYLISTS, R.string.tidal_playlist, GRID_VIEW);
        mapResponse(TidalHelper.USER_TRACKS, R.string.tidal_tracks, LIST_VIEW);
        mapResponse(TidalHelper.USER_ABLUMS, R.string.tidal_album, GRID_VIEW);
        mapResponse(TidalHelper.USER_ARTISTS, R.string.tidal_artist, GRID_VIEW);
        Call<TidalBaseResponse> call = TidalHelper.getInstance(getContext()).getUserPlayLists(0,10);
        mRequestChain.submit(call, resp->{
            TidalHelper.getInstance(getContext()).setMyPlaylists(resp.getItems());
            mItemList.add(new NestedItemDescription(R.string.user_playlist, GRID_VIEW, resp.getItems(), "/playlists"));
        });
        mRequestChain.submit(null, (response) -> {
            mAdapter = new NestedItemAdapter(getContext(), mItemList, true, false);
            TidalPopupMenu.newInstance(getActivity()).saveNestedItems(mItemList);
            mRecyclerView.setAdapter(mAdapter);
            mProgressBar.setVisibility(View.GONE);
            mHasResponse = true;
        });
    }

    private void mapResponse(String path, int titleResId, int type) {
        Call<UserMusicResponse> call = TidalHelper.getInstance(getContext()).getUserMusic(path,0,10);
        mRequestChain.submit(call, new ResponseHandler(titleResId, type, path));
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAYER_STATE_CHANGED);
        intentFilter.addAction(ACTION_SONG_CHANGED);
        intentFilter.addAction(ACTION_REFRESH_LIST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mUpdateItemSongListReceiver, intentFilter);
        loadAll();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRequestChain != null) {
            mRequestChain.cancel();
            mRequestChain = null;
        }
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mUpdateItemSongListReceiver);
    }

    class ResponseHandler implements RequestChain.Callback<UserMusicResponse> {
        private int resId;
        private int type;
        private String path;

        ResponseHandler(int resId, int type, String path) {
            this.resId = resId;
            this.type = type;
            this.path = path;
        }

        @Override
        public void onResponse(UserMusicResponse response) {
            if (response != null) {

                List<Item> playlists = new ArrayList();

                for (int i = 0; i < response.getItems().size(); i++) {
                    UserMusicResponse.UserItem items = response.getItems().get(i);
                    items.getItem();
                    playlists.add(items.getItem());
                }
                mItemList.add(new NestedItemDescription(resId, type, playlists, path));
            }

        }
    }

}
