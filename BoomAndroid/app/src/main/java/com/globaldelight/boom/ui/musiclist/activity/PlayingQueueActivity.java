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
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
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
    ImageView toolImage;
    TextView toolTxt;
    private PlayingQueueListAdapter playingQueueListAdapter;
    private RecyclerView recyclerView;
    private PermissionChecker permissionChecker;
    private View emptyView;
    //added by nidhin
    private ItemTouchHelper mItemTouchHelper;
    private BroadcastReceiver upnextBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ACTION_UPDATE_QUEUE :
                    if(playingQueueListAdapter != null)
                        playingQueueListAdapter.updateList(App.getPlayingQueueHandler().getUpNextList());
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
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay_out);
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
                        playingQueueListAdapter = new PlayingQueueListAdapter(PlayingQueueActivity.this, App.getPlayingQueueHandler().getUpNextList(), PlayingQueueActivity.this);
                        recyclerView.setAdapter(playingQueueListAdapter);
                        recyclerView.setHasFixedSize(true);
                        setUpItemTouchHelper();
                        //setUpAnimationDecoratorHelper();
                    }
                });
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

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.stay_out, R.anim.slide_out_right);
    }


    //added by nidhin

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
            Drawable xMark;
            //  int xMarkMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(getResources().getColor(R.color.colorPrimary));

             /*  xMark = ContextCompat.getDrawable(PlayingQueueActivity.this, R.drawable.ic_clear_24dp);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) PlayingQueueActivity.this.getResources().getDimension(R.dimen.ic_clear_margin);*/
                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {


                // Logger.LOGD("****************", "start=" + viewHolder.getAdapterPosition() + "stop " + target.getAdapterPosition());
                PlayingQueueListAdapter.ListPosition start = playingQueueListAdapter.getPositionObject(viewHolder.getAdapterPosition());
                // Log.d("From", "pos" + start.getItemPosition() + " list=" + start.getListType());
                PlayingQueueListAdapter.ListPosition to = playingQueueListAdapter.getPositionObject(target.getAdapterPosition());
                //  Log.d("to", "pos" + to.getItemPosition() + " list=" + to.getListType());


                if (to.getListType() == start.getListType()) {
                    if (to.getListType() == PlayingQueueListAdapter.ITEM_VIEW_TYPE_LIST_MANUAL || to.getListType() == PlayingQueueListAdapter.ITEM_VIEW_TYPE_LIST_AUTO) {
                        Collections.swap(playingQueueListAdapter.getListForType(to.getListType()), start.getItemPosition(), to.getItemPosition());
                        playingQueueListAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    }


                }
                return false;
            }

            /* //defines the enabled move directions in each state (idle, swiping, dragging).
             @Override
             public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                 return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                         ItemTouchHelper.DOWN | ItemTouchHelper.UP );
                 // return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                 //  ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END );
             }*/
            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();


                //playingQueueListAdapter = new PlayingQueueListAdapter(PlayingQueueActivity.this, playingQueue,recyclerView);
                PlayingQueueListAdapter testAdapter = (PlayingQueueListAdapter) recyclerView.getAdapter();
                if (!testAdapter.isSwipeDeleteAllowed(position)) {
                    return 0;
                }

                if (testAdapter.isUndoOn() && testAdapter.isPendingRemoval(position)) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                PlayingQueueListAdapter adapter = (PlayingQueueListAdapter) recyclerView.getAdapter();
                boolean undoOn = adapter.isUndoOn();
                if (undoOn) {
                    adapter.swipedItemPendingRemoval(swipedPosition);
                } else {
                    adapter.removeSwipedItem(adapter.getPositionObject(swipedPosition));
                }
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                // PlayingQueueListAdapter.ListPosition objPosition=adapter.getPosition(viewHolder.getAdapterPosition());


                View itemView = viewHolder.itemView;

                // not sure why, but this method get's called for viewholder that are already swiped away
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

                // draw x mark

                   /* int itemHeight = itemView.getBottom() - itemView.getTop();
                    int intrinsicWidth = xMark.getIntrinsicWidth();
                    int intrinsicHeight = xMark.getIntrinsicWidth();

                    int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                    int xMarkRight = itemView.getRight() - xMarkMargin;
                    int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                    int xMarkBottom = xMarkTop + intrinsicHeight;
                    xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                    xMark.draw(c);
*/


                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }


        };
        mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * We're gonna setup another ItemDecorator that will draw the red background in the empty space while the items are animating to thier new positions
     * after an item is removed.
     */
    private void setUpAnimationDecoratorHelper() {
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            // we want to cache this and not allocate anything repeatedly in the onDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(getResources().getColor(R.color.colorPrimary));
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) {
                    init();
                }

                // only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {

                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();

                    // this we need to find out
                    int top = 0;
                    int bottom = 0;

                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(c);

                }
                super.onDraw(c, parent, state);
            }

        });
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }
}
