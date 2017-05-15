package com.globaldelight.boom.ui.musiclist.adapter;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.globaldelight.boom.App;
import com.globaldelight.boom.Media.MediaType;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.analytics.UtilAnalytics;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.handler.UpNextPlayingQueue;
import com.globaldelight.boom.ui.widgets.RegularButton;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.OnStartDragListener;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;
import java.io.File;

import static android.view.LayoutInflater.from;
import static com.globaldelight.boom.utils.Utils.dpToPx;

/**
 * Created by Rahul Agarwal on 8/8/2016.
 */

public class UpNextListAdapter extends RecyclerView.Adapter<UpNextListAdapter.SimpleItemViewHolder> {
    private static final int PENDING_REMOVAL_TIMEOUT = 2000;
    final Handler swipeDeletehandler = new Handler();
    public int itemDeletePosition = -1, playingItemPosition;
    OnStartDragListener mOnStartDragListener;
    private RecyclerView recyclerView;
    private Context context;
    private int WIDTH, HEIGHT;

    public UpNextListAdapter(Context context, OnStartDragListener dragListener, RecyclerView recyclerView) {
        this.context = context;
        this.mOnStartDragListener = dragListener;
        this.recyclerView = recyclerView;
        WIDTH = Utils.dpToPx(context, 62);
        HEIGHT = Utils.dpToPx(context, 62);
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
        } else if(App.getPlayingQueueHandler().getUpNextList().getUpNextItemCount() > 0){
            MediaItem item = (MediaItem) App.getPlayingQueueHandler().getUpNextList().getUpNextItemList().get(position);
            if(null != item) {
                // we need to show the "normal" state
                holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.app_background));
                holder.layout.setVisibility(View.VISIBLE);
                holder.undoButton.setVisibility(View.GONE);
                holder.undoButton.setOnClickListener(null);
                if (null == item.getItemArtUrl())
                    item.setItemArtUrl(App.getPlayingQueueHandler().getUpNextList().getAlbumArtList().get(item.getItemAlbum()));

                if (null == item.getItemArtUrl())
                    item.setItemArtUrl(MediaItem.UNKNOWN_ART_URL);

                setArt(holder, item.getItemArtUrl());
                holder.name.setText(item.getItemTitle());
                holder.artistName.setText(item.getItemArtist());

                if (null != App.getPlayerEventHandler().getPlayingItem() && position == App.getPlayingQueueHandler().getUpNextList().getPlayingItemIndex()
                       && item.getItemId() == App.getPlayerEventHandler().getPlayingItem().getItemId()) {
                    playingItemPosition = position;
                    holder.art_overlay.setVisibility(View.VISIBLE);
                    holder.art_overlay_play.setVisibility(View.VISIBLE);
                    holder.loadCloud.setVisibility(View.GONE);
                    holder.name.setTextColor(ContextCompat.getColor(context, R.color.upnext_playing_title));
                    boolean isMediaItem = item.getMediaType() == MediaType.DEVICE_MEDIA_LIB;
                    if (App.getPlayerEventHandler().isPlaying()) {
                        holder.art_overlay_play.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_player_pause, null));
                    } else {
                        if (!isMediaItem && App.getPlayerEventHandler().isTrackWaitingForPlay() && !App.getPlayerEventHandler().isPaused())
                            holder.loadCloud.setVisibility(View.VISIBLE);
                        holder.art_overlay_play.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_player_play, null));
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
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Dragg_Animation_usedIn_Up_next);
                return false;
            }
        });

    }

    public void setOnItemClick(SimpleItemViewHolder holder, final int itemPosition) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!App.getPlayerEventHandler().isTrackLoading()){
                    App.getPlayingQueueHandler().getUpNextList().setNewItemAsPlayingItem(itemPosition);
                }
                try {
                    recyclerView.scrollToPosition(itemPosition);
                } catch (Exception e) {
                }
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Song_Played_Up_next);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setHeaderBg(SimpleItemViewHolder holder) {
        holder.mainView.setElevation(dpToPx(context, 2));
    }

    private void setArt(SimpleItemViewHolder holder, String path) {
        if ( path == null ) {
            path = "";
        }
        Picasso.with(context).load(new File(path))
                .placeholder(R.drawable.ic_default_art_grid)
                .resize(WIDTH, HEIGHT)
                .into(holder.img);
    }

    @Override
    public int getItemCount() {
        return App.getPlayingQueueHandler().getUpNextList().getUpNextItemCount();
    }

    public boolean isSwipeDeleteAllowed(int postion) {
        if(postion == playingItemPosition){
            return false;
        }else if (postion >= 0 && postion < getItemCount())
            return true;
        return false;
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
                            App.getPlayingQueueHandler().getUpNextList().removeItem(itemDeletePosition);

                            if(playingItemPosition > itemDeletePosition){
                                App.getPlayingQueueHandler().getUpNextList().setPlayingItemIndex(playingItemPosition-1);
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

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {


        //For Header View
        public RegularTextView headerText;
        public RegularTextView buttonClrear;

        //For Song View
        public RegularTextView name, artistName;//added by nidhin
        public View mainView, art_overlay;
        public ImageView img, art_overlay_play;
        public ProgressBar loadCloud;

        public LinearLayout imgHandle;
        public RegularButton undoButton;
        /*functions to implement swipe delete action-made for multi delete*/
        public LinearLayout layout;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            headerText = (RegularTextView) itemView.findViewById(R.id.queue_header_text);

            buttonClrear = (RegularTextView) itemView.findViewById(R.id.btn_clear);
            layout = (LinearLayout) itemView.findViewById(R.id.viewcontent);
            img = (ImageView) itemView.findViewById(R.id.song_item_img);
            art_overlay_play = (ImageView) itemView.findViewById(R.id.song_item_img_overlay_play);
            art_overlay = itemView.findViewById(R.id.song_item_img_overlay);
            loadCloud = (ProgressBar) itemView.findViewById(R.id.load_cloud );
            imgHandle = (LinearLayout) itemView.findViewById(R.id.queue_item_handle);
            name = (RegularTextView) itemView.findViewById(R.id.queue_item_name);
            artistName = (RegularTextView) itemView.findViewById(R.id.queue_item_artist);
            undoButton = (RegularButton) itemView.findViewById(R.id.undo_button);
        }
    }
}
