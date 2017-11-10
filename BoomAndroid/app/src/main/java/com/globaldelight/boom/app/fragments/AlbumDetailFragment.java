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
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.collection.local.callback.IMediaItem;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.local.callback.IMediaItemCollection;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.app.adapters.model.ListDetail;
import com.globaldelight.boom.app.adapters.album.AlbumDetailAdapter;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;

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
                case ACTION_PLAYER_STATE_CHANGED:
                    if(null != albumDetailAdapter)
                        albumDetailAdapter.notifyDataSetChanged();
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
    public void onDetach() {
        super.onDetach();
        mActivity = null;
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
        Bundle b = mActivity.getIntent().getBundleExtra("bundle");
        dataCollection = (MediaItemCollection) b.getParcelable("mediaItemCollection");
        initValues();
        new LoadAlbumSongs().execute();
        setForAnimation();
    }

    private void initValues(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAYER_STATE_CHANGED);
        if(null != mActivity) {
            LocalBroadcastManager.getInstance(mActivity).registerReceiver(mUpdatePlayingItem, intentFilter);

            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) mActivity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                if (dataCollection.getParentType() == ItemType.ALBUM) {
                    appBarLayout.setTitle(dataCollection.getItemTitle());
                } else {
                    appBarLayout.setTitle(dataCollection.getItemAt(dataCollection.getCurrentIndex()).getItemTitle());
                }
                appBarLayout.setCollapsedTitleTypeface(ResourcesCompat.getFont(getActivity(), R.font.titilliumweb_semibold));
                appBarLayout.setExpandedTitleTypeface(ResourcesCompat.getFont(getActivity(), R.font.titilliumweb_semibold));
            }
        }
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
    }

    public void onFloatPlayAlbums() {
        App.playbackManager().stop();
        if (dataCollection.getParentType() == ItemType.ALBUM && dataCollection.count() > 0) {
            App.playbackManager().queue().addItemListToPlay(dataCollection, 0);
        } else if (dataCollection.getParentType() == ItemType.ARTIST && ((IMediaItemCollection)dataCollection.getItemAt(dataCollection.getCurrentIndex())).count() > 0) {
            App.playbackManager().queue().addItemListToPlay((IMediaItemCollection)dataCollection.getItemAt(dataCollection.getCurrentIndex()), 0);
        } else if (dataCollection.getParentType() == ItemType.GENRE && ((IMediaItemCollection)dataCollection.getItemAt(dataCollection.getCurrentIndex())).count() > 0) {
            App.playbackManager().queue().addItemListToPlay((IMediaItemCollection)dataCollection.getItemAt(dataCollection.getCurrentIndex()), 0);
        }

        if ( albumDetailAdapter != null ) {
            albumDetailAdapter.notifyDataSetChanged();
        }
    }

    private class LoadAlbumSongs extends AsyncTask<Void, Integer, IMediaItemCollection> {

        private Activity mActivity = AlbumDetailFragment.this.mActivity;

        @Override
        protected IMediaItemCollection doInBackground(Void... params) {
            if(dataCollection.getParentType() == ItemType.ALBUM && dataCollection.count() == 0) {
                dataCollection.setMediaElement(MediaController.getInstance(mActivity).getAlbumTrackList(dataCollection));
            }else if(dataCollection.getParentType() == ItemType.ARTIST && ((IMediaItemCollection) dataCollection.getItemAt(dataCollection.getCurrentIndex())).count() == 0){
                ((IMediaItemCollection) dataCollection.getItemAt(dataCollection.getCurrentIndex())).setMediaElement(MediaController.getInstance(mActivity).getArtistAlbumsTrackList(dataCollection));
            }else if(dataCollection.getParentType() == ItemType.GENRE &&
                    ((IMediaItemCollection) dataCollection.getItemAt(dataCollection.getCurrentIndex())).count() == 0){
                ((IMediaItemCollection) dataCollection.getItemAt(dataCollection.getCurrentIndex())).setMediaElement(MediaController.getInstance(mActivity).getGenreAlbumsTrackList(dataCollection));
            }
            return dataCollection;
        }

        @Override
        protected void onPostExecute(IMediaItemCollection iMediaItemBase) {
            super.onPostExecute(iMediaItemBase);

            IMediaItemCollection collection = iMediaItemBase;
            if(iMediaItemBase.getItemType() != ItemType.ALBUM) {
                collection = (IMediaItemCollection) iMediaItemBase.getItemAt(iMediaItemBase.getCurrentIndex());
            }

            StringBuilder itemCount = new StringBuilder();
            itemCount.append(getResources().getString(collection.count() > 1 ? R.string.songs : R.string.song));
            itemCount.append(" ").append(collection.count());

            if(collection.getItemType() == ItemType.ALBUM){
                listDetail = new ListDetail(collection.getItemTitle(), collection.getItemSubTitle(), itemCount.toString());
            }else{
                listDetail = new ListDetail(iMediaItemBase.getItemTitle(), collection.getItemSubTitle(), itemCount.toString());
            }

            rootView.setLayoutManager(new LinearLayoutManager(mActivity));
            albumDetailAdapter = new AlbumDetailAdapter(mActivity, collection, listDetail);
            if (collection.count() < 1) {
                listIsEmpty();
            }
            rootView.setAdapter(albumDetailAdapter);
        }
    }

    public void listIsEmpty() {
        rootView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mUpdatePlayingItem);
        super.onDestroy();
    }
}