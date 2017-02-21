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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.handler.search.Search;
import com.globaldelight.boom.ui.musiclist.adapter.SearchListAdapter;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.decorations.SearchListSpacesItemDecoration;
import com.globaldelight.boom.R;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;

/**
 * Created by Rahul Agarwal on 14-11-16.
 */
public class SearchViewFragment extends Fragment {

    private RecyclerView recyclerView;
    private Activity mActivity;
    private View mainView, emptyView;
    private SearchListAdapter adapter;
    private GridLayoutManager gridLayoutManager;


    public SearchViewFragment(){}

    private BroadcastReceiver mUpdatePlayingItem = new BroadcastReceiver() {
        @Override
        public void onReceive(Context mActivity, Intent intent) {
            switch (intent.getAction()){
                case ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY:
                    if(null != adapter)
                        adapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,
                container, false);
        mainView = view;
        mActivity = getActivity();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        mActivity.registerReceiver(mUpdatePlayingItem, intentFilter);
    }

    private class LoadSearchResult extends AsyncTask<String, Void, Search> {

        @Override
        protected void onPreExecute() {
            recyclerView = (RecyclerView) mainView.findViewById(R.id.search_view_results);
            emptyView = mainView.findViewById(R.id.search_empty_view);
            super.onPreExecute();
        }

        @Override
        protected Search doInBackground(String... params) {
            Search searchRes = new Search();
            searchRes.getSearchResult(mActivity, params[0], true);
            return searchRes;
        }

        @Override
        protected void onPostExecute(Search search) {
            super.onPostExecute(search);
            if (null != mActivity) {
                final boolean isPhone = Utils.isPhone(mActivity);
                if (isPhone) {
                    gridLayoutManager =
                            new GridLayoutManager(mActivity, 2);
                } else {
                    gridLayoutManager =
                            new GridLayoutManager(mActivity, 3);
                }
                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (adapter.whatView(position) == SearchListAdapter.ITEM_VIEW_TYPE_LIST_ALBUM ||
                                adapter.whatView(position) == SearchListAdapter.ITEM_VIEW_TYPE_LIST_ARTIST) {
                            return 1;
                        } else {
                            if(isPhone)
                                return 2;
                            else
                                return 3;
                        }
                    }
                });
                recyclerView.setLayoutManager(gridLayoutManager);
                adapter = new SearchListAdapter(mActivity, mActivity, search, recyclerView, isPhone);
                recyclerView.addItemDecoration(new SearchListSpacesItemDecoration(2, adapter));
                recyclerView.setAdapter(adapter);
                if(search.getSongCount() + search.getAlbumCount() + search.getArtistCount() > 0){
                    showEmpty(false);
                }else{
                    showEmpty(true);
                }

            }
        }
    }

    public void updateSearchResult(String query) {
        new LoadSearchResult().execute(query);
    }

    public void showEmpty(boolean isEmpty) {
        if(null != recyclerView)
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if(null != emptyView)
            emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroy() {
        mActivity.unregisterReceiver(mUpdatePlayingItem);
        super.onDestroy();
    }
}
