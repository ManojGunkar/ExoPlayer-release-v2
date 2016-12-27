package com.globaldelight.boom.ui.musiclist.adapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaLibrary.DeviceMediaQuery;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.ui.musiclist.activity.DeviceMusicActivity;
import com.globaldelight.boom.ui.widgets.CoachMarkTextView;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.async.Action;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 8/8/2016.
 */
public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SimpleItemViewHolder> {

    private static final String TAG = "SongListAdapter-TAG";
    ArrayList<MediaItem> itemList;
    private Context context;
    private Activity activity;

    public SongListAdapter(Context context, FragmentActivity activity, ArrayList<? extends IMediaItemBase> itemList) {
        this.context = context;
        this.activity = activity;
        this.itemList = (ArrayList<MediaItem>) itemList;
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

        if(null == getMediaItem(position).getItemArtUrl())
            getMediaItem(position).setItemArtUrl(DeviceMediaQuery.getAlbumArtByAlbumId(context, getMediaItem(position).getItemAlbumId()));

        if(null == getMediaItem(position).getItemArtUrl())
            getMediaItem(position).setItemArtUrl(MediaItem.UNKNOWN_ART_URL);

        setAlbumArt(getMediaItem(position).getItemArtUrl(), holder);

        if(App.getUserPreferenceHandler().isLibFromHome()){
            holder.menu.setVisibility(View.VISIBLE);
            holder.songChk.setVisibility(View.GONE);
            setOnClicks(holder, position);
        }else{
            holder.menu.setVisibility(View.GONE);
            holder.songChk.setVisibility(View.VISIBLE);
            if(App.getUserPreferenceHandler().getItemIDList().contains(getMediaItem(position).getItemId())){
                holder.songChk.setChecked(true);
            }else {
                holder.songChk.setChecked(false);
            }
            holder.songChk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    App.getUserPreferenceHandler().addItemToPlayList(getMediaItem(position));
                }
            });
        }

        MediaItem nowPlayingItem = (MediaItem) App.getPlayingQueueHandler().getUpNextList().getPlayingItem();
        if(null != nowPlayingItem /*&& nowPlayingItem.getParentType() == ItemType.SONGS*/ /*&& (App.getPlayerEventHandler().isPlaying() || App.getPlayerEventHandler().isPaused())*/){
            if(getMediaItem(position).getItemId() == nowPlayingItem.getItemId()){
                holder.name.setTextColor(context.getResources().getColor(R.color.boom_yellow));
            }else{
                holder.name.setTextColor(context.getResources().getColor(R.color.white));
            }
        }
    }

    private void setAlbumArt(String path, SimpleItemViewHolder holder) {
        int size = Utils.dpToPx(context, 90);
            if (PlayerUtils.isPathValid(path))
                Picasso.with(context).load(new File(path)).error(context.getResources().getDrawable(R.drawable.ic_default_list, null)).resize(size,
                        size).centerCrop().into(holder.img);
            else {
                setDefaultArt(holder, size);
            }
    }

    private void setDefaultArt(SimpleItemViewHolder holder, int size) {
        holder.img.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_default_list));
    }

    private void setOnClicks(final SimpleItemViewHolder holder, final int position) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(App.getPlayingQueueHandler().getUpNextList()!=null){
                    App.getPlayingQueueHandler().getUpNextList().addToPlay(itemList, position, false);
                    notifyDataSetChanged();
                }
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_MUSIC_PLAYED_FROM_SONG_SECTION);
            }
        });

        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchorView) {
                    PopupMenu pm = new PopupMenu(context, anchorView);
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
                                        Utils util = new Utils(context);
                                        ArrayList list = new ArrayList<IMediaItemBase>();
                                        list.add(getMediaItem(position));
                                        util.addToPlaylist(activity, list, null);
                                        break;
                                    case R.id.popup_song_add_fav:
                                        if (MediaController.getInstance(context).isFavouriteItems(getMediaItem(position).getItemId())) {
                                            MediaController.getInstance(context).removeItemToFavoriteList(getMediaItem(position).getItemId());
                                            Toast.makeText(context, context.getResources().getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show();
                                        } else {
                                            MediaController.getInstance(context).addSongsToFavoriteList(getMediaItem(position));
                                            Toast.makeText(context, context.getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                }
                            }catch (Exception e){

                            }
                            return false;
                        }
                    });
                    if (MediaController.getInstance(context).isFavouriteItems(getMediaItem(position).getItemId())) {
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

    public MediaItem getMediaItem(int position){
        return itemList.get(position);
    }

    public void onBackPressed() {
        if (activity != null && getMediaItem(0).getItemType() == ItemType.SONGS)
            ((DeviceMusicActivity) activity).killActivity();
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public RegularTextView name;
        public CoachMarkTextView artistName;
        public View mainView;
        public ImageView img, menu;
        public CheckBox songChk;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            img = (ImageView) itemView.findViewById(R.id.song_item_img);
            name = (RegularTextView) itemView.findViewById(R.id.song_item_name);
            menu = (ImageView) itemView.findViewById(R.id.song_item_menu);
            artistName = (CoachMarkTextView) itemView.findViewById(R.id.song_item_artist);
            songChk = (CheckBox) itemView.findViewById(R.id.song_chk);
        }
    }
}
