package com.globaldelight.boom.ui.musiclist.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
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

import java.io.File;
import java.util.LinkedList;

import static android.view.LayoutInflater.from;
import static com.globaldelight.boom.utils.Utils.dpToPx;
import static com.globaldelight.boom.utils.Utils.getUriToResource;
import static com.squareup.picasso.MemoryPolicy.NO_CACHE;
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
    final Handler swipeDeletehandler = new Handler();
    public ListPosition itemDelete;
    OnStartDragListener mOnStartDragListener;
    private LinkedList<IMediaItemBase> mHistoryList;
    private LinkedList<UpNextList.UpNextItem> mPlaying;
    private LinkedList<IMediaItemBase> mUpnextManualList;
    private LinkedList<IMediaItemBase> mUpnextAutoList;
    private int headerHistoryPos, headerPlayingPos, headerManualPos,
            headerAutoPos, totalSize;
    private Context context;

    public PlayingQueueListAdapter(Context context, UpNextList playingQueue, OnStartDragListener dragListener) {
        this.context = context;
        init(playingQueue.getHistoryList(), playingQueue.getPlayingList(), playingQueue.getManualUpNextList(), playingQueue.getAutoUpNextList());
        this.mOnStartDragListener = dragListener;
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
        } else if (position > headerAutoPos)
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

            case ITEM_VIEW_TYPE_HEADER_MANUAL:

            case ITEM_VIEW_TYPE_HEADER_HISTORY:

            case ITEM_VIEW_TYPE_HEADER_AUTO:
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
                if (mUpnextManualList != null && mUpnextManualList.size() > 0) {
                    setOnClearBottonClick(holder, QueueType.Manual_UpNext, position);
                    holder.buttonClrear.setVisibility(View.VISIBLE);
                } else {
                    holder.buttonClrear.setVisibility(View.INVISIBLE);
                }
                break;
            case ITEM_VIEW_TYPE_HEADER_PLAYING:
                setHeaderBg(holder);
                holder.headerText.setText(R.string.Playing);
                holder.buttonClrear.setVisibility(View.INVISIBLE);
                break;
            case ITEM_VIEW_TYPE_HEADER_HISTORY:
                setHeaderBg(holder);
                holder.headerText.setText(R.string.History);
                if (mHistoryList != null && mHistoryList.size() > 0) {
                    holder.buttonClrear.setVisibility(View.VISIBLE);
                    setOnClearBottonClick(holder, QueueType.History, position);
                } else {
                    holder.buttonClrear.setVisibility(View.INVISIBLE);
                }
                break;
            case ITEM_VIEW_TYPE_HEADER_AUTO:
                setHeaderBg(holder);
                holder.headerText.setText(R.string.up_next_auto);
                if (mUpnextAutoList != null && mUpnextAutoList.size() > 0) {
                    holder.buttonClrear.setVisibility(View.VISIBLE);
                    setOnClearBottonClick(holder, QueueType.Auto_UpNext, position);
                } else {
                    holder.buttonClrear.setVisibility(View.INVISIBLE);
                }
                break;
            case ITEM_VIEW_TYPE_LIST_MANUAL:

                item = getPositionObject(position);

                if (itemDelete != null && itemDelete.getItemPosition() == item.getItemPosition() && item.getListType() == itemDelete.getListType()) {
                    holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                    holder.layout.setVisibility(View.GONE);
                    holder.undoButton.setVisibility(View.INVISIBLE);
                    holder.undoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            swipeDeletehandler.removeMessages(0);
                            itemDelete = null;
                            int p = getViewPosition(item);
                            notifyItemChanged(p);

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


                if (itemDelete != null && itemDelete.getItemPosition() == item.getItemPosition() && item.getListType() == itemDelete.getListType()) {

                    // we need to show the "undo" state of the row
                    holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));

                    holder.layout.setVisibility(View.GONE);
                    holder.undoButton.setVisibility(View.INVISIBLE);
                    holder.undoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            swipeDeletehandler.removeMessages(0);
                            itemDelete = null;
                            int p = getViewPosition(item);
                            notifyItemChanged(p);

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
                } else {
                    // we need to show the "normal" state
                    holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorBackground));
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

    public void setOnItemClick(SimpleItemViewHolder holder, final QueueType queueType, final int itemPosition) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (queueType) {
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
                        if (itemDelete != null) {
                            swipeDeletehandler.removeMessages(0);
                            itemDelete = null;
                        }
                        if (App.getPlayingQueueHandler().getUpNextList().getManualUpNextList().size() > 0) {
                            App.getPlayingQueueHandler().getUpNextList().getManualUpNextList().clear();
                            updateList(App.getPlayingQueueHandler().getUpNextList());
                        }

                        break;
                    case Auto_UpNext:
                        if (itemDelete != null) {

                            swipeDeletehandler.removeMessages(0);
                            itemDelete = null;
                        }

                        if (App.getPlayingQueueHandler().getUpNextList().getAutoUpNextList().size() > 0) {
                            App.getPlayingQueueHandler().getUpNextList().getAutoUpNextList().clear();
                            updateList(App.getPlayingQueueHandler().getUpNextList());
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

    public boolean isSwipeDeleteAllowed(int postion) {
        ListPosition objPosition = getPositionObject(postion);
        if (itemDelete == null && (objPosition.getListType() == ITEM_VIEW_TYPE_LIST_MANUAL || objPosition.getListType() == ITEM_VIEW_TYPE_LIST_AUTO)) {

            return true;
        }
        return false;
    }

    public void removeSwipedItem(RecyclerView.ViewHolder viewholder) {
        if (itemDelete == null) {
            final ListPosition postionObject = getPositionObject(viewholder.getAdapterPosition());
            itemDelete = getPositionObject(viewholder.getAdapterPosition());
            notifyItemChanged(viewholder.getAdapterPosition());
            swipeDeletehandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (itemDelete != null) {
                        try {
                            getListForType(postionObject.getListType()).remove(postionObject.getItemPosition());
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                        updateHeaderPosition();
                        notifyDataSetChanged();
                        itemDelete = null;
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
