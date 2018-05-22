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
import com.globaldelight.boom.collection.base.IMediaItemCollection;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.tidal.tidalconnector.model.ItemWrapper;
import com.globaldelight.boom.tidal.tidalconnector.model.response.PlaylistResponse;
import com.globaldelight.boom.tidal.utils.TidalPopupMenu;
import com.globaldelight.boom.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Manoj Kumar on 05-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class PlaylistTrackAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TYPE_HEADER = 0;
    private final static int TYPE_ITEM = 1;

    private Context mContext;
    private List<ItemWrapper> mItems = Collections.emptyList();
    private String mHeaderTitle;
    private ArrayList<Item> tracks;
    private boolean isUserMode=false;
    private boolean isUserPlaylist=false;

    public PlaylistTrackAdapter(Context context, List<ItemWrapper> items, String headerTitle,boolean isUserMode, boolean isUserPlaylist) {
        this.mContext = context;
        this.mItems = items;
        this.mHeaderTitle = headerTitle;
        this.isUserMode=isUserMode;
        tracks = new ArrayList();
        for (int i = 0; i < mItems.size(); i++) {
            ItemWrapper item = mItems.get(i);
            tracks.add(item.getItem());
        }
        this.isUserPlaylist=isUserPlaylist;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.card_header_recycler_view, parent, false);
            HeaderViewHolder holder = new HeaderViewHolder(itemView);
            return holder;
        } else {
            ItemViewHolder vh = new ItemViewHolder(inflater.inflate(R.layout.item_edit_track, parent, false));
            vh.itemView.setOnClickListener((v) -> onClick(vh));
            return vh;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position < 1) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.txtHeaderTitle.setText(mHeaderTitle);
            headerViewHolder.txtHeaderDetail.setText("Song : " + mItems.size());
            headerViewHolder.imgMore.setOnClickListener(view->{

            });
        }
        else {
            ItemViewHolder viewHolder = (ItemViewHolder) holder;
            Item item = mItems.get(position-1).getItem();
            String imageUrl = item.getItemArtUrl();
            final int size = Utils.largeImageSize(mContext);

            Glide.with(mContext).load(imageUrl)
                    .placeholder(R.drawable.ic_default_art_grid)
                    .centerCrop()
                    .override(size, size)
                    .into(viewHolder.imgTrackThumbnail);

            viewHolder.txtTitle.setText(item.getTitle());
            viewHolder.txtSubTitle.setText(item.getDescription());


            if (isUserPlaylist){
                viewHolder.imgReArragneTrack.setVisibility(View.VISIBLE);
            }else {
                viewHolder.imgReArragneTrack.setVisibility(View.GONE);
            }

            viewHolder.imgMenuTrack.setOnClickListener(v -> {
                TidalPopupMenu.newInstance((Activity) mContext).showPopup(v, item, isUserMode,isUserPlaylist);
            });

            updatePlayingStation(viewHolder, tracks.get(position-1));
        }


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
        return mItems.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 1) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    private void onClick(ItemViewHolder holder) {
        int position = holder.getAdapterPosition() - 1;
        if (position <0) {
            return;
        }

         App.playbackManager().queue().addItemListToPlay(tracks, position, false);
    }

    public List<Item> getItems() {
        return tracks;
    }

    protected class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView txtTitle;
        private TextView txtSubTitle;
        private View mainView;
        private View overlay;
        private ImageView imgTrackThumbnail;
        private ImageView imgOverlayPlay;
        private ImageView imgMenuTrack;
        private ImageView imgReArragneTrack;
        private ProgressBar progressBar;

        public ItemViewHolder(View itemView) {
            super(itemView);

            mainView = itemView;
            imgTrackThumbnail = itemView.findViewById(R.id.song_item_img);
            imgReArragneTrack = itemView.findViewById(R.id.img_rearrange_track);
            imgMenuTrack = itemView.findViewById(R.id.img_menu_track);
            imgOverlayPlay = itemView.findViewById(R.id.song_item_img_overlay_play);
            overlay = itemView.findViewById(R.id.song_item_img_overlay);
            progressBar = itemView.findViewById(R.id.load_cloud);
            txtTitle = itemView.findViewById(R.id.txt_title_track);
            txtSubTitle = itemView.findViewById(R.id.txt_sub_title_track);

        }
    }

    protected class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView txtHeaderTitle;
        private TextView txtHeaderDetail;
        private ImageView imgMore;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            txtHeaderTitle = itemView.findViewById(R.id.header_sub_title);
            txtHeaderDetail = itemView.findViewById(R.id.header_detail);
            imgMore = itemView.findViewById(R.id.recycler_header_menu);
        }
    }
}
