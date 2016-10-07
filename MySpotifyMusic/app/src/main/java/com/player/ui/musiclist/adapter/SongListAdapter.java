package com.player.ui.musiclist.adapter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
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

import com.player.App;
import com.player.data.MediaCollection.IMediaItemBase;
import com.player.data.DeviceMediaCollection.MediaItem;
import com.player.data.MediaLibrary.ItemType;
import com.player.data.PlayingQueue.QueueType;
import com.player.myspotifymusic.R;
import com.player.ui.musiclist.activity.DeviceMusicActivity;
import com.player.ui.widgets.IconizedMenu;
import com.player.utils.PermissionChecker;
import com.player.utils.Utils;
import com.player.utils.async.Action;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 8/8/2016.
 */
public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SimpleItemViewHolder> {

    private static final String TAG = "SongListAdapter-TAG";
    ArrayList<MediaItem> itemList;
    private PermissionChecker permissionChecker;
    private int selectedSongId = -1;
    private SimpleItemViewHolder selectedHolder;
    private Context context;
    private Activity activity;

    public SongListAdapter(Context context, FragmentActivity activity, ArrayList<? extends IMediaItemBase> itemList, PermissionChecker permissionChecker) {
        this.context = context;
        this.activity = activity;
        this.itemList = (ArrayList<MediaItem>) itemList;
        this.permissionChecker = permissionChecker;
    }

    @Override
    public SongListAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_song_item, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final SongListAdapter.SimpleItemViewHolder holder, final int position) {
        holder.name.setText(itemList.get(position).getItemTitle());
        holder.artistName.setText(itemList.get(position).getItemArtist());
        holder.mainView.setElevation(0);
        setAlbumArt(itemList.get(position).getItemArtUrl(), holder);
        if (selectedHolder != null)
            selectedHolder.mainView.setBackgroundColor(ContextCompat
                    .getColor(context, R.color.appBackground));
        selectedSongId = -1;
        selectedHolder = null;
        setOnClicks(holder, position);
    }

    private void setAlbumArt(String path, SimpleItemViewHolder holder) {
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

    private void setOnClicks(final SimpleItemViewHolder holder, final int position) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animate(holder);
                if(App.getPlayingQueueHandler().getPlayingQueue()!=null){
                    App.getPlayingQueueHandler().getPlayingQueue().addItemToQueue(QueueType.Playing, itemList.get(position), -1);
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
                                if(App.getPlayingQueueHandler().getPlayingQueue()!=null){
                                    App.getPlayingQueueHandler().getPlayingQueue().addItemToQueue(QueueType.Manual_UpNext, itemList.get(position), -1);
                                }
                                break;
                            case R.id.popup_song_add_playlist:
                                Utils util = new Utils(context);
                                util.addToPlaylist(activity, itemList.get(position));

                                break;
                        }
                        return false;
                    }
                });
            }
        });
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
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public void onViewRecycled(SimpleItemViewHolder holder) {
        super.onViewRecycled(holder);
        holder.img.setImageDrawable(null);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
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
        } else {
            if (activity != null && itemList.get(0).getItemType() == ItemType.SONGS)
                ((DeviceMusicActivity) activity).killActivity();
        }
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public TextView name, artistName;
        public View mainView, menu;
        public ImageView img;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            img = (ImageView) itemView.findViewById(R.id.song_item_img);
            name = (TextView) itemView.findViewById(R.id.song_item_name);
            menu = itemView.findViewById(R.id.song_item_menu);
            artistName = (TextView) itemView.findViewById(R.id.song_item_artist);
        }
    }


}
