package com.globaldelight.boom.ui.musiclist.adapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.task.PlayerService;
import com.globaldelight.boom.ui.musiclist.ListDetail;
import com.globaldelight.boom.ui.musiclist.activity.DeviceMusicActivity;
import com.globaldelight.boom.ui.widgets.CoachMarkTextView;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.OnStartDragListener;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.async.Action;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import static com.globaldelight.boom.data.MediaLibrary.ItemType.BOOM_PLAYLIST;
import static com.globaldelight.boom.data.MediaLibrary.ItemType.PLAYLIST;
import static com.globaldelight.boom.data.MediaLibrary.ItemType.SONGS;


public class ItemSongListAdapter extends RecyclerView.Adapter<ItemSongListAdapter.SimpleItemViewHolder> {

    public static final int TYPE_HEADER = 111;
    public static final int TYPE_ITEM = 222;
    private static final String TAG = "ItemSongListAdapter-TAG";
    OnStartDragListener mOnStartDragListener;
    private MediaItemCollection collection;
    private PermissionChecker permissionChecker;
    private int selectedSongId = -1;
    private SimpleItemViewHolder selectedHolder;
    private Activity activity;
    private MediaItem currentItem;
    private ListDetail listDetail;

    public ItemSongListAdapter(Activity activity, IMediaItemCollection collection, ListDetail listDetail, PermissionChecker permissionChecker, OnStartDragListener dragListener) {
        this.activity = activity;
        this.collection = (MediaItemCollection) collection;
        this.permissionChecker = permissionChecker;
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
                    inflate(R.layout.recycler_view_header, parent, false);
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

            if(App.getUserPreferenceHandler().isLibFromHome()){
                holder.mMore.setVisibility(View.VISIBLE);
            }else{
                holder.mMore.setVisibility(View.INVISIBLE);
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
            setAlbumArt(currentItem.getItemArtUrl(), holder);
            if (selectedHolder != null)
                selectedHolder.mainView.setBackgroundColor(ContextCompat
                        .getColor(activity, R.color.appBackground));
            selectedSongId = -1;
            selectedHolder = null;

            if(App.getUserPreferenceHandler().isLibFromHome()){
                holder.menu.setVisibility(View.VISIBLE);
                holder.songChk.setVisibility(View.GONE);
                setOnClicks(holder, pos);
            }else{
                holder.menu.setVisibility(View.GONE);
                holder.songChk.setVisibility(View.VISIBLE);
                setOnCheckedChanged(holder, pos);
            }
        } else if (position >= 1 && collection.getItemType() == BOOM_PLAYLIST) {
            int pos = position - 1;
            currentItem = (MediaItem) collection.getMediaElement().get(pos);
            holder.undoButton.setVisibility(View.INVISIBLE);
            holder.name.setText(currentItem.getItemTitle());
            holder.artistName.setText(currentItem.getItemArtist());
            holder.mainView.setElevation(0);
            setAlbumArt(currentItem.getItemArtUrl(), holder);
            if (selectedHolder != null)
                selectedHolder.mainView.setBackgroundColor(ContextCompat
                        .getColor(activity, R.color.appBackground));
            selectedSongId = -1;
            selectedHolder = null;

            if(App.getUserPreferenceHandler().isLibFromHome()){
                holder.menu.setVisibility(View.VISIBLE);
                holder.songChk.setVisibility(View.GONE);
                setOnClicks(holder, pos);
            }else{
                holder.menu.setVisibility(View.GONE);
                holder.songChk.setVisibility(View.VISIBLE);
                setOnCheckedChanged(holder, pos);
            }
            setDragHandle(holder);
        }
    }

    private void setOnCheckedChanged(SimpleItemViewHolder holder, final int pos) {

        if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
            if(App.getUserPreferenceHandler().getItemIDList().contains(collection.getMediaElement().get(pos).getItemId())){
                holder.songChk.setChecked(true);
            }else {
                holder.songChk.setChecked(false);
            }
        }else{
            if(App.getUserPreferenceHandler().getItemIDList().contains(((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().get(pos).getItemId())){
                holder.songChk.setChecked(true);
            }else {
                holder.songChk.setChecked(false);
            }
        }
        holder.songChk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                    App.getUserPreferenceHandler().addItemToPlayList((MediaItem) collection.getMediaElement().get(pos));
                }else{
                    App.getUserPreferenceHandler().addItemToPlayList((MediaItem) ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().get(pos));
                }
            }
        });
    }

    private void setAlbumArt(String path, SimpleItemViewHolder holder) {
        if (path != null && !path.equals("null"))
            Picasso.with(activity).load(new File(path)).error(activity.getResources().getDrawable(R.drawable.ic_default_list, null))
                    .noFade().resize(dpToPx(90), dpToPx(90)).centerCrop().into(holder.img);
        else{
            setDefaultArt(holder, dpToPx(90));
        }
    }

    private void setDefaultArt(SimpleItemViewHolder holder, int size) {

        holder.img.setImageBitmap(Utils.getBitmapOfVector(activity, R.drawable.ic_default_list,
                size, size));
    }

    private void setOnMenuClickListener(SimpleItemViewHolder holder, int position) {
        holder.mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchorView) {
                PopupMenu pm = new PopupMenu(activity, anchorView);
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.album_header_add_play_next:
                                if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                                    if(collection.getMediaElement().size() > 0)
                                        App.getPlayingQueueHandler().getUpNextList().addItemListToUpNextFrom(collection.getMediaElement());
                                }else{
                                    if(((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().size() > 0)
                                    App.getPlayingQueueHandler().getUpNextList().addItemListToUpNextFrom(((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement());
                                }
                                break;
                            case R.id.album_header_add_to_upnext :
                                if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                                    if(collection.getMediaElement().size() > 0)
                                        App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext(collection);
                                }else{
                                    if(((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().size() > 0)
                                        App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext(collection.getMediaElement().get(collection.getCurrentIndex()));
                                }
                                break;
                            case R.id.album_header_add_to_playlist:
                                Utils util = new Utils(activity);

                                if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                                    if(collection.getMediaElement().size() > 0)
                                        util.addToPlaylist(activity, collection.getMediaElement(), null);
                                }else{
                                    if(((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().size() > 0)
                                        util.addToPlaylist(activity, ((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement(), null);
                                }
                                break;
                            case R.id.album_header_shuffle :
                                if (App.getPlayingQueueHandler().getUpNextList() != null) {
//                                    if(!App.getPlayerEventHandler().isPlaying() && !App.getPlayerEventHandler().isPaused()){
                                    if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                                        if(collection.getMediaElement().size() > 0)
                                            App.getPlayingQueueHandler().getUpNextList().addToPlay((ArrayList<MediaItem>) collection.getMediaElement(), 0);
                                    }else{
                                        if(((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().size() > 0)
                                        App.getPlayingQueueHandler().getUpNextList().addToPlay((ArrayList<MediaItem>) ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement(), 0);
                                    }
//                                    }
                                    activity.sendBroadcast(new Intent(PlayerService.ACTION_SHUFFLE_SONG));
                                }
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

    private void setOnClicks(final SimpleItemViewHolder holder, final int position) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animate(holder);
                if(App.getPlayingQueueHandler().getUpNextList()!=null){
                    if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                        App.getPlayingQueueHandler().getUpNextList().addToPlay((ArrayList<MediaItem>) collection.getMediaElement(), position);
                    }else{
                        App.getPlayingQueueHandler().getUpNextList().addToPlay((ArrayList<MediaItem>) ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement(), position);
                    }
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
                            switch (item.getItemId()) {
                                case R.id.popup_song_add_queue:
                                    if (App.getPlayingQueueHandler().getUpNextList() != null) {
                                        if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                                            App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext((MediaItem) collection.getMediaElement().get(position));
                                        } else {
                                            App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext((MediaItem) ((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().get(position));
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
                                        if (MediaController.getInstance(activity).isFavouriteItems(collection.getMediaElement().get(position).getItemId())) {
                                            MediaController.getInstance(activity).removeItemToFavoriteList(collection.getMediaElement().get(position).getItemId());
                                        } else {
                                            MediaController.getInstance(activity).addSongsToFavoriteList(collection.getMediaElement().get(position));
                                        }
                                    } else {
                                        if (MediaController.getInstance(activity).isFavouriteItems(collection.getMediaElement().get(position).getItemId())) {
                                            MediaController.getInstance(activity).removeItemToFavoriteList(((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().get(position).getItemId());
                                        } else {
                                            MediaController.getInstance(activity).addSongsToFavoriteList(((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().get(position));
                                        }
                                    }
                                    break;
                            }
                            return false;
                        }
                    });
                    if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
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
                    pm.show();
            }
        });
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
    public void updateNewList(IMediaItemCollection collection) {
        this.collection = (MediaItemCollection) collection;
        notifyDataSetChanged();
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
                ((DeviceMusicActivity) activity).killActivity();
        }
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public TextView name, artistName;
        public View mainView;
        public ImageView img, menu;

        public CheckBox songChk;
        public RegularTextView headerTitle, headerSubTitle;
        public CoachMarkTextView headerDetail;
        public Button undoButton;
        public ImageView imgHandle;
        ImageView mShuffle, mMore;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            img = (ImageView) itemView.findViewById(R.id.song_item_img);
            name = (TextView) itemView.findViewById(R.id.song_item_name);
            menu = (ImageView) itemView.findViewById(R.id.song_item_menu);
            artistName = (TextView) itemView.findViewById(R.id.song_item_artist);
            undoButton = (Button) itemView.findViewById(R.id.undo_button);
            imgHandle = (ImageView) itemView.findViewById(R.id.song_item_handle);
            headerTitle = (RegularTextView) itemView.findViewById(R.id.header_title);
            headerSubTitle = (RegularTextView) itemView.findViewById(R.id.header_sub_title);
            headerDetail = (CoachMarkTextView) itemView.findViewById(R.id.header_detail);
            mMore = (ImageView) itemView.findViewById(R.id.recycler_header_menu);
            songChk = (CheckBox) itemView.findViewById(R.id.song_chk);
        }
    }

}
