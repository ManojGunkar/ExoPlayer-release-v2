package com.globaldelight.boom.ui.musiclist.activity;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.ui.musiclist.adapter.BoomPlayListAdapter;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.decorations.AlbumListSpacesItemDecoration;
import com.globaldelight.boom.utils.decorations.SimpleDividerItemDecoration;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 31-08-2016.
 */
public class BoomPlaylistActivity extends BoomMasterActivity {
    private RecyclerView recyclerView;
    private BoomPlayListAdapter boomPlayListAdapter;
    private PermissionChecker permissionChecker;
    private View emptyView;
    FloatingActionButton addBoomPlaylist;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boom_playlist);

        setToolbarTitle("Boom Playlist");
        init();
        addBoomPlaylist = (FloatingActionButton)findViewById(R.id.add_boom_playlist);

        addBoomPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPlaylistDialog();
            }
        });
    }

    private void init() {
        recyclerView = (RecyclerView) findViewById(R.id.playlistContainer);
        emptyView = findViewById(R.id.playlist_empty_view);
        checkPermissions();
    }

    private void checkPermissions() {
        permissionChecker = new PermissionChecker(this, this, recyclerView);
        permissionChecker.check(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getResources().getString(R.string.storage_permission),
                new PermissionChecker.OnPermissionResponse() {
                    @Override
                    public void onAccepted() {
                        setArtistList();
                    }

                    @Override
                    public void onDecline() {
                        finish();
                    }
                });
    }

    private void setArtistList() {


        new Thread(new Runnable() {
            public void run() {
                final ArrayList<? extends IMediaItemBase>  playList = MediaController.getInstance(BoomPlaylistActivity.this).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB)/*MediaQuery.getPlayList(context)*/;
                final GridLayoutManager gridLayoutManager =
                        new GridLayoutManager(BoomPlaylistActivity.this, 2);
                        runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        gridLayoutManager.scrollToPosition(0);
                        recyclerView.setLayoutManager(gridLayoutManager);
                        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(BoomPlaylistActivity.this, Utils.getWindowWidth(BoomPlaylistActivity.this)));
                        recyclerView.addItemDecoration(new AlbumListSpacesItemDecoration(Utils.dpToPx(BoomPlaylistActivity.this, 0)));
                        boomPlayListAdapter = new BoomPlayListAdapter(BoomPlaylistActivity.this, recyclerView, playList, permissionChecker);
                        recyclerView.setAdapter(boomPlayListAdapter);
                        recyclerView.setHasFixedSize(true);
                    }
                });
                if (playList.size() < 1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listIsEmpty();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(boomPlayListAdapter != null){
            boomPlayListAdapter.updateNewList((ArrayList<? extends MediaItemCollection>) MediaController.getInstance(BoomPlaylistActivity.this).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB));
        }
    }

    public void listIsEmpty() {
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    public void listNoMoreEmpty() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onBackPress() {
        boomPlayListAdapter.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void newPlaylistDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.new_playlist)
                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (!input.toString().matches("")) {
                            MediaController.getInstance(BoomPlaylistActivity.this).createBoomPlaylist(input.toString());
                            listNoMoreEmpty();
                            boomPlayListAdapter.updateNewList((ArrayList<? extends MediaItemCollection>) MediaController.getInstance(BoomPlaylistActivity.this).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB));
                            Snackbar.make(recyclerView, "PlayList Created...!", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }).show();
    }

    @Override
    protected void onPause() {
        Log.d("BoomPlaylistActivity", "Pause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("BoomPlaylistActivity", "Destroy");
    }
}
