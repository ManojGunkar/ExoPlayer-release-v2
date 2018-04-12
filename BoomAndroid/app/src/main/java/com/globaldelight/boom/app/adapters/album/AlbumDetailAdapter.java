package com.globaldelight.boom.app.adapters.album;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.base.IMediaItemCollection;
import com.globaldelight.boom.app.adapters.model.ListDetail;
import com.globaldelight.boom.utils.OverFlowMenuUtils;

/**
 * Created by Rahul Agarwal on 8/9/2016.
 */
public class AlbumDetailAdapter extends RecyclerView.Adapter<AlbumDetailAdapter.SimpleItemViewHolder> {

    public static final int TYPE_HEADER = 111;
    public static final int TYPE_ITEM = 222;
    private Activity activity;
    private MediaItemCollection collection;
    private ListDetail listDetail;

    public AlbumDetailAdapter(Activity activity, IMediaItemCollection item, ListDetail listDetail) {
        this.activity = activity;
        this.collection = (MediaItemCollection) item;
        this.listDetail = listDetail;
    }

    @Override
    public AlbumDetailAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.card_album_list, parent, false);
            SimpleItemViewHolder holder = new SimpleItemViewHolder(itemView);
            setOnClickListeners(holder);
            return holder;

        }else{
            View itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.card_header_recycler_view, parent, false);
            SimpleItemViewHolder holder = new SimpleItemViewHolder(itemView);
            setOnMenuClickListener(holder);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(final AlbumDetailAdapter.SimpleItemViewHolder holder, final int position) {
        if(position < 1){
            if(listDetail.getmSubTitle() != null) {
                holder.headerSubTitle.setText(listDetail.getmSubTitle());
            }else{
                holder.headerSubTitle.setVisibility(View.GONE);
            }
            if(listDetail.getmDetail() != null) {
                holder.headerDetail.setText(listDetail.getmDetail());
            }else{
                holder.headerDetail.setVisibility(View.GONE);
            }
        }else if(position >= 1) {
            int pos = position -1;
            MediaItem nowPlayingItem = (MediaItem) App.playbackManager().queue().getPlayingItem();
            MediaItem curItem = (MediaItem)collection.getItemAt(pos);
            updatePlayingItem(null != nowPlayingItem && curItem.equalTo(nowPlayingItem), holder);
            holder.name.setText(curItem.getTitle());
            holder.count.setText(String.valueOf(pos + 1));
            holder.duration.setText(curItem.getDuration());

        }
    }

    private void updatePlayingItem(boolean isPlayingItem, SimpleItemViewHolder holder) {
        holder.count.setSelected(isPlayingItem);
        holder.name.setSelected(isPlayingItem);
        if(isPlayingItem){
            holder.art_overlay.setVisibility(View.VISIBLE);
            holder.art_overlay_play.setVisibility(View.VISIBLE);
            holder.art_overlay_play.setImageResource(App.playbackManager().isTrackPlaying() ? R.drawable.ic_player_pause : R.drawable.ic_player_play);
        }else{
            holder.art_overlay.setVisibility(View.GONE);
            holder.art_overlay_play.setVisibility(View.GONE);
        }
    }

    private void setOnMenuClickListener(SimpleItemViewHolder holder) {
        holder.mMore.setOnClickListener(this::onHeaderMenuClicked);
    }

    private void setOnClickListeners(final SimpleItemViewHolder holder) {
        holder.itemView.setOnClickListener((view)->onItemClicked(view, holder));
        holder.menu.setOnClickListener((view)->onItemMenuClicked(view,holder));
    }

    @Override
    public int getItemCount() {
        return collection.count()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position < 1){
            return TYPE_HEADER;
        }else{
            return TYPE_ITEM;
        }
    }


    private void onHeaderMenuClicked(View view) {
        OverFlowMenuUtils.showCollectionMenu(activity, view, R.menu.collection_header_popup, collection);
    }


    private void onItemClicked(View view, SimpleItemViewHolder holder) {
        final int position = holder.getAdapterPosition() - 1;
        if ( position == -1 ) {
            return;
        }
        if (App.playbackManager().queue() != null ) {
            if ( collection.count() > 0) {
                App.playbackManager().queue().addItemListToPlay(collection, position);
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            }, 500);
        }
        FlurryAnalytics.getInstance(activity.getApplicationContext()).setEvent(FlurryEvents.Song_Played_On_Tapping_Alumb_Thumbnail);
    }

    private void onItemMenuClicked(View view, SimpleItemViewHolder holder) {
        final int position = holder.getAdapterPosition() - 1;
        if ( position == -1 ) {
            return;
        }
        OverFlowMenuUtils.showMediaItemMenu(activity, view, R.menu.media_item_popup, collection.getItemAt(position));
    }

    public static class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        public TextView name, count, duration;
        public View art_overlay;
        public ImageView art_overlay_play;
        public LinearLayout menu;

        public TextView headerSubTitle, headerDetail;
        ImageView mMore;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            art_overlay_play = itemView.findViewById(R.id.song_item_img_overlay_play);
            art_overlay = itemView.findViewById(R.id.song_item_img_overlay);
            name = itemView.findViewById(R.id.album_item_name);
            duration = itemView.findViewById(R.id.album_item_duration);
            count = itemView.findViewById(R.id.album_item_count);
            menu = itemView.findViewById(R.id.album_item_overflow_menu);

            headerSubTitle = itemView.findViewById(R.id.header_sub_title);
            headerDetail = itemView.findViewById(R.id.header_detail);
            mMore = itemView.findViewById(R.id.recycler_header_menu);
        }
    }
}
