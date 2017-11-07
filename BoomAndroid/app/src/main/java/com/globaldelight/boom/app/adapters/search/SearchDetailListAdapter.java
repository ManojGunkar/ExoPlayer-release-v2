package com.globaldelight.boom.app.adapters.search;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.app.activities.AlbumDetailActivity;
import com.globaldelight.boom.app.activities.AlbumDetailItemActivity;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.collection.local.callback.IMediaItemCollection;
import com.globaldelight.boom.app.adapters.search.utils.SearchResult;
import com.globaldelight.boom.utils.OverFlowMenuUtils;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.async.Action;
import com.globaldelight.boom.R;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 14-11-16.
 */

public class SearchDetailListAdapter extends RecyclerView.Adapter<SearchDetailListAdapter.SimpleItemViewHolder> {
    private Context context;
    private Activity activity;
    private ArrayList<? extends IMediaItemBase> resultItemList;
    private String mResultType;
    public static final int TYPE_ROW = 111;
    public static final int TYPE_GRID = 222;
    private boolean isPhone;

    public SearchDetailListAdapter(Activity searchDetailListActivity, ArrayList<? extends IMediaItemBase> resultItemList, String mResultType, boolean isPhone) {
        this.context = searchDetailListActivity;
        activity = searchDetailListActivity;
        this.resultItemList = resultItemList;
        this.mResultType = mResultType;
        this.isPhone = isPhone;
    }

    @Override
    public SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if(viewType == TYPE_ROW) {
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.card_song_item, parent, false);
        }else{
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.card_grid_item, parent, false);
        }
        return new SearchDetailListAdapter.SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SimpleItemViewHolder holder, final int position) {
        if(mResultType.equals(SearchResult.SONGS)){
            holder.name.setText(resultItemList.get(position).getItemTitle());
            holder.artistName.setText(((MediaItem)resultItemList.get(position)).getItemArtist());
            holder.mainView.setElevation(0);
            if(null == resultItemList.get(position).getItemArtUrl())
                resultItemList.get(position).setItemArtUrl(App.playbackManager().queue().getAlbumArtList().get(((MediaItem) resultItemList.get(position)).getItemAlbum()));

            if(null == resultItemList.get(position).getItemArtUrl())
                resultItemList.get(position).setItemArtUrl(MediaItem.UNKNOWN_ART_URL);

            setSongArt(resultItemList.get(position).getItemArtUrl(), holder);

            updatePlayingTrack(holder, resultItemList.get(position).getItemId());
            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    animate(holder);
                    App.playbackManager().queue().addItemToPlay(resultItemList.get(position));
                }
            });

            holder.menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View anchorView) {
                    OverFlowMenuUtils.showMediaItemMenu((Activity)context, anchorView, R.menu.media_item_popup, resultItemList.get(position));
                }
            });
        }else if(mResultType.equals(SearchResult.ALBUMS)){
            final MediaItemCollection theCollection = (MediaItemCollection) resultItemList.get(position);
            holder.defaultImg.setVisibility(View.VISIBLE);
            holder.title.setText(theCollection.getItemTitle());
            holder.subTitle.setText(theCollection.getItemSubTitle());
            int size = setSize(holder);
            setArtistImg(theCollection.getItemArtUrl(), holder);

            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(context, AlbumDetailActivity.class);
                            Bundle b = new Bundle();
                            b.putParcelable("mediaItemCollection", theCollection);
                            i.putExtra("bundle", b);
                            context.startActivity(i);
                        }
                    }, 100);
                }
            });

            holder.grid_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final IMediaItemCollection selectedCollection = (MediaItemCollection) resultItemList.get(position);
                    if(selectedCollection.count() == 0)
                        selectedCollection.setMediaElement(MediaController.getInstance(context).getAlbumTrackList(selectedCollection));
                    OverFlowMenuUtils.showCollectionMenu((Activity)context, v, R.menu.collection_popup, selectedCollection);
                }
            });

        }else if(mResultType.equals(SearchResult.ARTISTS)){
            holder.defaultImg.setVisibility(View.VISIBLE);
            final MediaItemCollection selected = (MediaItemCollection) resultItemList.get(position);


            holder.title.setText(selected.getItemTitle());
            final int count = selected.getItemCount();
            int albumCount = selected.getItemListCount();
            holder.subTitle.setText((count<=1 ? context.getResources().getString(R.string.song) : context.getResources().getString(R.string.songs)) +" "+count+" "+
                    (albumCount<=1 ? context.getResources().getString(R.string.album) : context.getResources().getString(R.string.albums)) +" "+albumCount);
            int size = setSize(holder);
            if(null == resultItemList.get(position).getItemArtUrl())
                selected.setItemArtUrl(App.playbackManager().queue().getArtistArtList().get(selected.getItemId()));

            if(null == resultItemList.get(position).getItemArtUrl())
                resultItemList.get(position).setItemArtUrl(MediaItem.UNKNOWN_ART_URL);

            setArtistImg(selected.getItemArtUrl(), holder);

            holder.grid_menu.setVisibility(View.VISIBLE);

            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(context, AlbumDetailItemActivity.class);
                            Bundle b = new Bundle();
                            b.putParcelable("mediaItemCollection", (MediaItemCollection)resultItemList.get(position));
                            i.putExtra("bundle", b);
                            context.startActivity(i);
                        }
                    }, 100);
                }
            });

            holder.grid_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final IMediaItemCollection selectedCollection = (MediaItemCollection) resultItemList.get(position);
                    if(selectedCollection.count() == 0){
                        selectedCollection.setMediaElement(MediaController.getInstance(context).getArtistAlbumsList(selectedCollection));
                    }
                    IMediaItemCollection rootCollection =  ((IMediaItemCollection)selectedCollection.getItemAt(0));
                    if(rootCollection.count() == 0)
                        rootCollection.setMediaElement(MediaController.getInstance(activity).getArtistTrackList(selectedCollection));
                    OverFlowMenuUtils.showCollectionMenu((Activity)context, v, R.menu.collection_popup, rootCollection);
                }
            });
        }
        holder.mainView.setElevation(Utils.dpToPx(context, 2));
    }
    private void updatePlayingTrack(SimpleItemViewHolder holder, long itemId){
        IMediaItemBase nowPlayingItem = App.playbackManager().queue().getPlayingItem();
        if(null != nowPlayingItem){
            if(itemId == nowPlayingItem.getItemId()){
                holder.name.setSelected(true);
                holder.art_overlay.setVisibility(View.VISIBLE);
                holder.art_overlay_play.setVisibility(View.VISIBLE);
                if(App.playbackManager().isTrackPlaying()){
                    holder.art_overlay_play.setImageResource(R.drawable.ic_player_pause);
                }else{
                    holder.art_overlay_play.setImageResource(R.drawable.ic_player_play);
                }
            }else{
                holder.name.setSelected(false);
                holder.art_overlay.setVisibility(View.INVISIBLE);
                holder.art_overlay_play.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void animate(final SearchDetailListAdapter.SimpleItemViewHolder holder) {
        //using action for smooth animation
        new Action() {

            public static final String TAG = "SEARCH";

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
                animateElevation(0, Utils.dpToPx(context, 10), holder);
                animateElevation(Utils.dpToPx(context, 10), 0, holder);
            }
        }.execute();
    }

    private ValueAnimator animateElevation(int from, int to, final SearchDetailListAdapter.SimpleItemViewHolder holder) {
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

    private int setSize(SearchDetailListAdapter.SimpleItemViewHolder holder) {
        int size = (Utils.getWindowWidth(context) / (isPhone ? 2 : 3))
                - (int)context.getResources().getDimension(R.dimen.card_grid_img_margin);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
        holder.defaultImg.setLayoutParams(layoutParams);
        holder.defaultImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return size;
    }

    private void setArtistImg(String path, SearchDetailListAdapter.SimpleItemViewHolder holder) {
        final int size = Utils.largeImageSize(context);
        Glide.with(context).load(path)
                .placeholder(R.drawable.ic_default_art_grid)
                .override(size, size)
                .centerCrop()
                .into(holder.defaultImg);
    }

    private void setSongArt(String path, SearchDetailListAdapter.SimpleItemViewHolder holder) {
        final int size = Utils.smallImageSize(activity);
        Glide.with(context)
                .load(path)
                .placeholder(R.drawable.ic_default_art_grid)
                .override(size, size)
                .centerCrop()
                .into(holder.img);
    }

    @Override
    public int getItemCount() {
        return resultItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(mResultType.equals(SearchResult.SONGS)){
            return TYPE_ROW;
        }else{
            return TYPE_GRID;
        }
    }

    public static class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        public View mainView, art_overlay;

        //For Header View
        public View headerHolder;
        public TextView headerText, headerCount;

        //For Song Lists
        public TextView name, artistName;
        public ImageView img, art_overlay_play;
        public LinearLayout menu;

        //        For Albums grid
        public TextView title, subTitle;
        public ImageView defaultImg;
        public View gridBottomBg, grid_menu;
        public TableLayout artTable;
        public FrameLayout imgPanel;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;

            headerHolder = itemView.findViewById(R.id.search_header_holder);
            headerText = (TextView) itemView.findViewById(R.id.search_header_text);
            headerCount = (TextView) itemView.findViewById(R.id.search_header_count);

            img = (ImageView) itemView.findViewById(R.id.song_item_img);
            name = (TextView) itemView.findViewById(R.id.song_item_name);
            menu = (LinearLayout) itemView.findViewById(R.id.song_item_overflow_menu);
            artistName = (TextView) itemView.findViewById(R.id.song_item_artist);
            art_overlay_play = (ImageView) itemView.findViewById(R.id.song_item_img_overlay_play);
            art_overlay = itemView.findViewById(R.id.song_item_img_overlay);

            title = (TextView) itemView.findViewById(R.id.card_grid_title);
            subTitle = (TextView) itemView.findViewById(R.id.card_grid_sub_title);
            defaultImg = (ImageView) itemView.findViewById(R.id.card_grid_default_img);
            artTable = (TableLayout)itemView.findViewById(R.id.card_grid_art_table);
            gridBottomBg = itemView.findViewById(R.id.card_grid_bottom);
            grid_menu = itemView.findViewById(R.id.card_grid_menu);
            imgPanel = (FrameLayout) itemView.findViewById(R.id.card_grid_img_panel);
        }
    }
}
