package com.globaldelight.boom.ui.musiclist.activity;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.globaldelight.boom.handler.search.SearchResult;
import com.globaldelight.boom.R;
import com.globaldelight.boom.handler.search.Search;
import com.globaldelight.boom.ui.musiclist.adapter.SearchDetailListAdapter;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.decorations.SimpleDividerItemDecoration;

/**
 * Created by Rahul Agarwal on 14-11-16.
 */

public class SearchDetailListActivity extends AppCompatActivity {
    Toolbar toolbar;
    private RecyclerView recyclerView;
    private String mResultType, mQuery;
    private RegularTextView toolbarTitle;
    private SearchDetailListAdapter adapter;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_detail);

        mResultType = getIntent().getStringExtra("list_type");
        mQuery = getIntent().getStringExtra("query");;
        initView();
    }

    private void initView() {

        recyclerView = (RecyclerView) findViewById(R.id.rv_search_detail_activity);

        toolbar = (Toolbar) findViewById(R.id.search_detail_toolbar);

        toolbarTitle = (RegularTextView)findViewById(R.id.search_detail_toolbr_title) ;
        toolbarTitle.setText(mResultType);
        try {
            setSupportActionBar(toolbar);
        } catch (IllegalStateException e) {
        }
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addDetailList();
        setForAnimation();
    }


    private void setForAnimation() {
        recyclerView.scrollTo(0, 100);
    }

    private void addDetailList() {
        Search result = new Search();
        final GridLayoutManager manager = new GridLayoutManager(this, 2);
        if(mResultType.equals(SearchResult.ARTISTS)){
            adapter = new SearchDetailListAdapter(this, result.getResultArtistList(this, mQuery, false), mResultType);
        }else if(mResultType.equals(SearchResult.ALBUMS)){
            adapter = new SearchDetailListAdapter(this, result.getResultAlbumList(this, mQuery, false), mResultType);
        }else if(mResultType.equals(SearchResult.SONGS)){
            adapter = new SearchDetailListAdapter(this, result.getResultSongList(this, mQuery, false), mResultType);
            recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this, 0));
        }

        recyclerView.setHasFixedSize(true);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(mResultType.equals(SearchResult.ARTISTS)){
                    return 1;
                }else if(mResultType.equals(SearchResult.ALBUMS)){
                    return 1;
                }else if(mResultType.equals(SearchResult.SONGS)){
                    return 2;
                }else
                    return 0;
            }
        });
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d("Query : ", query);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}