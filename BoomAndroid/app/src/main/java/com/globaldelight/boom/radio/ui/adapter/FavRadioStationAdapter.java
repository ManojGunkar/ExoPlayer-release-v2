package com.globaldelight.boom.radio.ui.adapter;

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
import com.globaldelight.boom.playbackEvent.utils.MediaType;
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

    private int mSelectedPosition=-1;

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
                .into(viewHolder.imgStationThumbnail);
        if (mSelectedPosition==position){
            viewHolder.txtTitle.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        }else {
            viewHolder.txtTitle.setTextColor(mContext.getResources().getColor(R.color.white));
        }
        viewHolder.imgFavRadio.setImageDrawable(mContext.getDrawable(R.drawable.fav_selected));

        viewHolder.itemView.setOnClickListener(v -> {
            mSelectedPosition=position;
            App.playbackManager().queue().addItemToPlay(mContents.get(position));
            notifyDataSetChanged();
        });
        updatePlayingStation(viewHolder,mContents.get(position));
    }

    private void updatePlayingStation(FavViewHolder holder, IMediaElement item){
        IMediaElement nowPlayingItem = App.playbackManager().queue().getPlayingItem();
        if(null != nowPlayingItem) {
            boolean isMediaItem = (nowPlayingItem.getMediaType() == MediaType.RADIO);
            if ( item.equalTo(nowPlayingItem) ) {
                holder.overlay.setVisibility(View.VISIBLE );
                holder.imgOverlayPlay.setVisibility( View.VISIBLE );
                //  holder.title.setSelected(true);
                if (App.playbackManager().isTrackPlaying()) {
                    holder.progressBar.setVisibility(View.GONE);
                    holder.imgOverlayPlay.setImageResource(R.drawable.ic_player_pause);
                    if( !isMediaItem && App.playbackManager().isTrackLoading() ) {
                        holder.progressBar.setVisibility(View.VISIBLE);
                    } else {
                        holder.progressBar.setVisibility(View.GONE);
                    }
                } else {
                    holder.progressBar.setVisibility(View.GONE);
                    holder.imgOverlayPlay.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_player_play, null));
                }
            } else {
                holder.overlay.setVisibility( View.GONE );
                holder.imgOverlayPlay.setVisibility( View.GONE );
                holder.progressBar.setVisibility(View.GONE);
                //   holder.title.setSelected(false);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mContents == null ? 0 : mContents.size();
    }

    protected class FavViewHolder extends RecyclerView.ViewHolder {

        private TextView txtTitle;
        private TextView txtSubTitle;
        private View mainView;
        private View overlay;
        private ImageView imgStationThumbnail;
        private ImageView imgOverlayPlay;
        private ImageView imgFavRadio;
        private ProgressBar progressBar;

        public FavViewHolder(View itemView) {
            super(itemView);

            mainView = itemView;
            imgStationThumbnail = itemView.findViewById(R.id.song_item_img);
            imgFavRadio=itemView.findViewById(R.id.img_fav_station);
            imgOverlayPlay = itemView.findViewById(R.id.song_item_img_overlay_play);
            overlay = itemView.findViewById(R.id.song_item_img_overlay);
            progressBar = itemView.findViewById(R.id.load_cloud );
            txtTitle =  itemView.findViewById(R.id.txt_title_station);
            txtSubTitle = itemView.findViewById(R.id.txt_sub_title_station);

        }
    }
}
