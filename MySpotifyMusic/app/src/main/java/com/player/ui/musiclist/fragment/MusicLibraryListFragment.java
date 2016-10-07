package com.player.ui.musiclist.fragment;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import com.player.data.MediaCollection.IMediaItemBase;
import com.player.data.MediaLibrary.ItemType;
import com.player.data.MediaLibrary.MediaController;
import com.player.data.MediaLibrary.MediaType;
import com.player.myspotifymusic.R;
import com.player.ui.musiclist.adapter.AlbumsGridAdapter;
import com.player.ui.musiclist.adapter.ArtistsGridAdapter;
import com.player.ui.musiclist.adapter.DefaultPlayListAdapter;
import com.player.ui.musiclist.adapter.GenreGridAdapter;
import com.player.ui.musiclist.adapter.SongListAdapter;
import com.player.utils.PermissionChecker;
import com.player.utils.Utils;
import com.player.utils.decorations.AlbumListSpacesItemDecoration;
import com.player.utils.decorations.SimpleDividerItemDecoration;

import java.util.ArrayList;


public class MusicLibraryListFragment extends Fragment {
    private boolean isOrderByAlbum=true;
    private Context context;
    private View mainView;
    private RecyclerView recyclerView;
    private SongListAdapter songListAdapter;
    private AlbumsGridAdapter albumsGridAdapter;
    private ArtistsGridAdapter artistsGridAdapter;
    private DefaultPlayListAdapter defaultPlayListAdapter;
    private GenreGridAdapter genreGridAdapter;
    private PermissionChecker permissionChecker;
    private View emptyView;
    private int page;
    private String title;

    public static MusicLibraryListFragment getInstance(int page, String title) {
        MusicLibraryListFragment fragmentFirst = new MusicLibraryListFragment();
        Bundle args = new Bundle();
        args.putInt("item", page);
        args.putString("title", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("item", 0);
        title = getArguments().getString("title");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view  = getItemView(inflater, container);

        mainView = view;
        context = mainView.getContext();
        init();
        return view;
    }

    private void setSongList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<? extends IMediaItemBase> songList = MediaController.getInstance(context).getMediaCollectionItemList(ItemType.SONGS, MediaType.DEVICE_MEDIA_LIB) /*MediaQuery.getSongList(context)*/;
                final LinearLayoutManager llm = new LinearLayoutManager(context);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setLayoutManager(llm);
                        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context, 0));
                        recyclerView.setHasFixedSize(true);
                        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);
                                songListAdapter.recyclerScrolled();
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
                        songListAdapter = new SongListAdapter(context, MusicLibraryListFragment.this.getActivity(), songList, permissionChecker);
                        recyclerView.setAdapter(songListAdapter);
                        if (songList.size() < 1) {
                            listIsEmpty();
                        }
                    }
                });
            }
        }).start();
    }

    private void setAlbumList() {

        new Thread(new Runnable() {
            public void run() {
                final ArrayList<? extends IMediaItemBase> albumList = MediaController.getInstance(context).getMediaCollectionItemList(ItemType.ALBUM, MediaType.DEVICE_MEDIA_LIB)/*MediaQuery.getAlbumList(context, !isOrderByAlbum)*/;
                final GridLayoutManager gridLayoutManager =
                        new GridLayoutManager(mainView.getContext(), 2);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        gridLayoutManager.scrollToPosition(0);
                        recyclerView.setLayoutManager(gridLayoutManager);
                        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context, Utils.getWindowWidth(context)));
                        recyclerView.addItemDecoration(new AlbumListSpacesItemDecoration(Utils.dpToPx(context, 0)));
                        albumsGridAdapter = new AlbumsGridAdapter(context, recyclerView, albumList, permissionChecker);
                        recyclerView.setAdapter(albumsGridAdapter);
                        recyclerView.setHasFixedSize(true);
                    }
                });
                if (albumList.size() < 1) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listIsEmpty();
                        }
                    });
                }
            }
        }).start();
    }

    private void setArtistList() {

        new Thread(new Runnable() {
            public void run() {
                final ArrayList<? extends IMediaItemBase> artistList = MediaController.getInstance(context).getMediaCollectionItemList(ItemType.ARTIST, MediaType.DEVICE_MEDIA_LIB)/*MediaQuery.getArtistList(context)*/;
                final GridLayoutManager gridLayoutManager =
                        new GridLayoutManager(mainView.getContext(), 2);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        gridLayoutManager.scrollToPosition(0);
                        recyclerView.setLayoutManager(gridLayoutManager);
                        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context, Utils.getWindowWidth(context)));
                        recyclerView.addItemDecoration(new AlbumListSpacesItemDecoration(Utils.dpToPx(context, 0)));
                        artistsGridAdapter = new ArtistsGridAdapter(context, recyclerView, artistList, permissionChecker);
                        recyclerView.setAdapter(artistsGridAdapter);
                        recyclerView.setHasFixedSize(true);
                    }
                });
                if (artistList.size() < 1) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listIsEmpty();
                        }
                    });
                }
            }
        }).start();
    }

    private void setDefaultPlayList() {
        new Thread(new Runnable() {
            public void run() {
                final ArrayList<? extends IMediaItemBase>  playList = MediaController.getInstance(context).getMediaCollectionItemList(ItemType.PLAYLIST, MediaType.DEVICE_MEDIA_LIB)/*MediaQuery.getPlayList(context)*/;
                final GridLayoutManager gridLayoutManager =
                        new GridLayoutManager(mainView.getContext(), 2);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        gridLayoutManager.scrollToPosition(0);
                        recyclerView.setLayoutManager(gridLayoutManager);
                        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context, Utils.getWindowWidth(context)));
                        recyclerView.addItemDecoration(new AlbumListSpacesItemDecoration(Utils.dpToPx(context, 0)));
                        defaultPlayListAdapter = new DefaultPlayListAdapter(context, recyclerView, playList, permissionChecker);
                        recyclerView.setAdapter(defaultPlayListAdapter);
                        recyclerView.setHasFixedSize(true);
                    }
                });
                if (playList.size() < 1) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listIsEmpty();
                        }
                    });
                }
            }
        }).start();
    }

    private void setGenreList() {

        new Thread(new Runnable() {
            public void run() {
                final ArrayList<? extends IMediaItemBase> genreList = MediaController.getInstance(context).getMediaCollectionItemList(ItemType.GENRE, MediaType.DEVICE_MEDIA_LIB)/*MediaQuery.getGenreList(context)*/;
                final GridLayoutManager gridLayoutManager =
                        new GridLayoutManager(mainView.getContext(), 2);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        gridLayoutManager.scrollToPosition(0);
                        recyclerView.setLayoutManager(gridLayoutManager);
                        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context, Utils.getWindowWidth(context)));
                        recyclerView.addItemDecoration(new AlbumListSpacesItemDecoration(Utils.dpToPx(context, 0)));
                        genreGridAdapter = new GenreGridAdapter(context, recyclerView, genreList, permissionChecker);
                        recyclerView.setAdapter(genreGridAdapter);
                        recyclerView.setHasFixedSize(true);
                    }
                });
                if (genreList.size() < 1) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listIsEmpty();
                        }
                    });
                }
            }
        }).start();
    }

    public void listIsEmpty() {
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void init() {
        recyclerView = (RecyclerView) mainView.findViewById(R.id.albumsListContainer);
        emptyView = mainView.findViewById(R.id.album_empty_view);
        checkPermissions();
    }

    private void checkPermissions() {
        permissionChecker = new PermissionChecker(context, getActivity(), mainView);
        permissionChecker.check(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getResources().getString(R.string.storage_permission),
                new PermissionChecker.OnPermissionResponse() {
                    @Override
                    public void onAccepted() {
                        switch (title){
                            case "Song":
                                setSongList();
                                break;
                            case "Album":
                                setAlbumList();
                                break;
                            case "Artist":
                                setArtistList();
                                break;
                            case "PlayList":
                                setDefaultPlayList();
                                break;
                            case "Genre":
                                setGenreList();
                                break;
                        }
                    }

                    @Override
                    public void onDecline() {
                        getActivity().finish();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onBackPress() {
        songListAdapter.onBackPressed();
    }

    public void setPermissionChecker(PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
    }

    private View getItemView(LayoutInflater inflater, ViewGroup container){
        View view = inflater.inflate(R.layout.fragment_music_library_list,
                container, false);
        return view;
    }
}
