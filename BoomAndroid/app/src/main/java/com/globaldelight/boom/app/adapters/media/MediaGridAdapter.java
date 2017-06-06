package com.globaldelight.boom.app.adapters.media;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TableLayout;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.activities.AlbumDetailActivity;
import com.globaldelight.boom.app.activities.AlbumDetailItemActivity;
import com.globaldelight.boom.app.analytics.AnalyticsHelper;
import com.globaldelight.boom.app.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.collection.local.callback.IMediaItemCollection;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.view.RegularTextView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by adarsh on 05/06/17.
 */

public class MediaGridAdapter extends RecyclerView.Adapter<MediaGridAdapter.ViewHolder> {

    private static final String TAG = "MediaGridAdapter-TAG";
    private ArrayList<MediaItemCollection> itemList;
    private Activity activity;
    private RecyclerView recyclerView;
    private boolean isPhone;
    protected Context context;
    private ItemClickListener clickListener;


    public MediaGridAdapter(Activity activity, RecyclerView recyclerView,
                            ArrayList<? extends IMediaItemBase> itemList, boolean isPhone) {
        this.context = activity;
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.itemList = (ArrayList<MediaItemCollection>) itemList;
        this.isPhone = isPhone;
        clickListener = new DefaultClickListener(activity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_grid_item, parent, false);
        ViewHolder holder = new ViewHolder(itemView);
        setOnClicks(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mediaImageView.setVisibility(View.VISIBLE);
        MediaItemCollection mediaItem = itemList.get(position);

        holder.title.setText(mediaItem.getItemTitle());
        if ( mediaItem.getItemType() == ItemType.ALBUM ) {
            holder.subTitle.setText(mediaItem.getItemSubTitle());
        }
        else {
            holder.subTitle.setText(createMediaInfo(mediaItem));
        }

        int size = setSize(holder);
        if(null == mediaItem.getItemArtUrl()) {
            if ( mediaItem.getItemType() == ItemType.ARTIST ) {
                mediaItem.setItemArtUrl(App.getPlayingQueueHandler().getUpNextList().getArtistArtList().get(mediaItem.getItemId()));
            }
            else {
                mediaItem.setItemArtUrl(App.getPlayingQueueHandler().getUpNextList().getAlbumArtList().get(mediaItem.getItemSubTitle()));
            }
        }

        if(null == mediaItem.getItemArtUrl())
            mediaItem.setItemArtUrl(MediaItem.UNKNOWN_ART_URL);

        setMediaImage(holder, mediaItem);
        holder.overflowMenu.setVisibility(View.VISIBLE);
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }


    private String createMediaInfo(MediaItemCollection mediaItem) {
        int count = mediaItem.getItemCount();
        int albumCount = mediaItem.getItemListCount();
        StringBuilder sb = new StringBuilder();
        sb.append((count<=1 ? context.getResources().getString(R.string.song) : context.getResources().getString(R.string.songs)));
        sb.append(" ");
        sb.append(count);
        sb.append(" ");
        sb.append((albumCount<=1 ? context.getResources().getString(R.string.album) :
                context.getResources().getString(R.string.albums)));
        sb.append(" ");
        sb.append(albumCount);

        return sb.toString();
    }


    private void setMediaImage(ViewHolder holder, MediaItemCollection item) {
        String path = item.getItemArtUrl();
        if ( path == null ) path = "";
        Picasso.with(context).load(new File(path))
                .placeholder(R.drawable.ic_default_art_grid)
                .into(holder.mediaImageView);
    }


    private int setSize(ViewHolder holder) {
        Utils utils = new Utils(context);
        int size = (utils.getWindowWidth(context) / (isPhone ? 2 : 3))
                - (int)context.getResources().getDimension(R.dimen.card_grid_img_margin);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
        holder.mediaImageView.setLayoutParams(layoutParams);
        holder.mediaImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return size;
    }


    private void setOnClicks(final ViewHolder holder) {

        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final int position = holder.getAdapterPosition();
                final MediaItemCollection mediaItem = itemList.get(position);

                recyclerView.smoothScrollToPosition(position);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        clickListener.onItemClicked(mediaItem);
                    }
                }, 100);
            }
        });

        holder.overflowMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final int position = holder.getAdapterPosition();
                final MediaItemCollection mediaItem = itemList.get(position);

                PopupMenu pm = new PopupMenu(context, view);
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        clickListener.onMenuClicked(item.getItemId(), mediaItem);
                        return false;
                    }
                });
                pm.inflate(R.menu.album_popup);
                pm.show();
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public RegularTextView title, subTitle;
        public ImageView mediaImageView;
        public View overflowMenu, mainView;
        public TableLayout artTable;

        public ViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            title = (RegularTextView) itemView.findViewById(R.id.card_grid_title);
            subTitle = (RegularTextView) itemView.findViewById(R.id.card_grid_sub_title);
            mediaImageView = (ImageView) itemView.findViewById(R.id.card_grid_default_img);
            artTable = (TableLayout)itemView.findViewById(R.id.card_grid_art_table);
            overflowMenu = itemView.findViewById(R.id.card_grid_menu);
        }
    }

    public interface ItemClickListener {
        void onItemClicked(MediaItemCollection mediaItem);
        void onMenuClicked(int itemId, MediaItemCollection mediaItem);
    }

    // TODO: Move this class out
    private static class DefaultClickListener implements ItemClickListener {

        private Activity activity;

        public DefaultClickListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onItemClicked(MediaItemCollection mediaItem) {
            Intent i = createItemTapIntent(mediaItem);
            activity.startActivity(i);
        }

        @Override
        public void onMenuClicked(int itemId, MediaItemCollection mediaItem) {
            ArrayList<? extends IMediaItemBase> itemToAdd = getMediaListToAdd(mediaItem);
            switch (itemId) {
                case R.id.popup_album_play_next:
                    App.getPlayingQueueHandler().getUpNextList().addItemAsPlayNext(itemToAdd);
                    break;
                case R.id.popup_album_add_queue:
                    App.getPlayingQueueHandler().getUpNextList().addItemAsUpNext(itemToAdd);
                    break;
                case R.id.popup_album_add_playlist:
                    Utils util = new Utils(activity);
                    util.addToPlaylist(activity, itemToAdd, null);
                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);
                    break;
            }
        }


        private ArrayList<? extends IMediaItemBase> getMediaListToAdd(MediaItemCollection mediaItem) {

            final MediaController controller = MediaController.getInstance(activity);

            switch (mediaItem.getItemType()) {
                case GENRE: {
                    if(mediaItem.getParentType() == ItemType.GENRE && mediaItem.getMediaElement().size() == 0)
                        mediaItem.setMediaElement(controller.getGenreAlbumsList(mediaItem));

                    IMediaItemCollection rootCollection = (IMediaItemCollection)mediaItem.getMediaElement().get(0);
                    if(rootCollection.getMediaElement().size() == 0)
                        rootCollection.setMediaElement(controller.getGenreTrackList(mediaItem));

                    return rootCollection.getMediaElement();
                }

                case ARTIST: {
                    if(mediaItem.getParentType() == ItemType.ARTIST && mediaItem.getMediaElement().size() == 0)
                        mediaItem.setMediaElement(controller.getArtistAlbumsList(mediaItem));

                    IMediaItemCollection rootCollection = (IMediaItemCollection)mediaItem.getMediaElement().get(0);
                    if(rootCollection.getMediaElement().size() == 0)
                        rootCollection.setMediaElement(controller.getArtistTrackList(mediaItem));

                    return rootCollection.getMediaElement();
                }

                case ALBUM: {
                    if(mediaItem.getMediaElement().size() == 0)
                        mediaItem.setMediaElement(MediaController.getInstance(activity).getAlbumTrackList(mediaItem));
                    return mediaItem.getMediaElement();
                }

                default:
                    break;
            }

            return null;
        }

        private Intent createItemTapIntent(MediaItemCollection mediaItem) {
            Class<?> theClass = AlbumDetailItemActivity.class;
            if ( mediaItem.getItemType() == ItemType.ALBUM ) {
                theClass = AlbumDetailActivity.class;
            }

            Intent i = new Intent(activity, theClass);
            i.putExtra("mediaItemCollection", mediaItem);
            return i;
        }
    }
}
