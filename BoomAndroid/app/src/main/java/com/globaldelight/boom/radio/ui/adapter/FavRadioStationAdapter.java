package com.globaldelight.boom.radio.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.radio.webconnector.responsepojo.RadioStationResponse;
import com.globaldelight.boom.utils.Utils;

import java.util.List;

/**
 * Created by Manoj Kumar on 13-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class FavRadioStationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<RadioStationResponse.Content> mContents;


    public FavRadioStationAdapter(Context context, List<RadioStationResponse.Content> contents) {
        this.mContents = contents;
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return   new FavViewHolder(inflater.inflate(R.layout.item_local_radio, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FavViewHolder viewHolder = (FavViewHolder) holder;
        viewHolder.txtTitle.setText(mContents.get(position).getName());
        viewHolder.txtSubTitle.setText(mContents.get(position).getDescription());
        final int size = Utils.largeImageSize(mContext);
        Glide.with(mContext).load(mContents.get(position).getLogo())
                .placeholder(R.drawable.ic_default_art_grid)
                .centerCrop()
                .override(size, size)
                .into(viewHolder.imgLocalRadioLogo);
        viewHolder.imgFavRadio.setImageDrawable(mContext.getDrawable(R.drawable.fav_selected));
    }

    @Override
    public int getItemCount() {
        return mContents == null ? 0 : mContents.size();
    }

    protected class FavViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imgLocalRadioLogo;
        private ImageView imgFavRadio;
        private TextView txtTitle;
        private TextView txtSubTitle;

        public FavViewHolder(View itemView) {
            super(itemView);

            imgLocalRadioLogo = itemView.findViewById(R.id.img_title_logo_local_radio);
            imgFavRadio = itemView.findViewById(R.id.img_fav_radio_station);
            txtTitle = itemView.findViewById(R.id.txt_title_local_radio);
            txtSubTitle = itemView.findViewById(R.id.txt_sub_title_local_radio);
            this.itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position < 0) {
                return;
            }

            App.playbackManager().queue().addItemToPlay(mContents.get(position));
        }
    }
}
