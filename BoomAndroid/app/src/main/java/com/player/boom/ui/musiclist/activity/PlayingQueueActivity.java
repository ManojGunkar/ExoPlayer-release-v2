package com.player.boom.ui.musiclist.activity;

import android.Manifest;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.player.boom.App;
import com.player.boom.R;
import com.player.boom.data.MediaCollection.IMediaItemBase;
import com.player.boom.handler.PlayingQueue.QueueType;
import com.player.boom.ui.musiclist.adapter.PlayingQueueListAdapter;
import com.player.boom.utils.PermissionChecker;
import com.player.boom.utils.Utils;
import com.player.boom.utils.decorations.AlbumListSpacesItemDecoration;
import com.player.boom.utils.decorations.SimpleDividerItemDecoration;

import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Rahul Agarwal on 29-09-16.
 */

public class PlayingQueueActivity extends AppCompatActivity {
    Toolbar toolbar;
    ImageView toolImage;
    TextView toolTxt;
    private PlayingQueueListAdapter playingQueueListAdapter;
    private RecyclerView recyclerView;
    private PermissionChecker permissionChecker;
    private View emptyView;
    public static final String ACTION_UPDATE_QUEUE = "ACTION_UPDATE_QUEUE";

    private BroadcastReceiver upnextBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ACTION_UPDATE_QUEUE :
                    final Map<QueueType, LinkedList<IMediaItemBase>> playingQueue = App.getPlayingQueueHandler().getPlayingQueue().getPlayingQueue();
                    if(playingQueueListAdapter != null)
                        playingQueueListAdapter.updateList(playingQueue);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE_QUEUE);
        registerReceiver(upnextBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(upnextBroadcastReceiver);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing_queue);

        initView();
        setupToolbar();
    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.playing_queue_Container);
        emptyView = findViewById(R.id.playing_queue_empty_view);
        checkPermissions();
    }

    private void setupToolbar() {
        toolbar= (Toolbar) findViewById(R.id.toolbar_queue);
        toolImage = (ImageView)findViewById(R.id.toolImg_queue);
        toolTxt = (TextView) findViewById(R.id.toolTitle_queue);
        toolImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_album_white_24dp, null));
        toolTxt.setText("Playing Queue");
        toolTxt.setTextSize(18);
        try {
            setSupportActionBar(toolbar);
        }catch (IllegalStateException e){}
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setTitle("Music Library"/*getIntent().getStringExtra("name")*/);
        }
    }

    private void checkPermissions() {
        permissionChecker = new PermissionChecker(this, this, recyclerView);
        permissionChecker.check(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getResources().getString(R.string.storage_permission),
                new PermissionChecker.OnPermissionResponse() {
                    @Override
                    public void onAccepted() {
                        setPlayingQueueList();
                    }

                    @Override
                    public void onDecline() {
                        finish();
                    }
                });
    }

    public void setPlayingQueueList(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Map<QueueType, LinkedList<IMediaItemBase>> playingQueue = App.getPlayingQueueHandler().getPlayingQueue().getPlayingQueue();
                final GridLayoutManager gridLayoutManager =
                        new GridLayoutManager(PlayingQueueActivity.this, 1);
                        runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        gridLayoutManager.scrollToPosition(0);
                        recyclerView.setLayoutManager(gridLayoutManager);
                        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(PlayingQueueActivity.this, Utils.getWindowWidth(PlayingQueueActivity.this)));
                        recyclerView.addItemDecoration(new AlbumListSpacesItemDecoration(Utils.dpToPx(PlayingQueueActivity.this, 0)));
                        playingQueueListAdapter = new PlayingQueueListAdapter(PlayingQueueActivity.this, playingQueue);
                        recyclerView.setAdapter(playingQueueListAdapter);
                        recyclerView.setHasFixedSize(true);
                    }
                });
                if (playingQueue.size() < 1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listIsEmpty();
                        }
                    });
                }
            }
        }).start();
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

    public void listIsEmpty() {
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    public void listNoMoreEmpty() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
