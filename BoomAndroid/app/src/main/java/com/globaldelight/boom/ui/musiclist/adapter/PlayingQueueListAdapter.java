package com.globaldelight.boom.ui.musiclist.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.handler.PlayingQueue.QueueType;
import com.globaldelight.boom.handler.PlayingQueue.UpNextList;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.ui.widgets.RoundedTransformation;
import com.globaldelight.boom.utils.OnStartDragListener;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.view.LayoutInflater.from;
import static com.globaldelight.boom.utils.ImageConverter.getRoundedCornerBitmap;
import static com.globaldelight.boom.utils.Utils.dpToPx;
import static com.globaldelight.boom.utils.Utils.getBitmapOfVector;
import static com.globaldelight.boom.utils.Utils.getUriToResource;
import static com.squareup.picasso.MemoryPolicy.NO_CACHE;
import static com.squareup.picasso.Picasso.LoadedFrom;
import static com.squareup.picasso.Picasso.with;

public class PlayingQueueListAdapter extends RecyclerView.Adapter<PlayingQueueListAdapter.SimpleItemViewHolder> {
    public static final int ITEM_VIEW_TYPE_HEADER_HISTORY = 0;
    public static final int ITEM_VIEW_TYPE_HEADER_PLAYING = 1;
    public static final int ITEM_VIEW_TYPE_HEADER_MANUAL = 2;
    public static final int ITEM_VIEW_TYPE_HEADER_AUTO = 3;
    public static final int ITEM_VIEW_TYPE_LIST_HISTORY = 4;
    public static final int ITEM_VIEW_TYPE_LIST_PLAYING = 5;
    public static final int ITEM_VIEW_TYPE_LIST_MANUAL = 6;
    public static final int ITEM_VIEW_TYPE_LIST_AUTO = 7;
    public static final int ITEM_VIEW_TYPE_NULL = 8;
    private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec
    public static int deleteCountControl = 1;
    Typeface font;
    HashMap<ListPosition, Runnable> pendingRunnables = new HashMap<>(); // map of items to pending runnables, so we can cancel a removal if need be
    List<ListPosition> itemsPendingRemoval;
    OnStartDragListener mOnStartDragListener;
    private LinkedList<IMediaItemBase> mHistoryList;
    private LinkedList<UpNextList.UpNextItem> mPlaying;
    private LinkedList<IMediaItemBase> mUpnextManualList;
    private LinkedList<IMediaItemBase> mUpnextAutoList;
    private int headerHistoryPos, headerPlayingPos, headerManualPos,
            headerAutoPos, totalSize;
    private Context context;
    private Handler handler = new Handler(); // hanlder for running delayed runnables
    private boolean undoOn = true; // is undo on, you can turn it on from the toolbar menu

    public PlayingQueueListAdapter(Context context, UpNextList playingQueue, OnStartDragListener dragListener) {
        this.context = context;
        init(playingQueue.getHistoryList(), playingQueue.getPlayingList(), playingQueue.getManualUpNextList(), playingQueue.getAutoUpNextList());
        font = Typeface.createFromAsset(context.getAssets(), "fonts/TitilliumWeb-Regular.ttf");
        this.mOnStartDragListener = dragListener;
        itemsPendingRemoval = new ArrayList<>();
    }


    private void init(LinkedList<IMediaItemBase> history, LinkedList<UpNextList.UpNextItem> playing, LinkedList<IMediaItemBase> upnext, LinkedList<IMediaItemBase> upnextAuto) {
        this.mHistoryList = history;
        this.mPlaying = playing;
        this.mUpnextManualList = upnext;
        this.mUpnextAutoList = upnextAuto;

        updateHeaderPosition();
    }

    public void updateHeaderPosition() {
        headerHistoryPos = 0;

        headerHistoryPos = 0;
        headerPlayingPos = mHistoryList.size() + 1;
        headerManualPos = mHistoryList.size() + mPlaying.size() + 2;
        headerAutoPos = mHistoryList.size() + mPlaying.size() + mUpnextManualList.size() + 3;
        this.totalSize = mHistoryList.size() + mPlaying.size() + mUpnextManualList.size() + mUpnextAutoList.size() + 4;

    }

    public int whatView(int position) {
        if (position == headerHistoryPos) {
            return ITEM_VIEW_TYPE_HEADER_HISTORY;
        } else if (position == headerPlayingPos) {
            return ITEM_VIEW_TYPE_HEADER_PLAYING;
        } else if (position == headerManualPos) {
            return ITEM_VIEW_TYPE_HEADER_MANUAL;
        } else if (position == headerAutoPos) {
            return ITEM_VIEW_TYPE_HEADER_AUTO;
        } else if (position > 0 && position < headerPlayingPos) {
            return ITEM_VIEW_TYPE_LIST_HISTORY;
        } else if (position > headerPlayingPos && position < headerManualPos) {
            return ITEM_VIEW_TYPE_LIST_PLAYING;
        } else if (position > headerManualPos && position < headerAutoPos) {
            return ITEM_VIEW_TYPE_LIST_MANUAL;
        } else if (position > headerAutoPos) {
            return ITEM_VIEW_TYPE_LIST_AUTO;
        } else
            return ITEM_VIEW_TYPE_NULL;
    }

    public void updateList(UpNextList playingQueue) {
        init(playingQueue.getHistoryList(), playingQueue.getPlayingList(), playingQueue.getManualUpNextList(), playingQueue.getAutoUpNextList());
        notifyDataSetChanged();
    }

    private int getPosition(int position) {
        if (position > headerHistoryPos && position < headerPlayingPos) {
            position = position - 1;
        } else if (position > headerPlayingPos && position < headerManualPos) {
            position = position - mHistoryList.size() - 2;
        } else if (position > headerManualPos && position < headerAutoPos) {
            position = position - mHistoryList.size() - mPlaying.size() - 3;
        } else  if(position > headerAutoPos)
            position = position - mHistoryList.size() - mPlaying.size() - mUpnextManualList.size() - 4;

        return position == -1 ? 0 : position;
    }

    public ListPosition getPositionObject(int position) {
        int listType = 0;

        if (position > headerHistoryPos && position < headerPlayingPos) {
            position = position - 1;
            listType = 4;
        } else if (position > headerPlayingPos && position < headerManualPos) {
            position = position - mHistoryList.size() - 2;
            listType = 5;
        } else if (position > headerManualPos && position < headerAutoPos) {
            position = position - mHistoryList.size() - mPlaying.size() - 3;
            listType = 6;
        } else if (position > headerAutoPos) {
            position = position - mHistoryList.size() - mPlaying.size() - mUpnextManualList.size() - 4;
            listType = 7;
        }
        return new ListPosition(listType, position == -1 ? 0 : position);
    }
    @Override
    public SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case ITEM_VIEW_TYPE_HEADER_PLAYING:
                itemView = from(parent.getContext()).
                        inflate(R.layout.header_playing_queue, parent, false);
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_HEADER_MANUAL:
                itemView = from(parent.getContext()).
                        inflate(R.layout.header_playing_queue, parent, false);
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_HEADER_HISTORY:
                itemView = from(parent.getContext()).
                        inflate(R.layout.header_playing_queue, parent, false);
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_HEADER_AUTO :
                itemView = from(parent.getContext()).
                        inflate(R.layout.header_playing_queue, parent, false);
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_LIST_MANUAL:

            case ITEM_VIEW_TYPE_LIST_HISTORY:

            case ITEM_VIEW_TYPE_LIST_PLAYING:

            case ITEM_VIEW_TYPE_LIST_AUTO:

                itemView = from(parent.getContext()).
                        inflate(R.layout.row_playing_queue, parent, false);
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_NULL:
                itemView = from(parent.getContext()).
                        inflate(R.layout.empty_search_layout, parent, false);
                return new SimpleItemViewHolder(itemView);
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final SimpleItemViewHolder holder, final int position) {
        int itemPosition;
        final ListPosition item;
        switch (whatView(position)) {
            case ITEM_VIEW_TYPE_HEADER_MANUAL:
                setHeaderBg(holder);
                holder.headerText.setText(R.string.up_next_manual);
                holder.headerText.setTypeface(font);
                if (mUpnextManualList != null && mUpnextManualList.size() > 0) {
                    holder.buttonClrear.setTypeface(font);
                    setOnClearBottonClick(holder, QueueType.Manual_UpNext, position);
                    holder.buttonClrear.setVisibility(View.VISIBLE);
                } else {
                    holder.buttonClrear.setVisibility(View.INVISIBLE);
                }
                break;
            case ITEM_VIEW_TYPE_HEADER_PLAYING:
                setHeaderBg(holder);
                holder.headerText.setText(R.string.Playing);
                holder.headerText.setTypeface(font);
                holder.buttonClrear.setVisibility(View.INVISIBLE);
                break;
            case ITEM_VIEW_TYPE_HEADER_HISTORY:
                setHeaderBg(holder);
                holder.headerText.setText(R.string.History);
                holder.headerText.setTypeface(font);
                if (mHistoryList != null && mHistoryList.size() > 0) {
                    holder.buttonClrear.setTypeface(font);
                    holder.buttonClrear.setVisibility(View.VISIBLE);
                    setOnClearBottonClick(holder, QueueType.History, position);
                } else {
                    holder.buttonClrear.setVisibility(View.INVISIBLE);
                }
                break;
            case ITEM_VIEW_TYPE_HEADER_AUTO:
                setHeaderBg(holder);
                holder.headerText.setText(R.string.up_next_auto);
                holder.headerText.setTypeface(font);
                if (mUpnextAutoList != null && mUpnextAutoList.size() > 0) {
                    holder.buttonClrear.setVisibility(View.VISIBLE);
                    holder.buttonClrear.setTypeface(font);
                    setOnClearBottonClick(holder, QueueType.Auto_UpNext, position);
                } else {
                    holder.buttonClrear.setVisibility(View.INVISIBLE);
                }
                break;
            case ITEM_VIEW_TYPE_LIST_MANUAL:

                item = getPositionObject(position);
                if (pendingSwipeListContains(item)) {
                    holder.itemView.setBackgroundColor(Color.GRAY);
                    holder.layout.setVisibility(View.GONE);
                    holder.undoButton.setVisibility(View.INVISIBLE);
                    holder.undoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // user wants to undo the removal, let's cancel the pending task
                            // Runnable pendingRemovalRunnable = pendingRunnables.get(item);
                            Runnable pendingRemovalRunnable = getSwipePendingRunnable(item);
                            removeSwipePendingRunnable(item);
                            if (pendingRemovalRunnable != null)
                                handler.removeCallbacks(pendingRemovalRunnable);
                            removeFromPendingList(item);
                            int p = getViewPosition(item);
                            notifyItemChanged(p);
                        }
                    });
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            holder.undoButton.setTypeface(font);
                            holder.undoButton.setVisibility(View.VISIBLE);
                        }
                    }, 500);

                } else {
                    try {
                        // we need to show the "normal" state
                        holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorBackground));
                        holder.layout.setVisibility(View.VISIBLE);
                        holder.undoButton.setVisibility(View.GONE);
                        holder.undoButton.setOnClickListener(null);
                        itemPosition = getPositionObject(position).getItemPosition();
                        setArt(holder, mUpnextManualList.get(itemPosition).getItemArtUrl(), whatView(position));
                        holder.name.setText(mUpnextManualList.get(itemPosition).getItemTitle());
                        holder.artistName.setText(((IMediaItem) mUpnextManualList.get(itemPosition)).getItemArtist());
                        setOnItemClick(holder, QueueType.Manual_UpNext, itemPosition);
                        setDragHandle(holder);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case ITEM_VIEW_TYPE_LIST_PLAYING:
                itemPosition = getPosition(position);
                setArt(holder, mPlaying.get(itemPosition).getUpNextItem().getItemArtUrl(), whatView(position));
                holder.name.setText(mPlaying.get(itemPosition).getUpNextItem().getItemTitle());
                holder.artistName.setText(((IMediaItem) mPlaying.get(itemPosition).getUpNextItem()).getItemArtist());
                holder.mainView.setElevation(dpToPx(context, 2));
                holder.imgHandle.setVisibility(View.INVISIBLE);
                setOnItemClick(holder, QueueType.Playing, itemPosition);
                holder.undoButton.setVisibility(View.INVISIBLE);

                break;
            case ITEM_VIEW_TYPE_LIST_HISTORY:
                itemPosition = getPosition(position);
                setArt(holder, mHistoryList.get(itemPosition).getItemArtUrl(), whatView(position));
                holder.name.setText(mHistoryList.get(itemPosition).getItemTitle());
                holder.artistName.setText(((IMediaItem) mHistoryList.get(itemPosition)).getItemArtist());
                holder.imgHandle.setVisibility(View.INVISIBLE);
                holder.undoButton.setVisibility(View.INVISIBLE);
                setOnItemClick(holder, QueueType.History, itemPosition);
                break;
            case ITEM_VIEW_TYPE_LIST_AUTO:

                item = getPositionObject(position);

                if (pendingSwipeListContains(item)) {
                    // we need to show the "undo" state of the row
                    holder.itemView.setBackgroundColor(Color.GRAY);

                    holder.layout.setVisibility(View.GONE);
                    holder.undoButton.setVisibility(View.INVISIBLE);
                    holder.undoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // user wants to undo the removal, let's cancel the pending task
                            // Runnable pendingRemovalRunnable = pendingRunnables.get(item);
                            Runnable pendingRemovalRunnable = getSwipePendingRunnable(item);
                            removeSwipePendingRunnable(item);
                            if (pendingRemovalRunnable != null)
                                handler.removeCallbacks(pendingRemovalRunnable);
                            removeFromPendingList(item);

                            int p = getViewPosition(item);

                            notifyItemChanged(p);

                        }
                    });
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            holder.undoButton.setVisibility(View.VISIBLE);
                        }
                    }, 500);
                } else {
                    // we need to show the "normal" state
                    holder.layout.setVisibility(View.VISIBLE);
                    holder.undoButton.setVisibility(View.GONE);
                    holder.undoButton.setOnClickListener(null);
                    itemPosition = getPositionObject(position).getItemPosition();
                    setArt(holder, mUpnextAutoList.get(itemPosition).getItemArtUrl(), whatView(position));
                    holder.name.setText(mUpnextAutoList.get(itemPosition).getItemTitle());
                    holder.artistName.setText(((IMediaItem) mUpnextAutoList.get(itemPosition)).getItemArtist());
                    setOnItemClick(holder, QueueType.Auto_UpNext, itemPosition);
                    setDragHandle(holder);
                }
                break;
            default:
                break;
        }
    }

    public void setDragHandle(final SimpleItemViewHolder holder) {

        holder.imgHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN || MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mOnStartDragListener.onStartDrag(holder);
                }
                return false;
            }
        });

    }
    public void setOnItemClick(SimpleItemViewHolder holder, final QueueType queueType, final int itemPosition){
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (queueType){
                    case History:
                        App.getPlayingQueueHandler().getUpNextList().addToPlay(QueueType.History, itemPosition);
                        break;
                    case Playing:
                        App.getPlayingQueueHandler().getUpNextList().addToPlay(QueueType.Playing, itemPosition);
                        break;
                    case Manual_UpNext:
                        App.getPlayingQueueHandler().getUpNextList().addToPlay(queueType, itemPosition);
                        break;
                    case Auto_UpNext:
                        App.getPlayingQueueHandler().getUpNextList().addToPlay(queueType, itemPosition);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setHeaderBg(SimpleItemViewHolder holder) {
        holder.mainView.setElevation(dpToPx(context, 2));
    }

    private void setArt(SimpleItemViewHolder holder, String path, int what) {
        //For album art
        if (path != null) {
            if (isFilePathExist(path)) {
                setAlbumArt(path, holder, what);
            } else setDefaultView(holder, what);
        } else {
            setDefaultView(holder, what);
        }
    }

    private boolean isFilePathExist(String albumArtPath) {
        File imgFile = new File(albumArtPath);
        return imgFile.exists();
    }

    private void setDefaultView(SimpleItemViewHolder holder, int what) {
        switch (what) {
            case ITEM_VIEW_TYPE_LIST_PLAYING:
                with(context).load(getUriToResource(context, R.drawable.default_album_art)).error(R.drawable.default_album_art).resize(dpToPx(context, 60),
                        dpToPx(context, 60)).centerCrop().memoryPolicy(NO_CACHE).into(holder.img);
                break;
            case ITEM_VIEW_TYPE_LIST_HISTORY:
                with(context).load(getUriToResource(context, R.drawable.default_album_art)).error(R.drawable.default_album_art).resize(dpToPx(context, 60),
                        dpToPx(context, 60)).centerCrop().memoryPolicy(NO_CACHE).into(holder.img);
                break;
            case ITEM_VIEW_TYPE_LIST_MANUAL:
                with(context).load(getUriToResource(context, R.drawable.default_album_art)).error(R.drawable.default_album_art).transform(new RoundedTransformation(100, 0))
                        .noFade().resize(dpToPx(context, 60),
                        dpToPx(context, 60)).centerCrop().memoryPolicy(NO_CACHE).into(holder.img);
                break;
        }
    }

    private void setAlbumArt(String path, SimpleItemViewHolder holder, int what) {

        if (path != null && !path.equals("null")) {
            with(context).load(new File(path)).error(R.drawable.default_album_art).resize(dpToPx(context, 60),
                    dpToPx(context, 60)).centerCrop().memoryPolicy(NO_CACHE).into(holder.img);
        } else {
            setDefaultView(holder, what);
        }
    }

    @Override
    public int getItemCount() {
        return totalSize;
    }

    @Override
    public int getItemViewType(int position) {
        return whatView(position);
    }

    public void setOnClearBottonClick(final SimpleItemViewHolder holder, final QueueType queueType, final int itemPosition) {
        holder.buttonClrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (queueType) {
                    case History:
                        if (App.getPlayingQueueHandler().getUpNextList().getHistoryList().size() > 0) {
                            App.getPlayingQueueHandler().getUpNextList().clearHistory();
                            updateList(App.getPlayingQueueHandler().getUpNextList());
                        }
                        break;

                    case Manual_UpNext:
                        if (itemsPendingRemoval.size() == 0) {//block clear when single item delete on wait
                            if (App.getPlayingQueueHandler().getUpNextList().getManualUpNextList().size() > 0) {
                                App.getPlayingQueueHandler().getUpNextList().getManualUpNextList().clear();
                                updateList(App.getPlayingQueueHandler().getUpNextList());
                            }
                        } else {
                            holder.buttonClrear.setTextColor(Color.RED);
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Do something after 100ms
                                    holder.buttonClrear.setTextColor(context.getResources().getColor(R.color.white));
                                }
                            }, 500);

                        }
                        break;
                    case Auto_UpNext:
                        if (itemsPendingRemoval.size() == 0) {//block clear when single item delete on wait
                            if (App.getPlayingQueueHandler().getUpNextList().getAutoUpNextList().size() > 0) {
                                App.getPlayingQueueHandler().getUpNextList().getAutoUpNextList().clear();
                                updateList(App.getPlayingQueueHandler().getUpNextList());
                            }
                        } else {
                            holder.buttonClrear.setTextColor(Color.RED);
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Do something after 100ms
                                    holder.buttonClrear.setTextColor(context.getResources().getColor(R.color.white));
                                }
                            }, 500);

                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public int getViewPosition(ListPosition listPositionData) {
        int normalposition = listPositionData.getItemPosition();
        switch (listPositionData.getListType()) {
            case ITEM_VIEW_TYPE_LIST_PLAYING:
                normalposition = headerPlayingPos + listPositionData.getItemPosition() + 1;
                break;


            case ITEM_VIEW_TYPE_LIST_HISTORY:
                normalposition = headerHistoryPos + listPositionData.getItemPosition() + 1;
                break;


            case ITEM_VIEW_TYPE_LIST_MANUAL:

                normalposition = headerManualPos + listPositionData.getItemPosition() + 1;

                break;

            case ITEM_VIEW_TYPE_LIST_AUTO:
                normalposition = headerAutoPos + listPositionData.getItemPosition() + 1;
                break;

        }
        return normalposition;
    }
    public LinkedList<IMediaItemBase> getListForType(int type) {

        switch (type) {
            case ITEM_VIEW_TYPE_LIST_HISTORY:
                return mHistoryList;
            case ITEM_VIEW_TYPE_LIST_MANUAL:
                return mUpnextManualList;
            case ITEM_VIEW_TYPE_LIST_AUTO:
                return mUpnextAutoList;
        }
        return null;
    }

    public boolean pendingSwipeListContains(ListPosition
                                                    item) {

        for (ListPosition lItem : itemsPendingRemoval) {
            if (lItem.getItemPosition() == item.getItemPosition() && lItem.getListType() == item.getListType()) {
                return true;
            }
        }
        return false;
    }

    public Runnable getSwipePendingRunnable(ListPosition item) {

        for (Map.Entry<ListPosition, Runnable> entry : pendingRunnables.entrySet()) {
            if (entry.getKey().getItemPosition() == item.getItemPosition() && entry.getKey().getListType() == item.getListType()) {
                return entry.getValue();

            }
        }

        return null;
    }

    public synchronized void removeSwipePendingRunnable(ListPosition item) {
        try {
            for (Map.Entry<ListPosition, Runnable> entry : pendingRunnables.entrySet()) {
                if (entry.getKey().getItemPosition() == item.getItemPosition() && entry.getKey().getListType() == item.getListType()) {

                    Log.d("removing", entry.getKey().getItemPosition() + "");
                    pendingRunnables.remove(entry.getKey());
                }
            }
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }

    }

    public boolean isSwipeDeleteAllowed(int postion) {
        ListPosition objPosition = getPositionObject(postion);
        if (itemsPendingRemoval.size() < deleteCountControl && (objPosition.getListType() == ITEM_VIEW_TYPE_LIST_MANUAL || objPosition.getListType() == ITEM_VIEW_TYPE_LIST_AUTO)) {
            return true;
        }
        return false;
    }

    public void removeFromPendingList(ListPosition item) {
        for (ListPosition lItem : itemsPendingRemoval) {
            if (lItem.getItemPosition() == item.getItemPosition() && lItem.getListType() == item.getListType()) {
                itemsPendingRemoval.remove(lItem);

                break;
            }
        }
    }

    public void swipedItemPendingRemoval(int position) {
        final ListPosition item = getPositionObject(position);
        boolean contains = pendingSwipeListContains(item);
        if (!contains && itemsPendingRemoval.size() < deleteCountControl) {
            itemsPendingRemoval.add(item);
            // this will redraw row in "undo" state
            notifyItemChanged(position);
            // let's create, store and post a runnable to remove the item
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    removeSwipedItem(item);
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(item, pendingRemovalRunnable);
        }
    }

    public void removeSwipedItem(ListPosition item) {

        int type = item.getListType();

        if (pendingSwipeListContains(item)) {
            removeFromPendingList(item);
        }
        switch (type) {
            case ITEM_VIEW_TYPE_LIST_PLAYING:
                break;

            case ITEM_VIEW_TYPE_LIST_HISTORY:
                break;

            case ITEM_VIEW_TYPE_LIST_MANUAL:
                mUpnextManualList.remove(item.getItemPosition());
                updateHeaderPosition();
                notifyDataSetChanged();
                break;
            case ITEM_VIEW_TYPE_LIST_AUTO:
                mUpnextAutoList.remove(item.getItemPosition());
                updateHeaderPosition();
                notifyDataSetChanged();
                break;

        }
    }

    public boolean isPendingRemoval(int position) {
        ListPosition item = getPositionObject(position);
        return itemsPendingRemoval.contains(item);
    }

    public boolean isUndoOn() {
        return undoOn;
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {


        //For Header View
        public RegularTextView headerText;
        public RegularTextView buttonClrear;

        //For Song View
        public RegularTextView name, artistName;//added by nidhin
        public View mainView;
        public ImageView img;

        public ImageView imgHandle;
        // public ImageView imgMenu;
        public Button undoButton;
        /*functions to implement swipe delete action-made for multi delete*/
        public LinearLayout layout;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            headerText = (RegularTextView) itemView.findViewById(R.id.queue_header_text);

            buttonClrear = (RegularTextView) itemView.findViewById(R.id.btn_clear);
            layout = (LinearLayout) itemView.findViewById(R.id.viewcontent);
            undoButton = (Button) itemView.findViewById(R.id.undo_button);
            img = (ImageView) itemView.findViewById(R.id.queue_item_img);
            imgHandle = (ImageView) itemView.findViewById(R.id.queue_item_handle);
            // imgMenu = (ImageView) itemView.findViewById(R.id.queue_item_menu);
            name = (RegularTextView) itemView.findViewById(R.id.queue_item_name);
            // menu = itemView.findViewById(R.id.queue_item_menu);
            artistName = (RegularTextView) itemView.findViewById(R.id.queue_item_artist);
        }
    }

    public class ListPosition {
        private int ListType, position;

        public ListPosition(int ListType, int position) {
            this.ListType = ListType;
            this.position = position;
        }


        public int getListType() {
            return ListType;
        }

        public int getItemPosition() {
            return position;
        }
    }

}
