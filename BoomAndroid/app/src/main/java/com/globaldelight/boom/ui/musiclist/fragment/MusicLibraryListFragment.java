package com.globaldelight.boom.ui.musiclist.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.ui.musiclist.adapter.AlbumsGridAdapter;
import com.globaldelight.boom.ui.musiclist.adapter.ArtistsGridAdapter;
import com.globaldelight.boom.ui.musiclist.adapter.DefaultPlayListAdapter;
import com.globaldelight.boom.ui.musiclist.adapter.songAdapter.SongListAdapter;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.decorations.AlbumListSpacesItemDecoration;
import com.globaldelight.boom.utils.decorations.SimpleDividerItemDecoration;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.ui.musiclist.adapter.GenreGridAdapter;
import com.globaldelight.boom.utils.Utils;

import java.util.ArrayList;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class MusicLibraryListFragment extends Fragment {
    private boolean isOrderByAlbum=true;
    private Context context;
    private View mainView;
    private LinearLayout mLibContainer, mLibProgress;
    private RecyclerView recyclerView;
    private SongListAdapter songListAdapter;
    private AlbumsGridAdapter albumsGridAdapter;
    private ArtistsGridAdapter artistsGridAdapter;
    private DefaultPlayListAdapter defaultPlayListAdapter;
    private GenreGridAdapter genreGridAdapter;
    private PermissionChecker permissionChecker;
    private RegularTextView emptyListTxt;
    private LinearLayout emptyPlayList;
    private ProgressBar mLibLoad;
    private int page;
    private int title;
    private GridLayoutManager gridLayoutManager;

    public static MusicLibraryListFragment getInstance(int page, int title) {
        MusicLibraryListFragment fragmentFirst = new MusicLibraryListFragment();
        Bundle args = new Bundle();
        args.putInt("item", page);
        args.putInt("title", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("item", 0);
        title = getArguments().getInt("title");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view  = getItemView(inflater, container);

        mainView = view;
        context = mainView.getContext();
        initViews();
        return view;
    }

    private BroadcastReceiver mPlayerEventBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY :
                    if(null != songListAdapter){
                        songListAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if(null != songListAdapter)
            songListAdapter.notifyDataSetChanged();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY);
        if(null != getActivity())
            getActivity().registerReceiver(mPlayerEventBroadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(null != getActivity())
            getActivity().unregisterReceiver(mPlayerEventBroadcastReceiver);
    }

    public void listIsEmpty(boolean isPlayList) {
        mLibContainer.setVisibility(View.VISIBLE);
        mLibProgress.setVisibility(View.GONE);
        emptyPlayList.setVisibility(View.VISIBLE);
        if(isPlayList){
            emptyListTxt.setText(getActivity().getResources().getString(R.string.playlist_placeholder_txt));
        }else{
            emptyListTxt.setText(getActivity().getResources().getString(R.string.no_music_placeholder_txt));
        }
        recyclerView.setVisibility(View.GONE);
        mLibLoad.setVisibility(View.GONE);
        mLibLoad.setEnabled(false);
    }

    private void initViews() {
        mLibContainer = (LinearLayout)mainView.findViewById(R.id.lib_container);
        mLibProgress = (LinearLayout)mainView.findViewById(R.id.lib_progress);
        recyclerView = (RecyclerView) mainView.findViewById(R.id.albumsListContainer);
        emptyListTxt = (RegularTextView) mainView.findViewById(R.id.empty_list_txt);
        emptyPlayList = (LinearLayout) mainView.findViewById(R.id.album_empty_view) ;
        mLibContainer.setVisibility(View.GONE);
        mLibLoad = (ProgressBar)mainView.findViewById(R.id.lib_load);
        mLibLoad.setVisibility(View.VISIBLE);
        mLibLoad.setEnabled(true);
        fetchMusicList();
    }

    public void onBackPress() {
        songListAdapter.onBackPressed();
    }

    private synchronized void fetchMusicList(){
        switch (title){
            case R.string.songs:
                new LoadSongsList().execute(title);
                break;
            case R.string.albums:
            case R.string.artists:
            case R.string.playlists:
            case R.string.genres:
                new LoadCollectionList().execute(title);
                break;
        }
    }

    private View getItemView(LayoutInflater inflater, ViewGroup container){
        View view = inflater.inflate(R.layout.fragment_music_library_list,
                container, false);
        return view;
    }

    public void killActivity() {
        getActivity().finish();
    }


    private class LoadCollectionList extends AsyncTask<Integer, Integer, ArrayList<? extends IMediaItemBase>> {
        Integer param;
        @Override
        protected synchronized ArrayList<? extends IMediaItemBase> doInBackground(Integer... params) {
            param = params[0];
            if(param == R.string.albums)
                return MediaController.getInstance(context).getMediaCollectionItemList(ItemType.ALBUM, MediaType.DEVICE_MEDIA_LIB)/*MediaQuery.getAlbumList(context, !isOrderByAlbum)*/;
            else if(param == R.string.artists)
                return MediaController.getInstance(context).getMediaCollectionItemList(ItemType.ARTIST, MediaType.DEVICE_MEDIA_LIB)/*MediaQuery.getArtistList(context)*/;
            else if(param == R.string.playlists)
                return MediaController.getInstance(context).getMediaCollectionItemList(ItemType.PLAYLIST, MediaType.DEVICE_MEDIA_LIB)/*MediaQuery.getPlayList(context)*/;
            else
                return MediaController.getInstance(context).getMediaCollectionItemList(ItemType.GENRE, MediaType.DEVICE_MEDIA_LIB)/*MediaQuery.getGenreList(context)*/;
        }

        @Override
        protected synchronized void onPostExecute(ArrayList<? extends IMediaItemBase> iMediaCollectionList) {
            super.onPostExecute(iMediaCollectionList);
            if (null != getActivity()) {
                boolean isPhone = Utils.isPhone(getActivity());
                if(isPhone){
                    gridLayoutManager =
                            new GridLayoutManager(mainView.getContext(), 2);
                }else{
                    gridLayoutManager =
                            new GridLayoutManager(mainView.getContext(), 3);
                }
                gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                gridLayoutManager.scrollToPosition(0);
                recyclerView.setLayoutManager(gridLayoutManager);
                recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context, Utils.getWindowWidth(context)));
                recyclerView.addItemDecoration(new AlbumListSpacesItemDecoration(Utils.dpToPx(context, 0)));
                if(param == R.string.albums) {
                    albumsGridAdapter = new AlbumsGridAdapter(context, getActivity(), recyclerView, iMediaCollectionList, isPhone);
                    recyclerView.setAdapter(albumsGridAdapter);
                } else if(param == R.string.artists){
                    artistsGridAdapter = new ArtistsGridAdapter(context, getActivity(), recyclerView, iMediaCollectionList, isPhone);
                    recyclerView.setAdapter(artistsGridAdapter);
                } else if(param == R.string.playlists) {
                    defaultPlayListAdapter = new DefaultPlayListAdapter(context, getActivity(), recyclerView, iMediaCollectionList, isPhone);
                    recyclerView.setAdapter(defaultPlayListAdapter);
                }else{
                    genreGridAdapter = new GenreGridAdapter(context, getActivity(), recyclerView, iMediaCollectionList, isPhone);
                    recyclerView.setAdapter(genreGridAdapter);
                }
                recyclerView.setHasFixedSize(true);
                mLibContainer.setVisibility(View.VISIBLE);
                mLibLoad.setVisibility(View.GONE);
                mLibLoad.setEnabled(false);

                if (iMediaCollectionList.size() < 1) {
                    listIsEmpty(false);
                }
            }
        }
    }

    private class LoadSongsList extends AsyncTask<Integer, Integer, ArrayList<? extends IMediaItemBase>> {
        @Override
        protected ArrayList<? extends IMediaItemBase> doInBackground(Integer... params) {
            return MediaController.getInstance(context).getMediaCollectionItemList(ItemType.SONGS, MediaType.DEVICE_MEDIA_LIB) /*MediaQuery.getUpNextSongs(context)*/;
        }

        @Override
        protected void onPostExecute(ArrayList<? extends IMediaItemBase> iMediaItemList) {
            super.onPostExecute(iMediaItemList);
            FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_SONG_COUNT);
            final LinearLayoutManager llm = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(llm);
//                        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context, 0));
            recyclerView.setHasFixedSize(true);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
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
            songListAdapter = new SongListAdapter(getActivity(), MusicLibraryListFragment.this, iMediaItemList);
            recyclerView.setAdapter(songListAdapter);
            if (iMediaItemList.size() < 1) {
                listIsEmpty(false);
            }
            mLibContainer.setVisibility(View.VISIBLE);
            mLibLoad.setVisibility(View.GONE);
            mLibLoad.setEnabled(false);
        }
    }

}
