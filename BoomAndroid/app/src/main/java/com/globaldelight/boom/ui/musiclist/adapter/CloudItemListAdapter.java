package com.globaldelight.boom.ui.musiclist.adapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
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
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.ui.musiclist.fragment.ItemSongListFragment;
import com.globaldelight.boom.ui.widgets.CoachMarkTextView;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.async.Action;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 20-11-16.
 */
public class CloudItemListAdapter extends RecyclerView.Adapter<CloudItemListAdapter.SimpleItemViewHolder> {

    private static final String TAG = "CloudItemListAdapter-TAG";
    public static final int ITEM_VIEW_TYPE_DEVICE_MEDIA = 0;
    public static final int ITEM_VIEW_TYPE_DROPBOX = 1;
    public static final int ITEM_VIEW_TYPE_GOOGLE_DRIVE = 2;
    ArrayList<? extends IMediaItemBase> itemList;
    private int selectedSongId = -1;
    private CloudItemListAdapter.SimpleItemViewHolder selectedHolder;
    private Activity activity;
    private ItemType listItemType;
    ItemSongListFragment fragment;

    public CloudItemListAdapter(Activity activity, ItemSongListFragment fragment, ArrayList<? extends IMediaItemBase> itemList, ItemType listItemType) {
        this.activity = activity;
        this.fragment = fragment;
        this.itemList = itemList;
        this.listItemType = listItemType;
    }

    @Override
    public CloudItemListAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch(viewType) {
            case ITEM_VIEW_TYPE_GOOGLE_DRIVE:
                itemView = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.card_song_item, parent, false);
                return new CloudItemListAdapter.SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_DROPBOX:
                itemView = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.card_song_item, parent, false);
                return new CloudItemListAdapter.SimpleItemViewHolder(itemView);
            default:
                itemView = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.card_song_item, parent, false);
                return new CloudItemListAdapter.SimpleItemViewHolder(itemView);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final CloudItemListAdapter.SimpleItemViewHolder holder, final int position) {
        holder.name.setText(itemList.get(position).getItemTitle());
        if(whatView(position) == ITEM_VIEW_TYPE_GOOGLE_DRIVE) {
            setDefaultArt(holder);
        }else if(whatView(position) == ITEM_VIEW_TYPE_DROPBOX){
            setDefaultArt(holder);
        }else{
            holder.artistName.setText(((MediaItem) itemList.get(position)).getItemArtist());
            holder.mainView.setElevation(0);
            if ((itemList.get(position)).getMediaType() == MediaType.DEVICE_MEDIA_LIB && null == itemList.get(position).getItemArtUrl())
                itemList.get(position).setItemArtUrl(App.getPlayingQueueHandler().getUpNextList().getAlbumArtList().get(((MediaItem) itemList.get(position)).getItemAlbum()));

            if (null == itemList.get(position).getItemArtUrl())
                itemList.get(position).setItemArtUrl(MediaItem.UNKNOWN_ART_URL);

            setAlbumArt(itemList.get(position).getItemArtUrl(), holder);
            if (selectedHolder != null)
                selectedHolder.mainView.setBackgroundColor(ContextCompat
                        .getColor(activity, R.color.appBackground));
            selectedSongId = -1;
            selectedHolder = null;
        }

        if (App.getUserPreferenceHandler().isLibFromHome()) {
            holder.menu.setVisibility(View.VISIBLE);
            holder.songChk.setVisibility(View.GONE);
            setOnClicks(holder, position);
        } else {
            holder.menu.setVisibility(View.GONE);
            holder.songChk.setVisibility(View.VISIBLE);
            if (App.getUserPreferenceHandler().getItemIDList().contains(itemList.get(position).getItemId())) {
                holder.songChk.setChecked(true);
            } else {
                holder.songChk.setChecked(false);
            }
            holder.songChk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    App.getUserPreferenceHandler().addItemToPlayList((IMediaItem) itemList.get(position));
                }
            });
        }

        IMediaItem nowPlayingItem = (IMediaItem) App.getPlayingQueueHandler().getUpNextList().getPlayingItem();
        if (null != nowPlayingItem /*&& nowPlayingItem.getParentType() == ItemType.SONGS*/ /*&& (App.getPlayerEventHandler().isPlaying() || App.getPlayerEventHandler().isPaused())*/) {
            if(nowPlayingItem.getMediaType() == MediaType.DEVICE_MEDIA_LIB) {
                if (itemList.get(position).getItemId() == nowPlayingItem.getItemId()) {
                    holder.name.setTextColor(activity.getResources().getColor(R.color.boom_yellow));
                } else {
                    holder.name.setTextColor(activity.getResources().getColor(R.color.white));
                }
            }else{
                if (itemList.get(position).getItemTitle().equals(nowPlayingItem.getItemTitle())) {
                    holder.name.setTextColor(activity.getResources().getColor(R.color.boom_yellow));
                } else {
                    holder.name.setTextColor(activity.getResources().getColor(R.color.white));
                }
            }
        }
    }

    private void setAlbumArt(String path, CloudItemListAdapter.SimpleItemViewHolder holder) {
        if (PlayerUtils.isPathValid(path ))
            Picasso.with(activity).load(new File(path)).error(activity.getResources().getDrawable(R.drawable.ic_default_list, null))/*.resize(dpToPx(90),
                    dpToPx(90)).centerCrop()*/.into(holder.img);
        else{
            setDefaultArt(holder);
        }
    }

    private void setDefaultArt(CloudItemListAdapter.SimpleItemViewHolder holder/*, int size*/) {
        holder.img.setImageDrawable(activity.getResources().getDrawable( R.drawable.ic_default_list ));
    }

    private void setOnClicks(final CloudItemListAdapter.SimpleItemViewHolder holder, final int position) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animate(holder);
                if(App.getPlayingQueueHandler().getUpNextList()!=null){
                    App.getPlayingQueueHandler().getUpNextList().addToPlay((ArrayList<IMediaItem>) itemList, position, false);
                    notifyDataSetChanged();
                }
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_MUSIC_PLAYED_FROM_FAVOURITE_SECTION);

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
                                        App.getPlayingQueueHandler().getUpNextList().addItemToUpNextFrom( itemList.get(position));
                                        break;
                                    case R.id.popup_song_add_queue:
                                        App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext((IMediaItem) itemList.get(position));
                                        break;
                                    case R.id.popup_song_add_playlist:
                                        Utils util = new Utils(activity);
                                        ArrayList list = new ArrayList();
                                        list.add(itemList.get(position));
                                        util.addToPlaylist(activity, list, null);
                                        break;
                                    case R.id.popup_song_add_fav:
                                        if(itemList.get(position).getMediaType() == MediaType.DEVICE_MEDIA_LIB) {
                                            MediaController.getInstance(activity).removeItemToFavoriteList(itemList.get(position).getItemId());
                                            Toast.makeText(activity, activity.getResources().getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show();
                                        }else{
                                            if(MediaController.getInstance(activity).isFavouriteItems(itemList.get(position).getItemTitle())){
                                                MediaController.getInstance(activity).removeItemToFavoriteList(itemList.get(position).getItemTitle());
                                                Toast.makeText(activity, activity.getResources().getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show();
                                            }else{
                                                MediaController.getInstance(activity).addSongsToFavoriteList(itemList.get(position));
                                                Toast.makeText(activity, activity.getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        updateFavoriteList(MediaController.getInstance(activity).getFavouriteListItems());
                                        break;
                                }
                            }catch (Exception e){

                            }
                            return false;
                        }
                    });
                    if(listItemType == ItemType.FAVOURITE ||
                            MediaController.getInstance(activity).isFavouriteItems(itemList.get(position).getItemTitle())) {
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
                fragment.listIsEmpty(itemList.size());
            }
        }
    }

    public void animate(final CloudItemListAdapter.SimpleItemViewHolder holder) {
        //using action for smooth animation
        new Action() {

            @NonNull
            @Override
            public String id() {
                return TAG;
            }

            @Nullable
            @Override
            protected Object run() throws InterruptedException {
                return null;
            }

            @Override
            protected void done(@Nullable Object result) {
                animateElevation(0, dpToPx(10), holder);
                animateElevation(dpToPx(10), 0, holder);
            }
        }.execute();
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public void onViewRecycled(CloudItemListAdapter.SimpleItemViewHolder holder) {
        super.onViewRecycled(holder);
        holder.img.setImageDrawable(null);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return whatView(position);
    }

    public int whatView(int position) {
        if (itemList.get(position).getMediaType()== MediaType.DEVICE_MEDIA_LIB) {
            return ITEM_VIEW_TYPE_DEVICE_MEDIA;
        } else if (itemList.get(position).getMediaType()== MediaType.DROP_BOX) {
            return ITEM_VIEW_TYPE_DROPBOX;
        }else{
            return ITEM_VIEW_TYPE_GOOGLE_DRIVE;
        }
    }

    private ValueAnimator animateElevation(int from, int to, final CloudItemListAdapter.SimpleItemViewHolder holder) {
        Integer elevationFrom = from;
        Integer elevationTo = to;
        ValueAnimator colorAnimation =
                ValueAnimator.ofObject(
                        new ArgbEvaluator(), elevationFrom, elevationTo);
        colorAnimation.setInterpolator(new DecelerateInterpolator());
        colorAnimation.addUpdateListener(
                new ValueAnimator.AnimatorUpdateListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        holder.mainView.setElevation(
                                (Integer) animator.getAnimatedValue());
                    }

                });
        colorAnimation.setDuration(500);
        if (from != 0)
            colorAnimation.setStartDelay(colorAnimation.getDuration() + 300);
        colorAnimation.start();
        return colorAnimation;
    }

    public void recyclerScrolled() {
        if (selectedHolder != null && selectedSongId != -1) {
            animateElevation(12, 0, selectedHolder);
            selectedSongId = -1;
            selectedHolder.mainView.setBackgroundColor(ContextCompat
                    .getColor(activity, R.color.appBackground));
        }
    }

    public void onBackPressed() {
        if (selectedSongId != -1) {
            animateElevation(12, 0, selectedHolder);
            selectedHolder.mainView.setBackgroundColor(ContextCompat
                    .getColor(activity, R.color.appBackground));
            selectedSongId = -1;
            selectedHolder = null;
        }
    }

    public void updateMediaList(ArrayList<IMediaItemBase> mediaList) {
        if(null != mediaList){
            this.itemList = mediaList;
        }
        notifyDataSetChanged();
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
