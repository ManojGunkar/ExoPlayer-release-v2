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
import com.globaldelight.boom.utils.Result;
import com.globaldelight.boom.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

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
    private PlaylistDialogAdapter adapter;
    private boolean isUserPlaylistTrack=false;

    private TidalPopupMenu(Activity activity) {
        this.mActivity = activity;
    }

    public static TidalPopupMenu newInstance(Activity activity) {
        return new TidalPopupMenu(activity);
    }

    public void showPopup(View view, Item item, boolean isFavourite,boolean isUserPlaylistTrack) {
        mItem = item;
        this.isUserPlaylistTrack=isUserPlaylistTrack;
        getMenu(view,isFavourite,isUserPlaylistTrack).setOnMenuItemClickListener(this::onMenuItemClick);
    }

    private PopupMenu getMenu(View view,boolean isFavourite,boolean isUserPlaylistTrack) {
        PopupMenu popupMenu = new PopupMenu(mActivity, view);
        popupMenu.inflate(R.menu.tidal_menu);
        if ((isFavourite&&isUserPlaylistTrack)||(isFavourite&&!isUserPlaylistTrack))
            popupMenu.getMenu().findItem(R.id.tidal_menu_remove_fav).setVisible(true);
        else
            popupMenu.getMenu().findItem(R.id.tidal_menu_remove_fav).setVisible(false);
        popupMenu.getMenu().findItem(R.id.tidal_menu_add_to_fav).setVisible(!isFavourite);

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


    public void refreshList() {
        Intent intent = new Intent(ACTION_REFRESH_LIST);
        intent.putExtra("item", new Gson().toJson(mItem));
        LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent);
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
        Call<Void> call = TidalHelper.getInstance(mActivity).addToFavorites(mItem);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(mActivity, "Added Successfully", Toast.LENGTH_SHORT).show();
                refreshList();
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
                refreshList();
                Toast.makeText(mActivity, "Removed Successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(mActivity, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
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
