package com.globaldelight.boom.app.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.collection.local.callback.IMediaItemCollection;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.app.adapters.model.ListDetail;
import com.globaldelight.boom.app.adapters.album.DetailAlbumGridAdapter;
import com.globaldelight.boom.view.MarginDecoration;
import com.globaldelight.boom.utils.Utils;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class AlbumDetailItemFragment extends Fragment {

    private IMediaItemCollection collection;
    private ListDetail listDetail;
    private RecyclerView rootView;
    private GridLayoutManager gridLayoutManager;
    private DetailAlbumGridAdapter detailAlbumGridAdapter;
    private Activity mActivity;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AlbumDetailItemFragment() {
    }

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
        collection = (MediaItemCollection) b.getParcelable("mediaItemCollection");
        setListDetail();
        new LoadAlbumItems().execute();
        setForAnimation();
//        FlurryAnalyticHelper.init(mActivity);
    }

    private void setListDetail() {
        StringBuilder albumCount = new StringBuilder();
        albumCount.append(collection.getItemListCount() > 1 ? getResources().getString(R.string.albums) : getResources().getString(R.string.album));
        albumCount.append(" ");
        albumCount.append(collection.getItemListCount());

        StringBuilder songCount = new StringBuilder();
        songCount.append(collection.getItemCount()>1 ? getResources().getString(R.string.songs) : getResources().getString(R.string.song));
        songCount.append(" ");
        songCount.append(collection.getItemCount());

        listDetail = new ListDetail(collection.getItemTitle(), albumCount.toString(), songCount.toString());
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) this.mActivity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(collection.getItemTitle());
        }
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
    }

    public void onFloatPlayAlbums() {
        if(collection.getParentType() == ItemType.ARTIST && ((IMediaItemCollection)collection.getItemAt(0)).count() == 0) {
            ((IMediaItemCollection) collection.getItemAt(0)).setMediaElement(MediaController.getInstance(mActivity).getArtistTrackList(collection));
        }else if(collection.getParentType() == ItemType.GENRE && ((IMediaItemCollection)collection.getItemAt(0)).count() == 0) {
            ((IMediaItemCollection) collection.getItemAt(0)).setMediaElement(MediaController.getInstance(mActivity).getGenreTrackList(collection));
        }
        App.playbackManager().queue().addItemListToPlay((IMediaItemCollection) collection.getItemAt(0), 0);
        detailAlbumGridAdapter.notifyDataSetChanged();
    }

    private class LoadAlbumItems extends AsyncTask<Void, Integer, IMediaItemBase> {

        @Override
        protected IMediaItemBase doInBackground(Void... params) {
//            ItemType.ARTIST && ItemType.GENRE
            if(collection.getParentType() == ItemType.ARTIST && collection.count() == 0)
                collection.setMediaElement(MediaController.getInstance(mActivity).getArtistAlbumsList(collection));
            else if(collection.getParentType() == ItemType.GENRE && collection.count() == 0)
                collection.setMediaElement(MediaController.getInstance(mActivity).getGenreAlbumsList(collection));
            return collection;
        }

        @Override
        protected void onPostExecute(IMediaItemBase iMediaItemBase) {
            super.onPostExecute(iMediaItemBase);
            boolean isPhone = Utils.isPhone(mActivity);
            if(isPhone){
                gridLayoutManager =
                        new GridLayoutManager(mActivity, 2);
            }else{
                gridLayoutManager =
                        new GridLayoutManager(mActivity, 3);
            }
            rootView.setLayoutManager(gridLayoutManager);
            rootView.addItemDecoration(new MarginDecoration(mActivity));
            rootView.setHasFixedSize(true);
            detailAlbumGridAdapter = new DetailAlbumGridAdapter(mActivity, rootView, (IMediaItemCollection) iMediaItemBase, listDetail, isPhone);
            rootView.setAdapter(detailAlbumGridAdapter);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return detailAlbumGridAdapter.isHeader(position) ? gridLayoutManager.getSpanCount() : 1;
                }
            });
            if (((IMediaItemCollection) iMediaItemBase).count() < 1) {
                listIsEmpty();
            }
        }
    }

    public void listIsEmpty() {
        rootView.setVisibility(View.GONE);
    }
    @Override
    public  void onStart() {
        super.onStart();
//        FlurryAnalyticHelper.flurryStartSession(mActivity);
        FlurryAnalytics.getInstance(getActivity()).startSession();
    }

    @Override
    public void onStop() {
        super.onStop();
//        FlurryAnalyticHelper.flurryStopSession(mActivity);
        FlurryAnalytics.getInstance(getActivity()).endSession();

    }

}
