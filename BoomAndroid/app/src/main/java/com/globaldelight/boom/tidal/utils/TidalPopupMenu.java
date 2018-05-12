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
public class TidalPopupMenu implements PopupMenu.OnMenuItemClickListener{

    private Context mContext;

    private static TidalPopupMenu instance;
    private String uuid=null;
    private String trackId=null;
    private String albumId=null;
    private String artist=null;

    private boolean isPlaylistAdd=false;
    private boolean isAlbumAdd=false;
    private boolean isArtistAdd=false;
    private boolean isTrackAdd=false;
    private boolean isPlaylistDel=false;
    private boolean isAlbumDel=false;
    private boolean isArtistDel=false;
    private boolean isTrackDel=false;

    private TidalPopupMenu(Context context){
        this.mContext=context;
    }

    public static TidalPopupMenu getInstance(Context context){
        if (instance==null)instance=new TidalPopupMenu(context);
        return instance;
    }

    public void addToPlaylist(View view, String uuid){
        this.uuid=uuid;
        getMenu(view).setOnMenuItemClickListener(this::onMenuItemClick);
    }

    public void deletePlaylist(View view, String uuid){
        isPlaylistDel=true;
        this.uuid=uuid;
        PopupMenu popupMenu=getMenu(view);
        popupMenu.getMenu().findItem(R.id.tidal_menu_remove_fav).setVisible(true);
        popupMenu.getMenu().findItem(R.id.tidal_menu_add_to_fav).setVisible(false);
        popupMenu.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    public void deleteAlbum(View view,String albumId){
        isAlbumDel=true;
        this.albumId=albumId;
        PopupMenu popupMenu=getMenu(view);
        popupMenu.getMenu().findItem(R.id.tidal_menu_remove_fav).setVisible(true);
        popupMenu.getMenu().findItem(R.id.tidal_menu_add_to_fav).setVisible(false);
        popupMenu.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    private PopupMenu getMenu(View view){
        PopupMenu popupMenu=new PopupMenu(mContext, view);
        popupMenu.inflate(R.menu.tidal_menu);
        popupMenu.show();
        return popupMenu;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.tidal_menu_add_to_fav:
                addToPlaylist();
                break;

            case R.id.tidal_menu_remove_fav:
                removePlaylist();
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

    private void addToPlaylist(){
        RequestChain requestChain=new RequestChain(mContext);
        Call<JsonElement> call=TidalHelper.getInstance(mContext).addToPlaylist(uuid);
        requestChain.submit(call, resp -> {
            if (resp==null)
            Toast.makeText(mContext,"Added Successfully",Toast.LENGTH_SHORT).show();
            else Toast.makeText(mContext,"Something went wrong",Toast.LENGTH_SHORT).show();
        });
    }

    private void removePlaylist(){
        RequestChain requestChain=new RequestChain(mContext);
        Call<JsonElement> call=TidalHelper.getInstance(mContext).removePlaylist(uuid);
        requestChain.submit(call,resp -> {
            if (resp==null)
                Toast.makeText(mContext,"Added Successfully",Toast.LENGTH_SHORT).show();
            else Toast.makeText(mContext,"Something went wrong",Toast.LENGTH_SHORT).show();
        });
    }
}
