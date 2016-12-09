package com.globaldelight.boom.ui.musiclist.activity;

import android.Manifest;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.handler.PlayingQueue.QueueType;
import com.globaldelight.boom.ui.musiclist.adapter.PlayingQueueListAdapter;
import com.globaldelight.boom.utils.OnStartDragListener;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.decorations.AlbumListSpacesItemDecoration;
import com.globaldelight.boom.utils.decorations.SimpleDividerItemDecoration;

import java.util.Collections;

/**
 * Created by Rahul Agarwal on 29-09-16.
 */

public class PlayingQueueActivity extends AppCompatActivity implements OnStartDragListener {
    public static final String ACTION_UPDATE_QUEUE = "ACTION_UPDATE_QUEUE";
    Toolbar toolbar;
    private ProgressBar mQueueLoad;
    ImageView toolImage;
    TextView toolTxt;
    private LinearLayout mQueueContainer, mLibProgress;
    private PlayingQueueListAdapter playingQueueListAdapter;
    private RecyclerView recyclerView;
    private PermissionChecker permissionChecker;
    private View emptyView;
    private ItemTouchHelper mItemTouchHelper;
    private BroadcastReceiver upnextBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_UPDATE_QUEUE:
                    if (playingQueueListAdapter != null)
                        playingQueueListAdapter.updateList(App.getPlayingQueueHandler().getUpNextList());
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(upnextBroadcastReceiver);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing_queue);

        initView();
        setupToolbar();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE_QUEUE);
        registerReceiver(upnextBroadcastReceiver, filter);
    }

    private void initView() {
        mQueueContainer = (LinearLayout)findViewById(R.id.queue_container);
        mLibProgress = (LinearLayout)findViewById(R.id.queue_progress);
        recyclerView = (RecyclerView) findViewById(R.id.playing_queue_Container);
        emptyView = findViewById(R.id.playing_queue_empty_view);
        mQueueContainer.setVisibility(View.GONE);
        mQueueLoad = (ProgressBar) findViewById(R.id.queue_load);
        mQueueLoad.setVisibility(View.VISIBLE);
        mQueueLoad.setEnabled(true);
        checkPermissions();
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_queue);
        toolImage = (ImageView) findViewById(R.id.toolImg_queue);
        toolTxt = (TextView) findViewById(R.id.toolTitle_queue);
        toolImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_album_white_24dp, null));
        toolTxt.setText(getResources().getString(R.string.title_playingque));
        toolTxt.setTextSize(18);
        try {
            setSupportActionBar(toolbar);
        } catch (IllegalStateException e) {
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    public void setPlayingQueueList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final GridLayoutManager gridLayoutManager =
                        new GridLayoutManager(PlayingQueueActivity.this, 1);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        recyclerView.setLayoutManager(gridLayoutManager);
                        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(PlayingQueueActivity.this, Utils.getWindowWidth(PlayingQueueActivity.this)));
                        recyclerView.addItemDecoration(new AlbumListSpacesItemDecoration(Utils.dpToPx(PlayingQueueActivity.this, 0)));
                        playingQueueListAdapter = new PlayingQueueListAdapter(PlayingQueueActivity.this, App.getPlayingQueueHandler().getUpNextList(), PlayingQueueActivity.this);
                        recyclerView.setAdapter(playingQueueListAdapter);
                        gridLayoutManager.scrollToPosition(playingQueueListAdapter.getPlayingHeaderPosition());
                        recyclerView.setHasFixedSize(true);
                        setUpItemTouchHelper();
                        mQueueContainer.setVisibility(View.VISIBLE);
                        mQueueLoad.setVisibility(View.GONE);
                        mQueueLoad.setEnabled(false);
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        /*MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.stay_out, R.anim.slide_out_right);
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

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.stay_out, R.anim.slide_out_right);
    }

    /**
     * This is the standard support library way of implementing "swipe to delete" feature. You can do custom drawing in onChildDraw method
     * but whatever you draw will disappear once the swipe is over, and while the items are animating to their new position the recycler view
     * background will be visible. That is rarely an desired effect.
     */
    private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN
                , ItemTouchHelper.LEFT) {

            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(getResources().getColor(R.color.colorPrimary));


                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {


                PlayingQueueListAdapter.ListPosition start = playingQueueListAdapter.getPositionObject(viewHolder.getAdapterPosition());

                PlayingQueueListAdapter.ListPosition to = playingQueueListAdapter.getPositionObject(target.getAdapterPosition());

                if (to.getListType() == start.getListType()) {
                    if (to.getListType() == PlayingQueueListAdapter.ITEM_VIEW_TYPE_LIST_MANUAL || to.getListType() == PlayingQueueListAdapter.ITEM_VIEW_TYPE_LIST_AUTO) {
                        Collections.swap(App.getPlayingQueueHandler().getUpNextList().getAutoUpNextList(), start.getItemPosition(), to.getItemPosition());
                        playingQueueListAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                        playingQueueListAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                        playingQueueListAdapter.notifyItemChanged(target.getAdapterPosition());
                    }
                }
                return true;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();


                PlayingQueueListAdapter testAdapter = (PlayingQueueListAdapter) recyclerView.getAdapter();
                if (!testAdapter.isSwipeDeleteAllowed(position)) {
                    return 0;
                }

                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                PlayingQueueListAdapter adapter = (PlayingQueueListAdapter) recyclerView.getAdapter();
                adapter.removeSwipedItem(viewHolder);
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {


                View itemView = viewHolder.itemView;

                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }

                if (!initiated) {
                    init();
                }

                // draw red background
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);


                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }


        };
        mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }
}
