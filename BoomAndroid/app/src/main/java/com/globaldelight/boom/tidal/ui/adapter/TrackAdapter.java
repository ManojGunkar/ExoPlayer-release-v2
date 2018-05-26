package com.globaldelight.boom.tidal.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.collection.base.IMediaElement;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.tidal.utils.TidalPopupMenu;
import com.globaldelight.boom.utils.Utils;

import java.util.Collections;
import java.util.List;

/**
 * Created by Manoj Kumar on 28-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TrackAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Item> mItems = Collections.emptyList();

    public TrackAdapter(Context context, List<Item> items) {
        this.mContext = context;
        this.mItems = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemViewHolder vh = new ItemViewHolder(inflater.inflate(R.layout.item_track, parent, false));
        vh.itemView.setOnClickListener((v) -> onClick(vh));
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        Item item = mItems.get(position);
        String imageUrl = item.getItemArtUrl();
        final int size = Utils.largeImageSize(mContext);

        Glide.with(mContext).load(imageUrl)
                .placeholder(R.drawable.ic_default_art_grid)
                .centerCrop()
                .override(size, size)
                .into(viewHolder.imgTrackThumbnail);
        viewHolder.txtSubTitle.setText(item.getDescription());
        viewHolder.txtTitle.setText(item.getTitle());

        viewHolder.imgMenuTrack.setOnClickListener(v -> {
            TidalPopupMenu.newInstance((Activity) mContext).showPopup(v, item);
        });

        updatePlayingStation(viewHolder, item);

    }

    private void updatePlayingStation(ItemViewHolder holder, IMediaElement item) {
        IMediaElement nowPlayingItem = App.playbackManager().queue().getPlayingItem();
        holder.overlay.setVisibility(View.GONE);
        holder.imgOverlayPlay.setVisibility(View.GONE);
        holder.progressBar.setVisibility(View.GONE);
        holder.txtTitle.setSelected(false);

        if (null != nowPlayingItem) {
            if (item.equalTo(nowPlayingItem)) {
                holder.overlay.setVisibility(View.VISIBLE);
                holder.imgOverlayPlay.setVisibility(View.VISIBLE);
                holder.txtTitle.setSelected(true);
                holder.progressBar.setVisibility(View.GONE);
                holder.imgOverlayPlay.setImageResource(R.drawable.ic_player_play);
                if (App.playbackManager().isTrackPlaying()) {
                    holder.imgOverlayPlay.setImageResource(R.drawable.ic_player_pause);
                    if (App.playbackManager().isTrackLoading()) {
                        holder.progressBar.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private void onClick(ItemViewHolder holder) {
        final int position = holder.getAdapterPosition();
        if (position < 0) {
            return;
        }

        App.playbackManager().queue().addItemListToPlay(mItems, position, false);
    }

    protected class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView txtTitle;
        private TextView txtSubTitle;
        private TextView txtSongIndex;
        private View mainView;
        private View overlay;
        private ImageView imgTrackThumbnail;
        private ImageView imgOverlayPlay;
        private ImageView imgMenuTrack;
        private ProgressBar progressBar;

        public ItemViewHolder(View itemView) {
            super(itemView);

            mainView = itemView;
            imgTrackThumbnail = itemView.findViewById(R.id.song_item_img);
            imgMenuTrack = itemView.findViewById(R.id.img_menu_track);
            imgOverlayPlay = itemView.findViewById(R.id.song_item_img_overlay_play);
            overlay = itemView.findViewById(R.id.song_item_img_overlay);
            progressBar = itemView.findViewById(R.id.load_cloud);
            txtTitle = itemView.findViewById(R.id.txt_title_track);
            txtSubTitle = itemView.findViewById(R.id.txt_sub_title_track);
            txtSongIndex = itemView.findViewById(R.id.txt_song_index);

        }
    }
}
