package com.player.boom.ui.musiclist.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.player.boom.App;
import com.player.boom.R;
import com.player.boom.data.MediaCollection.IMediaItem;
import com.player.boom.data.MediaCollection.IMediaItemBase;
import com.player.boom.handler.PlayingQueue.PlayerEventHandler;
import com.player.boom.handler.PlayingQueue.QueueType;
import com.player.boom.ui.widgets.RoundedTransformation;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.LinkedList;
import java.util.Map;

import static android.view.LayoutInflater.from;
import static com.player.boom.utils.ImageConverter.getRoundedCornerBitmap;
import static com.player.boom.utils.Utils.dpToPx;
import static com.player.boom.utils.Utils.getBitmapOfVector;
import static com.player.boom.utils.Utils.getUriToResource;
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
    private LinkedList<IMediaItemBase> mHistoryList;
    private LinkedList<IMediaItemBase> mPlayingList;
    private LinkedList<IMediaItemBase> mUpnextManualList;
    private LinkedList<IMediaItemBase> mUpnextAutoList;
    private int headerHistoryPos, headerPlayingPos, headerManualPos,
            headerAutoPos, totalSize;
    private Context context;

    public PlayingQueueListAdapter(Context context, Map<QueueType, LinkedList<IMediaItemBase>> playingQueue) {
        this.context = context;
        init(playingQueue.get(QueueType.History), playingQueue.get(QueueType.Playing), playingQueue.get(QueueType.Manual_UpNext), playingQueue.get(QueueType.Auto_UpNext));
    }


    private void init(LinkedList<IMediaItemBase> history, LinkedList<IMediaItemBase> playing, LinkedList<IMediaItemBase> upnext, LinkedList<IMediaItemBase> upnextAuto) {
        this.mHistoryList = history;
        this.mPlayingList = playing;
        this.mUpnextManualList = upnext;
        this.mUpnextAutoList = upnextAuto;


        headerHistoryPos = 0;
        headerPlayingPos = mHistoryList.size() + 1;
        headerManualPos = mHistoryList.size() + mPlayingList.size() + 2;
        headerAutoPos = mHistoryList.size() + mPlayingList.size() + mUpnextManualList.size() + 3;
        this.totalSize = mHistoryList.size() + mPlayingList.size() + mUpnextManualList.size() + mUpnextAutoList.size() + 4;
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

    public void updateList(Map<QueueType, LinkedList<IMediaItemBase>> playingQueue) {
        init(playingQueue.get(QueueType.History), playingQueue.get(QueueType.Playing), playingQueue.get(QueueType.Manual_UpNext), playingQueue.get(QueueType.Auto_UpNext));
        notifyDataSetChanged();
    }

    private int getPosition(int position) {
        if (position > headerHistoryPos && position < headerPlayingPos) {
            position = position - 1;
        } else if (position > headerPlayingPos && position < headerManualPos) {
            position = position - mHistoryList.size() - 2;
        } else if (position > headerManualPos && position < headerAutoPos) {
            position = position - mHistoryList.size() - mPlayingList.size() - 3;
        } else  if(position > headerAutoPos)
            position = position - mHistoryList.size() - mPlayingList.size() - mUpnextManualList.size() - 4;

        return position;
    }

    @Override
    public SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case ITEM_VIEW_TYPE_HEADER_PLAYING:
//                if (mPlayingList.size() <= 0) {
//                    itemView = from(parent.getContext()).
//                            inflate(R.layout.empty_search_layout, parent, false);
//                    return new SimpleItemViewHolder(itemView);
//                }
                itemView = from(parent.getContext()).
                        inflate(R.layout.header_playing_queue, parent, false);
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_HEADER_MANUAL:
//                if (mUpnextManualList.size() <= 0) {
//                    itemView = from(parent.getContext()).
//                            inflate(R.layout.empty_search_layout, parent, false);
//                    return new SimpleItemViewHolder(itemView);
//                }
                itemView = from(parent.getContext()).
                        inflate(R.layout.header_playing_queue, parent, false);
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_HEADER_HISTORY:
//                if (mHistoryList.size() <= 0) {
//                    itemView = from(parent.getContext()).
//                            inflate(R.layout.empty_search_layout, parent, false);
//                    return new SimpleItemViewHolder(itemView);
//                }
                itemView = from(parent.getContext()).
                        inflate(R.layout.header_playing_queue, parent, false);
                return new SimpleItemViewHolder(itemView);
            case ITEM_VIEW_TYPE_HEADER_AUTO :
//                if (mUpnextAutoList.size() <= 0) {
//                    itemView = from(parent.getContext()).
//                            inflate(R.layout.empty_search_layout, parent, false);
//                    return new SimpleItemViewHolder(itemView);
//                }
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
        switch (whatView(position)) {
            case ITEM_VIEW_TYPE_HEADER_MANUAL:
//                if (mUpnextManualList.size() <= 0) {
//                    return;
//                }
                setHeaderBg(holder);
                holder.headerText.setText(R.string.up_next_manual);
                break;
            case ITEM_VIEW_TYPE_HEADER_PLAYING:
//                if (mPlayingList.size() <= 0) {
//                    return;
//                }
                setHeaderBg(holder);
                holder.headerText.setText(R.string.Playing);
                break;
            case ITEM_VIEW_TYPE_HEADER_HISTORY:
//                if (mHistoryList.size() <= 0) {
//                    return;
//                }
                setHeaderBg(holder);
                holder.headerText.setText(R.string.History);
                break;
            case ITEM_VIEW_TYPE_HEADER_AUTO:
//                if (mUpnextAutoList.size() <= 0) {
//                    return;
//                }
                setHeaderBg(holder);
                holder.headerText.setText(R.string.up_next_auto);
                break;
            case ITEM_VIEW_TYPE_LIST_MANUAL:
                itemPosition = getPosition(position);
                holder.img.setImageDrawable(new ColorDrawable(0xffffffff));
                setArt(holder, mUpnextManualList.get(itemPosition).getItemArtUrl(), whatView(position));
                holder.name.setText(mUpnextManualList.get(itemPosition).getItemTitle());
                holder.artistName.setText(((IMediaItem) mUpnextManualList.get(itemPosition)).getItemArtist());
                holder.mainView.setBackgroundColor(0xffffffff);
                holder.mainView.setElevation(dpToPx(context, 2));
                setOnItemClick(holder, QueueType.Manual_UpNext, itemPosition);
                break;
            case ITEM_VIEW_TYPE_LIST_PLAYING:
                itemPosition = getPosition(position);
                holder.img.setImageDrawable(new ColorDrawable(0xffffffff));
                setArt(holder, mPlayingList.get(itemPosition).getItemArtUrl(), whatView(position));
                holder.name.setText(mPlayingList.get(itemPosition).getItemTitle());
                holder.artistName.setText(((IMediaItem) mPlayingList.get(itemPosition)).getItemArtist());
                holder.mainView.setBackgroundColor(0xffffffff);
                holder.mainView.setElevation(dpToPx(context, 2));
                setOnItemClick(holder, QueueType.Playing, itemPosition);
                break;
            case ITEM_VIEW_TYPE_LIST_HISTORY:
                itemPosition = getPosition(position);
                holder.img.setImageDrawable(new ColorDrawable(0xffffffff));
                setArt(holder, mHistoryList.get(itemPosition).getItemArtUrl(), whatView(position));
                holder.name.setText(mHistoryList.get(itemPosition).getItemTitle());
                holder.artistName.setText(((IMediaItem) mHistoryList.get(itemPosition)).getItemArtist());
                holder.mainView.setBackgroundColor(0xffffffff);
                holder.mainView.setElevation(dpToPx(context, 2));
                setOnItemClick(holder, QueueType.History, itemPosition);
                break;
            case ITEM_VIEW_TYPE_LIST_AUTO:
                itemPosition = getPosition(position);
                holder.img.setImageDrawable(new ColorDrawable(0xffffffff));
                setArt(holder, mUpnextAutoList.get(itemPosition).getItemArtUrl(), whatView(position));
                holder.name.setText(mUpnextAutoList.get(itemPosition).getItemTitle());
                holder.artistName.setText(((IMediaItem) mUpnextAutoList.get(itemPosition)).getItemArtist());
                holder.mainView.setBackgroundColor(0xffffffff);
                holder.mainView.setElevation(dpToPx(context, 2));
                setOnItemClick(holder, QueueType.Auto_UpNext, itemPosition);
                break;
            default:
                break;
        }
    }

    public void setOnItemClick(SimpleItemViewHolder holder, final QueueType queueType, final int itemPosition){
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (queueType){
                    case History:
                        App.getPlayingQueueHandler().getPlayingQueue().addListItemToPlaying(queueType, itemPosition);
                        updateList(App.getPlayingQueueHandler().getPlayingQueue().getPlayingQueue());
                        break;
                    case Playing:
                        PlayerEventHandler.getPlayerEventInstance(context).Play();
                        break;
                    case Manual_UpNext:
                        App.getPlayingQueueHandler().getPlayingQueue().addListItemToPlaying(queueType, itemPosition);
                        updateList(App.getPlayingQueueHandler().getPlayingQueue().getPlayingQueue());
                        break;
                    case Auto_UpNext:
                        App.getPlayingQueueHandler().getPlayingQueue().addListItemToPlaying(queueType, itemPosition);
                        updateList(App.getPlayingQueueHandler().getPlayingQueue().getPlayingQueue());
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setHeaderBg(SimpleItemViewHolder holder) {
//        holder.mainView.setBackgroundColor(getColor(context, R.color.appBackground));
//        holder.mainView.setBackgroundColor(0xffffffff);
        holder.mainView.setElevation(dpToPx(context, 2));
    }

    private void setSongArt(String path, SimpleItemViewHolder holder) {
        int size = dpToPx(context, 50);
        if (path != null)
            with(context).load(new File(path)).resize(size,
                    size).centerCrop().into(holder.img);
        else {
            holder.img.setImageBitmap(getBitmapOfVector(context, R.drawable.default_album_art,
                    size, size));
        }

    }

    public void setImageToView(String url, final SimpleItemViewHolder holder) {
        with(context).load(new File(url)).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
                Bitmap circularBitmap = getRoundedCornerBitmap(bitmap, 100);
                holder.img.setImageBitmap(circularBitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    private void setArt(SimpleItemViewHolder holder, String path, int what) {
        //For album art
        if (path != null) {
            if (isFilePathExist(path)) {
//                new AlbumItemLoad(context, path, holder).execute();
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

            switch (what) {
                case ITEM_VIEW_TYPE_LIST_PLAYING:
                    with(context).load(new File(path)).resize(dpToPx(context, 60),
                            dpToPx(context, 60)).centerCrop().memoryPolicy(NO_CACHE).into(holder.img);
                    break;
                case ITEM_VIEW_TYPE_LIST_HISTORY:
                    with(context).load(new File(path)).resize(dpToPx(context, 60),
                            dpToPx(context, 60)).centerCrop().memoryPolicy(NO_CACHE).into(holder.img);
                    break;
                case ITEM_VIEW_TYPE_LIST_MANUAL:
                    with(context).load(new File(path)).transform(new RoundedTransformation(100, 0))
                            .noFade().resize(dpToPx(context, 60),
                            dpToPx(context, 60)).centerCrop().memoryPolicy(NO_CACHE).into(holder.img);
                    break;
                case ITEM_VIEW_TYPE_LIST_AUTO:
                    with(context).load(new File(path)).transform(new RoundedTransformation(100, 0))
                            .noFade().resize(dpToPx(context, 60),
                            dpToPx(context, 60)).centerCrop().memoryPolicy(NO_CACHE).into(holder.img);
                    break;
            }
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

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        //For Header View
        public TextView headerText;

        //For Song View
        public TextView name, artistName;
        public View mainView, menu;
        public ImageView img;
        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            headerText = (TextView) itemView.findViewById(R.id.queue_header_text);
            img = (ImageView) itemView.findViewById(R.id.queue_item_img);
            name = (TextView) itemView.findViewById(R.id.queue_item_name);
            menu = itemView.findViewById(R.id.queue_item_menu);
            artistName = (TextView) itemView.findViewById(R.id.queue_item_artist);
        }
    }

}
