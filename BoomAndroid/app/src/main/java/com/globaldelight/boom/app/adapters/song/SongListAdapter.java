package com.globaldelight.boom.app.adapters.song;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.AnalyticsHelper;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.app.analytics.UtilAnalytics;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.local.callback.IMediaItem;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.playbackEvent.utils.MediaType;
import com.globaldelight.boom.app.fragments.FavouriteListFragment;
import com.globaldelight.boom.view.RegularTextView;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 20-11-16.
 */
public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongViewHolder> {

    private static final String TAG = "SongListAdapter-TAG";
    ArrayList<? extends IMediaItemBase> itemList;
    private Activity activity;
    private ItemType listItemType;
    Fragment fragment;
    private int WIDTH, HEIGHT;


    public SongListAdapter(Activity activity, Fragment fragment, ArrayList<? extends IMediaItemBase> itemList, ItemType listItemType) {
        this.activity = activity;
        this.fragment = fragment;
        this.itemList = itemList;
        this.listItemType = listItemType;
        WIDTH = Utils.dpToPx(activity, 62);
        HEIGHT = Utils.dpToPx(activity, 62);
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
        holder.mainView.setElevation(0);
        holder.name.setText(mediaItem.getItemTitle());
        holder.artistName.setVisibility(null != mediaItem.getItemArtist() ? View.VISIBLE : View.GONE);
        holder.artistName.setText(mediaItem.getItemArtist());
        if (null == mediaItem.getItemArtUrl()) {
            mediaItem.setItemArtUrl(App.getPlayingQueueHandler().getUpNextList().getAlbumArtList().get(mediaItem.getItemAlbum()));
        }
        if (null == mediaItem.getItemArtUrl()) {
            mediaItem.setItemArtUrl(MediaItem.UNKNOWN_ART_URL);
        }

        setAlbumArt(mediaItem.getItemArtUrl(), holder);

        updatePlayingTrack(holder, itemList.get(position).getItemId());
    }

    private void updatePlayingTrack(SongViewHolder holder, long itemId){
        IMediaItemBase nowPlayingItem = App.getPlayingQueueHandler().getUpNextList().getPlayingItem();
        if(null != nowPlayingItem) {
            boolean isMediaItem = (nowPlayingItem.getMediaType() == MediaType.DEVICE_MEDIA_LIB);
            if (itemId == nowPlayingItem.getItemId()) {
                holder.art_overlay.setVisibility(View.VISIBLE );
                holder.art_overlay_play.setVisibility( View.VISIBLE );
                holder.name.setTextColor( ContextCompat.getColor(activity, R.color.track_selected_title) );
                if (App.getPlayerEventHandler().isPlaying()) {
                    holder.loadCloud.setVisibility(View.GONE);
                    holder.art_overlay_play.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_player_pause, null));
                } else {
                    if(!isMediaItem && App.getPlayerEventHandler().isTrackWaitingForPlay() && !App.getPlayerEventHandler().isPaused())
                        holder.loadCloud.setVisibility(View.VISIBLE);
                    else
                        holder.loadCloud.setVisibility(View.GONE);
                    holder.art_overlay_play.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_player_play, null));
                }
            } else {
                holder.art_overlay.setVisibility( View.INVISIBLE );
                holder.art_overlay_play.setVisibility( View.INVISIBLE );
                holder.loadCloud.setVisibility(View.GONE);
                holder.name.setTextColor(ContextCompat.getColor(activity, R.color.track_title));
            }
        }
    }

    private void setAlbumArt(String path, SongViewHolder holder) {
        if ( path == null ) path = "";
        Picasso.with(activity)
                .load(new File(path))
                .placeholder(R.drawable.ic_default_art_grid)
                .resize(WIDTH, HEIGHT)
                .into(holder.img);
    }

    private void setOnClicks(final SongViewHolder holder) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int position = holder.getAdapterPosition();
                if (!App.getPlayerEventHandler().isTrackLoading()) {
//                    if(itemList.get(position).getMediaType() != MediaType.DEVICE_MEDIA_LIB && null != App.getPlayingQueueHandler().getUpNextList().getPlayingItem()
//                            && App.getPlayingQueueHandler().getUpNextList().getPlayingItem().getItemId() != itemList.get(position).getItemId())
//                        holder.loadCloud.setVisibility(View.VISIBLE);
                    App.getPlayingQueueHandler().getUpNextList().addItemListToPlay(itemList, position);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    }, 500);
                }
                if(itemList.get(position).getMediaType()==MediaType.DROP_BOX){
                    FlurryAnalyticHelper.logEvent(UtilAnalytics.Song_Played_from_DropBox_thum_Nail);

                }else if(itemList.get(position).getMediaType()==MediaType.GOOGLE_DRIVE){
                    FlurryAnalyticHelper.logEvent(UtilAnalytics.Song_Played_from_GoogleDrive_thum_Nail);
                }
                else {
                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_MUSIC_PLAYED_FROM_SONG_SECTION);
                }
//                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_MUSIC_PLAYED_FROM_FAVOURITE_SECTION);
                if(listItemType == ItemType.RECENT_PLAYED){
                    FlurryAnalyticHelper.logEvent(UtilAnalytics.Song_Played_Recent_Playlist);
                }else if(listItemType == ItemType.FAVOURITE){
                    FlurryAnalyticHelper.logEvent(UtilAnalytics.Song_Played_favourite_Playlist);
                }
            }
        });

        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchorView) {
                    final int position = holder.getAdapterPosition();
                    PopupMenu pm = new PopupMenu(activity, anchorView);
                    pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            try {
                                switch (item.getItemId()) {
                                    case R.id.popup_song_play_next:
                                        App.getPlayingQueueHandler().getUpNextList().addItemAsPlayNext(itemList.get(position));
                                        break;
                                    case R.id.popup_song_add_queue:
                                        App.getPlayingQueueHandler().getUpNextList().addItemAsUpNext(itemList.get(position));
                                        break;
                                    case R.id.popup_song_add_playlist:
                                        Utils util = new Utils(activity);
                                        ArrayList list = new ArrayList();
                                        list.add(itemList.get(position));
                                        util.addToPlaylist(activity, list, null);
                                        break;
                                    case R.id.popup_song_add_fav:
                                        if(MediaController.getInstance(activity).isFavoriteItem(itemList.get(position).getItemId())){
                                            MediaController.getInstance(activity).removeItemToFavoriteList(itemList.get(position).getItemId());
                                            Toast.makeText(activity, activity.getResources().getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show();
                                        }else{
                                            MediaController.getInstance(activity).addItemToFavoriteList((IMediaItem) itemList.get(position));
                                            Toast.makeText(activity, activity.getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                                        }
                                        if(listItemType == ItemType.FAVOURITE)
                                            updateFavoriteList(MediaController.getInstance(activity).getFavoriteList());
                                        break;
                                }
                            }catch (Exception e){

                            }
                            return false;
                        }
                    });
                    if(listItemType == ItemType.FAVOURITE ||
                            MediaController.getInstance(activity).isFavoriteItem(itemList.get(position).getItemId())) {
                        pm.inflate(R.menu.song_remove_fav);
                    }else{
                        pm.inflate(R.menu.song_add_fav);
                    }

                    pm.show();
            }
        });
    }

    private void updateFavoriteList(ArrayList<? extends IMediaItemBase> newList) {
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

    public void updateMediaList(ArrayList<IMediaItemBase> mediaList) {
        if(null != mediaList){
            this.itemList = mediaList;
        }
        notifyDataSetChanged();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {

        public RegularTextView name, artistName;
        public View mainView, art_overlay;
        public ImageView img, art_overlay_play;
        public LinearLayout menu;
        public ProgressBar loadCloud;

        public SongViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            img = (ImageView) itemView.findViewById(R.id.song_item_img);
            art_overlay_play = (ImageView) itemView.findViewById(R.id.song_item_img_overlay_play);
            art_overlay = itemView.findViewById(R.id.song_item_img_overlay);
            loadCloud = (ProgressBar) itemView.findViewById(R.id.load_cloud );
            name = (RegularTextView) itemView.findViewById(R.id.song_item_name);
            menu = (LinearLayout) itemView.findViewById(R.id.song_item_overflow_menu);
            artistName = (RegularTextView) itemView.findViewById(R.id.song_item_artist);
        }
    }
}
