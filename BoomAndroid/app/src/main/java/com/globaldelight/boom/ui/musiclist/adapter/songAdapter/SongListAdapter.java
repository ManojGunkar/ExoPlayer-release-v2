package com.globaldelight.boom.ui.musiclist.adapter.songAdapter;

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
import android.widget.PopupMenu;
import android.widget.Toast;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.ui.musiclist.fragment.MediaItemListFragment;
import com.globaldelight.boom.ui.musiclist.fragment.MusicLibraryListFragment;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 8/8/2016.
 */
public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SimpleItemViewHolder> {

    private static final String TAG = "SongListAdapter-TAG";
    ArrayList<IMediaItem> itemList;
    private Activity activity;
    private Fragment fragment;

    public SongListAdapter(Activity activity, Fragment fragment, ArrayList<? extends IMediaItemBase> itemList) {
        this.activity = activity;
        this.fragment = fragment;
        this.itemList = (ArrayList<IMediaItem>) itemList;
    }

    @Override
    public SongListAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_song_item, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final SongListAdapter.SimpleItemViewHolder holder, final int position) {
        holder.name.setText(getMediaItem(position).getItemTitle());
        holder.artistName.setText(getMediaItem(position).getItemArtist());

        holder.mainView.setElevation(0);

        if(null == getMediaItem(position).getItemArtUrl()) {
            getMediaItem(position).setItemArtUrl(App.getPlayingQueueHandler().getUpNextList().getAlbumArtList().get(getMediaItem(position).getItemAlbum()));
        }
        if(null == getMediaItem(position).getItemArtUrl())
            getMediaItem(position).setItemArtUrl(MediaItem.UNKNOWN_ART_URL);

        setAlbumArt(getMediaItem(position).getItemArtUrl(), holder);

        updatePlayingTrack(holder, position, getMediaItem(position).getItemId());

        setOnClicks(holder, position);
    }

    private void updatePlayingTrack(SimpleItemViewHolder holder, int position, long itemId){
        IMediaItem nowPlayingItem = (IMediaItem) App.getPlayingQueueHandler().getUpNextList().getPlayingItem();
        if(null != nowPlayingItem){
            holder.name.setTextColor(itemId == nowPlayingItem.getItemId() ? ContextCompat.getColor(activity, R.color.track_selected_title)
            : ContextCompat.getColor(activity, R.color.track_title));
            holder.art_overlay.setVisibility(itemId == nowPlayingItem.getItemId() ? View.INVISIBLE : View.INVISIBLE);
            holder.art_overlay_play.setVisibility(itemId == nowPlayingItem.getItemId() ? View.INVISIBLE : View.INVISIBLE);
            if(itemId == nowPlayingItem.getItemId()){
                if(App.getPlayerEventHandler().isPlaying()){
                    holder.art_overlay_play.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_player_pause, null));
                }else{
                    holder.art_overlay_play.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_player_play, null));
                }
            }
        }
    }

    private void setAlbumArt(String path, SimpleItemViewHolder holder) {
        int size = (int) activity.getResources().getDimension(R.dimen.track_list_album_art_size);
            if (PlayerUtils.isPathValid(path))
                Picasso.with(activity).load(new File(path)).error(activity.getResources().getDrawable(R.drawable.ic_default_list, null))/*.resize(size,
                        size).centerCrop()*/.into(holder.img);
            else {
                setDefaultArt(holder, size);
            }
    }

    private void setDefaultArt(SimpleItemViewHolder holder, int size) {
        holder.img.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_default_list, null));
    }

    private void setOnClicks(final SimpleItemViewHolder holder, final int position) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(App.getPlayingQueueHandler().getUpNextList()!=null){
                    App.getPlayingQueueHandler().getUpNextList().addToPlay(itemList, position, true, false);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    }, 200);
                }
                try {
                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_MUSIC_PLAYED_FROM_SONG_SECTION);
                }catch (Exception e){}
            }
        });

        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchorView) {
                    PopupMenu pm = new PopupMenu(activity, anchorView);
                    pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            try {
                                switch (item.getItemId()) {
                                    case R.id.popup_song_play_next:
                                        App.getPlayingQueueHandler().getUpNextList().addItemToUpNextFrom(getMediaItem(position));
                                        break;
                                    case R.id.popup_song_add_queue:
                                        App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext(getMediaItem(position));
                                        break;
                                    case R.id.popup_song_add_playlist:
                                        Utils util = new Utils(activity);
                                        ArrayList list = new ArrayList<IMediaItemBase>();
                                        list.add(getMediaItem(position));
                                        util.addToPlaylist(activity, list, null);
                                        break;
                                    case R.id.popup_song_add_fav:
                                        if (MediaController.getInstance(activity).isFavouriteItems(getMediaItem(position).getItemId())) {
                                            MediaController.getInstance(activity).removeItemToFavoriteList(getMediaItem(position).getItemId());
                                            Toast.makeText(activity, activity.getResources().getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show();
                                        } else {
                                            MediaController.getInstance(activity).addSongsToFavoriteList(getMediaItem(position));
                                            Toast.makeText(activity, activity.getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                }
                            }catch (Exception e){

                            }
                            return false;
                        }
                    });
                    if (MediaController.getInstance(activity).isFavouriteItems(getMediaItem(position).getItemId())) {
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
        return itemList.size();
    }

    public IMediaItem getMediaItem(int position){
        return itemList.get(position);
    }

    public void onBackPressed() {
        if (activity != null && getMediaItem(0).getItemType() == ItemType.SONGS){
            if(fragment instanceof MusicLibraryListFragment)
                ((MusicLibraryListFragment)fragment).killActivity();
            else
                ((MediaItemListFragment)fragment).killActivity();
        }
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public RegularTextView name, artistName;
        public View mainView, art_overlay;
        public ImageView img, menu, art_overlay_play;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            img = (ImageView) itemView.findViewById(R.id.song_item_img);
            art_overlay_play = (ImageView) itemView.findViewById(R.id.song_item_img_overlay_play);
            art_overlay = itemView.findViewById(R.id.song_item_img_overlay);
            name = (RegularTextView) itemView.findViewById(R.id.song_item_name);
            menu = (ImageView) itemView.findViewById(R.id.song_item_menu);
            artistName = (RegularTextView) itemView.findViewById(R.id.song_item_artist);
        }
    }
}
