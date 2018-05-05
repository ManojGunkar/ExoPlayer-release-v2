package com.globaldelight.boom.tidal.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.R;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.utils.Utils;

import java.util.Collections;
import java.util.List;

/**
 * Created by Manoj Kumar on 28-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Item> mItems = Collections.emptyList();

    public TidalGridAdapter(Context context, List<Item> items) {
        this.mContext = context;
        this.mItems = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ItemViewHolder(inflater.inflate(R.layout.card_grid_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        Item item = mItems.get(position);
        String image = item.getItemArtUrl();
        viewHolder.imgItemCover.setVisibility(View.VISIBLE);
        final int size = Utils.largeImageSize(mContext);
        Glide.with(mContext).load(image)
                .placeholder(R.drawable.ic_default_art_grid)
                .centerCrop()
                .override(size, size)
                .into(viewHolder.imgItemCover);

        viewHolder.txtItemTitle.setText(item.getTitle());
        viewHolder.txtItemSubTitle.setText(item.getDescription());

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    protected class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView txtItemTitle;
        private TextView txtItemSubTitle;
        private ImageView imgItemCover;
        private ImageView imgItemMenu;

        public ItemViewHolder(View itemView) {
            super(itemView);
            txtItemTitle = itemView.findViewById(R.id.card_grid_title);
            txtItemSubTitle = itemView.findViewById(R.id.card_grid_sub_title);
            imgItemCover = itemView.findViewById(R.id.card_grid_default_img);
            imgItemMenu = itemView.findViewById(R.id.card_grid_menu);
        }
    }
}
