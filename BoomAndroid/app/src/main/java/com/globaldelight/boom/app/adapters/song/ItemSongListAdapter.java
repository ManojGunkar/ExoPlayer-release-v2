package com.globaldelight.boom.app.adapters.song;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.collection.base.IMediaElement;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.base.IMediaItem;
import com.globaldelight.boom.collection.base.IMediaItemCollection;
import com.globaldelight.boom.playbackEvent.utils.MediaType;
import com.globaldelight.boom.app.adapters.model.ListDetail;
import com.globaldelight.boom.app.fragments.AlbumSongListFragment;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.OnStartDragListener;
import com.globaldelight.boom.utils.OverFlowMenuUtils;

import static com.globaldelight.boom.playbackEvent.utils.ItemType.BOOM_PLAYLIST;

/**
 * Created by Rahul Agarwal on 8/8/2016.
 */

public class ItemSongListAdapter extends RecyclerView.Adapter<ItemSongListAdapter.SimpleItemViewHolder> {

    public static final int TYPE_HEADER = 111;
    public static final int TYPE_ITEM = 222;
    private static final String TAG = "ItemSongListAdapter-TAG";
    private OnStartDragListener mOnStartDragListener;
    private IMediaItemCollection collection;
    private int selectedSongId = -1;
    private SimpleItemViewHolder selectedHolder;
    private Activity activity;
    private ListDetail listDetail;
    private AlbumSongListFragment fragment;


    public ItemSongListAdapter(Activity activity, AlbumSongListFragment fragment, IMediaItemCollection collection, ListDetail listDetail, OnStartDragListener dragListener) {
        this.activity = activity;
        this.fragment = fragment;
        this.collection = collection;
        this.listDetail = listDetail;
        this.mOnStartDragListener = dragListener;
    }
    @Override
    public ItemSongListAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if ( viewType == TYPE_HEADER ) {
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.card_header_recycler_view, parent, false);
        }
        else {
            int layoutResId = collection.getItemType() == BOOM_PLAYLIST ? R.layout.card_boomplaylist_song_item : R.layout.card_song_item;
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(layoutResId, parent, false);

        }

        SimpleItemViewHolder holder = new SimpleItemViewHolder(itemView);
        return holder;
    }

    @Override
    public int getItemCount() {
        return collection.count() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position < 1){
            return TYPE_HEADER;
        }else{
            return TYPE_ITEM;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final ItemSongListAdapter.SimpleItemViewHolder holder, final int position) {

        if(position < 1){
            bindHeader(holder);
        } else if ( collection.getItemType() == BOOM_PLAYLIST ) {
            bindBoomPlaylist(holder, position-1);
        }
        else {
            bindSongs(holder, position-1);
        }

        if(position >= 1){
            updatePlayingTrack(holder, position-1);
        }
    }

    private void bindHeader(final ItemSongListAdapter.SimpleItemViewHolder holder) {
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
    }

    private void bindSongs(final ItemSongListAdapter.SimpleItemViewHolder holder, final int position) {
        IMediaItem currentItem = (MediaItem) collection.getItemAt(position);

        holder.name.setText(currentItem.getTitle());
        holder.artistName.setText(currentItem.getItemArtist());
        holder.itemView.setElevation(0);

        setAlbumArt(currentItem.getItemArtUrl(), holder);
        if (selectedHolder != null)
            selectedHolder.itemView.setBackgroundColor(ContextCompat
                    .getColor(activity, R.color.appBackground));
        selectedSongId = -1;
        selectedHolder = null;
        setOnClicks(holder, position);
    }

    private void bindBoomPlaylist(final ItemSongListAdapter.SimpleItemViewHolder holder, final int position) {
        IMediaItem currentItem = (IMediaItem) collection.getItemAt(position);
        holder.undoButton.setVisibility(View.INVISIBLE);
        holder.name.setText(currentItem.getTitle());
        holder.itemView.setElevation(0);
        holder.artistName.setVisibility(null != currentItem.getItemArtist() ? View.VISIBLE : View.GONE);
        holder.artistName.setText(currentItem.getItemArtist());

        setAlbumArt(currentItem.getItemArtUrl(), holder);

        if (selectedHolder != null)
            selectedHolder.itemView.setBackgroundColor(ContextCompat
                    .getColor(activity, R.color.appBackground));
        selectedSongId = -1;
        selectedHolder = null;

        setOnClicks(holder, position);
        setDragHandle(holder);
    }

    private void updatePlayingTrack(SimpleItemViewHolder holder, int position){
        IMediaItem currentItem = (IMediaItem) collection.getItemAt(position);
        IMediaElement nowPlayingItem = App.playbackManager().queue().getPlayingItem();
        if(null != nowPlayingItem ){
            boolean isMediaItem = (nowPlayingItem.getMediaType() == MediaType.DEVICE_MEDIA_LIB);
            if(currentItem.equalTo(nowPlayingItem)){
                holder.name.setSelected(true);
                holder.art_overlay.setVisibility(View.VISIBLE);
                holder.art_overlay_play.setVisibility(View.VISIBLE);
                holder.loadCloud.setVisibility(View.GONE);
                if( App.playbackManager().isTrackPlaying() ){
                    holder.art_overlay_play.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_player_pause, null));
                    if(!isMediaItem && App.playbackManager().isTrackLoading())
                        holder.loadCloud.setVisibility(View.VISIBLE);
                } else {
                    holder.art_overlay_play.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_player_play, null));
                }
            }else{
                holder.name.setSelected(false);
                holder.loadCloud.setVisibility(View.GONE);
                holder.art_overlay.setVisibility(View.GONE);
                holder.art_overlay_play.setVisibility(View.GONE);
            }
        }
        else {
            holder.name.setSelected(false);
            holder.loadCloud.setVisibility(View.GONE);
            holder.art_overlay.setVisibility(View.GONE);
            holder.art_overlay_play.setVisibility(View.GONE);
        }
    }

    private void setAlbumArt(String path, SimpleItemViewHolder holder) {
        final int size = Utils.smallImageSize(activity);
        Glide.with(activity)
                .load(path)
                .placeholder(R.drawable.ic_default_art_grid)
                .centerCrop()
                .override(size, size)
                .into(holder.img);
    }

    private void setOnMenuClickListener(SimpleItemViewHolder holder) {
        holder.mMore.setOnClickListener(this::onHeaderMenuClicked);
    }

    private void setOnClicks(final SimpleItemViewHolder holder, final int position) {
        holder.itemView.setOnClickListener((view)->onItemClicked(view, holder, position));
        holder.menu.setOnClickListener((view)->onItemMenuClicked(view, position));
    }

    private void onHeaderMenuClicked(View view) {
        if(collection.getItemType() == BOOM_PLAYLIST){
            OverFlowMenuUtils.showCollectionMenu(activity, view, R.menu.boomplaylist_header_menu, collection);
        }else{
            OverFlowMenuUtils.showCollectionMenu(activity, view, R.menu.collection_header_popup, collection);
        }
    }

    private void onItemClicked(View view, SimpleItemViewHolder holder, int position) {
        animate(holder);
        switch (collection.getParentType()) {
            case ItemType.BOOM_PLAYLIST:
                FlurryAnalytics.getInstance(activity.getApplicationContext()).setEvent(FlurryEvents.Tapped_from_Boom_playlist_Thumbnail);
                break;
            case ItemType.PLAYLIST:
                FlurryAnalytics.getInstance(activity.getApplicationContext()).setEvent(FlurryEvents.Tapped_from_playlist_Thumbnail);
                break;
            case ItemType.ARTIST:
                FlurryAnalytics.getInstance(activity.getApplicationContext()).setEvent(FlurryEvents.Tapped_from_ARTIST_AllSongs_Thumbnail);
                break;
            case ItemType.GENRE:
                FlurryAnalytics.getInstance(activity.getApplicationContext()).setEvent(FlurryEvents.Tapped_from_GENERE_AllSongs_Thumbnail);
                break;

        }

        App.playbackManager().queue().addItemListToPlay(collection, position);
    }

    private void onItemMenuClicked(View view, int position) {
        switch ( collection.getItemType() ) {
            case BOOM_PLAYLIST:
                OverFlowMenuUtils.showPlaylistItemMenu(activity, view, R.menu.boomplaylist_item_menu, collection.getItemAt(position), collection);
                break;
            default:
                OverFlowMenuUtils.showMediaItemMenu(activity, view, R.menu.media_item_popup, collection.getItemAt(position));
                break;
        }
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
        this.collection = collections;
        this.listDetail = listDetail;
        notifyDataSetChanged();
    }

    public void animate(final SimpleItemViewHolder holder) {
        animateElevation(0, dpToPx(10), holder);
        animateElevation(dpToPx(10), 0, holder);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
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
                        holder.itemView.setElevation(
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
            selectedHolder.itemView.setBackgroundColor(ContextCompat
                    .getColor(activity, R.color.appBackground));
        }
    }

    public static class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public TextView name, artistName;
        public View art_overlay;
        public ImageView img, art_overlay_play;
        public LinearLayout menu;
        public ProgressBar loadCloud;

        public TextView headerSubTitle, headerDetail;
        public Button undoButton;
        public ImageView imgHandle;
        ImageView mMore;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.song_item_img);
            art_overlay_play = (ImageView) itemView.findViewById(R.id.song_item_img_overlay_play);
            art_overlay = itemView.findViewById(R.id.song_item_img_overlay);
            loadCloud = (ProgressBar) itemView.findViewById(R.id.load_cloud );
            name = (TextView) itemView.findViewById(R.id.song_item_name);
            menu = (LinearLayout) itemView.findViewById(R.id.song_item_overflow_menu);
            artistName = (TextView) itemView.findViewById(R.id.song_item_artist);
            undoButton = (Button) itemView.findViewById(R.id.undo_button);
            imgHandle = (ImageView) itemView.findViewById(R.id.song_item_handle);
            headerSubTitle = (TextView) itemView.findViewById(R.id.header_sub_title);
            headerDetail = (TextView) itemView.findViewById(R.id.header_detail);
            mMore = (ImageView) itemView.findViewById(R.id.recycler_header_menu);
        }
    }

}
