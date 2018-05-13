package com.globaldelight.boom.tidal.utils;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.globaldelight.boom.R;
import com.globaldelight.boom.utils.RequestChain;
import com.google.gson.JsonElement;

import retrofit2.Call;

/**
 * Created by Manoj Kumar on 09-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalPopupMenu implements PopupMenu.OnMenuItemClickListener {

    private static TidalPopupMenu instance;
    private Context mContext;
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

    private TidalPopupMenu(Context context) {
        this.mContext = context;
    }

    public static TidalPopupMenu getInstance(Context context) {
        if (instance == null) instance = new TidalPopupMenu(context);
        return instance;
    }

    public void addToPlaylist(View view, String uuid) {
        this.isPlaylistAdd=true;
        this.uuid = uuid;
        getMenu(view).setOnMenuItemClickListener(this::onMenuItemClick);
    }

    public void addToTrack(View view, String trackId) {
        this.isTrackAdd=true;
        this.trackId = trackId;
        getMenu(view).setOnMenuItemClickListener(this::onMenuItemClick);
    }

    public void addToAlbum(View view, String albumId) {
        this.isAlbumAdd=true;
        this.albumId = albumId;
        getMenu(view).setOnMenuItemClickListener(this::onMenuItemClick);
    }

    public void addToArtist(View view, String artist) {
        this.isArtistAdd=true;
        this.artist =artist;
        getMenu(view).setOnMenuItemClickListener(this::onMenuItemClick);
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
        PopupMenu popupMenu = new PopupMenu(mContext, view);
        popupMenu.inflate(R.menu.tidal_menu);
        popupMenu.show();
        return popupMenu;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tidal_menu_add_to_fav:
                if (isPlaylistAdd)
                    addToMyMusic(TidalHelper.getInstance(mContext).addToPlaylist(uuid));
                if (isAlbumAdd)
                    addToMyMusic(TidalHelper.getInstance(mContext).addToAlbum(albumId));
                if (isArtistAdd)
                    addToMyMusic(TidalHelper.getInstance(mContext).addToArtist(artist));
                if (isTrackAdd)
                    addToMyMusic(TidalHelper.getInstance(mContext).addToTrack(trackId));
                break;

            case R.id.tidal_menu_remove_fav:
                if (isPlaylistDel)
                    removeFromMyMusic(TidalHelper.getInstance(mContext).removePlaylist(uuid));
                if (isAlbumDel)
                    removeFromMyMusic(TidalHelper.getInstance(mContext).removeAlbum(albumId));
                if (isTrackDel)
                    removeFromMyMusic(TidalHelper.getInstance(mContext).removeTrack(trackId));
                if (isArtistDel)
                    removeFromMyMusic(TidalHelper.getInstance(mContext).removeArtist(artist));
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
        RequestChain requestChain = new RequestChain(mContext);
        requestChain.submit(call, resp -> {
            if (resp == null)
                Toast.makeText(mContext, "Added Successfully", Toast.LENGTH_SHORT).show();
            else Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
        });
    }

    private void removeFromMyMusic(Call<JsonElement> call) {
        RequestChain requestChain = new RequestChain(mContext);
        requestChain.submit(call, resp -> {
            if (resp == null)
                Toast.makeText(mContext, "Remove Successfully", Toast.LENGTH_SHORT).show();
            else Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
        });
    }


}
