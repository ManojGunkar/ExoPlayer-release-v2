package com.globaldelight.boom.app.adapters.song;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.base.IMediaElement;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.playbackEvent.utils.MediaType;
import com.globaldelight.boom.app.fragments.FavouriteListFragment;
import com.globaldelight.boom.utils.OverFlowMenuUtils;
import com.globaldelight.boom.utils.Utils;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 20-11-16.
 */
public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongViewHolder> {

    private static final String TAG = "SongListAdapter-TAG";
    ArrayList<? extends IMediaElement> itemList;
    private Activity activity;
    private @ItemType int listItemType;
    Fragment fragment;


    public SongListAdapter(Activity activity, Fragment fragment, ArrayList<? extends IMediaElement> itemList, @ItemType int listItemType) {
        this.activity = activity;
        this.fragment = fragment;
        this.itemList = itemList;
        this.listItemType = listItemType;
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_song_item, parent, false);
        SongViewHolder holder = new SongViewHolder(itemView);
        setOnClicks(holder);
        return holder;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final SongViewHolder holder, final int position) {
        MediaItem mediaItem = (MediaItem) itemList.get(position);
        holder.position = position;
        holder.mainView.setElevation(0);
        holder.title.setText(mediaItem.getTitle());
        holder.description.setVisibility(null != mediaItem.getItemArtist() ? View.VISIBLE : View.GONE);
        holder.description.setText(mediaItem.getItemArtist());
        setAlbumArt(mediaItem.getItemArtUrl(), holder);

        updatePlayingTrack(holder, itemList.get(position));
    }

    private void updatePlayingTrack(SongViewHolder holder, IMediaElement item){
        IMediaElement nowPlayingItem = App.playbackManager().queue().getPlayingItem();
        if(null != nowPlayingItem) {
            boolean isMediaItem = (nowPlayingItem.getMediaType() == MediaType.DEVICE_MEDIA_LIB);
            if ( item.equalTo(nowPlayingItem) ) {
                holder.overlay.setVisibility(View.VISIBLE );
                holder.overlayPlay.setVisibility( View.VISIBLE );
                holder.title.setSelected(true);
                if (App.playbackManager().isTrackPlaying()) {
                    holder.progressIndicator.setVisibility(View.GONE);
                    holder.overlayPlay.setImageResource(R.drawable.ic_player_pause);
                    if( !isMediaItem && App.playbackManager().isTrackLoading() ) {
                        holder.progressIndicator.setVisibility(View.VISIBLE);
                    } else {
                        holder.progressIndicator.setVisibility(View.GONE);
                    }
                } else {
                    holder.progressIndicator.setVisibility(View.GONE);
                    holder.overlayPlay.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_player_play, null));
                }
            } else {
                holder.overlay.setVisibility( View.GONE );
                holder.overlayPlay.setVisibility( View.GONE );
                holder.progressIndicator.setVisibility(View.GONE);
                holder.title.setSelected(false);
            }
        }
    }

    private void setAlbumArt(@NonNull String path, SongViewHolder holder) {
        final int size = Utils.smallImageSize(activity);
        Glide.with(activity)
                .load(path)
                .placeholder(R.drawable.ic_default_art_grid)
                .centerCrop()
                .override(size, size)
                .into(holder.img);
    }

    private void setOnClicks(final SongViewHolder holder) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int position = holder.position;
                if ( position == -1) {
                    return;
                }

                App.playbackManager().queue().addItemListToPlay(itemList, position);
                if(itemList.get(position).getMediaType()==MediaType.DROP_BOX){
                    FlurryAnalytics.getInstance(activity.getApplicationContext()).setEvent(FlurryEvents.Song_Played_from_DropBox_thum_Nail);

                }else if(itemList.get(position).getMediaType()==MediaType.GOOGLE_DRIVE){
                    FlurryAnalytics.getInstance(activity.getApplicationContext()).setEvent(FlurryEvents.Song_Played_from_GoogleDrive_thum_Nail);
                }
                else {
                    FlurryAnalytics.getInstance(activity.getApplicationContext()).setEvent(FlurryEvents.EVENT_MUSIC_PLAYED_FROM_SONG_SECTION);
                }
                if(listItemType == ItemType.RECENT_PLAYED){
                    FlurryAnalytics.getInstance(activity.getApplicationContext()).setEvent(FlurryEvents.Song_Played_Recent_Playlist);
                }else if(listItemType == ItemType.FAVOURITE){
                    FlurryAnalytics.getInstance(activity.getApplicationContext()).setEvent(FlurryEvents.Song_Played_favourite_Playlist);
                }
            }
        });

        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchorView) {
                final int position = holder.position;
                OverFlowMenuUtils.showMediaItemMenu(activity, anchorView,R.menu.media_item_popup, itemList.get(position));
                if(listItemType == ItemType.FAVOURITE) {
                    updateFavoriteList(MediaController.getInstance(activity).getFavoriteList());
                }

            }
        });
    }

    private void updateFavoriteList(ArrayList<? extends IMediaElement> newList) {
        if(listItemType == ItemType.FAVOURITE) {
            itemList = newList;
            notifyDataSetChanged();
            if (itemList.size() == 0) {
                ((FavouriteListFragment)fragment).listIsEmpty(itemList.size());
            }
        }
    }

    @Override
    public int getItemCount() {
        return null == itemList ? 0 : itemList.size();
    }

    public void updateMediaList(ArrayList<? extends IMediaElement> mediaList) {
        if(null != mediaList){
            this.itemList = mediaList;
        }
        notifyDataSetChanged();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {

        public TextView title, description;
        public View mainView, overlay;
        public ImageView img, overlayPlay;
        public LinearLayout menu;
        public ProgressBar progressIndicator;
        public int position;

        public SongViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            img = (ImageView) itemView.findViewById(R.id.song_item_img);
            overlayPlay = (ImageView) itemView.findViewById(R.id.song_item_img_overlay_play);
            overlay = itemView.findViewById(R.id.song_item_img_overlay);
            progressIndicator = (ProgressBar) itemView.findViewById(R.id.load_cloud );
            title = (TextView) itemView.findViewById(R.id.song_item_name);
            menu = (LinearLayout) itemView.findViewById(R.id.song_item_overflow_menu);
            description = (TextView) itemView.findViewById(R.id.song_item_artist);
        }
    }
}
