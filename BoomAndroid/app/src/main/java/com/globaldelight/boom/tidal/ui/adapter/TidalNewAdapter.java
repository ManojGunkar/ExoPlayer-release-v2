package com.globaldelight.boom.tidal.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.tidal.tidalconnector.TidalRequestController;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;

import java.util.Collections;
import java.util.List;

/**
 * Created by Manoj Kumar on 28-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalNewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Item> mItems= Collections.emptyList();

    public TidalNewAdapter(Context context,List<Item> items){
        this.mContext=context;
        this.mItems=items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Item item=mItems.get(position);
        String image=item.getCover();
        image=image.replace("-","/");
        String imageUrl= TidalRequestController.IMAGE_BASE_URL+image+"/320×320.jpg";

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    protected class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView txtItemTitle;
        private ImageView imgItemCover;

        public ItemViewHolder(View itemView) {
            super(itemView);
            txtItemTitle=itemView.findViewById(R.id.txt_tidal_title);
            imgItemCover=itemView.findViewById(R.id.img_tidal_cover);
        }
    }
}
