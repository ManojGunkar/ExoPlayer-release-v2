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
import com.globaldelight.boom.tidal.tidalconnector.model.Curated;
import com.globaldelight.boom.utils.Utils;

import java.util.Collections;
import java.util.List;

/**
 * Created by Manoj Kumar on 06-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class CuratedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Curated> mGenres = Collections.emptyList();
    private boolean isMoods=false;

    public CuratedAdapter(Context context, List<Curated> genres, boolean isMoods) {
        this.mContext = context;
        this.mGenres = genres;
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
        Curated genres = mGenres.get(position);

        String image=genres.getImage();
        image= TidalRequestController.IMAGE_BASE_URL+image.replace("-","/")+"/320x320.jpg";

        final int size = Utils.largeImageSize(mContext);
        Glide.with(mContext).load(image)
                .placeholder(R.drawable.ic_default_art_grid)
                .centerCrop()
                .override(size, size)
                .into(viewHolder.imgGenresCover);

        if (isMoods){
            viewHolder.imgGenresCover.setMaxHeight(150);
            viewHolder.imgGenresCover.setMaxHeight(110);
        }

        viewHolder.txtGenresTitle.setText(genres.getName());

        viewHolder.itemView.setOnClickListener(v -> {

           /* Intent intent = new Intent(mContext, GridDetailActivity.class);
            intent.putExtra("imageurl", image);
            intent.putExtra("title", item.getTitle());
            if (item.getUuid() != null) {
                intent.putExtra("id", item.getUuid());
                intent.putExtra("isPlaylist", true);
            } else
                intent.putExtra("id", item.getId());

            mContext.startActivity(intent);*/
        });

    }

    @Override
    public int getItemCount() {
        return mGenres.size();
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
