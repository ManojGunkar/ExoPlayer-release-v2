package com.globaldelight.boom.ui.musiclist.adapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
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
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.ui.musiclist.activity.FavouriteListActivity;
import com.globaldelight.boom.ui.widgets.CoachMarkTextView;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.async.Action;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Rahul Agarwal on 20-11-16.
 */
public class FavouriteListAdapter extends RecyclerView.Adapter<FavouriteListAdapter.SimpleItemViewHolder> {

    private static final String TAG = "FavouriteListAdapter-TAG";
    LinkedList<? extends IMediaItemBase> itemList;
    private int selectedSongId = -1;
    private FavouriteListAdapter.SimpleItemViewHolder selectedHolder;
    private Context context;
    private RecyclerView recyclerView;

    public FavouriteListAdapter(Context context, RecyclerView recyclerView, LinkedList<? extends IMediaItemBase> itemList) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.itemList = itemList;
    }

    @Override
    public FavouriteListAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_song_item, parent, false);
        return new FavouriteListAdapter.SimpleItemViewHolder(itemView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final FavouriteListAdapter.SimpleItemViewHolder holder, final int position) {
        holder.name.setText(itemList.get(position).getItemTitle());
        holder.artistName.setText(((MediaItem)itemList.get(position)).getItemArtist());
        holder.mainView.setElevation(0);
        if((itemList.get(position)).getMediaType() == MediaType.DEVICE_MEDIA_LIB && null == itemList.get(position).getItemArtUrl())
            itemList.get(position).setItemArtUrl(App.getPlayingQueueHandler().getUpNextList().getAlbumArtList().get(((MediaItem) itemList.get(position)).getItemAlbum()));

        if(null == itemList.get(position).getItemArtUrl())
            itemList.get(position).setItemArtUrl(MediaItem.UNKNOWN_ART_URL);

        setAlbumArt(itemList.get(position).getItemArtUrl(), holder);
        if (selectedHolder != null)
            selectedHolder.mainView.setBackgroundColor(ContextCompat
                    .getColor(context, R.color.appBackground));
        selectedSongId = -1;
        selectedHolder = null;

        if(App.getUserPreferenceHandler().isLibFromHome()){
            holder.menu.setVisibility(View.VISIBLE);
            holder.songChk.setVisibility(View.GONE);
            setOnClicks(holder, position);
        }else{
            holder.menu.setVisibility(View.GONE);
            holder.songChk.setVisibility(View.VISIBLE);
            if(App.getUserPreferenceHandler().getItemIDList().contains(itemList.get(position).getItemId())){
                holder.songChk.setChecked(true);
            }else {
                holder.songChk.setChecked(false);
            }
            holder.songChk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    App.getUserPreferenceHandler().addItemToPlayList((MediaItem)  itemList.get(position));
                }
            });
        }

        MediaItem nowPlayingItem = (MediaItem) App.getPlayingQueueHandler().getUpNextList().getPlayingItem();
        if(null != nowPlayingItem /*&& nowPlayingItem.getParentType() == ItemType.SONGS*/ /*&& (App.getPlayerEventHandler().isPlaying() || App.getPlayerEventHandler().isPaused())*/){
            if(itemList.get(position).getItemId() == nowPlayingItem.getItemId()){
                holder.name.setTextColor(context.getResources().getColor(R.color.boom_yellow));
            }else{
                holder.name.setTextColor(context.getResources().getColor(R.color.white));
            }
        }
    }

    private void setAlbumArt(String path, FavouriteListAdapter.SimpleItemViewHolder holder) {
        if (PlayerUtils.isPathValid(path ))
            Picasso.with(context).load(new File(path)).error(context.getResources().getDrawable(R.drawable.ic_default_list, null)).resize(dpToPx(90),
                    dpToPx(90)).centerCrop().into(holder.img);
        else{
            setDefaultArt(holder, dpToPx(90));
        }
    }

    private void setDefaultArt(FavouriteListAdapter.SimpleItemViewHolder holder, int size) {
        holder.img.setImageDrawable(context.getResources().getDrawable( R.drawable.ic_default_list ));
    }

    private void setOnClicks(final FavouriteListAdapter.SimpleItemViewHolder holder, final int position) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animate(holder);
                if(App.getPlayingQueueHandler().getUpNextList()!=null){
                    App.getPlayingQueueHandler().getUpNextList().addToPlay((LinkedList<MediaItem>) itemList, position, false);
                    notifyDataSetChanged();
                }
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_MUSIC_PLAYED_FROM_FAVOURITE_SECTION);

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
                                        App.getPlayingQueueHandler().getUpNextList().addItemToUpNextFrom( itemList.get(position));
                                        break;
                                    case R.id.popup_song_add_queue:
                                        App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext((MediaItem) itemList.get(position));
                                        break;
                                    case R.id.popup_song_add_playlist:
                                        Utils util = new Utils(context);
                                        ArrayList list = new ArrayList<IMediaItemBase>();
                                        list.add(itemList.get(position));
                                        util.addToPlaylist((FavouriteListActivity) context, list, null);
                                        break;
                                    case R.id.popup_song_add_fav:
                                        MediaController.getInstance(context).removeItemToFavoriteList(itemList.get(position).getItemId());
                                        itemList = MediaController.getInstance(context).getFavouriteListItems();
                                        updateFavoriteList(MediaController.getInstance(context).getFavouriteListItems());
                                        Toast.makeText(context, context.getResources().getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }catch (Exception e){

                            }
                            return false;
                        }
                    });
                    pm.inflate(R.menu.song_remove_fav);
                    pm.show();
            }
        });
    }

    private void updateFavoriteList(LinkedList<? extends IMediaItemBase> newList) {
        itemList = newList;
        notifyDataSetChanged();
        if(itemList.size() == 0){
            ((FavouriteListActivity)context).listIsEmpty();
        }
    }

    public void animate(final FavouriteListAdapter.SimpleItemViewHolder holder) {
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
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public void onViewRecycled(FavouriteListAdapter.SimpleItemViewHolder holder) {
        super.onViewRecycled(holder);
        holder.img.setImageDrawable(null);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    private ValueAnimator animateElevation(int from, int to, final FavouriteListAdapter.SimpleItemViewHolder holder) {
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
                    .getColor(context, R.color.appBackground));
        }
    }

    public void onBackPressed() {
        if (selectedSongId != -1) {
            animateElevation(12, 0, selectedHolder);
            selectedHolder.mainView.setBackgroundColor(ContextCompat
                    .getColor(context, R.color.appBackground));
            selectedSongId = -1;
            selectedHolder = null;
        }
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
