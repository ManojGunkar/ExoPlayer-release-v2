package com.globaldelight.boom.app.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.playbackEvent.handler.UpNextPlayingQueue;
import com.globaldelight.boom.app.receivers.actions.PlayerEvents;
import com.globaldelight.boom.app.adapters.song.UpNextListAdapter;
import com.globaldelight.boom.utils.OnStartDragListener;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.decorations.AlbumListSpacesItemDecoration;
import com.globaldelight.boom.utils.decorations.SimpleDividerItemDecoration;

import java.util.Collections;

/**
 * Created by Rahul Agarwal on 27-01-17.
 */

public class UpNextListFragment extends Fragment implements OnStartDragListener {
    private UpNextListAdapter upNextListAdapter;
    private PermissionChecker permissionChecker;
    private ItemTouchHelper mItemTouchHelper;
    private RecyclerView rootView;
    private Activity mActivity;

    private BroadcastReceiver upnextBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PlayerEvents.ACTION_QUEUE_UPDATED:
                case PlayerEvents.ACTION_SONG_CHANGED:
                    if (upNextListAdapter != null)
                        upNextListAdapter.updateList(App.playbackManager().queue());
                    break;
                case PlayerEvents.ACTION_PLAYER_STATE_CHANGED:
                    if (upNextListAdapter != null)
                        upNextListAdapter.updateList(App.playbackManager().queue());
                    break;
            }
        }
    };

    public UpNextListFragment(){}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            mActivity = (Activity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
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
        initViews();
    }

    private void initViews() {
    }

    private void loadPlayingQueue() {
        GridLayoutManager gridLayoutManager =
                new GridLayoutManager(mActivity, 1);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rootView.setLayoutManager(gridLayoutManager);
        rootView.addItemDecoration(new SimpleDividerItemDecoration(mActivity, Utils.getWindowWidth(mActivity)));
        rootView.addItemDecoration(new AlbumListSpacesItemDecoration(Utils.dpToPx(mActivity, 0)));
        upNextListAdapter = new UpNextListAdapter(mActivity, UpNextListFragment.this, rootView);
        rootView.setAdapter(upNextListAdapter);
        gridLayoutManager.scrollToPosition(App.playbackManager().queue().getPlayingItemIndex());
        rootView.setHasFixedSize(true);
        setUpItemTouchHelper();
        rootView.setVisibility(View.VISIBLE);
    }

    private void checkPermissions() {
        permissionChecker = new PermissionChecker(mActivity, rootView, PermissionChecker.STORAGE_WRITE_PERMISSION );
        permissionChecker.check(Manifest.permission.READ_EXTERNAL_STORAGE,
                getResources().getString(R.string.storage_permission),
                new PermissionChecker.OnPermissionResponse() {
                    @Override
                    public void onAccepted() {
                        loadPlayingQueue();
                    }

                    @Override
                    public void onDecline() {
                        mActivity.finish();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
                background = new ColorDrawable(ContextCompat.getColor(mActivity ,R.color.upnext_delete_background));
                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                if(App.playbackManager().queue().getUpNextItemCount() > 0) {
                    int start = viewHolder.getAdapterPosition();
                    int to = target.getAdapterPosition();
                    if (start == App.playbackManager().queue().getPlayingItemIndex()) {
                        App.playbackManager().queue().setPlayingItemIndex(to);
                    } else if (to == App.playbackManager().queue().getPlayingItemIndex()) {
                        App.playbackManager().queue().setPlayingItemIndex(start);
                    }
                    Collections.swap(App.playbackManager().queue().getUpNextItemList(), start, to);
                    upNextListAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    upNextListAdapter.notifyItemChanged(target.getAdapterPosition());
                    upNextListAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                }
                return true;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();

                UpNextListAdapter adapter = (UpNextListAdapter) recyclerView.getAdapter();
                if (!adapter.isSwipeDeleteAllowed(position)) {
                    return 0;
                }

                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                UpNextListAdapter adapter = (UpNextListAdapter) rootView.getAdapter();
                adapter.removeSwipedItem(viewHolder);
//                upNextListAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
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
        mItemTouchHelper.attachToRecyclerView(rootView);
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public  void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayerEvents.ACTION_QUEUE_UPDATED);
        filter.addAction(PlayerEvents.ACTION_SONG_CHANGED);
        filter.addAction(PlayerEvents.ACTION_PLAYER_STATE_CHANGED);
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(upnextBroadcastReceiver, filter);

        checkPermissions();
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(upnextBroadcastReceiver);
    }
}
