package com.globaldelight.boom.app.adapters.song;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.playbackEvent.utils.MediaType;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.playbackEvent.handler.UpNextPlayingQueue;
import com.globaldelight.boom.utils.OnStartDragListener;
import com.globaldelight.boom.utils.Utils;

import static android.view.LayoutInflater.from;

/**
 * Created by Rahul Agarwal on 8/8/2016.
 */

public class UpNextListAdapter extends RecyclerView.Adapter<UpNextListAdapter.SimpleItemViewHolder> {
    private static final int PENDING_REMOVAL_TIMEOUT = 2000;
    private final Handler swipeDeletehandler = new Handler();
    private int itemDeletePosition = -1, playingItemPosition;
    private OnStartDragListener mOnStartDragListener;
    private RecyclerView recyclerView;
    private Context context;

    public UpNextListAdapter(Context context, OnStartDragListener dragListener, RecyclerView recyclerView) {
        this.context = context;
        this.mOnStartDragListener = dragListener;
        this.recyclerView = recyclerView;
    }

    public void updateList(UpNextPlayingQueue playingQueue) {
        notifyDataSetChanged();
    }

    @Override
    public SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = from(parent.getContext()).
                inflate(R.layout.card_upnext, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final SimpleItemViewHolder holder, final int position) {
        if (itemDeletePosition == (position)) {
            // we need to show the "undo" state of the row
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.upnext_delete_background));

            holder.layout.setVisibility(View.GONE);
            holder.undoButton.setVisibility(View.INVISIBLE);
            holder.undoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    swipeDeletehandler.removeMessages(0);
                    itemDeletePosition = -1;
                    notifyItemChanged(position);

                }
            });
            //handler showing undo button after some time
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (holder.undoButton.getVisibility() == View.INVISIBLE)
                        holder.undoButton.setVisibility(View.VISIBLE);
                }
            }, 500);
        } else if(App.playbackManager().queue().getUpNextItemCount() > 0){
            MediaItem item = (MediaItem) App.playbackManager().queue().getUpNextItemList().get(position);
            if(null != item) {
                // we need to show the "normal" state
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.app_background));
                holder.layout.setVisibility(View.VISIBLE);
                holder.undoButton.setVisibility(View.GONE);
                holder.undoButton.setOnClickListener(null);

                setArt(holder, item.getItemArtUrl());
                holder.name.setText(item.getTitle());
                holder.artistName.setText(item.getItemArtist());

                if (null != App.playbackManager().getPlayingItem() && position == App.playbackManager().queue().getPlayingItemIndex()
                       && item.equalTo(App.playbackManager().getPlayingItem())) {
                    playingItemPosition = position;
                    holder.art_overlay.setVisibility(View.VISIBLE);
                    holder.art_overlay_play.setVisibility(View.VISIBLE);
                    holder.loadCloud.setVisibility(View.GONE);
                    holder.name.setTextColor(ContextCompat.getColor(context, R.color.upnext_playing_title));
                    boolean isMediaItem = item.getMediaType() == MediaType.DEVICE_MEDIA_LIB;
                    if (App.playbackManager().isTrackPlaying() ) {
                        holder.art_overlay_play.setImageResource(R.drawable.ic_player_pause);
                        if (!isMediaItem && App.playbackManager().isTrackLoading() )
                            holder.loadCloud.setVisibility(View.VISIBLE);
                    } else {
                        holder.art_overlay_play.setImageResource(R.drawable.ic_player_play);
                    }
                } else {
                    holder.art_overlay.setVisibility(View.GONE);
                    holder.art_overlay_play.setVisibility(View.GONE);
                    holder.loadCloud.setVisibility(View.GONE);
                    holder.name.setTextColor(ContextCompat.getColor(context, R.color.upnext_track_title));
                }
                setOnItemClick(holder, position);
                setDragHandle(holder);
            }
        }
    }

    public void setDragHandle(final SimpleItemViewHolder holder) {

        holder.imgHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN || MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_HOVER_ENTER
                        || MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_SCROLL) {
                    mOnStartDragListener.onStartDrag(holder);
                }
              //  FlurryAnalyticHelper.logEvent(UtilAnalytics.Dragg_Animation_usedIn_Up_next);
                FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.Dragg_Animation_usedIn_Up_next);

                return false;
            }
        });

    }

    public void setOnItemClick(SimpleItemViewHolder holder, final int itemPosition) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.playbackManager().queue().setNewItemAsPlayingItem(itemPosition);
                try {
                    recyclerView.scrollToPosition(itemPosition);
                } catch (Exception e) {
                }
//                FlurryAnalyticHelper.logEvent(UtilAnalytics.Song_Played_Up_next);
                FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.Song_Played_Up_next);
            }
        });
    }

    private void setArt(SimpleItemViewHolder holder, String path) {
        final int size = Utils.smallImageSize(context);
        Glide.with(context).load(path)
                .placeholder(R.drawable.ic_default_art_grid)
                .centerCrop()
                .override(size, size)
                .into(holder.img);
    }

    @Override
    public int getItemCount() {
        return App.playbackManager().queue().getUpNextItemCount();
    }

    public boolean isSwipeDeleteAllowed(int postion) {
        if(postion == playingItemPosition){
            return false;
        }

        return true;
    }

    public void removeSwipedItem(final RecyclerView.ViewHolder viewholder) {
        if (itemDeletePosition == -1) {
            itemDeletePosition = viewholder.getAdapterPosition();
            notifyItemChanged(viewholder.getAdapterPosition());
            swipeDeletehandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (itemDeletePosition >= 0) {
                        try {
                            App.playbackManager().queue().removeItem(itemDeletePosition);

                            if(playingItemPosition > itemDeletePosition){
                                App.playbackManager().queue().setPlayingItemIndex(playingItemPosition-1);
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                        notifyDataSetChanged();
                        itemDeletePosition = -1;
                    }
                }
            }, PENDING_REMOVAL_TIMEOUT);
        }
    }

    public static class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        //For Song View
        public TextView name, artistName;//added by nidhin
        public View mainView, art_overlay;
        public ImageView img, art_overlay_play;
        public ProgressBar loadCloud;

        public LinearLayout imgHandle;
        public Button undoButton;
        /*functions to implement swipe delete action-made for multi delete*/
        public LinearLayout layout;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            layout = (LinearLayout) itemView.findViewById(R.id.viewcontent);
            img = (ImageView) itemView.findViewById(R.id.song_item_img);
            art_overlay_play = (ImageView) itemView.findViewById(R.id.song_item_img_overlay_play);
            art_overlay = itemView.findViewById(R.id.song_item_img_overlay);
            loadCloud = (ProgressBar) itemView.findViewById(R.id.load_cloud );
            imgHandle = (LinearLayout) itemView.findViewById(R.id.queue_item_handle);
            name = (TextView) itemView.findViewById(R.id.queue_item_name);
            artistName = (TextView) itemView.findViewById(R.id.queue_item_artist);
            undoButton = (Button) itemView.findViewById(R.id.undo_button);
        }
    }
}
