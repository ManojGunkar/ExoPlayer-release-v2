package com.globaldelight.boom.tidal.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.adapters.song.PlayListAdapter;
import com.globaldelight.boom.tidal.tidalconnector.TidalRequestController;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.tidal.ui.adapter.PlaylistDialogAdapter;
import com.globaldelight.boom.utils.RequestChain;
import com.globaldelight.boom.utils.Utils;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Header;

/**
 * Created by Manoj Kumar on 09-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalPopupMenu implements PopupMenu.OnMenuItemClickListener, PlaylistDialogAdapter.Callback {

    private Activity mActivity;
    private String uuid = null;
    private String trackId = null;
    private String albumId = null;
    private String artist = null;

    private boolean isPlaylistAdd = false;
    private boolean isAlbumAdd = false;
    private boolean isArtistAdd = false;
    private boolean isTrackAdd = false;

    private boolean isPlaylistDel = false;
    private boolean isAlbumDel = false;
    private boolean isArtistDel = false;
    private boolean isTrackDel = false;
    private PlaylistDialogAdapter adapter;

    private TidalPopupMenu(Activity activity) {
        this.mActivity = activity;
    }

    public static TidalPopupMenu newInstance(Activity activity) {
        return new TidalPopupMenu(activity);
    }

    public void addToPlaylist(View view, String uuid) {
        this.isPlaylistAdd = true;
        this.uuid = uuid;
        getMenu(view).setOnMenuItemClickListener(this::onMenuItemClick);
    }

    public void addToTrack(View view, String trackId) {
        this.isTrackAdd = true;
        this.trackId = trackId;
        getMenu(view).setOnMenuItemClickListener(this::onMenuItemClick);
    }

    public void addToAlbum(View view, String albumId) {
        this.isAlbumAdd = true;
        this.albumId = albumId;
        getMenu(view).setOnMenuItemClickListener(this::onMenuItemClick);
    }

    public void addToArtist(View view, String artist) {
        this.isArtistAdd = true;
        this.artist = artist;
        getMenu(view).setOnMenuItemClickListener(this::onMenuItemClick);
    }

    public void deleteArtists(View view, String artist) {
        isPlaylistDel = true;
        this.artist = artist;
        PopupMenu popupMenu = getMenu(view);
        popupMenu.getMenu().findItem(R.id.tidal_menu_remove_fav).setVisible(true);
        popupMenu.getMenu().findItem(R.id.tidal_menu_add_to_fav).setVisible(false);
        popupMenu.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    public void deletePlaylist(View view, String uuid) {
        isPlaylistDel = true;
        this.uuid = uuid;
        PopupMenu popupMenu = getMenu(view);
        popupMenu.getMenu().findItem(R.id.tidal_menu_remove_fav).setVisible(true);
        popupMenu.getMenu().findItem(R.id.tidal_menu_add_to_fav).setVisible(false);
        popupMenu.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    public void deleteTrack(View view, String trackId) {
        isTrackDel = true;
        this.trackId = trackId;
        PopupMenu popupMenu = getMenu(view);
        popupMenu.getMenu().findItem(R.id.tidal_menu_remove_fav).setVisible(true);
        popupMenu.getMenu().findItem(R.id.tidal_menu_add_to_fav).setVisible(false);
        popupMenu.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    public void deleteAlbum(View view, String albumId) {
        isAlbumDel = true;
        this.albumId = albumId;
        PopupMenu popupMenu = getMenu(view);
        popupMenu.getMenu().findItem(R.id.tidal_menu_remove_fav).setVisible(true);
        popupMenu.getMenu().findItem(R.id.tidal_menu_add_to_fav).setVisible(false);
        popupMenu.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    private PopupMenu getMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(mActivity, view);
        popupMenu.inflate(R.menu.tidal_menu);
        popupMenu.show();
        return popupMenu;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tidal_menu_add_to_fav:
                if (isPlaylistAdd)
                    addToMyMusic(TidalHelper.getInstance(mActivity).addToPlaylist(uuid));
                if (isAlbumAdd)
                    addToMyMusic(TidalHelper.getInstance(mActivity).addToAlbum(albumId));
                if (isArtistAdd)
                    addToMyMusic(TidalHelper.getInstance(mActivity).addToArtist(artist));
                if (isTrackAdd)
                    addToMyMusic(TidalHelper.getInstance(mActivity).addToTrack(trackId));
                break;

            case R.id.tidal_menu_remove_fav:
                if (isPlaylistDel)
                    removeFromMyMusic(TidalHelper.getInstance(mActivity).removePlaylist(uuid));
                if (isAlbumDel)
                    removeFromMyMusic(TidalHelper.getInstance(mActivity).removeAlbum(albumId));
                if (isTrackDel)
                    removeFromMyMusic(TidalHelper.getInstance(mActivity).removeTrack(trackId));
                if (isArtistDel)
                    removeFromMyMusic(TidalHelper.getInstance(mActivity).removeArtist(artist));
                break;

            case R.id.tidal_menu_add_to_playlist:
                showPlaylistDialog();
                break;

            case R.id.tidal_menu_add_to_upnext:

                break;

            case R.id.tidal_menu_play_next:

                break;

            case R.id.tidal_menu_shuffle:

                break;
        }
        return false;
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
            if (resp == null)
                Toast.makeText(mActivity, "Remove Successfully", Toast.LENGTH_SHORT).show();
            else Toast.makeText(mActivity, "Something went wrong", Toast.LENGTH_SHORT).show();
        });
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
                            TidalRequestController.Callback callback=TidalRequestController.getTidalClient();
                            Call<Item> call=callback.createPlaylist(
                                    UserCredentials.getCredentials(mActivity).getSessionId(),
                                    UserCredentials.getCredentials(mActivity).getUserId(),
                                    input.toString(),"My Playlist");
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

        ArrayList<String> ids = new ArrayList<>();
        if (isTrackAdd) {
            ids.add(trackId);
        }

        TidalHelper.getInstance(mActivity).addItemToPlaylist(ids,item.getId());
    }
}
