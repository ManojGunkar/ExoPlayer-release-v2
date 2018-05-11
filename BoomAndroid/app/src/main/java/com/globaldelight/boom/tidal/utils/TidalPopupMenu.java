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

    private TidalPopupMenu(Context context){
        this.mContext=context;
    }

    public static TidalPopupMenu getInstance(Context context){
        if (instance==null)instance=new TidalPopupMenu(context);
        return instance;
    }

    public void showMenu(View view,String uuid){
        this.uuid=uuid;
        PopupMenu popupMenu=new PopupMenu(mContext, view);
        popupMenu.inflate(R.menu.tidal_menu);
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.tidal_menu_add_to_fav:
                addToFav();
                break;

            case R.id.tidal_menu_remove_fav:

                break;
        }
        return false;
    }

    private void addToFav(){
        RequestChain requestChain=new RequestChain(mContext);
        Call<JsonElement> call=TidalHelper.getInstance(mContext).addToPlaylist(uuid);
        requestChain.submit(call, resp -> {
            if (resp==null)
            Toast.makeText(mContext,"Added Successfully",Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(mContext,"Something went wrong",Toast.LENGTH_SHORT).show();
        });
    }
}
