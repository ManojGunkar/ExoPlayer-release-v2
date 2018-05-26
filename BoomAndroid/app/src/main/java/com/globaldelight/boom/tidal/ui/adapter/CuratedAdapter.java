package com.globaldelight.boom.tidal.ui.adapter;

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
import com.globaldelight.boom.tidal.tidalconnector.TidalRequestController;
import com.globaldelight.boom.tidal.tidalconnector.model.Curated;
import com.globaldelight.boom.tidal.ui.CuratedDetailActivity;
import com.globaldelight.boom.tidal.ui.GridDetailActivity;
import com.globaldelight.boom.utils.Utils;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;

/**
 * Created by Manoj Kumar on 06-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class CuratedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Curated> mCuratedList = Collections.emptyList();
    private boolean isMoods = false;

    public CuratedAdapter(Context context, List<Curated> genres, boolean isMoods) {
        this.mContext = context;
        this.mCuratedList = genres;
        this.isMoods = isMoods;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ItemViewHolder(inflater.inflate(R.layout.item_genres, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        Curated curated = mCuratedList.get(position);
        final int size = Utils.largeImageSize(mContext);
        Glide.with(mContext).load(curated.getImageUrl())
                .placeholder(R.drawable.ic_default_art_grid)
                .centerCrop()
                .override(size, size)
                .into(viewHolder.imgGenresCover);

        viewHolder.txtGenresTitle.setText(curated.getName());

        viewHolder.itemView.setOnClickListener(v -> {

            if (isMoods) {
                Intent intent = new Intent(mContext, GridDetailActivity.class);
                intent.putExtra(GridDetailActivity.CURATED_KEY, new Gson().toJson(curated));
                mContext.startActivity(intent);
            } else {
                Intent intent = new Intent(mContext, CuratedDetailActivity.class);
                intent.putExtra("imageCurated", curated.getImageUrl());
                intent.putExtra("title", curated.getName());
                if (curated.getHasAlbums()) {
                    String path = "genres/" + curated.getPath() + "/albums";
                    intent.putExtra("albumPath", path);
                }
                if (curated.getHasPlaylists()) {
                    String path = "genres/" + curated.getPath() + "/playlists";
                    intent.putExtra("playlistPath", path);
                }
                if (curated.getHasTracks()) {
                    String path = "genres/" + curated.getPath() + "/tracks";
                    intent.putExtra("trackPath", path);
                }
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mCuratedList.size();
    }

    protected class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView txtGenresTitle;
        private ImageView imgGenresCover;

        public ItemViewHolder(View itemView) {
            super(itemView);
            txtGenresTitle = itemView.findViewById(R.id.txt_genres);
            imgGenresCover = itemView.findViewById(R.id.img_genres);
        }
    }
}
