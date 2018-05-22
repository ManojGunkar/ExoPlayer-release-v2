package com.globaldelight.boom.tidal.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.R;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.tidal.ui.GridDetailActivity;
import com.globaldelight.boom.tidal.utils.TidalPopupMenu;
import com.globaldelight.boom.utils.Utils;

import java.util.Collections;
import java.util.List;

/**
 * Created by Manoj Kumar on 28-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class GridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Item> mItems = Collections.emptyList();

    private boolean isUserMode = false;
    private boolean isArtists = false;

    public GridAdapter(Context context, List<Item> items, boolean isUserMode, boolean isArtists) {
        this.mContext = context;
        this.mItems = items;
        this.isUserMode = isUserMode;
        this.isArtists = isArtists;
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

        final int size = Utils.largeImageSize(mContext);
        Glide.with(mContext).load(image)
                .placeholder(R.drawable.ic_default_art_grid)
                .centerCrop()
                .override(size, size)
                .into(viewHolder.imgItemCover);

        viewHolder.imgItemCover.setVisibility(View.VISIBLE);
        viewHolder.txtItemTitle.setText(item.getTitle() == null ? item.getName() : item.getTitle());
        viewHolder.txtItemSubTitle.setText(item.getDescription());
        
        viewHolder.imgItemMenu.setOnClickListener(view -> {
            TidalPopupMenu.newInstance((Activity) mContext).showPopup(view, item, isUserMode,false);
        });

        viewHolder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(mContext, GridDetailActivity.class);
            intent.putExtra("imageurl", image);
            intent.putExtra("title", item.getTitle());
            intent.putExtra("isUserMode", isUserMode);
            if (item.getUuid() != null) {
                intent.putExtra("id", item.getUuid());
                intent.putExtra("isPlaylist", true);
            } else
                intent.putExtra("id", item.getId());
            if (isArtists)
                intent.putExtra("isArtists", true);

            if (item.getType()!=null&&item.getType().equals("USER"))
                intent.putExtra("isUserPlaylist", true);

            mContext.startActivity(intent);
        });

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
