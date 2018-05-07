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
import com.globaldelight.boom.tidal.tidalconnector.TidalRequestController;
import com.globaldelight.boom.tidal.tidalconnector.model.Curated;
import com.globaldelight.boom.tidal.ui.CuratedDetailActivity;
import com.globaldelight.boom.tidal.ui.GridDetailActivity;
import com.globaldelight.boom.utils.Utils;

import java.util.Collections;
import java.util.List;

/**
 * Created by Manoj Kumar on 06-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class CuratedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Curated> mCuratedList = Collections.emptyList();
    private boolean isMoods=false;
    private String image;

    public CuratedAdapter(Context context, List<Curated> genres, boolean isMoods) {
        this.mContext = context;
        this.mCuratedList = genres;
        this.isMoods=isMoods;
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
        image= curated.getImage();
        image=TidalRequestController.IMAGE_BASE_URL+image.replace("-","/")+"/320x320.jpg";
        final int size = Utils.largeImageSize(mContext);
        Glide.with(mContext).load(image)
                .placeholder(R.drawable.ic_default_art_grid)
                .centerCrop()
                .override(size, size)
                .into(viewHolder.imgGenresCover);

        viewHolder.txtGenresTitle.setText(curated.getName());

        viewHolder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, CuratedDetailActivity.class);
            intent.putExtra("imageCurated", image);
            intent.putExtra("title", curated.getName());
            if (curated.getHasAlbums()){
                if (isMoods){
                    String path="moods/"+curated.getPath()+"/albums";
                    intent.putExtra("albumPath",path);
                }else {
                    String path="genres/"+curated.getPath()+"/albums";
                    intent.putExtra("albumPath",path);
                }
            }  if (curated.getHasPlaylists()){
                if (isMoods){
                    String path="moods/"+curated.getPath()+"/playlists";
                    intent.putExtra("playlistPath",path);
                }else {
                    String path="genres/"+curated.getPath()+"/playlists";
                    intent.putExtra("playlistPath",path);
                }
            }  if (curated.getHasTracks()){
                if (isMoods){
                    String path="moods/"+curated.getPath()+"/tracks";
                    intent.putExtra("trackPath",path);
                }else {
                    String path="genres/"+curated.getPath()+"/tracks";
                    intent.putExtra("trackPath",path);
                }
            }
            mContext.startActivity(intent);
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
