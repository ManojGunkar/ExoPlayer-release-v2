package com.globaldelight.boom.ui.musiclist.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.handler.search.Search;
import com.globaldelight.boom.ui.musiclist.activity.BoomMasterActivity;
import com.globaldelight.boom.ui.musiclist.adapter.SearchListAdapter;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.decorations.SearchListSpacesItemDecoration;
import com.globaldelight.boom.R;

/**
 * Created by Rahul Agarwal on 14-11-16.
 */
public class SearchViewFragment extends Fragment {

    private RecyclerView recyclerView;
    private Context context;
    private BoomMasterActivity activity;
    private View mainView, emptyView;
    private SearchListAdapter adapter;
    private GridLayoutManager gridLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,
                container, false);
        mainView = view;
        context = view.getContext();
        activity = (BoomMasterActivity) getActivity();
        recyclerView = (RecyclerView) mainView.findViewById(R.id.search_view_results);
        emptyView = mainView.findViewById(R.id.search_empty_view);
        return view;
    }

    private class LoadSearchResult extends AsyncTask<String, Void, Search> {

        @Override
        protected Search doInBackground(String... params) {
            Search searchRes = new Search();
            searchRes.getSearchResult(context, params[0], true);
            return searchRes;
        }

        @Override
        protected void onPostExecute(Search search) {
            super.onPostExecute(search);
            if(Utils.isPhone(getActivity())){
                gridLayoutManager =
                        new GridLayoutManager(mainView.getContext(), 2);
            }else{
                gridLayoutManager =
                        new GridLayoutManager(mainView.getContext(), 3);
            }
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (adapter.whatView(position) == SearchListAdapter.ITEM_VIEW_TYPE_LIST_ALBUM ||
                            adapter.whatView(position) == SearchListAdapter.ITEM_VIEW_TYPE_LIST_ARTIST) {
                        return 1;
                    } else {
                        return 2;
                    }
                }
            });
            recyclerView.setLayoutManager(gridLayoutManager);
            adapter = new SearchListAdapter(context, getActivity(), search, recyclerView);
            recyclerView.addItemDecoration(new SearchListSpacesItemDecoration(2, adapter));
            recyclerView.setAdapter(adapter);
        }
    }

    public void updateSearchResult(String query) {
        new LoadSearchResult().equals(query);
        showEmpty(false);
    }

    public void showEmpty(boolean isEmpty) {
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
