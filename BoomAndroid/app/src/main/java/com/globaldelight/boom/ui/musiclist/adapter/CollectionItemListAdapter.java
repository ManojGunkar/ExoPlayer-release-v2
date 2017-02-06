package com.globaldelight.boom.ui.musiclist.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.globaldelight.boom.App;
import com.globaldelight.boom.manager.PlayerServiceReceiver;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.ui.musiclist.ListDetail;
import com.globaldelight.boom.ui.widgets.CoachMarkTextView;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.Utils;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 15-12-16.
 */

public class CollectionItemListAdapter  extends RecyclerView.Adapter<CollectionItemListAdapter.SimpleItemViewHolder> {

    public static final int TYPE_HEADER = 111;
    public static final int TYPE_ITEM = 222;
    private Activity activity;
    private MediaItemCollection item;
    private ListDetail listDetail;

    public CollectionItemListAdapter(Activity activity, IMediaItemCollection item, ListDetail listDetail) {
        this.activity = activity;
        this.item = (MediaItemCollection) item;
        this.listDetail = listDetail;
    }

    @Override
    public CollectionItemListAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if(viewType == TYPE_ITEM) {
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.card_album_list, parent, false);
        }else{
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.card_header_recycler_view, parent, false);
        }
        return new CollectionItemListAdapter.SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CollectionItemListAdapter.SimpleItemViewHolder holder, final int position) {

        if(position < 1){
            if(listDetail.getmTitle() != null) {
                holder.headerTitle.setText(listDetail.getmTitle());
            }else{
                holder.headerTitle.setVisibility(View.GONE);
            }
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

            holder.mMore.setVisibility(View.VISIBLE);
            setOnMenuClickListener(holder, position);
        }else if(position >= 1) {
            String title;
            String duration;
            int pos = position -1;
            MediaItem nowPlayingItem = (MediaItem) App.getPlayingQueueHandler().getUpNextList().getPlayingItem();
            if (item.getItemType() == ItemType.ALBUM || item.getItemType() == ItemType.ARTIST || item.getItemType() == ItemType.GENRE
                    || item.getItemType() == ItemType.PLAYLIST || item.getItemType() == ItemType.BOOM_PLAYLIST) {
                title = item.getMediaElement().get(pos).getItemTitle();
                duration = ((MediaItem) item.getMediaElement().get(pos)).getDuration();
                if (null != nowPlayingItem && item.getMediaElement().get(pos).getItemId() == nowPlayingItem.getItemId()) {
                    holder.name.setTextColor(ContextCompat.getColor(activity, R.color.track_selected_title));
                    holder.count.setTextColor(ContextCompat.getColor(activity, R.color.track_selected_title));
                } else if (null != nowPlayingItem) {
                    holder.name.setTextColor(ContextCompat.getColor(activity, R.color.white));
                    holder.count.setTextColor(ContextCompat.getColor(activity, R.color.white));
                }

                holder.name.setText(title);
                holder.count.setText(String.valueOf(pos + 1));
                holder.duration.setText(duration);

                setOnClickListeners(holder, pos);
            }
        }
    }

    private void setOnMenuClickListener(CollectionItemListAdapter.SimpleItemViewHolder holder, int position) {
        holder.mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchorView) {
                PopupMenu pm = new PopupMenu(activity, anchorView);
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        try {
                            switch (menuItem.getItemId()) {
                                case R.id.album_header_add_play_next:
                                    if (item.getMediaElement().size() > 0) {
                                        App.getPlayingQueueHandler().getUpNextList().addItemListToUpNextFrom(item);
                                    }
                                    break;
                                case R.id.album_header_add_to_upnext:
                                    if (App.getPlayingQueueHandler().getUpNextList() != null) {
                                        if (item.getMediaElement().size() > 0) {
                                            App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext(item);
                                        }
                                    }
                                    break;
                                case R.id.album_header_add_to_playlist:
                                    Utils util = new Utils(activity);
                                    if (item.getMediaElement().size() > 0) {
                                        util.addToPlaylist(activity, item.getMediaElement(), null);
                                    }

                                    break;
                                case R.id.album_header_shuffle:
                                    if (App.getPlayingQueueHandler().getUpNextList() != null) {
//                                    if(!App.getPlayerEventHandler().isPlaying() && !App.getPlayerEventHandler().isPaused()){
                                        if (item.getMediaElement().size() > 0) {
                                            App.getPlayingQueueHandler().getUpNextList().addToPlay(item, 0, true);
                                        }
//                                    }
                                        activity.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_SHUFFLE_SONG));
                                    }
                                    break;
                            }
                        }catch (Exception e){}
                        return false;
                    }
                });
                pm.inflate(R.menu.album_header_menu);
                pm.show();
            }
        });
    }

    private void setOnClickListeners(final CollectionItemListAdapter.SimpleItemViewHolder holder, final int position) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (App.getPlayingQueueHandler().getUpNextList() != null) {
                    if (item.getMediaElement().size() > 0) {
                        App.getPlayingQueueHandler().getUpNextList().addToPlay(item, position, false);
                    }
                    holder.name.setTextColor(ContextCompat.getColor(activity, R.color.track_selected_title));
                    holder.count.setTextColor(ContextCompat.getColor(activity, R.color.track_selected_title));
                    notifyDataSetChanged();
                }
            }
        });

        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchorView) {
                PopupMenu pm = new PopupMenu(activity, anchorView);
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        try {
                            switch (menuItem.getItemId()) {
                                case R.id.popup_song_play_next:
                                    if (App.getPlayingQueueHandler().getUpNextList() != null) {
                                        if (item.getMediaElement().size() > 0) {
                                            App.getPlayingQueueHandler().getUpNextList().addItemToUpNextFrom(item.getMediaElement().get(position));
                                        }
                                    }
                                    break;
                                case R.id.popup_song_add_queue:
                                    if (App.getPlayingQueueHandler().getUpNextList() != null) {
                                        if (item.getMediaElement().size() > 0) {
                                            App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext((MediaItem) item.getMediaElement().get(position));
                                        }
                                    }
                                    break;
                                case R.id.popup_song_add_playlist:
                                    Utils util = new Utils(activity);
                                    ArrayList list = new ArrayList();
                                    if (item.getMediaElement().size() > 0) {
                                        list.add(item.getMediaElement().get(position));
                                        util.addToPlaylist(activity, list, null);
                                    }

                                    break;
                                case R.id.popup_song_add_fav:
                                    if (item.getMediaElement().size() > 0) {
                                        if (MediaController.getInstance(activity).isFavouriteItems(item.getMediaElement().get(position).getItemId())) {
                                            MediaController.getInstance(activity).removeItemToFavoriteList(item.getMediaElement().get(position).getItemId());
                                            Toast.makeText(activity, activity.getResources().getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show();
                                        } else {
                                            MediaController.getInstance(activity).addSongsToFavoriteList(item.getMediaElement().get(position));
                                            Toast.makeText(activity, activity.getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    break;
                            }
                        }catch (Exception e){

                        }
                        return false;
                    }
                });
                if (MediaController.getInstance(activity).isFavouriteItems(item.getMediaElement().get(position).getItemId())) {
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
//        if(item.getItemType() == ItemType.ALBUM){
            return item.getItemCount()+1;
//        }else{
//            return ((MediaItemCollection)item.getMediaElement().get(item.getCurrentIndex())).getMediaElement().size()+1;
//        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position < 1){
            return TYPE_HEADER;
        }else{
            return TYPE_ITEM;
        }
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public RegularTextView name, count, duration;
        public View mainView;
        public ImageView menu;

        public RegularTextView headerTitle, headerSubTitle, headerDetail;
        ImageView mShuffle, mMore;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            name = (RegularTextView) itemView.findViewById(R.id.album_item_name);
            duration = (RegularTextView) itemView.findViewById(R.id.album_item_duration);
            count = (RegularTextView) itemView.findViewById(R.id.album_item_count);
            menu = (ImageView) itemView.findViewById(R.id.album_item_menu);

            headerTitle = (RegularTextView) itemView.findViewById(R.id.header_title);
            headerSubTitle = (RegularTextView) itemView.findViewById(R.id.header_sub_title);
            headerDetail = (RegularTextView) itemView.findViewById(R.id.header_detail);
            mMore = (ImageView) itemView.findViewById(R.id.recycler_header_menu);
        }
    }

}

