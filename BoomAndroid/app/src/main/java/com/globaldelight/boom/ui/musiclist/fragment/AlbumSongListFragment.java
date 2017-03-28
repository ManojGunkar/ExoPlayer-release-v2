package com.globaldelight.boom.ui.musiclist.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import com.globaldelight.boom.App;
import com.globaldelight.boom.Media.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.Media.ItemType;
import com.globaldelight.boom.task.PlayerEvents;
import com.globaldelight.boom.ui.musiclist.ListDetail;
import com.globaldelight.boom.ui.musiclist.activity.AlbumSongListActivity;
import com.globaldelight.boom.ui.musiclist.adapter.songAdapter.ItemSongListAdapter;
import com.globaldelight.boom.utils.OnStartDragListener;

import java.util.Collections;
import static com.globaldelight.boom.Media.ItemType.BOOM_PLAYLIST;
import static com.globaldelight.boom.Media.ItemType.PLAYLIST;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_BOOM_ITEM_LIST;

/**
 * Created by Rahul Agarwal on 27-01-17.
 */

public class AlbumSongListFragment extends Fragment implements OnStartDragListener {

    private static IMediaItemCollection collection;
    private ListDetail listDetail;
    private RecyclerView rootView;
    private ItemSongListAdapter itemSongListAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private static boolean isMoved = false;
    Activity mActivity;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AlbumSongListFragment() {
    }

    private BroadcastReceiver mUpdatePlayingItem = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY:
                    if(null != itemSongListAdapter)
                        itemSongListAdapter.notifyDataSetChanged();
                    break;
                case ACTION_UPDATE_BOOM_ITEM_LIST:
                    updateBoomPlayList();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);
        if(null == mActivity)
            mActivity = getActivity();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        intentFilter.addAction(ACTION_UPDATE_BOOM_ITEM_LIST);
        mActivity.registerReceiver(mUpdatePlayingItem, intentFilter);
        collection = (MediaItemCollection) this.mActivity.getIntent().getParcelableExtra("mediaItemCollection");
        setDetail(collection);
        new LoadAlbumSongListItems().execute();
        setForAnimation();
        FlurryAnalyticHelper.init(mActivity);
    }

    private void setDetail(IMediaItemCollection collection) {
        StringBuilder itemCount = new StringBuilder();
        String title;
        int count;
        if(collection.getParentType() == BOOM_PLAYLIST || collection.getParentType() == PLAYLIST){
            title = collection.getItemTitle();
            count = collection.getMediaElement().size();

        }else{
            title = collection.getMediaElement().get(collection.getCurrentIndex()).getItemTitle();
            count = ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getItemCount();
        }
        itemCount.append(count > 1 ? getResources().getString(R.string.songs): getResources().getString(R.string.song));
        itemCount.append(" ").append(count);
        listDetail = new ListDetail(title, itemCount.toString(), null);

        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) this.mActivity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(title);
        }
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
    }

    public void onFloatPlayAlbumSongs() {
        try {
            if (collection.getParentType() == PLAYLIST || collection.getParentType() == BOOM_PLAYLIST) {
                App.getPlayingQueueHandler().getUpNextList().addItemListToPlay(collection.getMediaElement(), 0);
            } else {
                App.getPlayingQueueHandler().getUpNextList().addItemListToPlay(((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement(), 0);
            }
        }catch (Exception e){}
    }

    public void killActivity() {
        mActivity.finish();
    }

    private class LoadAlbumSongListItems extends AsyncTask<Void, Void, IMediaItemCollection> {

        @Override
        protected IMediaItemCollection doInBackground(Void... params) {
            //ItemType.PLAYLIST, ItemType.ARTIST && ItemType.GENRE
            if (collection.getParentType() == ItemType.BOOM_PLAYLIST && collection.getMediaElement().isEmpty()) {
                collection.setMediaElement(MediaController.getInstance(mActivity).getBoomPlayListTrackList(collection.getItemId()));
            } else if (collection.getParentType() == ItemType.PLAYLIST && collection.getMediaElement().isEmpty()) {
                collection.setMediaElement(MediaController.getInstance(mActivity).getPlayListTrackList(collection));
            }else if(collection.getParentType() == ItemType.ARTIST &&
                        ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().isEmpty()){ //ItemType.ARTIST && ItemType.GENRE
                    ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).setMediaElement(MediaController.getInstance(mActivity).getArtistTrackList(collection));
            }else if(collection.getParentType() == ItemType.GENRE &&
                    ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().isEmpty()){ //ItemType.ARTIST && ItemType.GENRE
                ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).setMediaElement(MediaController.getInstance(mActivity).getGenreTrackList(collection));
            }
            setDetail(collection);
            ((AlbumSongListActivity)mActivity).updateAlbumArt();
            return collection;
        }

        @Override
        protected void onPostExecute(IMediaItemCollection iMediaItemCollection) {
            super.onPostExecute(iMediaItemCollection);
            LinearLayoutManager llm = new LinearLayoutManager(mActivity);
            rootView.setLayoutManager(llm);
            rootView.setHasFixedSize(true);
            rootView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    itemSongListAdapter.recyclerScrolled();
                }

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                        // Do something
                    } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        // Do something
                    } else {
                        // Do something
                    }
                }
            });
            itemSongListAdapter = new ItemSongListAdapter(mActivity, AlbumSongListFragment.this, iMediaItemCollection, listDetail, AlbumSongListFragment.this);
            rootView.setAdapter(itemSongListAdapter);
            if (iMediaItemCollection.getParentType() == ItemType.BOOM_PLAYLIST) {
                setUpItemTouchHelper();
            }
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public void updateBoomPlayList(){
        if (collection.getParentType() == ItemType.BOOM_PLAYLIST) {
            collection = MediaController.getInstance(mActivity).getBoomPlayListItem(collection.getItemId());
            if(null != collection) {
                collection.setMediaElement(MediaController.getInstance(mActivity).getBoomPlayListTrackList(collection.getItemId()));
                setDetail(collection);
                itemSongListAdapter.updateNewList(collection, listDetail);
                ((AlbumSongListActivity)mActivity).updateAlbumArt();
            }else{
                getActivity().finish();
            }
        }
    }

    public void updateBoomPlaylistIfOrderChanged(){
        if(collection.getParentType() == BOOM_PLAYLIST && isMoved && collection.getMediaElement().size() > 0){
            isMoved= false;
            MediaController.getInstance(mActivity).addSongToBoomPlayList(collection.getItemId(), collection.getMediaElement(), true);
        }
    }

    public void updateAdapter(){
        if(null != itemSongListAdapter)
            itemSongListAdapter.notifyDataSetChanged();
    }

    private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN
                , 0) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                if (collection.getMediaElement().size() > 0 && target.getAdapterPosition() > 0) {
                    Collections.swap(collection.getMediaElement(), viewHolder.getAdapterPosition() - 1, target.getAdapterPosition() - 1);
                    itemSongListAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                    itemSongListAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    itemSongListAdapter.notifyItemChanged(target.getAdapterPosition());
                    isMoved = true;
                }
                return true;
            }

            @Override
            public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

        };
        mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(rootView);
    }

    @Override
    public void onDestroy() {
        mActivity.unregisterReceiver(mUpdatePlayingItem);
        super.onDestroy();
    }

    public void listIsEmpty() {
        rootView.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        FlurryAnalyticHelper.flurryStartSession(mActivity);
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAnalyticHelper.flurryStopSession(mActivity);
    }
}
