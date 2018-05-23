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
import com.globaldelight.boom.utils.Utils;
import com.google.gson.Gson;

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
    private Activity mActivity;
    private Item mItem;
    private List<Item> mItemList;
    private PlaylistDialogAdapter adapter;
    private TidalHelper mHelper;

    private TidalPopupMenu(Activity activity) {
        this.mActivity = activity;
        mHelper = TidalHelper.getInstance(mActivity);
    }

    public static TidalPopupMenu newInstance(Activity activity) {
        return new TidalPopupMenu(activity);
    }

    public void showPopup(View view, Item item) {
        mItem = item;
        getMenu(view).setOnMenuItemClickListener(this::onMenuItemClick);
    }

    public void showHeaderPopup(View view, Item item, List<Item> items) {
        mItem = item;
        mItemList = items;
        PopupMenu popupMenu = new PopupMenu(mActivity, view);
        if ( mItem.getItemType() == ItemType.PLAYLIST && mItem.getType().equals("USER") ) {
            popupMenu.inflate(R.menu.tidal_playlist_header_menu);
        }
        else {
            popupMenu.inflate(R.menu.tidal_collection_header_menu);
            boolean isFavourite =  mHelper.getFavoriteManager().isFavorite(mItem);
            popupMenu.getMenu().findItem(R.id.tidal_menu_remove_fav).setVisible(isFavourite);
            popupMenu.getMenu().findItem(R.id.tidal_menu_add_to_fav).setVisible(!isFavourite);
        }
        popupMenu.setOnMenuItemClickListener(this::onMenuItemClick);
        popupMenu.show();
    }


    private PopupMenu getMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(mActivity, view);
        if ( mItem.getItemType() == ItemType.PLAYLIST && mItem.getType().equals("USER") ) {
            popupMenu.inflate(R.menu.playlist_boom_menu);
        }
        else {
            popupMenu.inflate(R.menu.tidal_item_menu);
            boolean isFavourite =  mHelper.getFavoriteManager().isFavorite(mItem);
            popupMenu.getMenu().findItem(R.id.tidal_menu_remove_fav).setVisible(isFavourite);
            popupMenu.getMenu().findItem(R.id.tidal_menu_add_to_fav).setVisible(!isFavourite);
        }
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

            case R.id.collection_shuffle_item:
                shuffle();
                break;

            case R.id.popup_playlist_rename:
            case R.id.playlist_rename_item:
                renamePlaylist();
                break;

            case R.id.popup_playlist_delete:
                deletePlaylist();
                break;
        }
        return false;
    }

    public void shuffle() {
        if (mItemList != null ){
            App.playbackManager().queue().addItemListToPlay(mItemList, 0, true);
        }
        else {
            mHelper.getTracks(mItem, result -> {
                if (result.isSuccess()) {
                    App.playbackManager().queue().addItemListToPlay(result.get(), 0, true);
                }
            });
        }
    }


    public void refreshList(String action) {
        Intent intent = new Intent(ACTION_REFRESH_LIST);
        intent.putExtra("item", new Gson().toJson(mItem));
        intent.putExtra("action", action);
        LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent);
    }


    private void addToQueue() {
        if (mItem.getItemType() == ItemType.SONGS) {
            App.playbackManager().queue().addItemAsUpNext(mItem);
        }
        else if (mItemList != null ){
            App.playbackManager().queue().addItemAsUpNext(mItemList);
        }
        else {
            mHelper.getTracks(mItem, result -> {
                if (result.isSuccess()) {
                    App.playbackManager().queue().addItemAsUpNext(result.get());
                }
            });
        }
    }

    private void playNext() {
        if (mItem.getItemType() == ItemType.SONGS) {
            App.playbackManager().queue().addItemAsPlayNext(mItem);
        }
        else if ( mItemList != null ) {
            App.playbackManager().queue().addItemAsPlayNext(mItemList);
        }
        else {
            mHelper.getTracks(mItem, result -> {
                if (result.isSuccess()) {
                    App.playbackManager().queue().addItemAsPlayNext(result.get());
                }
            });
        }
    }


    private void addToFavourites() {
        Call<Void> call = TidalHelper.getInstance(mActivity).addToFavorites(mItem);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(mActivity, "Added Successfully", Toast.LENGTH_SHORT).show();
                mHelper.getFavoriteManager().addToFavorites(mItem);
                refreshList("add");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(mActivity, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeFromFavourites() {
        Call<Void> call = TidalHelper.getInstance(mActivity).removeFromFavorites(mItem);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                refreshList("remove");
                mHelper.getFavoriteManager().removeFromFavorites(mItem);
                Toast.makeText(mActivity, "Removed Successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(mActivity, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renamePlaylist() {
        Utils.createDialogBuilder(mActivity)
                .title(R.string.rename)
                .cancelable(true)
                .positiveText(mActivity.getResources().getString(R.string.done))
                .negativeText(mActivity.getResources().getString(R.string.dialog_txt_cancel))
                .input(null, mItem.getTitle(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (input.toString().matches("")) {
                            renamePlaylist();
                        } else {
                            final String newName = input.toString();
                            mHelper.renamePlaylist(mItem, newName, (result)->{
                                if ( result.isSuccess() ) {
                                    mItem.setTitle(newName);
                                    Toast.makeText(mActivity, R.string.tidal_renamed, Toast.LENGTH_SHORT).show();
                                    refreshList("refresh");
                                }
                                else {
                                    Toast.makeText(mActivity, R.string.tidal_failed_rename, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).show();
    }

    private void deletePlaylist() {
        String content = mActivity.getResources().getString(R.string.delete_dialog_txt, mItem.getTitle());
        Utils.createDialogBuilder(mActivity).title(R.string.delete_dialog_title)
                .content(content)
                .positiveText(mActivity.getResources().getString(R.string.ok))
                .negativeText(mActivity.getResources().getString(R.string.dialog_txt_cancel))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        mHelper.deletePlaylist(mItem, (result)-> {
                            if ( result.isSuccess() ) {
                                refreshList("remove");
                                Toast.makeText(mActivity, R.string.playlist_deleted, Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(mActivity, R.string.tidal_failed_delete, Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).show();

    }

    private void showPlaylistDialog() {
        RecyclerView rv = (RecyclerView) mActivity.getLayoutInflater()
                .inflate(R.layout.addtoplaylist, null);
        adapter = new PlaylistDialogAdapter(mActivity, TidalHelper.getInstance(mActivity).getUserPlaylists(), this);
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
                                        TidalHelper.getInstance(mActivity).addToUserPlaylist(response.body());
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
        TidalHelper.getInstance(mActivity).addItemToPlaylist(mItem, item.getId(), (result)->{
            if ( result.isSuccess() ) {
                item.setNumberOfTracks(item.getNumberOfTracks() + 1);
                Toast.makeText(mActivity, R.string.added_to_playlist, Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(mActivity, R.string.failed_to_add_to_playlist, Toast.LENGTH_LONG).show();
            }
        });
    }
}
