package com.globaldelight.boom.ui.musiclist.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.ui.musiclist.ListDetail;
import com.globaldelight.boom.ui.musiclist.adapter.songAdapter.AlbumItemsListAdapter;

import java.util.ArrayList;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class AlbumDetailFragment extends Fragment {

    private IMediaItemCollection collection;
    private ListDetail listDetail;
    private RecyclerView rootView;
    private AlbumItemsListAdapter albumItemsListAdapter;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AlbumDetailFragment() {
    }

    private BroadcastReceiver mUpdatePlayingItem = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY:
                    if(null != albumItemsListAdapter)
                        albumItemsListAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        collection = (MediaItemCollection) this.getActivity().getIntent().getParcelableExtra("mediaItemCollection");

        initValues();
    }

    private void initValues(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        getActivity().registerReceiver(mUpdatePlayingItem, intentFilter);

        StringBuilder itemCount = new StringBuilder();

        itemCount.append(collection.getItemCount() > 1 ? getResources().getString(R.string.songs): getResources().getString(R.string.song));
        itemCount.append(" ").append(collection.getItemCount());

        listDetail = new ListDetail(collection.getItemTitle(), collection.getItemSubTitle(), itemCount.toString());

        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) this.getActivity().findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(collection.getItemTitle());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);

        new LoadAlbumSongs().execute();
        setForAnimation();
        return rootView;
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
    }

    public void onFloatPlayAlbums() {
        try {
            if (App.getPlayingQueueHandler().getUpNextList() != null) {
                if (collection.getItemType() == ItemType.ALBUM) {
                    App.getPlayingQueueHandler().getUpNextList().addToPlay(collection, 0, true);
                } else {
                    App.getPlayingQueueHandler().getUpNextList().addToPlay((ArrayList<IMediaItem>) ((MediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement(), 0, false, true);
                }
                albumItemsListAdapter.notifyDataSetChanged();
            }
        }catch (Exception e){

        }
    }

    private class LoadAlbumSongs extends AsyncTask<Void, Integer, IMediaItemBase> {

        @Override
        protected IMediaItemBase doInBackground(Void... params) {
            if(collection.getItemType() == ItemType.ALBUM && collection.getMediaElement().isEmpty()) {
                collection.setMediaElement(MediaController.getInstance(getActivity()).getMediaCollectionItemDetails(collection));
            }else if((collection.getItemType() == ItemType.ARTIST || collection.getItemType() == ItemType.GENRE) &&
                    ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().isEmpty()){ //ItemType.ARTIST && ItemType.GENRE
                ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).setMediaElement(MediaController.getInstance(getActivity()).getMediaCollectionItemDetails(collection));
            }
            return collection;
        }

        @Override
        protected void onPostExecute(IMediaItemBase iMediaItemBase) {
            super.onPostExecute(iMediaItemBase);
            rootView.setLayoutManager(new LinearLayoutManager(getActivity()));
            albumItemsListAdapter = new AlbumItemsListAdapter(getActivity(), (IMediaItemCollection) iMediaItemBase, listDetail);
            rootView.setAdapter(albumItemsListAdapter);

            if (((IMediaItemCollection) iMediaItemBase).getMediaElement().size() < 1) {
                listIsEmpty();
            }
        }
    }

    public void listIsEmpty() {
        rootView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mUpdatePlayingItem);
        super.onDestroy();
    }
}
