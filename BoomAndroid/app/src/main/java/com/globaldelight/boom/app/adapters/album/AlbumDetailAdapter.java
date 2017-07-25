package com.globaldelight.boom.app.adapters.album;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.collection.local.callback.IMediaItem;
import com.globaldelight.boom.app.receivers.PlayerServiceReceiver;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.local.callback.IMediaItemCollection;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.app.adapters.model.ListDetail;
import com.globaldelight.boom.view.RegularTextView;
import com.globaldelight.boom.utils.Utils;

import java.util.ArrayList;

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
                PopupMenu pm = new PopupMenu(activity, anchorView);
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.album_header_add_play_next:
                                App.playbackManager().queue().addItemAsPlayNext(collection);
                                break;
                            case R.id.album_header_add_to_upnext:
                                App.playbackManager().queue().addItemAsUpNext(collection);
                                break;
                            case R.id.album_header_add_to_playlist:
                                Utils.addToPlaylist(activity, collection, null);
                                break;
                            case R.id.album_header_shuffle:
                                activity.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_SHUFFLE_SONG));
                                App.playbackManager().queue().addItemListToPlay(collection, 0);
                                break;
                        }
                        return false;
                    }
                });
                pm.inflate(R.menu.album_header_menu);
                pm.show();
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
                PopupMenu pm = new PopupMenu(activity, anchorView);
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        IMediaItemBase selectedItem = collection.getItemAt(position);
                        switch (menuItem.getItemId()) {
                            case R.id.popup_song_play_next:
                                App.playbackManager().queue().addItemAsPlayNext(selectedItem);
                                break;
                            case R.id.popup_song_add_queue:
                                App.playbackManager().queue().addItemAsUpNext(selectedItem);
                                break;
                            case R.id.popup_song_add_playlist:
                                ArrayList list = new ArrayList();
                                list.add(selectedItem);
                                Utils.addToPlaylist(activity, list, null);
                                break;
                            case R.id.popup_song_add_fav:
                                if (MediaController.getInstance(activity).isFavoriteItem(selectedItem.getItemId())) {
                                    MediaController.getInstance(activity).removeItemToFavoriteList(selectedItem.getItemId());
                                    Toast.makeText(activity, activity.getResources().getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show();
                                } else {
                                    MediaController.getInstance(activity).addItemToFavoriteList((IMediaItem)selectedItem);
                                    Toast.makeText(activity, activity.getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }

                        return false;
                    }
                });
                if (MediaController.getInstance(activity).isFavoriteItem(collection.getItemAt(position).getItemId())) {
                    pm.inflate(R.menu.song_remove_fav);
                } else {
                    pm.inflate(R.menu.song_add_fav);
                }
                pm.show();
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
