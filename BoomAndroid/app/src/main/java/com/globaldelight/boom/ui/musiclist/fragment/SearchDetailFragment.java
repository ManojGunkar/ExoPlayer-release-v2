package com.globaldelight.boom.ui.musiclist.fragment;

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
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.handler.search.Search;
import com.globaldelight.boom.handler.search.SearchResult;
import com.globaldelight.boom.ui.musiclist.adapter.SearchDetailListAdapter;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.decorations.SimpleDividerItemDecoration;
import java.util.ArrayList;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;

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

    public SearchDetailFragment(){}

    private BroadcastReceiver mUpdatePlayingItem = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY:
                    if(null != adapter)
                        adapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_LIST_TYPE)) {
            mResultType = getArguments().getString(ARG_LIST_TYPE);
        }

        if (getArguments().containsKey(ARG_MEDIA_QUERY)) {
            mQuery = getArguments().getString(ARG_MEDIA_QUERY);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        getActivity().registerReceiver(mUpdatePlayingItem, intentFilter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);

        new LoadSearchDetailList().execute(mResultType, mQuery);

        setForAnimation();
        return rootView;
    }

    private class LoadSearchDetailList extends AsyncTask<String, Void, ArrayList<? extends IMediaItemBase>> {
        private String mResultType, mQuery;
        @Override
        protected ArrayList<? extends IMediaItemBase> doInBackground(String... params) {
            Search result = new Search();
            mResultType = params[0];
            mQuery = params[1];
            if(mResultType.equals(SearchResult.ARTISTS)){
                return result.getResultArtistList(getActivity(), mQuery, false);
            }else if(mResultType.equals(SearchResult.ALBUMS)){
                return result.getResultAlbumList(getActivity(), mQuery, false);
            }else if(mResultType.equals(SearchResult.SONGS)){
                return result.getResultSongList(getActivity(), mQuery, false);
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<? extends IMediaItemBase> iMediaItemList) {
            super.onPostExecute(iMediaItemList);
            final boolean isPhone = Utils.isPhone(getActivity());
            if(isPhone){
                gridLayoutManager =
                        new GridLayoutManager(getActivity(), 2);
            }else{
                gridLayoutManager =
                        new GridLayoutManager(getActivity(), 3);
            }

            adapter = new SearchDetailListAdapter(getActivity(), iMediaItemList, mResultType, isPhone);
            if(mResultType.equals(SearchResult.SONGS)){
                rootView.addItemDecoration(new SimpleDividerItemDecoration(getActivity(), 0));
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
        getActivity().unregisterReceiver(mUpdatePlayingItem);
        super.onDestroy();
    }
}