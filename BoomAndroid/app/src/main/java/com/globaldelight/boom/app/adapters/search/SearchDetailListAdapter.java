package com.globaldelight.boom.app.adapters.search;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.Toast;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.app.activities.AlbumDetailActivity;
import com.globaldelight.boom.app.activities.AlbumDetailItemActivity;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.local.callback.IMediaItem;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.collection.local.callback.IMediaItemCollection;
import com.globaldelight.boom.app.adapters.search.utils.SearchResult;
import com.globaldelight.boom.view.RegularTextView;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.async.Action;
import com.globaldelight.boom.R;
import com.squareup.picasso.Picasso;

import java.io.File;
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
                    if (!App.playbackManager().isTrackLoading()) {
                        App.playbackManager().queue().addItemToPlay(resultItemList.get(position));
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
                    PopupMenu pm = new PopupMenu(context, anchorView);
                    pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            try {
                                switch (item.getItemId()) {
                                    case R.id.popup_song_play_next:
                                        App.playbackManager().queue().addItemAsPlayNext(resultItemList.get(position));
                                        break;
                                    case R.id.popup_song_add_queue:
                                        App.playbackManager().queue().addItemAsUpNext(resultItemList.get(position));
                                        break;
                                    case R.id.popup_song_add_playlist:
                                        ArrayList list = new ArrayList<IMediaItemBase>();
                                        list.add(resultItemList.get(position));
                                        Utils.addToPlaylist(activity, list, null);
                                        break;
                                    case R.id.popup_song_add_fav:
                                        if (MediaController.getInstance(context).isFavoriteItem(resultItemList.get(position).getItemId())) {
                                            MediaController.getInstance(context).removeItemToFavoriteList(resultItemList.get(position).getItemId());
                                            Toast.makeText(context, context.getResources().getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show();
                                        } else {
                                            MediaController.getInstance(context).addItemToFavoriteList((IMediaItem) resultItemList.get(position));
                                            Toast.makeText(context, context.getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                }
                            }catch (Exception e){

                            }
                            return false;
                        }
                    });
                    if(MediaController.getInstance(context).isFavoriteItem(resultItemList.get(position).getItemId())){
                        pm.inflate(R.menu.song_remove_fav);
                    }else{
                        pm.inflate(R.menu.song_add_fav);
                    }
                    pm.show();
                }
            });
        }else if(mResultType.equals(SearchResult.ALBUMS)){
            holder.defaultImg.setVisibility(View.VISIBLE);
            holder.title.setText(resultItemList.get(position).getItemTitle());
            holder.subTitle.setText(((MediaItemCollection) resultItemList.get(position)).getItemSubTitle());
            int size = setSize(holder);
            setArtistImg(holder, ((MediaItemCollection) resultItemList.get(position)).getItemArtUrl(), size);

            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    recyclerView.smoothScrollToPosition(getPosition(position));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(context, AlbumDetailActivity.class);
                            i.putExtra("mediaItemCollection", (MediaItemCollection)resultItemList.get(position));
                            context.startActivity(i);
                        }
                    }, 100);
                }
            });

            holder.grid_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu pm = new PopupMenu(context, v);
                    pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            try {
                                if(((IMediaItemCollection)resultItemList.get(position)).getMediaElement().size() == 0)
                                    ((IMediaItemCollection)resultItemList.get(position)).setMediaElement(MediaController.getInstance(context).getAlbumTrackList((IMediaItemCollection) resultItemList.get(position)));

                                switch (item.getItemId()) {
                                    case R.id.popup_album_play_next:
                                        App.playbackManager().queue().addItemAsPlayNext(((IMediaItemCollection)resultItemList.get(position)).getMediaElement());
                                        break;
                                    case R.id.popup_album_add_queue:
                                        App.playbackManager().queue().addItemAsUpNext(((IMediaItemCollection)resultItemList.get(position)).getMediaElement());
                                        break;
                                    case R.id.popup_album_add_playlist:
                                        Utils.addToPlaylist(activity, ((MediaItemCollection) resultItemList.get(position)).getMediaElement(), null);
                                       // FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);
                                        FlurryAnalytics.getInstance(activity.getApplicationContext()).setEvent(FlurryEvents.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);

                                        break;
                                }
                            }catch (Exception e){

                            }
                            return false;
                        }
                    });
                    pm.inflate(R.menu.album_popup);
                    pm.show();
                }
            });

        }else if(mResultType.equals(SearchResult.ARTISTS)){
            holder.defaultImg.setVisibility(View.VISIBLE);

            holder.title.setText(resultItemList.get(position).getItemTitle());
            int count = ((MediaItemCollection)resultItemList.get(position)).getItemCount();
            int albumCount = ((MediaItemCollection)resultItemList.get(position)).getItemListCount();
            holder.subTitle.setText((count<=1 ? context.getResources().getString(R.string.song) : context.getResources().getString(R.string.songs)) +" "+count+" "+
                    (albumCount<=1 ? context.getResources().getString(R.string.album) : context.getResources().getString(R.string.albums)) +" "+albumCount);
            int size = setSize(holder);
            if(null == resultItemList.get(position).getItemArtUrl())
                resultItemList.get(position).setItemArtUrl(App.playbackManager().queue().getArtistArtList().get(((MediaItemCollection) resultItemList.get(position)).getItemId()));

            if(null == resultItemList.get(position).getItemArtUrl())
                resultItemList.get(position).setItemArtUrl(MediaItem.UNKNOWN_ART_URL);

            setArtistImg(holder, ((MediaItemCollection) resultItemList.get(position)).getItemArtUrl(), size);

            holder.grid_menu.setVisibility(View.VISIBLE);

            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    recyclerView.smoothScrollToPosition(getPosition(position));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(context, AlbumDetailItemActivity.class);
                            i.putExtra("mediaItemCollection", (MediaItemCollection)resultItemList.get(position));
                            context.startActivity(i);
                        }
                    }, 100);
                }
            });

            holder.grid_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu pm = new PopupMenu(context, v);
                    pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            try {
                                if(((IMediaItemCollection) resultItemList.get(position)).getMediaElement().size() == 0){
                                    ((IMediaItemCollection)resultItemList.get(position)).setMediaElement(MediaController.getInstance(context).getArtistAlbumsList((IMediaItemCollection) resultItemList.get(position)));
                                }
                                if(((IMediaItemCollection)((IMediaItemCollection) resultItemList.get(position)).getMediaElement().get(0)).getMediaElement().size() == 0)
                                    ((IMediaItemCollection)((IMediaItemCollection) resultItemList.get(position)).getMediaElement().get(0)).
                                            setMediaElement(MediaController.getInstance(activity).getArtistTrackList((IMediaItemCollection) resultItemList.get(position)));

                                switch (item.getItemId()) {
                                    case R.id.popup_album_play_next:
                                        App.playbackManager().queue().addItemAsPlayNext(((IMediaItemCollection)((IMediaItemCollection) resultItemList.get(position)).getMediaElement().get(0)).getMediaElement());
                                        break;
                                    case R.id.popup_album_add_queue:
                                        App.playbackManager().queue().addItemAsUpNext(((IMediaItemCollection)((IMediaItemCollection) resultItemList.get(position)).getMediaElement().get(0)).getMediaElement());
                                        break;
                                    case R.id.popup_album_add_playlist:
                                        Utils.addToPlaylist(activity, ((IMediaItemCollection)((IMediaItemCollection) resultItemList.get(position)).getMediaElement().get(0)).getMediaElement(), null);
                                     //   FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);
                                        FlurryAnalytics.getInstance(activity.getApplicationContext()).setEvent(FlurryEvents.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);

                                        break;
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            return false;
                        }
                    });
                    pm.inflate(R.menu.album_popup);
                    pm.show();
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

//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (size/(isPhone?2.5:3)));
//        holder.gridBottomBg.setLayoutParams(params);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
        holder.defaultImg.setLayoutParams(layoutParams);
        holder.defaultImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return size;
    }

    private void setArtistImg(final SearchDetailListAdapter.SimpleItemViewHolder holder, String path, final int size) {
        if ( path == null ) path = "";
        Picasso.with(context).load(new File(path))
                .placeholder(R.drawable.ic_default_art_grid)
                .into(holder.defaultImg);
    }

    private void setSongArt(String path, SearchDetailListAdapter.SimpleItemViewHolder holder) {
        if ( path == null ) path = "";
        Picasso.with(context)
                .load(new File(path))
                .placeholder(R.drawable.ic_default_art_grid)
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
            menu = (LinearLayout) itemView.findViewById(R.id.song_item_overflow_menu);
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
