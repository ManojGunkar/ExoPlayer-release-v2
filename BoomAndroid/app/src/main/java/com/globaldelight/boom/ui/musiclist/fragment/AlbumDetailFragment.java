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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.globaldelight.boom.App;
import com.globaldelight.boom.Media.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.Media.ItemType;
import com.globaldelight.boom.ui.musiclist.ListDetail;
import com.globaldelight.boom.ui.musiclist.adapter.songAdapter.AlbumDetailAdapter;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class AlbumDetailFragment extends Fragment {

    private IMediaItemCollection dataCollection;
    private ListDetail listDetail;
    private RecyclerView rootView;
    private AlbumDetailAdapter albumDetailAdapter;
    Activity mActivity;
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
                    if(null != albumDetailAdapter)
                        albumDetailAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);

        mActivity = getActivity();
        new LoadAlbumSongs().execute();
        setForAnimation();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dataCollection = (MediaItemCollection) this.mActivity.getIntent().getParcelableExtra("mediaItemCollection");

        initValues();
    }

    private void initValues(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        if(null != mActivity) {
            mActivity.registerReceiver(mUpdatePlayingItem, intentFilter);

            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) mActivity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                if (dataCollection.getParentType() == ItemType.ALBUM) {
                    appBarLayout.setTitle(dataCollection.getItemTitle());
                } else {
                    appBarLayout.setTitle(dataCollection.getMediaElement().get(dataCollection.getCurrentIndex()).getItemTitle());
                }
            }
        }
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
    }

    public void onFloatPlayAlbums() {
        if (dataCollection.getParentType() == ItemType.ALBUM && dataCollection.getMediaElement().size() > 0) {
            App.getPlayingQueueHandler().getUpNextList().addTrackCollectionToPlay(dataCollection, 0, true);
        } else if (dataCollection.getParentType() == ItemType.ARTIST && ((IMediaItemCollection)dataCollection.getMediaElement().get(dataCollection.getCurrentIndex())).getMediaElement().size() > 0) {
            App.getPlayingQueueHandler().getUpNextList().addCollectionItemTrackToPlay(dataCollection, 0, true);
        } else if (dataCollection.getParentType() == ItemType.GENRE && ((IMediaItemCollection)dataCollection.getMediaElement().get(dataCollection.getCurrentIndex())).getMediaElement().size() > 0) {
            App.getPlayingQueueHandler().getUpNextList().addCollectionItemTrackToPlay(dataCollection, 0, true);
        }
        albumDetailAdapter.notifyDataSetChanged();
    }

    private class LoadAlbumSongs extends AsyncTask<Void, Integer, IMediaItemCollection> {

        @Override
        protected IMediaItemCollection doInBackground(Void... params) {
            if(dataCollection.getParentType() == ItemType.ALBUM && dataCollection.getMediaElement().size() == 0) {
                dataCollection.setMediaElement(MediaController.getInstance(mActivity).getAlbumTrackList(dataCollection));
            }else if(dataCollection.getParentType() == ItemType.ARTIST && ((IMediaItemCollection) dataCollection.getMediaElement().get(dataCollection.getCurrentIndex())).getMediaElement().size() == 0){
                ((IMediaItemCollection) dataCollection.getMediaElement().get(dataCollection.getCurrentIndex())).setMediaElement(MediaController.getInstance(mActivity).getArtistTrackList(dataCollection));
            }else if(dataCollection.getParentType() == ItemType.GENRE &&
                    ((IMediaItemCollection) dataCollection.getMediaElement().get(dataCollection.getCurrentIndex())).getMediaElement().size() == 0){
                ((IMediaItemCollection) dataCollection.getMediaElement().get(dataCollection.getCurrentIndex())).setMediaElement(MediaController.getInstance(mActivity).getGenreAlbumsTrackList(dataCollection));
            }
            return dataCollection;
        }

        @Override
        protected void onPostExecute(IMediaItemCollection iMediaItemBase) {
            super.onPostExecute(iMediaItemBase);

            StringBuilder itemCount = new StringBuilder();

            if(iMediaItemBase.getItemType() == ItemType.ALBUM){
                itemCount.append(iMediaItemBase.getMediaElement().size() > 1 ? getResources().getString(R.string.songs): getResources().getString(R.string.song));
                itemCount.append(" ").append(iMediaItemBase.getMediaElement().size());
                listDetail = new ListDetail(iMediaItemBase.getItemTitle(), iMediaItemBase.getItemSubTitle(), itemCount.toString());
            }else{
                itemCount.append(((MediaItemCollection) iMediaItemBase.getMediaElement().get(iMediaItemBase.getCurrentIndex())).getMediaElement().size() > 1 ? getResources().getString(R.string.songs): getResources().getString(R.string.song));
                itemCount.append(" ").append(((MediaItemCollection) iMediaItemBase.getMediaElement().get(iMediaItemBase.getCurrentIndex())).getMediaElement().size());
                listDetail = new ListDetail(iMediaItemBase.getItemTitle(), ((MediaItemCollection) iMediaItemBase.getMediaElement().get(iMediaItemBase.getCurrentIndex())).getItemSubTitle(), itemCount.toString());
            }

            rootView.setLayoutManager(new LinearLayoutManager(mActivity));
            albumDetailAdapter = new AlbumDetailAdapter(mActivity, iMediaItemBase, listDetail);
            rootView.setAdapter(albumDetailAdapter);

            if(iMediaItemBase.getItemType() == ItemType.ALBUM){
                if (((IMediaItemCollection) iMediaItemBase).getMediaElement().size() < 1) {
                    listIsEmpty();
                }
            }else {
                if (((MediaItemCollection) iMediaItemBase.getMediaElement().get(iMediaItemBase.getCurrentIndex())).getMediaElement().size() < 1) {
                    listIsEmpty();
                }
            }
        }
    }

    public void listIsEmpty() {
        rootView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        mActivity.unregisterReceiver(mUpdatePlayingItem);
        super.onDestroy();
    }
}