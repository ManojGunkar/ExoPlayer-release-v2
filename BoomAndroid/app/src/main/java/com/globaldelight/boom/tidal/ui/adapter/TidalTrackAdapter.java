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
import com.globaldelight.boom.tidal.tidalconnector.TidalRequestController;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.utils.Utils;

import java.util.Collections;
import java.util.List;

/**
 * Created by Manoj Kumar on 28-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalTrackAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Item> mItems = Collections.emptyList();

    public TidalTrackAdapter(Context context, List<Item> items) {
        this.mContext = context;
        this.mItems = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ItemViewHolder(inflater.inflate(R.layout.item_track, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        Item item = mItems.get(position);
        String image = null;
        if (item.getAlbum() != null) {
            image = item.getAlbum().getCover();
            image = image.replace("-", "/");
            image = TidalRequestController.IMAGE_BASE_URL + image + "/320x320.jpg";
        }
        if (item.getImage() != null) {
            image = item.getImage();
            image = image.replace("-", "/");
            image = TidalRequestController.IMAGE_BASE_URL + image + "/320x214.jpg";
        }

        final int size = Utils.largeImageSize(mContext);
        Glide.with(mContext).load(image)
                .placeholder(R.drawable.ic_default_art_grid)
                .centerCrop()
                .override(size, size)
                .into(viewHolder.imgItemCover);

        viewHolder.txtItemTitle.setText(item.getTitle());

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
            txtItemTitle = itemView.findViewById(R.id.txt_title_track);
            imgItemCover = itemView.findViewById(R.id.img_thumb_tracks);
        }
    }
}
