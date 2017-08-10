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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.app.activities.AlbumDetailActivity;
import com.globaldelight.boom.app.activities.AlbumDetailItemActivity;
import com.globaldelight.boom.app.activities.SearchDetailActivity;
import com.globaldelight.boom.app.fragments.SearchDetailFragment;
import com.globaldelight.boom.collection.local.callback.IMediaItemCollection;
import com.globaldelight.boom.app.adapters.search.utils.SearchResult;
import com.globaldelight.boom.utils.OverFlowMenuUtils;
import com.globaldelight.boom.view.RegularTextView;
import com.globaldelight.boom.utils.async.Action;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.app.adapters.search.utils.Search;
import com.globaldelight.boom.utils.Utils;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 14-11-16.
 */
public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.SimpleItemViewHolder> {
    public static final int ITEM_VIEW_TYPE_HEADER_ARTISTS = 0;
    public static final int ITEM_VIEW_TYPE_HEADER_ALBUMS = 1;
    public static final int ITEM_VIEW_TYPE_HEADER_SONGS = 2;
    public static final int ITEM_VIEW_TYPE_LIST_ARTIST = 3;
    public static final int ITEM_VIEW_TYPE_LIST_ALBUM = 4;
    public static final int ITEM_VIEW_TYPE_LIST_SONG = 5;
    private ArrayList<? extends IMediaItemBase> songs;
    private ArrayList<? extends IMediaItemBase> albums;
    private ArrayList<? extends IMediaItemBase> artists;
    private int headerArtistPos, headerAlbumPos, headerSongPos,
            totalSize;
    private Context context;
    private Activity activity;
    private RecyclerView recyclerView;
    private Search searchRes;
    private boolean isPhone;
    public SearchListAdapter(Context context, Activity activity, Search searchRes, RecyclerView recyclerView, boolean isPhone) {
        this.context = context;
        this.activity = activity;
        this.searchRes = searchRes;
        this.recyclerView = recyclerView;
        this.isPhone = isPhone;
        init(searchRes.getSongResult(), searchRes.getAlbumResult(), searchRes.getArtistResult());
    }

    private void init(ArrayList<? extends IMediaItemBase> songs, ArrayList<? extends IMediaItemBase> albums, ArrayList<? extends IMediaItemBase> artists) {
        this.songs = songs;
        this.albums = albums;
        this.artists = artists;

        headerArtistPos = 0;
        headerAlbumPos = this.artists.size() + 1;
        headerSongPos = this.artists.size() + this.albums.size() + 2;
        this.totalSize = songs.size() + albums.size() + artists.size() + 3;
    }

    public int whatView(int position) {
        if (position == headerArtistPos) {
            return ITEM_VIEW_TYPE_HEADER_ARTISTS;
        } else if (position == headerAlbumPos) {
            return ITEM_VIEW_TYPE_HEADER_ALBUMS;
        } else if (position == headerSongPos) {
            return ITEM_VIEW_TYPE_HEADER_SONGS;
        } else if (position > 0 && position < headerAlbumPos) {
            return ITEM_VIEW_TYPE_LIST_ARTIST;
        } else if (position > headerAlbumPos && position < headerSongPos) {
            return ITEM_VIEW_TYPE_LIST_ALBUM;
        } else
            return ITEM_VIEW_TYPE_LIST_SONG;
    }

    public void updateList(Search searchRes) {
        init(searchRes.getSongResult(), searchRes.getAlbumResult(), searchRes.getArtistResult());
        notifyDataSetChanged();
    }

    private int getPosition(int position) {
        if (position > headerArtistPos && position < headerAlbumPos) {
            return position - 1;
        } else if (position > headerAlbumPos && position < headerSongPos) {
            return position - artists.size() - 2;
        } else
            return position - artists.size() - albums.size() - 3;
    }

    @Override
    public SearchListAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case ITEM_VIEW_TYPE_HEADER_ALBUMS:
                if(albums.size() > 0) {
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.card_header_search, parent, false);
                }else{
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.card_row_hide, parent, false);
                }
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_HEADER_SONGS:
                if(songs.size() > 0) {
                itemView = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.card_header_search, parent, false);
                }else{
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.card_row_hide, parent, false);
                }
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_HEADER_ARTISTS:
                if(artists.size() > 0){
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.card_header_search, parent, false);
                }else{
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.card_row_hide, parent, false);
                }
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_LIST_SONG:
                if(songs.size() > 0) {
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.card_song_item, parent, false);
                }else{
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.card_row_hide, parent, false);
                }
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_LIST_ARTIST:
                if(artists.size() > 0){
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.card_grid_item, parent, false);
                }else{
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.card_row_hide, parent, false);
                }
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_LIST_ALBUM:
                if(albums.size() > 0){
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.card_grid_item, parent, false);
                }else{
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.card_row_hide, parent, false);
                }
                        return new SimpleItemViewHolder(itemView);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(final SearchListAdapter.SimpleItemViewHolder holder, final int position) {

        if (whatView(position) == ITEM_VIEW_TYPE_HEADER_ARTISTS) {
            if(artists.size() > 0) {
                setHeaderBg(holder);
                holder.headerText.setText(R.string.artists);
                if (searchRes.getArtistCount() > 4) {
                    holder.headerCount.setText(searchRes.getArtistCount() - 4 + context.getResources().getString(R.string.more));
                } else {
                    holder.headerCount.setVisibility(View.GONE);
                }
                holder.headerCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startSearchDetailActivity(SearchResult.ARTISTS, searchRes.getQuery());
                    }
                });
            }
            return;
        } else if (whatView(position) == ITEM_VIEW_TYPE_HEADER_ALBUMS) {
            if(albums.size() > 0) {
                setHeaderBg(holder);
                holder.headerText.setText(R.string.albums);
                if (searchRes.getArtistCount() > 4) {
                    holder.headerCount.setText(searchRes.getAlbumCount() - 4 + context.getResources().getString(R.string.more));
                } else {
                    holder.headerCount.setVisibility(View.GONE);
                }
                holder.headerCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startSearchDetailActivity(SearchResult.ALBUMS, searchRes.getQuery());
                    }
                });
            }
            return;
        } else if (whatView(position) == ITEM_VIEW_TYPE_HEADER_SONGS) {
            if(songs.size() > 0) {
                setHeaderBg(holder);
                holder.headerText.setText(R.string.songs);
                if (searchRes.getArtistCount() > 4) {
                    holder.headerCount.setText(searchRes.getSongCount() - 4 + context.getResources().getString(R.string.more));
                } else {
                    holder.headerCount.setVisibility(View.GONE);
                }
                holder.headerCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startSearchDetailActivity(SearchResult.SONGS, searchRes.getQuery());
                    }
                });
            }
            return;
        } else if (whatView(position) == ITEM_VIEW_TYPE_LIST_ARTIST) {
            if (artists.size() == 0)
                return;

            holder.defaultImg.setVisibility(View.VISIBLE);

            holder.title.setText(artists.get(getPosition(position)).getItemTitle());
            final int count = ((MediaItemCollection)artists.get(getPosition(position))).getItemCount();
            int albumCount = ((MediaItemCollection)artists.get(getPosition(position))).getItemListCount();
            holder.subTitle.setText((count<=1 ? context.getResources().getString(R.string.song) : context.getResources().getString(R.string.songs)) +" "+count+" "+
                    (albumCount<=1 ? context.getResources().getString(R.string.album) : context.getResources().getString(R.string.albums)) +" "+albumCount);
            int size = setSize(holder);

            if(null == artists.get(getPosition(position)).getItemArtUrl())
                artists.get(getPosition(position)).setItemArtUrl(App.playbackManager().queue().getArtistArtList().get(artists.get(getPosition(position)).getItemId()));

            if(null == artists.get(getPosition(position)).getItemArtUrl())
                artists.get(getPosition(position)).setItemArtUrl(MediaItem.UNKNOWN_ART_URL);

            setArtistImg(holder, artists.get(getPosition(position)).getItemArtUrl());

            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    recyclerView.smoothScrollToPosition(getPosition(position));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(context, AlbumDetailItemActivity.class);
                            Bundle b = new Bundle();
                            b.putParcelable("mediaItemCollection", (MediaItemCollection)artists.get(getPosition(position)));
                            i.putExtra("bundle", b);
                            context.startActivity(i);
                        }
                    }, 100);
                }
            });
            holder.grid_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(((IMediaItemCollection) artists.get(getPosition(position))).count() == 0){
                        ((IMediaItemCollection)artists.get(getPosition(position))).setMediaElement(MediaController.getInstance(context).getArtistAlbumsList((IMediaItemCollection) artists.get(getPosition(position))));
                    }
                    if(((IMediaItemCollection)((IMediaItemCollection) artists.get(getPosition(position))).getItemAt(0)).count() == 0)
                        ((IMediaItemCollection)((IMediaItemCollection) artists.get(getPosition(position))).getItemAt(0)).
                                setMediaElement(MediaController.getInstance(activity).getArtistTrackList((IMediaItemCollection) artists.get(getPosition(position))));

                    IMediaItemCollection selected = ((IMediaItemCollection)((IMediaItemCollection) artists.get(getPosition(position))).getItemAt(0));
                    OverFlowMenuUtils.showCollectionMenu((Activity)context, v, R.menu.collection_popup, selected);
                    }
            });
        } else if (whatView(position) == ITEM_VIEW_TYPE_LIST_ALBUM) {
            if (albums.size() == 0)
                return;
            holder.defaultImg.setVisibility(View.VISIBLE);
            holder.title.setText(albums.get(getPosition(position)).getItemTitle());
            holder.subTitle.setText(((MediaItemCollection) albums.get(getPosition(position))).getItemSubTitle());
            int size = setSize(holder);
            setArtistImg(holder, albums.get(getPosition(position)).getItemArtUrl());

            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    recyclerView.smoothScrollToPosition(getPosition(position));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(context, AlbumDetailActivity.class);
                            Bundle b = new Bundle();
                            b.putParcelable("mediaItemCollection", (MediaItemCollection)albums.get(getPosition(position)));
                            i.putExtra("bundle", b);
                            context.startActivity(i);
                        }
                    }, 100);
                }
            });

            holder.grid_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(((IMediaItemCollection)albums.get(getPosition(position))).count() == 0)
                        ((IMediaItemCollection)albums.get(getPosition(position))).setMediaElement(MediaController.getInstance(context).getAlbumTrackList((IMediaItemCollection) albums.get(getPosition(position))));

                    OverFlowMenuUtils.showCollectionMenu((Activity)context, v, R.menu.collection_popup, ((IMediaItemCollection)albums.get(getPosition(position))));
                }
            });

        } else {
            if (songs.size() == 0)
                return;
            holder.name.setText(songs.get(getPosition(position)).getItemTitle());
            holder.artistName.setText(((MediaItem)songs.get(getPosition(position))).getItemArtist());
            holder.mainView.setElevation(0);
            if(null == songs.get(getPosition(position)).getItemArtUrl())
                songs.get(getPosition(position)).setItemArtUrl(App.playbackManager().queue().getAlbumArtList().get(((MediaItem) songs.get(getPosition(position))).getItemAlbum()));

            if(null == songs.get(getPosition(position)).getItemArtUrl())
                songs.get(getPosition(position)).setItemArtUrl(MediaItem.UNKNOWN_ART_URL);

            setSongArt(songs.get(getPosition(position)).getItemArtUrl(), holder);

            updatePlayingTrack(songs.get(getPosition(position)).getItemId(), holder, position);

            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    animate(holder);
                    App.playbackManager().queue().addItemToPlay(songs.get(getPosition(position)));
                }
            });

            holder.menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View anchorView) {
                    OverFlowMenuUtils.showMediaItemMenu((Activity)context, anchorView, R.menu.media_item_popup, songs.get(getPosition(position)));
                }
            });
        }
        holder.mainView.setElevation(Utils.dpToPx(context, 2));
    }

    private void updatePlayingTrack(long itemId, SimpleItemViewHolder holder, int position) {
        IMediaItemBase nowPlayingItem = App.playbackManager().queue().getPlayingItem();
        if(null != nowPlayingItem){
            if(itemId == nowPlayingItem.getItemId()){
                holder.name.setSelected(true);
                holder.art_overlay.setVisibility(View.VISIBLE);
                holder.art_overlay_play.setVisibility(View.VISIBLE);
                if(App.playbackManager().isTrackPlaying()){
                    holder.art_overlay_play.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_player_pause, null));
                }else{
                    holder.art_overlay_play.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_player_play, null));
                }
            }else{
                holder.name.setTextColor(ContextCompat.getColor(activity, R.color.track_title));
                holder.art_overlay.setVisibility(View.INVISIBLE);
                holder.art_overlay_play.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void startSearchDetailActivity(String listType, String query){
        Intent intent = new Intent(activity, SearchDetailActivity.class);
        intent.putExtra(SearchDetailFragment.ARG_LIST_TYPE, listType);
        intent.putExtra(SearchDetailFragment.ARG_MEDIA_QUERY, query);
        activity.startActivity(intent);
    }

    public void animate(final SimpleItemViewHolder holder) {
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

    private void setHeaderBg(SimpleItemViewHolder holder) {
        holder.mainView.setBackgroundColor(ContextCompat.getColor(context, R.color.appBackground));
        holder.mainView.setElevation(Utils.dpToPx(context, 0));
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

    private int setSize(SimpleItemViewHolder holder) {
        int size = (Utils.getWindowWidth(context) / (isPhone ? 2 : 3))
                - (int)context.getResources().getDimension(R.dimen.card_grid_img_margin);

//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (size/(isPhone?2.5:3)));
//        holder.gridBottomBg.setLayoutParams(params);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
        holder.defaultImg.setLayoutParams(layoutParams);
        holder.defaultImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return size;
    }

    private void setArtistImg(final SimpleItemViewHolder holder, String path) {
        final int size = Utils.largeImageSize(context);
        Glide.with(context).load(path)
                .placeholder(R.drawable.ic_default_art_grid)
                .centerCrop()
                .override(size, size)
                .into(holder.defaultImg);
    }

    private void setSongArt(String path, SimpleItemViewHolder holder) {
        final int size = Utils.smallImageSize(context);
        Glide.with(context)
                .load(path)
                .placeholder(R.drawable.ic_default_art_grid)
                .centerCrop()
                .override(size, size)
                .into(holder.img);
    }

    @Override
    public int getItemCount() {
        return totalSize;
    }

    @Override
    public int getItemViewType(int position) {
        return whatView(position);
    }

    public static class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public View mainView, art_overlay;

        //For Header View
        public View headerHolder;
        public RegularTextView headerText, headerCount;

        //For Song Lists
        public RegularTextView name, artistName;
        public ImageView img, art_overlay_play;
        public LinearLayout menu;

//        For Album grid
        public RegularTextView title, subTitle;
        public ImageView defaultImg;
        public View gridBottomBg, grid_menu;
        public TableLayout artTable;
        public FrameLayout imgPanel;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;

            headerHolder = itemView.findViewById(R.id.search_header_holder);
            headerText = (RegularTextView) itemView.findViewById(R.id.search_header_text);
            headerCount = (RegularTextView) itemView.findViewById(R.id.search_header_count);

            img = (ImageView) itemView.findViewById(R.id.song_item_img);
            name = (RegularTextView) itemView.findViewById(R.id.song_item_name);
            menu = (LinearLayout)itemView.findViewById(R.id.song_item_overflow_menu);
            artistName = (RegularTextView) itemView.findViewById(R.id.song_item_artist);
            art_overlay_play = (ImageView) itemView.findViewById(R.id.song_item_img_overlay_play);
            art_overlay = itemView.findViewById(R.id.song_item_img_overlay);

            title = (RegularTextView) itemView.findViewById(R.id.card_grid_title);
            subTitle = (RegularTextView) itemView.findViewById(R.id.card_grid_sub_title);
            defaultImg = (ImageView) itemView.findViewById(R.id.card_grid_default_img);
            artTable = (TableLayout)itemView.findViewById(R.id.card_grid_art_table);
            gridBottomBg = itemView.findViewById(R.id.card_grid_bottom);
            grid_menu = itemView.findViewById(R.id.card_grid_menu);
            imgPanel = (FrameLayout) itemView.findViewById(R.id.card_grid_img_panel);
        }
    }

}
