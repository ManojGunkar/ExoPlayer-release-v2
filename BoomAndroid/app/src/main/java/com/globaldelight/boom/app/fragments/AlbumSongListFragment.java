package com.globaldelight.boom.app.fragments;

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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.local.callback.IMediaItemCollection;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.app.adapters.model.ListDetail;
import com.globaldelight.boom.app.activities.AlbumSongListActivity;
import com.globaldelight.boom.app.adapters.song.ItemSongListAdapter;
import com.globaldelight.boom.utils.OnStartDragListener;

import java.util.Collections;
import static com.globaldelight.boom.playbackEvent.utils.ItemType.BOOM_PLAYLIST;
import static com.globaldelight.boom.playbackEvent.utils.ItemType.PLAYLIST;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_UPDATE_BOOM_ITEM_LIST;

/**
 * Created by Rahul Agarwal on 27-01-17.
 */

public class AlbumSongListFragment extends Fragment implements OnStartDragListener {
    private IMediaItemCollection collection;
    private ListDetail listDetail;
    private RecyclerView rootView;
    private ItemSongListAdapter itemSongListAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private static boolean isMoved = false;
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
                case ACTION_PLAYER_STATE_CHANGED:
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle b = getActivity().getIntent().getBundleExtra("bundle");
        collection = (MediaItemCollection) b.getParcelable("mediaItemCollection");
        setDetail(collection);
//        FlurryAnalyticHelper.init(mActivity);
    }

    private void setDetail(IMediaItemCollection collection) {
        StringBuilder itemCount = new StringBuilder();
        String title;
        int count;
        if(collection.getParentType() == BOOM_PLAYLIST || collection.getParentType() == PLAYLIST){
            title = collection.getItemTitle();
            count = collection.getMediaElement().size();

        }else{
            title = collection.getItemAt(collection.getCurrentIndex()).getItemTitle();
            count = ((IMediaItemCollection)collection.getItemAt(collection.getCurrentIndex())).getItemCount();
        }
        itemCount.append(count > 1 ? getResources().getString(R.string.songs): getResources().getString(R.string.song));
        itemCount.append(" ").append(count);
        listDetail = new ListDetail(title, itemCount.toString(), null);

        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout)getActivity().findViewById(R.id.toolbar_layout);
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
                App.playbackManager().queue().addItemListToPlay(collection, 0);
            } else {
                App.playbackManager().queue().addItemListToPlay(((IMediaItemCollection)collection.getItemAt(collection.getCurrentIndex())), 0);
            }
        }catch (Exception e){}
    }

    public void killActivity() {
        getActivity().finish();
    }

    private class LoadAlbumSongListItems extends AsyncTask<Void, Void, IMediaItemCollection> {

        private Activity activity = getActivity();

        @Override
        protected IMediaItemCollection doInBackground(Void... params) {
            //ItemType.PLAYLIST, ItemType.ARTIST && ItemType.GENRE
            if (collection.getParentType() == ItemType.BOOM_PLAYLIST && collection.count() == 0) {
                collection.setMediaElement(MediaController.getInstance(activity).getBoomPlayListTrackList(collection.getItemId()));
            } else if (collection.getParentType() == ItemType.PLAYLIST && collection.getMediaElement().isEmpty()) {
                collection.setMediaElement(MediaController.getInstance(activity).getPlayListTrackList(collection));
            }else if(collection.getParentType() == ItemType.ARTIST &&
                        ((IMediaItemCollection)collection.getItemAt(collection.getCurrentIndex())).count() == 0){ //ItemType.ARTIST && ItemType.GENRE
                    ((IMediaItemCollection)collection.getItemAt(collection.getCurrentIndex())).setMediaElement(MediaController.getInstance(getActivity()).getArtistTrackList(collection));
            }else if(collection.getParentType() == ItemType.GENRE &&
                    ((IMediaItemCollection)collection.getItemAt(collection.getCurrentIndex())).count() == 0){ //ItemType.ARTIST && ItemType.GENRE
                ((IMediaItemCollection)collection.getItemAt(collection.getCurrentIndex())).setMediaElement(MediaController.getInstance(getActivity()).getGenreTrackList(collection));
            }
            setDetail(collection);

            if ( collection.getParentType() == ItemType.BOOM_PLAYLIST || collection.getParentType() == ItemType.PLAYLIST ) {
                return collection;
            }
            else {
                return (IMediaItemCollection)collection.getItemAt(collection.getCurrentIndex());
            }
        }

        @Override
        protected void onPostExecute(IMediaItemCollection iMediaItemCollection) {
            super.onPostExecute(iMediaItemCollection);

            ((AlbumSongListActivity)activity).updateAlbumArt();


            LinearLayoutManager llm = new LinearLayoutManager(activity);
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
            itemSongListAdapter = new ItemSongListAdapter(activity, AlbumSongListFragment.this, iMediaItemCollection, listDetail, AlbumSongListFragment.this);
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
            collection = MediaController.getInstance(getActivity()).getBoomPlayListItem(collection.getItemId());
            if(null != collection) {
                collection.setMediaElement(MediaController.getInstance(getActivity()).getBoomPlayListTrackList(collection.getItemId()));
                setDetail(collection);
                itemSongListAdapter.updateNewList(collection, listDetail);
                ((AlbumSongListActivity)getActivity()).updateAlbumArt();
            }else{
                getActivity().finish();
            }
        }
    }

    public void updateBoomPlaylistIfOrderChanged(){
        if(collection.getParentType() == BOOM_PLAYLIST && isMoved && collection.getMediaElement().size() > 0){
            isMoved= false;
            MediaController.getInstance(getActivity()).addSongToBoomPlayList(collection.getItemId(), collection.getMediaElement(), true);
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

    public void listIsEmpty() {
        rootView.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAYER_STATE_CHANGED);
        intentFilter.addAction(ACTION_UPDATE_BOOM_ITEM_LIST);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mUpdatePlayingItem, intentFilter);

        new LoadAlbumSongListItems().execute();
        setForAnimation();
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mUpdatePlayingItem);
    }
}
