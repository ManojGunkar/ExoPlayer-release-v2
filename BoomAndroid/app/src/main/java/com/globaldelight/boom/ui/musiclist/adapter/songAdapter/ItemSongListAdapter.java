package com.globaldelight.boom.ui.musiclist.adapter.songAdapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.globaldelight.boom.App;
import com.globaldelight.boom.Media.ItemType;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.analytics.UtilAnalytics;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.Media.MediaType;
import com.globaldelight.boom.ui.musiclist.ListDetail;
import com.globaldelight.boom.ui.musiclist.fragment.AlbumSongListFragment;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.OnStartDragListener;
import com.globaldelight.boom.utils.OverFlowMenuUtils;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.async.Action;
import com.squareup.picasso.Picasso;

import java.io.File;

import static com.globaldelight.boom.Media.ItemType.BOOM_PLAYLIST;
import static com.globaldelight.boom.Media.ItemType.PLAYLIST;
import static com.globaldelight.boom.Media.ItemType.SONGS;

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

            setOnMenuClickListener(holder);
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

            holder.mainView.setElevation(0);

            holder.artistName.setVisibility(null != currentItem.getItemArtist() ? View.VISIBLE : View.GONE);
            holder.artistName.setText(currentItem.getItemArtist());

            setAlbumArt(App.getPlayingQueueHandler().getUpNextList().getAlbumArtList().get(currentItem.getItemAlbum()), holder);

            if (selectedHolder != null)
                selectedHolder.mainView.setBackgroundColor(ContextCompat
                        .getColor(activity, R.color.appBackground));
            selectedSongId = -1;
            selectedHolder = null;

            setOnClicks(holder, pos);
            setDragHandle(holder);
        }

        if(position >= 1){
            updatePlayingTrack(holder);
        }
    }

    private void updatePlayingTrack(SimpleItemViewHolder holder){
        IMediaItemBase nowPlayingItem = App.getPlayingQueueHandler().getUpNextList().getPlayingItem();
        if(null != nowPlayingItem ){
            boolean isMediaItem = (nowPlayingItem.getMediaType() == MediaType.DEVICE_MEDIA_LIB);
            if(currentItem.getItemId() == nowPlayingItem.getItemId()){
                holder.name.setTextColor(ContextCompat.getColor(activity, R.color.track_selected_title));
                holder.art_overlay.setVisibility(View.VISIBLE);
                holder.art_overlay_play.setVisibility(View.VISIBLE);
                holder.loadCloud.setVisibility(View.GONE);
                if(App.getPlayerEventHandler().isPlaying()){
                    holder.art_overlay_play.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_player_pause, null));
                } else {
                    if(!isMediaItem && App.getPlayerEventHandler().isTrackWaitingForPlay() && !App.getPlayerEventHandler().isPaused())
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
        if ( path == null ) path = "";
        Picasso.with(activity)
                .load(new File(path))
                .placeholder(R.drawable.ic_default_art_grid)
                .noFade()
                .into(holder.img);
    }

    private void setOnMenuClickListener(SimpleItemViewHolder holder) {

        holder.mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchorView) {

                if(collection.getItemType() == BOOM_PLAYLIST){
                    OverFlowMenuUtils.setBoomPlayListHeaderMenu(activity, anchorView, collection.getItemId(), collection.getItemTitle(), collection.getMediaElement());
                }else if(collection.getItemType() == PLAYLIST){
                    OverFlowMenuUtils.setPlayListHeaderMenu(activity, anchorView, collection.getMediaElement());
                }else{
                    OverFlowMenuUtils.setArtistGenreSongHeaderMenu(activity, anchorView, ((IMediaItemCollection) collection.getMediaElement().
                            get(collection.getCurrentIndex())).getMediaElement());
                }
            }
        });
    }

    private void setOnClicks(final SimpleItemViewHolder holder, final int position) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animate(holder);
                if(currentItem.getItemType()== ItemType.BOOM_PLAYLIST){
                    FlurryAnalyticHelper.logEvent(UtilAnalytics.Tapped_from_Boom_playlist_Thumbnail);
                }else if(currentItem.getItemType()== ItemType.PLAYLIST){
                    FlurryAnalyticHelper.logEvent(UtilAnalytics.Tapped_from_playlist_Thumbnail);
                }
//                FlurryAnalyticHelper.logEvent(UtilAnalytics.Music_played_from_playlist_section);
                if (currentItem.getItemType() == ItemType.ARTIST) {
                    FlurryAnalyticHelper.logEvent(UtilAnalytics.Tapped_from_ARTIST_AllSongs_Thumbnail);
                } else if (currentItem.getItemType() == ItemType.GENRE) {
                    FlurryAnalyticHelper.logEvent(UtilAnalytics.Tapped_from_GENERE_AllSongs_Thumbnail);
                }
                if (!App.getPlayerEventHandler().isTrackLoading()) {
                    if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                        App.getPlayingQueueHandler().getUpNextList().addItemListToPlay(collection.getMediaElement(), position);
                    }else{
                        App.getPlayingQueueHandler().getUpNextList().addItemListToPlay(((IMediaItemCollection) collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement(), position);
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            notifyDataSetChanged();
                        }
                    }, 500);
                }
            }
        });
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchorView) {
                if(collection.getItemType() == BOOM_PLAYLIST){
                    OverFlowMenuUtils.setBoomPlayListItemMenu(activity, anchorView, collection.getItemId(), collection.getMediaElement().get(position));
                }else if(collection.getItemType() == PLAYLIST){
                    OverFlowMenuUtils.setPlayListItemMenu(activity, anchorView, collection.getMediaElement().get(position));
                }else{
                    OverFlowMenuUtils.setArtistGenreSongItemMenu(activity, anchorView, ((IMediaItemCollection) collection.getMediaElement()
                            .get(collection.getCurrentIndex())).getMediaElement().get(position));
                }
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
    public void updateNewList(IMediaItemCollection collections, ListDetail listDetail) {
        this.collection = (MediaItemCollection) collections;
        this.listDetail = listDetail;
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
        if(collection.getItemType() == BOOM_PLAYLIST || collection.getItemType() == PLAYLIST)
            return collection.getMediaElement().size() + 1;
        else
            return ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().size() + 1;
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
        public ImageView img, art_overlay_play;
        public LinearLayout menu;
        public ProgressBar loadCloud;

        public RegularTextView headerSubTitle, headerDetail;
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
            menu = (LinearLayout) itemView.findViewById(R.id.song_item_overflow_menu);
            artistName = (RegularTextView) itemView.findViewById(R.id.song_item_artist);
            undoButton = (Button) itemView.findViewById(R.id.undo_button);
            imgHandle = (ImageView) itemView.findViewById(R.id.song_item_handle);
            headerSubTitle = (RegularTextView) itemView.findViewById(R.id.header_sub_title);
            headerDetail = (RegularTextView) itemView.findViewById(R.id.header_detail);
            mMore = (ImageView) itemView.findViewById(R.id.recycler_header_menu);
        }
    }

}
