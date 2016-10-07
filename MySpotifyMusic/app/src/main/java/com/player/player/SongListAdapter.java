package com.player.player;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.player.myspotifymusic.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 19-09-16.
 */
public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SimpleItemViewHolder> {

    private static final String TAG = "SongListAdapter-TAG";
    ArrayList<MediaItem> itemList;
    private int selectedSongId = -1;
    private SimpleItemViewHolder selectedHolder;
    private Context context;
    Activity activity;
    OnItemClickListener mItemClickListener;
    public SongListAdapter(Context context, Activity activity, ArrayList<MediaItem> itemList) {
        this.context = context;
        this.itemList = itemList;
        this.activity = activity;
    }

    @Override
    public SongListAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.old_card_song_item, parent, false);
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
                    .getColor(context, R.color.cardview_dark_background));
        selectedSongId = -1;
        selectedHolder = null;
        setOnClicks(holder, position);
    }

    private void setAlbumArt(String path, SimpleItemViewHolder holder) {
        if (path != null && !path.equals("null"))
            Picasso.with(context).load(new File(path)).resize(dpToPx(90),
                    dpToPx(90)).centerCrop().into(holder.img);
        else{
            Picasso.with(context).load(R.drawable.old_ic_list).resize(dpToPx(90),
                    dpToPx(90)).centerCrop().into(holder.img);
        }
    }

    private void setOnClicks(final SimpleItemViewHolder holder, final int position) {
       /* holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("mediaItem",itemList.get(position));
                activity.setResult(Activity.RESULT_OK, returnIntent);
                activity.finish();
            }
        });*/
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchorView) {
            }
        });
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
                    .getColor(context, R.color.cardview_dark_background));
        }
    }

    public void onBackPressed() {
        if (selectedSongId != -1) {
            animateElevation(12, 0, selectedHolder);
            selectedHolder.mainView.setBackgroundColor(ContextCompat
                    .getColor(context, R.color.cardview_dark_background));
            selectedSongId = -1;
            selectedHolder = null;
        }
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

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
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if(mItemClickListener != null){
                mItemClickListener.onItemClick(v, getPosition());
            }
        }
    }
    public interface OnItemClickListener {
        public void onItemClick(View view , int position);
    }

    public void setOnItemClickListener(final OnItemClickListener itemClickListener){
        mItemClickListener = itemClickListener;
    }

}
