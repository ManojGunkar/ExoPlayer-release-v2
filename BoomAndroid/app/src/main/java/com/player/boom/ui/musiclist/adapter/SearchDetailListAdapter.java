package com.player.boom.ui.musiclist.adapter;

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
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.player.boom.App;
import com.player.boom.R;
import com.player.boom.data.DeviceMediaCollection.MediaItem;
import com.player.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.player.boom.data.MediaCollection.IMediaItemBase;
import com.player.boom.data.MediaCollection.IMediaItemCollection;
import com.player.boom.handler.search.SearchResult;
import com.player.boom.ui.musiclist.activity.AlbumActivity;
import com.player.boom.ui.musiclist.activity.DetailAlbumActivity;
import com.player.boom.ui.musiclist.activity.SearchDetailListActivity;
import com.player.boom.utils.Utils;
import com.player.boom.utils.async.Action;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 14-11-16.
 */

public class SearchDetailListAdapter extends RecyclerView.Adapter<SearchDetailListAdapter.SimpleItemViewHolder> {
    private Context context;
    private ArrayList<? extends IMediaItemBase> resultItemList;
    private String mResultType;
    public static final int TYPE_ROW = 111;
    public static final int TYPE_GRID = 222;

    public SearchDetailListAdapter(SearchDetailListActivity searchDetailListActivity, ArrayList<? extends IMediaItemBase> resultItemList, String mResultType) {
        this.context = searchDetailListActivity;
        this.resultItemList = resultItemList;
        this.mResultType = mResultType;
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
            setSongArt(resultItemList.get(position).getItemArtUrl(), holder);
            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    animate(holder);
                    if(App.getPlayingQueueHandler().getUpNextList()!=null){
                        App.getPlayingQueueHandler().getUpNextList().addToPlay((ArrayList<MediaItem>) resultItemList, position);
                    }
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
                            Intent i = new Intent(context, AlbumActivity.class);
                            i.putExtra("mediaItemCollection", (MediaItemCollection)resultItemList.get(position));
                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    (Activity) context,
                                    new Pair<View, String>(holder.defaultImg, "transition:imgholder")
                            );
                            ActivityCompat.startActivity((Activity) context, i, options.toBundle());
                        }
                    }, 100);
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
            setArtistImg(holder, ((MediaItemCollection) resultItemList.get(position)).getItemArtUrl(), size);

            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    recyclerView.smoothScrollToPosition(getPosition(position));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(context, DetailAlbumActivity.class);
                            i.putExtra("mediaItemCollection", (MediaItemCollection)resultItemList.get(position));
                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    (Activity) context,
                                    new Pair<View, String>(holder.defaultImg, "transition:imgholder1")
                            );
                            ActivityCompat.startActivity((Activity) context, i, options.toBundle());
                        }
                    }, 100);
                }
            });
        }
        holder.mainView.setBackgroundColor(0xffffffff);
        holder.mainView.setElevation(dpToPx(2));
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
                animateElevation(0, dpToPx(10), holder);
                animateElevation(dpToPx(10), 0, holder);
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
        Utils utils = new Utils(context);
        int size = (utils.getWindowWidth(context)
                - utils.dpToPx(context, 15)) / 2;

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
        holder.defaultImg.setLayoutParams(layoutParams);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (size/2.5));
        holder.gridBottomBg.setLayoutParams(params);
        return size;
    }

    private void setArtistImg(final SearchDetailListAdapter.SimpleItemViewHolder holder, final String path, final int size) {
        if (isPathValid(path))
            Picasso.with(context).load(new File(path)).error(context.getResources().getDrawable(R.drawable.default_album_art_home, null))
                    .centerCrop().resize(size, size)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.defaultImg);
        else {
            holder.defaultImg.setImageBitmap(Utils.getBitmapOfVector(context, R.drawable.default_album_art_home, size, size));
        }
    }

    private void setSongArt(String path, SearchDetailListAdapter.SimpleItemViewHolder holder) {
        if (path != null && !path.equals("null"))
            Picasso.with(context).load(new File(path)).error(context.getResources().getDrawable(R.drawable.default_album_art, null)).resize(dpToPx(90),
                    dpToPx(90)).centerCrop().into(holder.img);
        else{
            setDefaultArt(holder, dpToPx(90));
        }
    }

    private void setDefaultArt(SearchDetailListAdapter.SimpleItemViewHolder holder, int size) {

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

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        public View mainView;

        //For Header View
        public View headerHolder;
        public TextView headerText, headerCount;

        //For Song Lists
        public TextView name, artistName;
        public View menu;
        public ImageView img;

        //        For Album grid
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
            menu = itemView.findViewById(R.id.song_item_menu);
            artistName = (TextView) itemView.findViewById(R.id.song_item_artist);

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
