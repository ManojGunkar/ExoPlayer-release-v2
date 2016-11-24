package com.globaldelight.boom.ui.musiclist.adapter;

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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.handler.search.SearchResult;
import com.globaldelight.boom.ui.widgets.CoachMarkTextView;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.async.Action;
import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.handler.search.Search;
import com.globaldelight.boom.ui.musiclist.activity.AlbumActivity;
import com.globaldelight.boom.ui.musiclist.activity.DetailAlbumActivity;
import com.globaldelight.boom.ui.musiclist.activity.SearchDetailListActivity;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
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

    public SearchListAdapter(Context context, FragmentActivity activity, Search searchRes, RecyclerView recyclerView) {
        this.context = context;
        this.activity = activity;
        this.searchRes = searchRes;
        this.recyclerView = recyclerView;
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
                            inflate(R.layout.search_header, parent, false);
                }else{
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.search_row_hide, parent, false);
                }
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_HEADER_SONGS:
                if(songs.size() > 0) {
                itemView = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.search_header, parent, false);
                }else{
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.search_row_hide, parent, false);
                }
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_HEADER_ARTISTS:
                if(artists.size() > 0){
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.search_header, parent, false);
                }else{
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.search_row_hide, parent, false);
                }
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_LIST_SONG:
                if(songs.size() > 0) {
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.card_song_item, parent, false);
                }else{
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.search_row_hide, parent, false);
                }
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_LIST_ARTIST:
                if(artists.size() > 0){
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.card_grid_item, parent, false);
                }else{
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.search_row_hide, parent, false);
                }
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_LIST_ALBUM:
                if(albums.size() > 0){
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.card_grid_item, parent, false);
                }else{
                    itemView = LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.search_row_hide, parent, false);
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
                    holder.headerCount.setText(searchRes.getArtistCount() - 4 + " MORE");
                } else {
                    holder.headerCount.setVisibility(View.GONE);
                }
                holder.headerCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, SearchDetailListActivity.class);
                        i.putExtra("list_type", SearchResult.ARTISTS);
                        i.putExtra("query", searchRes.getQuery());
                        context.startActivity(i);
                    }
                });
            }
            return;
        } else if (whatView(position) == ITEM_VIEW_TYPE_HEADER_ALBUMS) {
            if(albums.size() > 0) {
                setHeaderBg(holder);
                holder.headerText.setText(R.string.albums);
                if (searchRes.getArtistCount() > 4) {
                    holder.headerCount.setText(searchRes.getAlbumCount() - 4 + " MORE");
                } else {
                    holder.headerCount.setVisibility(View.GONE);
                }
                holder.headerCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, SearchDetailListActivity.class);
                        i.putExtra("list_type", SearchResult.ALBUMS);
                        i.putExtra("query", searchRes.getQuery());
                        context.startActivity(i);
                    }
                });
            }
            return;
        } else if (whatView(position) == ITEM_VIEW_TYPE_HEADER_SONGS) {
            if(songs.size() > 0) {
                setHeaderBg(holder);
                holder.headerText.setText(R.string.songs);
                if (searchRes.getArtistCount() > 4) {
                    holder.headerCount.setText(searchRes.getSongCount() - 4 + " MORE");
                } else {
                    holder.headerCount.setVisibility(View.GONE);
                }
                holder.headerCount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, SearchDetailListActivity.class);
                        i.putExtra("list_type", SearchResult.SONGS);
                        i.putExtra("query", searchRes.getQuery());
                        context.startActivity(i);
                    }
                });
            }
            return;
        } else if (whatView(position) == ITEM_VIEW_TYPE_LIST_ARTIST) {
            if (artists.size() == 0)
                return;

            holder.defaultImg.setVisibility(View.VISIBLE);

            holder.title.setText(artists.get(getPosition(position)).getItemTitle());
            int count = ((MediaItemCollection)artists.get(getPosition(position))).getItemCount();
            int albumCount = ((MediaItemCollection)artists.get(getPosition(position))).getItemListCount();
            holder.subTitle.setText((count<=1 ? context.getResources().getString(R.string.song) : context.getResources().getString(R.string.songs)) +" "+count+" "+
                    (albumCount<=1 ? context.getResources().getString(R.string.album) : context.getResources().getString(R.string.albums)) +" "+albumCount);
            int size = setSize(holder);
            setArtistImg(holder, ((MediaItemCollection) artists.get(getPosition(position))).getItemArtUrl(), size);

            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    recyclerView.smoothScrollToPosition(getPosition(position));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(context, DetailAlbumActivity.class);
                            i.putExtra("mediaItemCollection", (MediaItemCollection)artists.get(getPosition(position)));
                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    (Activity) context,
                                    new Pair<View, String>(holder.defaultImg, "transition:imgholder1")
                            );
                            ActivityCompat.startActivity((Activity) context, i, options.toBundle());
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
                            switch (item.getItemId()) {
                                case R.id.popup_album_add_queue :
                                    if(App.getPlayingQueueHandler().getUpNextList()!=null){
                                        ((MediaItemCollection)artists.get(getPosition(position))).setMediaElement(MediaController.getInstance(context).getMediaCollectionItemDetails((MediaItemCollection)artists.get(getPosition(position))));
                                        ((MediaItemCollection)((MediaItemCollection)artists.get(getPosition(position))).getMediaElement().get(0)).setMediaElement(MediaController.getInstance(context).getMediaCollectionItemDetails((MediaItemCollection)artists.get(getPosition(position))));

                                        App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext(((IMediaItemCollection)((IMediaItemCollection)artists.get(getPosition(position))).getMediaElement().get(((IMediaItemCollection)artists.get(getPosition(position))).getCurrentIndex())));
                                    }
                                    break;
                                case R.id.popup_album_add_playlist:
                                    Utils util = new Utils(context);
                                    ((MediaItemCollection)artists.get(getPosition(position))).setMediaElement(MediaController.getInstance(context).getMediaCollectionItemDetails((MediaItemCollection)artists.get(getPosition(position))));
                                    ((MediaItemCollection)((MediaItemCollection)artists.get(getPosition(position))).getMediaElement().get(0)).setMediaElement(MediaController.getInstance(context).getMediaCollectionItemDetails((MediaItemCollection)artists.get(getPosition(position))));

                                    util.addToPlaylist(activity, ((IMediaItemCollection)((IMediaItemCollection)artists.get(getPosition(position))).getMediaElement().get(((IMediaItemCollection)artists.get(getPosition(position))).getCurrentIndex())).getMediaElement(), null);
                                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);
                                    break;
                            }
                            return false;
                        }
                    });
                    pm.inflate(R.menu.album_popup);
                    pm.show();
                }
            });
        } else if (whatView(position) == ITEM_VIEW_TYPE_LIST_ALBUM) {
            if (albums.size() == 0)
                return;
            holder.defaultImg.setVisibility(View.VISIBLE);
            holder.title.setText(albums.get(getPosition(position)).getItemTitle());
            holder.subTitle.setText(((MediaItemCollection) albums.get(getPosition(position))).getItemSubTitle());
            int size = setSize(holder);
            setArtistImg(holder, ((MediaItemCollection) albums.get(getPosition(position))).getItemArtUrl(), size);
            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    recyclerView.smoothScrollToPosition(getPosition(position));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(context, AlbumActivity.class);
                            i.putExtra("mediaItemCollection", (MediaItemCollection)albums.get(getPosition(position)));
                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    (Activity) context,
                                    new Pair<View, String>(holder.defaultImg, "transition:imgholder")
                            );
                            ActivityCompat.startActivity((Activity) context, i, options.toBundle());
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
                            switch (item.getItemId()) {
                                case R.id.popup_album_add_queue :
                                    if(App.getPlayingQueueHandler().getUpNextList()!=null){
                                        ((MediaItemCollection)albums.get(getPosition(position))).setMediaElement(MediaController.getInstance(context).getMediaCollectionItemDetails((IMediaItemCollection) albums.get(getPosition(position))));
                                        App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext(albums.get(getPosition(position)));
                                    }
                                    break;
                                case R.id.popup_album_add_playlist:
                                    Utils util = new Utils(context);
                                    ((MediaItemCollection)albums.get(getPosition(position))).setMediaElement(MediaController.getInstance(context).getMediaCollectionItemDetails((IMediaItemCollection) albums.get(getPosition(position))));

                                    util.addToPlaylist(activity, ((MediaItemCollection)albums.get(getPosition(position))).getMediaElement(), null);
                                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);
                                    break;
                            }
                            return false;
                        }
                    });
                    pm.inflate(R.menu.album_popup);
                    pm.show();
                }
            });

        } else {
            if (songs.size() == 0)
                return;
            holder.name.setText(songs.get(getPosition(position)).getItemTitle());
            holder.artistName.setText(((MediaItem)songs.get(getPosition(position))).getItemArtist());
            holder.mainView.setElevation(0);
            setSongArt(songs.get(getPosition(position)).getItemArtUrl(), holder);
            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    animate(holder);
                    App.getPlayingQueueHandler().getUpNextList().addToPlay((ArrayList<MediaItem>) songs, getPosition(position));
                }
            });

            holder.menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View anchorView) {
                    PopupMenu pm = new PopupMenu(context, anchorView);
                    pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.popup_song_add_queue :
                                    App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext(((MediaItem)songs.get(getPosition(position))));
                                    break;
                                case R.id.popup_song_add_playlist:
                                    Utils util = new Utils(context);
                                    ArrayList list = new ArrayList<IMediaItemBase>();
                                    list.add(songs.get(getPosition(position)));
                                    util.addToPlaylist(activity, list, null);
                                    break;
                                case R.id.popup_song_add_fav :
                                    if(MediaController.getInstance(context).isFavouriteItems(songs.get(getPosition(position)).getItemId())){
                                        MediaController.getInstance(context).removeItemToList(false, songs.get(getPosition(position)).getItemId());
                                    }else{
                                        MediaController.getInstance(context).addSongsToList(false, songs.get(getPosition(position)));
                                    }
                                    break;
                            }
                            return false;
                        }
                    });
                    if(MediaController.getInstance(context).isFavouriteItems(songs.get(getPosition(position)).getItemId())){
                        pm.inflate(R.menu.song_remove_fav);
                    }else{
                        pm.inflate(R.menu.song_add_fav);
                    }
                    pm.show();
                }
            });
        }
        holder.mainView.setElevation(dpToPx(2));
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
                animateElevation(0, dpToPx(10), holder);
                animateElevation(dpToPx(10), 0, holder);
            }
        }.execute();
    }

    private void setHeaderBg(SimpleItemViewHolder holder) {
        holder.mainView.setBackgroundColor(ContextCompat.getColor(context, R.color.appBackground));
        holder.mainView.setElevation(dpToPx(0));
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
        Utils utils = new Utils(context);
        int size = (utils.getWindowWidth(context)
                - utils.dpToPx(context, 15)) / 2;

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
        holder.defaultImg.setLayoutParams(layoutParams);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (size/2.5));
        holder.gridBottomBg.setLayoutParams(params);
        return size;
    }

    private void setArtistImg(final SimpleItemViewHolder holder, final String path, final int size) {
        if (isPathValid(path))
            Picasso.with(context).load(new File(path)).error(context.getResources().getDrawable(R.drawable.default_album_art_home, null))
                    .centerCrop().resize(size, size)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.defaultImg);
        else {
            holder.defaultImg.setImageBitmap(Utils.getBitmapOfVector(context, R.drawable.default_album_art_home, size, size));
        }
    }

    private void setSongArt(String path, SimpleItemViewHolder holder) {
        if (path != null && !path.equals("null"))
            Picasso.with(context).load(new File(path)).error(context.getResources().getDrawable(R.drawable.default_album_art, null)).resize(dpToPx(90),
                    dpToPx(90)).centerCrop().into(holder.img);
        else{
            setDefaultArt(holder, dpToPx(90));
        }
    }

    private void setDefaultArt(SimpleItemViewHolder holder, int size) {

        holder.img.setImageBitmap(Utils.getBitmapOfVector(context, R.drawable.default_album_art,
                size, size));
    }

    private boolean fileExist(String albumArtPath) {
        File imgFile = new File(albumArtPath);
        return imgFile.exists();
    }

    public boolean isPathValid(String path) {
        return path != null && fileExist(path);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public int getItemCount() {
        return totalSize;
    }

    @Override
    public int getItemViewType(int position) {
        return whatView(position);
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public View mainView;

        //For Header View
        public View headerHolder;
        public TextView headerText, headerCount;

        //For Song Lists
        public RegularTextView name;
        public CoachMarkTextView artistName;
        public View menu;
        public ImageView img;

//        For Album grid
        public RegularTextView title;
        public CoachMarkTextView subTitle;
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
            name = (RegularTextView) itemView.findViewById(R.id.song_item_name);
            menu = itemView.findViewById(R.id.song_item_menu);
            artistName = (CoachMarkTextView) itemView.findViewById(R.id.song_item_artist);

            title = (RegularTextView) itemView.findViewById(R.id.card_grid_title);
            subTitle = (CoachMarkTextView) itemView.findViewById(R.id.card_grid_sub_title);
            defaultImg = (ImageView) itemView.findViewById(R.id.card_grid_default_img);
            artTable = (TableLayout)itemView.findViewById(R.id.card_grid_art_table);
            gridBottomBg = itemView.findViewById(R.id.card_grid_bottom);
            grid_menu = itemView.findViewById(R.id.card_grid_menu);
            imgPanel = (FrameLayout) itemView.findViewById(R.id.card_grid_img_panel);
        }
    }

}
