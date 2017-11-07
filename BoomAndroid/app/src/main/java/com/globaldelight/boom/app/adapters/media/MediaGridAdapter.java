package com.globaldelight.boom.app.adapters.media;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.activities.AlbumDetailActivity;
import com.globaldelight.boom.app.activities.AlbumDetailItemActivity;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.collection.local.callback.IMediaItemCollection;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.utils.OverFlowMenuUtils;
import com.globaldelight.boom.utils.Utils;

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
        holder.position = position;
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
                mediaItem.setItemArtUrl(App.playbackManager().queue().getArtistArtList().get(mediaItem.getItemId()));
            }
            else {
                mediaItem.setItemArtUrl(App.playbackManager().queue().getAlbumArtList().get(mediaItem.getItemSubTitle()));
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
        final int size = Utils.largeImageSize(context);
        Glide.with(context).load(item.getItemArtUrl())
                .placeholder(R.drawable.ic_default_art_grid)
                .centerCrop()
                .override(size, size)
                .into(holder.mediaImageView);
    }


    private int setSize(ViewHolder holder) {
        int size = (Utils.getWindowWidth(context) / (isPhone ? 2 : 3))
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

                final int position = holder.position;
                final MediaItemCollection mediaItem = itemList.get(position);

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
                final int position = holder.position;
                final MediaItemCollection mediaItem = itemList.get(position);
                OverFlowMenuUtils.showCollectionMenu((Activity)context, view, R.menu.collection_popup, getCollectionToAdd(mediaItem));
            }
        });
    }

    private IMediaItemCollection getCollectionToAdd(MediaItemCollection mediaItem) {

        final MediaController controller = MediaController.getInstance(activity);

        switch (mediaItem.getItemType()) {
            case ItemType.GENRE: {
                if(mediaItem.getParentType() == ItemType.GENRE && mediaItem.getMediaElement().size() == 0)
                    mediaItem.setMediaElement(controller.getGenreAlbumsList(mediaItem));

                IMediaItemCollection rootCollection = (IMediaItemCollection)mediaItem.getItemAt(0);
                if(rootCollection.getMediaElement().size() == 0)
                    rootCollection.setMediaElement(controller.getGenreTrackList(mediaItem));

                return rootCollection;
            }

            case ItemType.ARTIST: {
                if(mediaItem.getParentType() == ItemType.ARTIST && mediaItem.getMediaElement().size() == 0)
                    mediaItem.setMediaElement(controller.getArtistAlbumsList(mediaItem));

                IMediaItemCollection rootCollection = (IMediaItemCollection)mediaItem.getItemAt(0);
                if(rootCollection.getMediaElement().size() == 0)
                    rootCollection.setMediaElement(controller.getArtistTrackList(mediaItem));

                return rootCollection;
            }

            case ItemType.ALBUM: {
                if(mediaItem.getMediaElement().size() == 0)
                    mediaItem.setMediaElement(MediaController.getInstance(activity).getAlbumTrackList(mediaItem));
                return mediaItem;
            }

            default:
                break;
        }

        return null;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title, subTitle;
        public ImageView mediaImageView;
        public View overflowMenu, mainView;
        public TableLayout artTable;
        public int position = -1;

        public ViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            title = (TextView) itemView.findViewById(R.id.card_grid_title);
            subTitle = (TextView) itemView.findViewById(R.id.card_grid_sub_title);
            mediaImageView = (ImageView) itemView.findViewById(R.id.card_grid_default_img);
            artTable = (TableLayout)itemView.findViewById(R.id.card_grid_art_table);
            overflowMenu = itemView.findViewById(R.id.card_grid_menu);
        }
    }

    public interface ItemClickListener {
        void onItemClicked(MediaItemCollection mediaItem);
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

        private Intent createItemTapIntent(MediaItemCollection mediaItem) {
            Class<?> theClass = AlbumDetailItemActivity.class;
            if ( mediaItem.getItemType() == ItemType.ALBUM ) {
                theClass = AlbumDetailActivity.class;
            }

            Intent i = new Intent(activity, theClass);
            Bundle b = new Bundle();
            b.putParcelable("mediaItemCollection", mediaItem);
            i.putExtra("bundle", b);
            return i;
        }
    }
}
