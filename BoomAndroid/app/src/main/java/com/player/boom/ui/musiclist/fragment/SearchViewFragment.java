package com.player.boom.ui.musiclist.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.player.boom.R;
import com.player.boom.handler.search.Search;
import com.player.boom.ui.musiclist.activity.BoomMasterActivity;
import com.player.boom.ui.musiclist.adapter.SearchListAdapter;
import com.player.boom.utils.decorations.SearchListSpacesItemDecoration;
import com.player.boom.utils.decorations.SimpleDividerItemDecoration;

/**
 * Created by Rahul Agarwal on 14-11-16.
 */
public class SearchViewFragment extends Fragment {

    private RecyclerView recyclerView;
    private Context context;
    private BoomMasterActivity activity;
    private View mainView, emptyView;
    private SearchListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,
                container, false);
        mainView = view;
        context = view.getContext();
        activity = (BoomMasterActivity) getActivity();
        return view;
    }

    private void setRecyclerView(String query) {
        Search searchRes = new Search();
        searchRes.getSearchResult(context, query, true);
        recyclerView = (RecyclerView) mainView.findViewById(R.id.search_view_results);
        emptyView = mainView.findViewById(R.id.search_empty_view);
        final GridLayoutManager manager = new GridLayoutManager(context, 2);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
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
        recyclerView.setLayoutManager(manager);
        adapter = new SearchListAdapter(context, searchRes, recyclerView);
        recyclerView.addItemDecoration(new SearchListSpacesItemDecoration(2, adapter));
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }


    public void updateSearchResult(String query) {
        setRecyclerView(query);
    }
}
