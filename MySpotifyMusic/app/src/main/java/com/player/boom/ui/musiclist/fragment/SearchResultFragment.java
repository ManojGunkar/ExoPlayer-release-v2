package com.player.boom.ui.musiclist.fragment;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.player.boom.R;
import com.player.boom.utils.PermissionChecker;

/**
 * Created by Rahul Agarwal on 15-09-16.
 */
public class SearchResultFragment extends Fragment {
    private Context context;
    private View mainView;
    private String title;
    private int page;
    private RecyclerView recyclerView;
    private View emptyView;
    private PermissionChecker permissionChecker;
    
    public static SearchResultFragment getInstance(int page, String title) {
        SearchResultFragment fragmentFirst = new SearchResultFragment();
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

    private void init() {
//        recyclerView = (RecyclerView) mainView.findViewById(R.id.albumsListContainer);
//        emptyView = mainView.findViewById(R.id.album_empty_view);
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
//                                setSongList();
                                break;
                            case "Album":
//                                setAlbumList();
                                break;
                            case "Artist":
//                                setArtistList();
                                break;
                            case "PlayList":
//                                setDefaultPlayList();
                                break;
                            case "Genre":
//                                setGenreList();
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

    public void setPermissionChecker(PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
    }
    private View getItemView(LayoutInflater inflater, ViewGroup container) {
        View view = null;/* = inflater.inflate(R.layout.fragment_search_result,
                container, false);*/
        return view;
    }
}
