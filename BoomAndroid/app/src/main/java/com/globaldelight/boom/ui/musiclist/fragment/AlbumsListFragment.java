package com.globaldelight.boom.ui.musiclist.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.globaldelight.boom.Media.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.ui.musiclist.adapter.AlbumsGridAdapter;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.decorations.AlbumListSpacesItemDecoration;
import com.globaldelight.boom.utils.decorations.SimpleDividerItemDecoration;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 28-02-17.
 */

public class AlbumsListFragment extends Fragment {
    private Activity mActivity;
    private View mainView;
    private RecyclerView recyclerView;
    private AlbumsGridAdapter albumsGridAdapter;
    private ProgressBar mLibLoad;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            mActivity = (Activity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  = getItemView(inflater, container);
        mainView = view;
        if(null == mActivity)
            mActivity = getActivity();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
        FlurryAnalyticHelper.init(mActivity);
    }

    private View getItemView(LayoutInflater inflater, ViewGroup container){
        View view = inflater.inflate(R.layout.fragment_music_library_list,
                container, false);
        return view;
    }

    public void listIsEmpty(int size) {
        mainView.findViewById(R.id.lib_container).setVisibility(View.VISIBLE);
        if(size < 1) {
            ((ImageView) mainView.findViewById(R.id.list_empty_placeholder_icon)).setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_no_music_placeholder, null));
            ((RegularTextView) mainView.findViewById(R.id.list_empty_placeholder_txt)).setText(mActivity.getResources().getString(R.string.no_music_placeholder_txt));
        }
        mainView.findViewById(R.id.list_empty_placeholder).setVisibility(size < 1 ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(size > 0 ? View.VISIBLE : View.GONE);
        mLibLoad.setVisibility(View.GONE);
        mLibLoad.setEnabled(false);
    }

    private void initViews() {
        recyclerView = (RecyclerView) mainView.findViewById(R.id.albumsListContainer);
        mainView.findViewById(R.id.lib_container).setVisibility(View.GONE);
        mLibLoad = (ProgressBar)mainView.findViewById(R.id.lib_load);
        mLibLoad.setVisibility(View.VISIBLE);
        mLibLoad.setEnabled(true);
        new LoadCollectionList().execute();
    }

    private class LoadCollectionList extends AsyncTask<Void, Integer, ArrayList<? extends IMediaItemBase>> {
        GridLayoutManager gridLayoutManager;
        @Override
        protected synchronized ArrayList<? extends IMediaItemBase> doInBackground(Void... params) {
            return MediaController.getInstance(mActivity).getAlbumList();
        }

        @Override
        protected synchronized void onPostExecute(ArrayList<? extends IMediaItemBase> iMediaCollectionList) {
            super.onPostExecute(iMediaCollectionList);
            if (null != mActivity) {
                boolean isPhone = Utils.isPhone(mActivity);
                if(isPhone){
                    gridLayoutManager =
                            new GridLayoutManager(mActivity, 2);
                }else{
                    gridLayoutManager =
                            new GridLayoutManager(mActivity, 3);
                }
                gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                gridLayoutManager.scrollToPosition(0);
                recyclerView.setLayoutManager(gridLayoutManager);
                recyclerView.addItemDecoration(new SimpleDividerItemDecoration(mActivity, Utils.getWindowWidth(mActivity)));
                recyclerView.addItemDecoration(new AlbumListSpacesItemDecoration(Utils.dpToPx(mActivity, 0)));
                albumsGridAdapter = new AlbumsGridAdapter(mActivity, mActivity, recyclerView, iMediaCollectionList, isPhone);
                recyclerView.setAdapter(albumsGridAdapter);
                recyclerView.setHasFixedSize(true);
                listIsEmpty(iMediaCollectionList.size());
            }
        }
    }
    @Override
    public  void onStart() {
        super.onStart();
        FlurryAnalyticHelper.flurryStartSession(mActivity);
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAnalyticHelper.flurryStopSession(mActivity);
    }
}
