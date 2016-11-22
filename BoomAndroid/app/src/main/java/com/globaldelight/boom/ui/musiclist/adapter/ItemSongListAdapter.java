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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.ui.musiclist.ListDetail;
import com.globaldelight.boom.ui.musiclist.activity.DeviceMusicActivity;
import com.globaldelight.boom.ui.widgets.CoachMarkTextView;
import com.globaldelight.boom.ui.widgets.IconizedMenu;
import com.globaldelight.boom.ui.widgets.RegularTextView;
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
    private MediaItemCollection collection;
    private PermissionChecker permissionChecker;
    private int selectedSongId = -1;
    private SimpleItemViewHolder selectedHolder;
    private Activity activity;
    private MediaItem currentItem;
    private ListDetail listDetail;

    public ItemSongListAdapter(Activity activity, IMediaItemCollection collection, ListDetail listDetail, PermissionChecker permissionChecker) {
        this.activity = activity;
        this.collection = (MediaItemCollection) collection;
        this.permissionChecker = permissionChecker;
        this.listDetail = listDetail;
    }

    @Override
    public ItemSongListAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if(viewType == TYPE_ITEM) {
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

        }else if(position >= 1){
            int pos = position -1;
            if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
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
            setOnClicks(holder, pos);
        }
    }

    private void setAlbumArt(String path, SimpleItemViewHolder holder) {
        if (path != null && !path.equals("null"))
            Picasso.with(activity).load(new File(path)).error(activity.getResources().getDrawable(R.drawable.default_album_art, null))
                    .noFade().resize(dpToPx(90), dpToPx(90)).centerCrop().into(holder.img);
        else{
            setDefaultArt(holder, dpToPx(90));
        }
    }

    private void setDefaultArt(SimpleItemViewHolder holder, int size) {

        holder.img.setImageBitmap(Utils.getBitmapOfVector(activity, R.drawable.default_album_art,
                size, size));
    }

    private void setOnClicks(final SimpleItemViewHolder holder, final int position) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animate(holder);
              /*  switch (collection.getItemType()) {
                    case BOOM_PLAYLIST:

                        break;
                    case PLAYLIST:

                        break;
                    case ARTIST:

                        break;
                    case GENRE:

                        break;
                }*/


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
                IconizedMenu PopupMenu = new IconizedMenu(activity.getWindow().getContext(), anchorView);
                Menu menu = PopupMenu.getMenu();
                MenuInflater inflater = PopupMenu.getMenuInflater();
                inflater.inflate(R.menu.song_popup, PopupMenu.getMenu());
                PopupMenu.show();

                PopupMenu.setOnMenuItemClickListener(new IconizedMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.popup_song_add_queue :
                                if(App.getPlayingQueueHandler().getUpNextList()!=null){
                                    if (collection.getItemType() == PLAYLIST || collection.getItemType() == BOOM_PLAYLIST) {
                                        App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext((MediaItem) collection.getMediaElement().get(position));
                                    }else{
                                        App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext((MediaItem) ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).getMediaElement().get(position));
                                    }
                                }
                                break;
                            case R.id.popup_song_add_playlist :
                                if (collection.getItemType() == BOOM_PLAYLIST) {

                                }else {
                                    Utils util = new Utils(activity);
                                    util.addToPlaylist(activity, collection.getMediaElement().get(position));
                                }
                                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);
                                break;
                            case R.id.popup_song_add_fav :
                                Toast.makeText(activity, "Under Development...!", Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }
                });
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
        public View mainView, menu;
        public ImageView img;

        public RegularTextView headerTitle, headerSubTitle;
        public CoachMarkTextView headerDetail;
        ImageView mShuffle, mMore;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            img = (ImageView) itemView.findViewById(R.id.song_item_img);
            name = (TextView) itemView.findViewById(R.id.song_item_name);
            menu = itemView.findViewById(R.id.song_item_menu);
            artistName = (TextView) itemView.findViewById(R.id.song_item_artist);

            headerTitle = (RegularTextView) itemView.findViewById(R.id.header_title);
            headerSubTitle = (RegularTextView) itemView.findViewById(R.id.header_sub_title);
            headerDetail = (CoachMarkTextView) itemView.findViewById(R.id.header_detail);
        }
    }


}
