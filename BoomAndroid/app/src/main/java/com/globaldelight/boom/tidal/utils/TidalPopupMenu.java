package com.globaldelight.boom.tidal.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.tidal.tidalconnector.TidalRequestController;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.tidal.ui.adapter.PlaylistDialogAdapter;
import com.globaldelight.boom.utils.Log;
import com.globaldelight.boom.utils.RequestChain;
import com.globaldelight.boom.utils.Utils;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_REFRESH_LIST;

/**
 * Created by Manoj Kumar on 09-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalPopupMenu implements PopupMenu.OnMenuItemClickListener, PlaylistDialogAdapter.Callback {

    private static List<NestedItemDescription> mItemList = new ArrayList<>();
    private Activity mActivity;
    private Item mItem;
    private boolean mIsFavourite;
    private PlaylistDialogAdapter adapter;

    private TidalPopupMenu(Activity activity) {
        this.mActivity = activity;
    }

    public static TidalPopupMenu newInstance(Activity activity) {
        return new TidalPopupMenu(activity);
    }

    public void showPopup(View view, Item item, boolean isFavourite) {
        mItem = item;
        mIsFavourite = isFavourite;
        getMenu(view).setOnMenuItemClickListener(this::onMenuItemClick);
    }

    private PopupMenu getMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(mActivity, view);
        popupMenu.inflate(R.menu.tidal_menu);
        popupMenu.getMenu().findItem(R.id.tidal_menu_add_to_fav).setVisible(!mIsFavourite);
        popupMenu.getMenu().findItem(R.id.tidal_menu_remove_fav).setVisible(mIsFavourite);
        popupMenu.show();
        return popupMenu;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tidal_menu_add_to_fav:
                addToFavourites();
                break;

            case R.id.tidal_menu_remove_fav:
                removeFromFavourites();
                break;

            case R.id.tidal_menu_add_to_playlist:
                showPlaylistDialog();
                break;

            case R.id.tidal_menu_add_to_upnext:
                addToQueue();
                break;

            case R.id.tidal_menu_play_next:
                playNext();
                break;

            case R.id.tidal_menu_shuffle:
                break;
        }
        return false;
    }

    public void saveNestedItems(List<NestedItemDescription> itemList) {
        mItemList = itemList;
        Log.d("Tidal", "Nested Item Size=" + mItemList.size());
    }

    public List<NestedItemDescription> getNestedItems() {
        return mItemList;
    }

    public void refreshList() {
        NestedItemDescription itemDescription;
        List<NestedItemDescription> list = new ArrayList<>();
        for (int i = 0; i < mItemList.size(); i++) {

            List<Item> items = mItemList.get(i).itemList;
            int titleResId = mItemList.get(i).titleResId;
            String path = mItemList.get(i).apiPath;
            int type = mItemList.get(i).type;
            if (items.contains(mItem)) {
                items.remove(mItem);
            }
            itemDescription = new NestedItemDescription(titleResId, type, items, path);
            list.add(itemDescription);

        }
        saveNestedItems(list);
        LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(ACTION_REFRESH_LIST));
    }

    private void addToMyMusic(Call<JsonElement> call) {
        RequestChain requestChain = new RequestChain(mActivity);
        requestChain.submit(call, resp -> {
            if (resp == null)
                Toast.makeText(mActivity, "Added Successfully", Toast.LENGTH_SHORT).show();
            else Toast.makeText(mActivity, "Something went wrong", Toast.LENGTH_SHORT).show();
        });
    }

    private void removeFromMyMusic(Call<JsonElement> call) {
        RequestChain requestChain = new RequestChain(mActivity);
        requestChain.submit(call, resp -> {
            if (resp == null) {
                Toast.makeText(mActivity, "Remove Successfully", Toast.LENGTH_SHORT).show();
                refreshList();
            } else Toast.makeText(mActivity, "Something went wrong", Toast.LENGTH_SHORT).show();
        });
    }

    private void addToQueue() {
        if (mItem.getItemType() == ItemType.SONGS) {
            App.playbackManager().queue().addItemAsUpNext(mItem);
        } else {

        }
    }

    private void playNext() {
        if (mItem.getItemType() == ItemType.SONGS) {
            App.playbackManager().queue().addItemAsPlayNext(mItem);
        } else {

        }
    }


    private void addToFavourites() {
        switch (mItem.getItemType()) {
            case ItemType.SONGS:
                addToMyMusic(TidalHelper.getInstance(mActivity).addToTrack(mItem.getId()));
                break;

            case ItemType.ALBUM:
                addToMyMusic(TidalHelper.getInstance(mActivity).addToAlbum(mItem.getId()));
                break;

            case ItemType.ARTIST:
                addToMyMusic(TidalHelper.getInstance(mActivity).addToArtist(mItem.getId()));
                break;

            case ItemType.PLAYLIST:
                addToMyMusic(TidalHelper.getInstance(mActivity).addToPlaylist(mItem.getId()));
                break;
        }
    }

    private void removeFromFavourites() {
        switch (mItem.getItemType()) {
            case ItemType.SONGS:
                removeFromMyMusic(TidalHelper.getInstance(mActivity).removeTrack(mItem.getId()));
                break;

            case ItemType.ALBUM:
                removeFromMyMusic(TidalHelper.getInstance(mActivity).removeAlbum(mItem.getId()));
                break;

            case ItemType.ARTIST:
                removeFromMyMusic(TidalHelper.getInstance(mActivity).removeArtist(mItem.getId()));
                break;

            case ItemType.PLAYLIST:
                removeFromMyMusic(TidalHelper.getInstance(mActivity).removePlaylist(mItem.getId()));
                break;
        }
    }

    private void showPlaylistDialog() {
        RecyclerView rv = (RecyclerView) mActivity.getLayoutInflater()
                .inflate(R.layout.addtoplaylist, null);
        adapter = new PlaylistDialogAdapter(mActivity, TidalHelper.getInstance(mActivity).getMyPlaylists(), this);
        rv.setLayoutManager(new LinearLayoutManager(mActivity));
        rv.setAdapter(adapter);
        MaterialDialog dialog = Utils.createDialogBuilder(mActivity)
                .title(R.string.menu_add_boom_playlist)
                .customView(rv, false)
                .positiveText(R.string.new_playlist)
                .negativeText(R.string.dialog_txt_cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        showNewDialog();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                }).show();
    }

    private void showNewDialog() {
        Utils.createDialogBuilder(mActivity).title(R.string.new_playlist)
                .input(mActivity.getResources().getString(R.string.new_playlist), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (!input.toString().matches("")) {
                            TidalRequestController.Callback callback = TidalRequestController.getTidalClient();
                            Call<Item> call = callback.createPlaylist(
                                    UserCredentials.getCredentials(mActivity).getSessionId(),
                                    UserCredentials.getCredentials(mActivity).getUserId(),
                                    input.toString(), "My Playlist");
                            call.enqueue(new Callback<Item>() {
                                @Override
                                public void onResponse(Call<Item> call, Response<Item> response) {
                                    if (response.isSuccessful()) {
                                        String etag = response.headers().get("etag");
                                        UserCredentials.getCredentials(mActivity).setETag(etag);
                                    }
                                }

                                @Override
                                public void onFailure(Call<Item> call, Throwable t) {

                                }
                            });
                        }
                    }
                }).show();
    }

    @Override
    public void onItemSelected(Item item) {
        TidalHelper.getInstance(mActivity).addItemToPlaylist(mItem, item.getId());
    }
}
