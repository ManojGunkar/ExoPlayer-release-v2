package com.globaldelight.boom.app.fragments;

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
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.app.adapters.search.utils.Search;
import com.globaldelight.boom.app.adapters.search.utils.SearchResult;
import com.globaldelight.boom.app.adapters.search.SearchDetailListAdapter;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.decorations.SimpleDividerItemDecoration;
import java.util.ArrayList;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;

/**
 * Created by Rahul Agarwal on 27-01-17.
 */

public class SearchDetailFragment extends Fragment{
    public static final String ARG_LIST_TYPE = "search_list_type";
    public static final String ARG_MEDIA_QUERY = "search_media_query";

    private SearchDetailListAdapter adapter;
    private GridLayoutManager gridLayoutManager;
    private String mResultType, mQuery;
    private RecyclerView rootView;
    Activity mActivity;

    public SearchDetailFragment(){}

    private BroadcastReceiver mUpdatePlayingItem = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ACTION_PLAYER_STATE_CHANGED:
                    if(null != adapter)
                        adapter.notifyDataSetChanged();
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
        initSearch();
        new LoadSearchDetailList().execute(mResultType, mQuery);
        setForAnimation();
    }

    private void initSearch() {
        if (getArguments().containsKey(ARG_LIST_TYPE)) {
            mResultType = getArguments().getString(ARG_LIST_TYPE);
        }

        if (getArguments().containsKey(ARG_MEDIA_QUERY)) {
            mQuery = getArguments().getString(ARG_MEDIA_QUERY);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAYER_STATE_CHANGED);
        mActivity.registerReceiver(mUpdatePlayingItem, intentFilter);
    }

    private class LoadSearchDetailList extends AsyncTask<String, Void, ArrayList<? extends IMediaItemBase>> {
        private Activity mActivity = SearchDetailFragment.this.mActivity;
        private String mResultType, mQuery;
        @Override
        protected ArrayList<? extends IMediaItemBase> doInBackground(String... params) {
            Search result = new Search();
            mResultType = params[0];
            mQuery = params[1];
            if(mResultType.equals(SearchResult.ARTISTS)){
                return result.getResultArtistList(mActivity, mQuery, false);
            }else if(mResultType.equals(SearchResult.ALBUMS)){
                return result.getResultAlbumList(mActivity, mQuery, false);
            }else if(mResultType.equals(SearchResult.SONGS)){
                return result.getResultSongList(mActivity, mQuery, false);
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<? extends IMediaItemBase> iMediaItemList) {
            super.onPostExecute(iMediaItemList);
            final boolean isPhone = Utils.isPhone(mActivity);
            if(isPhone){
                gridLayoutManager =
                        new GridLayoutManager(mActivity, 2);
            }else{
                gridLayoutManager =
                        new GridLayoutManager(mActivity, 3);
            }

            adapter = new SearchDetailListAdapter(mActivity, iMediaItemList, mResultType, isPhone);
            if(mResultType.equals(SearchResult.SONGS)){
                rootView.addItemDecoration(new SimpleDividerItemDecoration(mActivity, 0));
            }
            rootView.setHasFixedSize(true);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if(mResultType.equals(SearchResult.ARTISTS)){
                        return 1;
                    }else if(mResultType.equals(SearchResult.ALBUMS)){
                        return 1;
                    }else if(mResultType.equals(SearchResult.SONGS)){
                        if(isPhone)
                            return 2;
                        else
                            return 3;
                    }else
                        return 0;
                }
            });
            rootView.setLayoutManager(gridLayoutManager);
            rootView.setAdapter(adapter);
        }
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
    }

    @Override
    public void onDestroy() {
        mActivity.unregisterReceiver(mUpdatePlayingItem);
        super.onDestroy();
    }
}
