package com.globaldelight.boom.ui.musiclist.adapter.songAdapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.globaldelight.boom.App;
import com.globaldelight.boom.manager.PlayerServiceReceiver;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.ui.musiclist.ListDetail;
import com.globaldelight.boom.ui.musiclist.activity.AlbumSongListActivity;
import com.globaldelight.boom.ui.musiclist.fragment.AlbumSongListFragment;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.OnStartDragListener;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.async.Action;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import static com.globaldelight.boom.data.MediaLibrary.ItemType.BOOM_PLAYLIST;
import static com.globaldelight.boom.data.MediaLibrary.ItemType.PLAYLIST;
import static com.globaldelight.boom.data.MediaLibrary.ItemType.SONGS;

/**
 * Created by Rahul Agarwal on 8/8/2016.
 */

public class ItemSongListAdapter extends RecyclerView.Adapter<ItemSongListAdapter.SimpleItemViewHolder> {

    public static final int TYPE_HEADER = 111;
    public static final int TYPE_ITEM = 222;
    private static final String TAG = "ItemSongListAdapter-TAG";
    OnStartDragListener mOnStartDragListener;
    private MediaItemCollection collection;
    private int selectedSongId = -1;
    private SimpleItemViewHolder selectedHolder;
    private Activity activity;
    private IMediaItem currentItem;
    private ListDetail listDetail;
    private AlbumSongListFragment fragment;

    public ItemSongListAdapter(Activity activity, AlbumSongListFragment fragment, IMediaItemCollection collection, ListDetail listDetail, OnStartDragListener dragListener) {
        this.activity = activity;
        this.fragment = fragment;
        this.collection = (MediaItemCollection) collection;
        this.listDetail = listDetail;
        this.mOnStartDragListener = dragListener;
    }
    @Override
    public ItemSongListAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if (viewType == TYPE_ITEM && collection.getItemType() == BOOM_PLAYLIST) {
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.card_boomplaylist_song_item, parent, false);
        } else if (viewType == TYPE_ITEM && collection.getItemType() != BOOM_PLAYLIST) {
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.card_song_item, parent, false);
        }else{
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.card_header_recycler_view, parent, false);
        }
        return new SimpleItemViewHolder(itemView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final ItemSongListAdapter.SimpleItemViewHolder holder, final int position) {

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

            setOnMenuClickListener(holder, position);
        } else if (position >= 1 && collection.getItemType() != BOOM_PLAYLIST) {
            int pos = position -1;
            if (collection.getItemType() == PLAYLIST) {
                currentItem = (MediaItem) collection.getMediaElement().get(pos);
            }else{
                currentItem = (MediaItem) ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().get(pos);
            }

            holder.name.setText(currentItem.getItemTitle());
            holder.artistName.setText(currentItem.getItemArtist());
            holder.mainView.setElevation(0);

            if(null == currentItem.getItemArtUrl())
                currentItem.setItemArtUrl(App.getPlayingQueueHandler().getUpNextList().getAlbumArtList().get(currentItem.getItemAlbum()));

            if(null == currentItem.getItemArtUrl())
                currentItem.setItemArtUrl(MediaItem.UNKNOWN_ART_URL);

            setAlbumArt(currentItem.getItemArtUrl(), holder);
            if (selectedHolder != null)
                selectedHolder.mainView.setBackgroundColor(ContextCompat
                        .getColor(activity, R.color.appBackground));
            selectedSongId = -1;
            selectedHolder = null;
            setOnClicks(holder, pos);
        } else if (position >= 1 && collection.getItemType() == BOOM_PLAYLIST) {
            int pos = position - 1;
            currentItem = (IMediaItem) collection.getMediaElement().get(pos);
            holder.undoButton.setVisibility(View.INVISIBLE);
            holder.name.setText(currentItem.getItemTitle());
            if(null == currentItem.getItemArtUrl())
                currentItem.setItemArtUrl(MediaItem.UNKNOWN_ART_URL);

            holder.mainView.setElevation(0);

            if(currentItem.getMediaType() == MediaType.DEVICE_MEDIA_LIB){
                holder.artistName.setText(currentItem.getItemArtist());
                if(null == currentItem.getItemArtUrl())
                    currentItem.setItemArtUrl(App.getPlayingQueueHandler().getUpNextList().getAlbumArtList().get(currentItem.getItemAlbum()));
            }

            setAlbumArt(currentItem.getItemArtUrl(), holder);

            if (selectedHolder != null)
                selectedHolder.mainView.setBackgroundColor(ContextCompat
                        .getColor(activity, R.color.appBackground));
            selectedSongId = -1;
            selectedHolder = null;

            setOnClicks(holder, pos);
            setDragHandle(holder);
        }

        if(position >= 1){
            updatePlayingTrack(holder, position);
        }
    }

    private void updatePlayingTrack(SimpleItemViewHolder holder, int position){
        IMediaItem nowPlayingItem = App.getPlayingQueueHandler().getUpNextList().getPlayingItem();
        boolean isMediaItem = (nowPlayingItem.getMediaType() == MediaType.DEVICE_MEDIA_LIB);
        if(null != nowPlayingItem ){
            if((isMediaItem && currentItem.getItemId() == nowPlayingItem.getItemId() )
                    || (!isMediaItem && currentItem.getItemTitle().equals(nowPlayingItem.getItemTitle()))){
                holder.name.setTextColor(ContextCompat.getColor(activity, R.color.track_selected_title));
                holder.art_overlay.setVisibility(View.VISIBLE);
                holder.art_overlay_play.setVisibility(View.VISIBLE);
                if(App.getPlayerEventHandler().isPlaying()){
                    holder.art_overlay_play.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_player_pause, null));
                    if(!isMediaItem)
                        holder.loadCloud.setVisibility(View.GONE);
                } else {
                    if(!isMediaItem && null != App.getPlayerEventHandler().getPlayer().getDataSource() && !App.getPlayerEventHandler().isPaused())
                        holder.loadCloud.setVisibility(View.VISIBLE);
                    holder.art_overlay_play.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_player_play, null));
                }
            }else{
                holder.name.setTextColor(ContextCompat.getColor(activity, R.color.track_title));
                holder.art_overlay.setVisibility(View.INVISIBLE);
                holder.art_overlay_play.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void setAlbumArt(String path, SimpleItemViewHolder holder) {
        if (PlayerUtils.isPathValid(path ))
            Picasso.with(activity).load(new File(path)).error(activity.getResources().getDrawable(R.drawable.ic_default_list, null))
                    .noFade().into(holder.img);
        else{
            setDefaultArt(holder);
        }
    }

    private void setDefaultArt(SimpleItemViewHolder holder) {

        holder.img.setImageDrawable(activity.getResources().getDrawable( R.drawable.ic_default_list, null));
    }

    private void setOnMenuClickListener(SimpleItemViewHolder holder, final int position) {
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
                                    if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                                        if (collection.getMediaElement().size() > 0)
                                            App.getPlayingQueueHandler().getUpNextList().addItemListToUpNextFrom(collection);
                                    } else {
                                        if (((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().size() > 0)
                                            App.getPlayingQueueHandler().getUpNextList().addItemListToUpNextFrom(collection.getMediaElement().get(collection.getCurrentIndex()));
                                    }
                                    break;
                                case R.id.album_header_add_to_upnext:
                                    if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                                        if (collection.getMediaElement().size() > 0)
                                            App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext(collection);
                                    } else {
                                        if (((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().size() > 0)
                                            App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext(collection.getMediaElement().get(collection.getCurrentIndex()));
                                    }
                                    break;
                                case R.id.album_header_add_to_playlist:
                                    Utils util = new Utils(activity);

                                    if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                                        if (collection.getMediaElement().size() > 0)
                                            util.addToPlaylist(activity, collection.getMediaElement(), null);
                                    } else {
                                        if (((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().size() > 0)
                                            util.addToPlaylist(activity, ((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement(), null);
                                    }
                                    break;
                                case R.id.album_header_shuffle:
                                    if (App.getPlayingQueueHandler().getUpNextList() != null) {
//                                    if(!App.getPlayerEventHandler().isPlaying() && !App.getPlayerEventHandler().isPaused()){
                                        if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                                            if (collection.getMediaElement().size() > 0)
                                                App.getPlayingQueueHandler().getUpNextList().addToPlay((ArrayList<IMediaItem>) collection.getMediaElement(), 0, false, true);
                                        } else {
                                            if (((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().size() > 0)
                                                App.getPlayingQueueHandler().getUpNextList().addToPlay((ArrayList<IMediaItem>) ((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement(), 0, false, true);
                                        }
//                                    }
                                        activity.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_SHUFFLE_SONG));
                                    }
                                    break;
                            }
                        }catch (Exception E){
                            Log.e("Error : ", E.getMessage());
                        }
                        return false;
                    }
                });
                pm.inflate(R.menu.album_header_menu);
                pm.show();
            }
        });
    }

    private void setOnClicks(final SimpleItemViewHolder holder, final int position) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animate(holder);
                if(App.getPlayingQueueHandler().getUpNextList()!=null){
                    if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                        App.getPlayingQueueHandler().getUpNextList().addToPlay((ArrayList<IMediaItem>) collection.getMediaElement(), position, false, false);
                    }else{
                        App.getPlayingQueueHandler().getUpNextList().addToPlay((ArrayList<IMediaItem>) ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement(), position, false, false);
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    }, 200);
                }
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
                                        if (App.getPlayingQueueHandler().getUpNextList() != null) {
                                            if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                                                App.getPlayingQueueHandler().getUpNextList().addItemToUpNextFrom(collection.getMediaElement().get(position));
                                            } else {
                                                App.getPlayingQueueHandler().getUpNextList().addItemToUpNextFrom(((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().get(position));
                                            }
                                        }
                                        break;
                                    case R.id.popup_song_add_queue:
                                        if (App.getPlayingQueueHandler().getUpNextList() != null) {
                                            if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                                                App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext((IMediaItem) collection.getMediaElement().get(position));
                                            } else {
                                                App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext((IMediaItem) ((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().get(position));
                                            }
                                        }
                                        break;
                                    case R.id.popup_song_add_playlist:
                                        Utils util = new Utils(activity);
                                        ArrayList list = new ArrayList();
                                        if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                                            list.add(collection.getMediaElement().get(position));
                                            util.addToPlaylist(activity, list, null);
                                        } else {
                                            list.add(((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().get(position));
                                            util.addToPlaylist(activity, list, null);
                                        }
                                        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);
                                        break;
                                    case R.id.popup_song_add_fav:
                                        if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                                            if(collection.getMediaElement().get(position).getMediaType() == MediaType.DEVICE_MEDIA_LIB) {
                                                if (MediaController.getInstance(activity).isFavouriteItems(collection.getMediaElement().get(position).getItemId())) {
                                                    MediaController.getInstance(activity).removeItemToFavoriteList(collection.getMediaElement().get(position).getItemId());
                                                    Toast.makeText(activity, activity.getResources().getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show();
                                                } else {
                                                    MediaController.getInstance(activity).addSongsToFavoriteList(collection.getMediaElement().get(position));
                                                    Toast.makeText(activity, activity.getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                                                }
                                            }else{
                                                if(MediaController.getInstance(activity).isFavouriteItems(collection.getMediaElement().get(position).getItemTitle())){
                                                    MediaController.getInstance(activity).removeItemToFavoriteList(collection.getMediaElement().get(position).getItemTitle());
                                                    Toast.makeText(activity, activity.getResources().getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show();
                                                }else{
                                                    MediaController.getInstance(activity).addSongsToFavoriteList(collection.getMediaElement().get(position));
                                                    Toast.makeText(activity, activity.getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        } else {
                                            if (MediaController.getInstance(activity).isFavouriteItems(((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().get(position).getItemId())) {
                                                MediaController.getInstance(activity).removeItemToFavoriteList(((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().get(position).getItemId());
                                                Toast.makeText(activity, activity.getResources().getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show();
                                            } else {
                                                MediaController.getInstance(activity).addSongsToFavoriteList(((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().get(position));
                                                Toast.makeText(activity, activity.getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        break;
                                    case R.id.boom_header_delete_songs:
                                        if (collection.getItemType() == BOOM_PLAYLIST) {
                                            if(collection.getMediaElement().get(position).getMediaType() == MediaType.DEVICE_MEDIA_LIB) {
                                                App.getBoomPlayListHelper().removeSong(collection.getMediaElement().get(position).getItemId(), (int) collection.getItemId());
                                            }else{
                                                App.getBoomPlayListHelper().removeSong(collection.getMediaElement().get(position).getItemTitle(), (int) collection.getItemId());
                                            }
                                            collection.getMediaElement().clear();
                                            collection.setMediaElement(MediaController.getInstance(activity).getMediaCollectionItemDetails(collection));
                                            collection.setItemCount(collection.getMediaElement().size());
                                            notifyItemRemoved(position);
                                            setDetail(collection.getItemTitle(), collection.getItemCount());
                                            notifyDataSetChanged();
                                            ((AlbumSongListActivity)activity).updateAlbumArt(collection);
                                        }
                                        break;
                                }
                            }catch (Exception e){

                            }
                            return false;
                        }
                    });
                if(collection.getItemType() != BOOM_PLAYLIST) {
                    if (collection.getItemType() == PLAYLIST) {
                        if (MediaController.getInstance(activity).isFavouriteItems(collection.getMediaElement().get(position).getItemId())) {
                            pm.inflate(R.menu.song_remove_fav);
                        } else {
                            pm.inflate(R.menu.song_add_fav);
                        }
                    } else {
                        if (MediaController.getInstance(activity).isFavouriteItems(((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().get(position).getItemId())) {
                            pm.inflate(R.menu.song_remove_fav);
                        } else {
                            pm.inflate(R.menu.song_add_fav);
                        }
                    }
                }else{
                    if(collection.getMediaElement().get(position).getMediaType() == MediaType.DEVICE_MEDIA_LIB) {
                        if (MediaController.getInstance(activity).isFavouriteItems(collection.getMediaElement().get(position).getItemId())) {
                            pm.inflate(R.menu.boom_playlist_song_remove_fav);
                        } else {
                            pm.inflate(R.menu.boom_playlist_song_add_fav);
                        }
                    }else {
                        if(MediaController.getInstance(activity).isFavouriteItems(collection.getMediaElement().get(position).getItemTitle())){
                            pm.inflate(R.menu.boom_playlist_song_remove_fav);
                        } else {
                            pm.inflate(R.menu.boom_playlist_song_add_fav);
                        }
                    }



                }
                    pm.show();
            }
        });
    }

    private void setDetail(String title, int count) {
        StringBuilder itemCount = new StringBuilder();
        itemCount.append(count > 1 ? activity.getResources().getString(R.string.songs): activity.getResources().getString(R.string.song));
        itemCount.append(" ").append(count);
        listDetail = new ListDetail(title, itemCount.toString(), null);
    }

    public void setDragHandle(final SimpleItemViewHolder holder) {

        holder.imgHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN || MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mOnStartDragListener.onStartDrag(holder);
                }
                return false;
            }
        });

    }
    public void updateNewList(IMediaItemCollection collections, ListDetail listDetail, int startPosition) {
//        this.collection.getMediaElement().clear();
        this.collection.setMediaElement(collections.getMediaElement());
        this.listDetail = listDetail;
    }

    public void animate(final SimpleItemViewHolder holder) {
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
    public void onViewRecycled(SimpleItemViewHolder holder) {
        super.onViewRecycled(holder);
//        holder.img.setImageDrawable(null);
    }

    @Override
    public int getItemCount() {
        return collection.getItemCount()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position < 1){
            return TYPE_HEADER;
        }else{
            return TYPE_ITEM;
        }
    }

    private ValueAnimator animateElevation(int from, int to, final SimpleItemViewHolder holder) {
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
        } else {
            if (activity != null && collection.getItemType() == SONGS)
                fragment.killActivity();
        }
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public RegularTextView name, artistName;
        public View mainView, art_overlay;
        public ImageView img, menu, art_overlay_play;
        public ProgressBar loadCloud;

        public RegularTextView headerTitle, headerSubTitle, headerDetail;
        public Button undoButton;
        public ImageView imgHandle;
        ImageView mShuffle, mMore;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            img = (ImageView) itemView.findViewById(R.id.song_item_img);
            art_overlay_play = (ImageView) itemView.findViewById(R.id.song_item_img_overlay_play);
            art_overlay = itemView.findViewById(R.id.song_item_img_overlay);
            loadCloud = (ProgressBar) itemView.findViewById(R.id.load_cloud );
            name = (RegularTextView) itemView.findViewById(R.id.song_item_name);
            menu = (ImageView) itemView.findViewById(R.id.song_item_menu);
            artistName = (RegularTextView) itemView.findViewById(R.id.song_item_artist);
            undoButton = (Button) itemView.findViewById(R.id.undo_button);
            imgHandle = (ImageView) itemView.findViewById(R.id.song_item_handle);
            headerTitle = (RegularTextView) itemView.findViewById(R.id.header_title);
            headerSubTitle = (RegularTextView) itemView.findViewById(R.id.header_sub_title);
            headerDetail = (RegularTextView) itemView.findViewById(R.id.header_detail);
            mMore = (ImageView) itemView.findViewById(R.id.recycler_header_menu);
        }
    }

}
