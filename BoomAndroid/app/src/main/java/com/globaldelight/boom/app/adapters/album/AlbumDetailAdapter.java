package com.globaldelight.boom.app.adapters.album;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.local.callback.IMediaItemCollection;
import com.globaldelight.boom.app.adapters.model.ListDetail;
import com.globaldelight.boom.utils.OverFlowMenuUtils;
import com.globaldelight.boom.view.RegularTextView;

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
            updatePlayingItem(null != nowPlayingItem && curItem.getItemId() == nowPlayingItem.getItemId(), holder);
            holder.name.setText(curItem.getItemTitle());
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
        holder.mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchorView) {
                OverFlowMenuUtils.showCollectionMenu(activity, anchorView, R.menu.collection_header_popup, collection);
            }
        });
    }

    private void setOnClickListeners(final SimpleItemViewHolder holder) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int position = holder.getAdapterPosition() - 1;
                if ( position == -1 ) {
                    return;
                }
                if (App.playbackManager().queue() != null && !App.playbackManager().isTrackLoading()) {
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
        });

        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchorView) {
                final int position = holder.getAdapterPosition() - 1;
                if ( position == -1 ) {
                    return;
                }
                OverFlowMenuUtils.showMediaItemMenu(activity, anchorView, R.menu.media_item_popup, collection.getItemAt(position));
            }
        });
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

    public static class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        public RegularTextView name, count, duration;
        public View art_overlay;
        public ImageView art_overlay_play;
        public LinearLayout menu;

        public RegularTextView headerSubTitle, headerDetail;
        ImageView mMore;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            art_overlay_play = (ImageView) itemView.findViewById(R.id.song_item_img_overlay_play);
            art_overlay = itemView.findViewById(R.id.song_item_img_overlay);
            name = (RegularTextView) itemView.findViewById(R.id.album_item_name);
            duration = (RegularTextView) itemView.findViewById(R.id.album_item_duration);
            count = (RegularTextView) itemView.findViewById(R.id.album_item_count);
            menu = (LinearLayout) itemView.findViewById(R.id.album_item_overflow_menu);

            headerSubTitle = (RegularTextView) itemView.findViewById(R.id.header_sub_title);
            headerDetail = (RegularTextView) itemView.findViewById(R.id.header_detail);
            mMore = (ImageView) itemView.findViewById(R.id.recycler_header_menu);
        }
    }
}
