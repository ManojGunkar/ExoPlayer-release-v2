package com.globaldelight.boom.ui.musiclist.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.globaldelight.boom.App;
import com.globaldelight.boom.Media.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.Media.ItemType;
import com.globaldelight.boom.ui.musiclist.ListDetail;
import com.globaldelight.boom.ui.musiclist.adapter.DetailAlbumGridAdapter;
import com.globaldelight.boom.ui.widgets.MarginDecoration;
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
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AlbumDetailItemFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        collection = (MediaItemCollection) this.getActivity().getIntent().getParcelableExtra("mediaItemCollection");

        setListDetail();
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
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) this.getActivity().findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(collection.getItemTitle());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);

        new LoadAlbumItems().execute();
        setForAnimation();
        return rootView;
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
    }

    public void onFloatPlayAlbums() {
        if(collection.getParentType() == ItemType.ARTIST && ((IMediaItemCollection)collection.getMediaElement().get(0)).getMediaElement().size() == 0) {
            ((IMediaItemCollection) collection.getMediaElement().get(0)).setMediaElement(MediaController.getInstance(getContext()).getArtistTrackList(collection));
        }else if(collection.getParentType() == ItemType.GENRE && ((IMediaItemCollection)collection.getMediaElement().get(0)).getMediaElement().size() == 0) {
            ((IMediaItemCollection) collection.getMediaElement().get(0)).setMediaElement(MediaController.getInstance(getContext()).getGenreTrackList(collection));
        }
        App.getPlayingQueueHandler().getUpNextList().addCollectionTrackToPlay(collection, 0, true);
        detailAlbumGridAdapter.notifyDataSetChanged();
    }

    private class LoadAlbumItems extends AsyncTask<Void, Integer, IMediaItemBase> {

        @Override
        protected IMediaItemBase doInBackground(Void... params) {
//            ItemType.ARTIST && ItemType.GENRE
            if(collection.getParentType() == ItemType.ARTIST && collection.getMediaElement().size() == 0)
                collection.setMediaElement(MediaController.getInstance(getActivity()).getArtistAlbumsList(collection));
            else if(collection.getParentType() == ItemType.GENRE && collection.getMediaElement().size() == 0)
                collection.setMediaElement(MediaController.getInstance(getActivity()).getGenreAlbumsList(collection));
            return collection;
        }

        @Override
        protected void onPostExecute(IMediaItemBase iMediaItemBase) {
            super.onPostExecute(iMediaItemBase);
            boolean isPhone = Utils.isPhone(getActivity());
            if(isPhone){
                gridLayoutManager =
                        new GridLayoutManager(getActivity(), 2);
            }else{
                gridLayoutManager =
                        new GridLayoutManager(getActivity(), 3);
            }
            rootView.setLayoutManager(gridLayoutManager);
            rootView.addItemDecoration(new MarginDecoration(getActivity()));
            rootView.setHasFixedSize(true);
            detailAlbumGridAdapter = new DetailAlbumGridAdapter(getActivity(), rootView, (IMediaItemCollection) iMediaItemBase, listDetail, isPhone);
            rootView.setAdapter(detailAlbumGridAdapter);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return detailAlbumGridAdapter.isHeader(position) ? gridLayoutManager.getSpanCount() : 1;
                }
            });
            if (((IMediaItemCollection) iMediaItemBase).getMediaElement().size() < 1) {
                listIsEmpty();
            }
        }
    }

    public void listIsEmpty() {
        rootView.setVisibility(View.GONE);
    }
}
