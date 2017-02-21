package com.globaldelight.boom.ui.musiclist.fragment;

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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.handler.PlayingQueue.UpNextList;
import com.globaldelight.boom.task.PlayerEvents;
import com.globaldelight.boom.ui.musiclist.adapter.UpNextListAdapter;
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
    private ProgressBar mQueueLoad;
    private UpNextListAdapter upNextListAdapter;
    private PermissionChecker permissionChecker;
    private ItemTouchHelper mItemTouchHelper;
    private RecyclerView rootView;
    Activity mActivity;

    private BroadcastReceiver upnextBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PlayerEvents.ACTION_UPDATE_QUEUE:
                    if (upNextListAdapter != null)
                        upNextListAdapter.updateList(App.getPlayingQueueHandler().getUpNextList());
                    break;
                case PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY:
                    if (upNextListAdapter != null)
                        upNextListAdapter.updateList(App.getPlayingQueueHandler().getUpNextList());
                    break;
            }
        }
    };

    public UpNextListFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);
        mActivity = getActivity();
        initViews();

        return rootView;
    }

    private void initViews() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayerEvents.ACTION_UPDATE_QUEUE);
        filter.addAction(PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        mActivity.registerReceiver(upnextBroadcastReceiver, filter);

        checkPermissions();
    }

    private class LoadPlayingQueueList extends AsyncTask<Void, Integer, UpNextList> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mQueueLoad = new ProgressBar(mActivity);
            rootView.setVisibility(View.GONE);
        }

        @Override
        protected UpNextList doInBackground(Void... params) {
            return App.getPlayingQueueHandler().getUpNextList();
        }

        @Override
        protected void onPostExecute(UpNextList upNextList) {
            super.onPostExecute(upNextList);
            GridLayoutManager gridLayoutManager =
                    new GridLayoutManager(mActivity, 1);
            gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            rootView.setLayoutManager(gridLayoutManager);
            rootView.addItemDecoration(new SimpleDividerItemDecoration(mActivity, Utils.getWindowWidth(mActivity)));
            rootView.addItemDecoration(new AlbumListSpacesItemDecoration(Utils.dpToPx(mActivity, 0)));
            upNextListAdapter = new UpNextListAdapter(mActivity, upNextList, UpNextListFragment.this, rootView);
            rootView.setAdapter(upNextListAdapter);
            gridLayoutManager.scrollToPosition(upNextListAdapter.getPlayingHeaderPosition());
            rootView.setHasFixedSize(true);
            setUpItemTouchHelper();
            rootView.setVisibility(View.VISIBLE);
            mQueueLoad.setVisibility(View.GONE);
            mQueueLoad.setEnabled(false);
        }
    }

    private void checkPermissions() {
        permissionChecker = new PermissionChecker(mActivity, mActivity, rootView);
        permissionChecker.check(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getResources().getString(R.string.storage_permission),
                new PermissionChecker.OnPermissionResponse() {
                    @Override
                    public void onAccepted() {
                        new LoadPlayingQueueList().execute();
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

                UpNextListAdapter.ListPosition start = upNextListAdapter.getPositionObject(viewHolder.getAdapterPosition());

                UpNextListAdapter.ListPosition to = upNextListAdapter.getPositionObject(target.getAdapterPosition());

                if (to.getListType() == start.getListType()) {
                    if (to.getListType() == UpNextListAdapter.ITEM_VIEW_TYPE_LIST_MANUAL || to.getListType() == UpNextListAdapter.ITEM_VIEW_TYPE_LIST_AUTO) {
                        if(to.getListType() == UpNextListAdapter.ITEM_VIEW_TYPE_LIST_MANUAL )
                            Collections.swap(App.getPlayingQueueHandler().getUpNextList().getManualUpNextList(), start.getItemPosition(), to.getItemPosition());
                        else
                            Collections.swap(App.getPlayingQueueHandler().getUpNextList().getAutoUpNextList(), start.getItemPosition(), to.getItemPosition());
                        upNextListAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                        upNextListAdapter.notifyItemChanged(target.getAdapterPosition());
                        upNextListAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    }
                }
                return true;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();

                UpNextListAdapter testAdapter = (UpNextListAdapter) recyclerView.getAdapter();
                if (!testAdapter.isSwipeDeleteAllowed(position)) {
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
    public void onDestroy() {
        mActivity.unregisterReceiver(upnextBroadcastReceiver);
        super.onDestroy();
    }
}
