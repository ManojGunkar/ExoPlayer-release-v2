package com.globaldelight.boom.ui.musiclist.activity;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.ui.musiclist.adapter.BoomPlayListAdapter;
import com.globaldelight.boom.ui.musiclist.adapter.FavouriteListAdapter;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.decorations.AlbumListSpacesItemDecoration;
import com.globaldelight.boom.utils.decorations.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Rahul Agarwal on 20-11-16.
 */

public class FavouriteListActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RegularTextView mToolbarTitle;
    private RecyclerView recyclerView;
    private FavouriteListAdapter adapter;
    private PermissionChecker permissionChecker;
    private View emptyView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(R.anim.push_up_in, R.anim.stay_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        initView();
    }

    private void initView() {
        toolbar = (Toolbar)findViewById(R.id.favourite_list_toolbar);

        mToolbarTitle = (RegularTextView) findViewById(R.id.favourite_list_toolbar_title);
        mToolbarTitle.setTextColor(Color.WHITE);

        recyclerView = (RecyclerView) findViewById(R.id.rv_favourite_list);

        emptyView = findViewById(R.id.fav_empty_view);

        checkPermissions();
    }

    private void setFavouriteList() {


        new Thread(new Runnable() {
            public void run() {
                final LinkedList<? extends IMediaItemBase> favList = MediaController.getInstance(FavouriteListActivity.this).getFavouriteListItems();
                final GridLayoutManager gridLayoutManager =
                        new GridLayoutManager(FavouriteListActivity.this, 2);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        gridLayoutManager.scrollToPosition(0);
                        recyclerView.setLayoutManager(gridLayoutManager);
                        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(FavouriteListActivity.this, Utils.getWindowWidth(FavouriteListActivity.this)));
                        recyclerView.addItemDecoration(new AlbumListSpacesItemDecoration(Utils.dpToPx(FavouriteListActivity.this, 0)));
                        adapter = new FavouriteListAdapter(FavouriteListActivity.this, recyclerView, favList, permissionChecker);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setHasFixedSize(true);
                    }
                });
                if (favList.size() < 1) {
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

    public void listIsEmpty() {
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void checkPermissions() {
        permissionChecker = new PermissionChecker(this, this, recyclerView);
        permissionChecker.check(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getResources().getString(R.string.storage_permission),
                new PermissionChecker.OnPermissionResponse() {
                    @Override
                    public void onAccepted() {
                        setFavouriteList();
                    }

                    @Override
                    public void onDecline() {
                        finish();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void killActivity() {

    }
}
