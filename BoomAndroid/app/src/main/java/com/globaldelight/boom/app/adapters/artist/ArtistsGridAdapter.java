package com.globaldelight.boom.app.adapters.artist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TableLayout;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.app.activities.AlbumDetailItemActivity;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.collection.local.callback.IMediaItemCollection;
import com.globaldelight.boom.view.RegularTextView;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 8/8/2016.
 */
public class ArtistsGridAdapter extends RecyclerView.Adapter<ArtistsGridAdapter.SimpleItemViewHolder>{

    private static final String TAG = "AlbumListAdapter-TAG";
    ArrayList<MediaItemCollection> itemList;
    private Context context;
    private Activity activity;
    private  RecyclerView recyclerView;
    private boolean isPhone;

    public ArtistsGridAdapter(Context context, Activity activity, RecyclerView recyclerView,
                              ArrayList<? extends IMediaItemBase> itemList, boolean isPhone) {
        this.context = context;
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.itemList = (ArrayList<MediaItemCollection>) itemList;
        this.isPhone = isPhone;
    }

    @Override
    public ArtistsGridAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_grid_item, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ArtistsGridAdapter.SimpleItemViewHolder holder, final int position) {
        holder.defaultImg.setVisibility(View.VISIBLE);

        holder.title.setText(itemList.get(position).getItemTitle());
        int count = itemList.get(position).getItemCount();
        int albumCount = itemList.get(position).getItemListCount();
        holder.subTitle.setText((count<=1 ? context.getResources().getString(R.string.song) : context.getResources().getString(R.string.songs)) +" "+count+" "+
                (albumCount<=1 ? context.getResources().getString(R.string.album) : context.getResources().getString(R.string.albums)) +" "+albumCount);
        int size = setSize(holder);
        if(null==itemList.get(position).getItemArtUrl())
            itemList.get(position).setItemArtUrl(App.playbackManager().queue().getArtistArtList().get(itemList.get(position).getItemId()));

        if(null==itemList.get(position).getItemArtUrl())
            itemList.get(position).setItemArtUrl(MediaItem.UNKNOWN_ART_URL);

        setArtistImg(holder, position, size);

        holder.grid_menu.setVisibility(View.VISIBLE);
        setOnClicks(holder, position);
    }

    private void setArtistImg(final SimpleItemViewHolder holder, final int position, final int size) {
        String path = itemList.get(position).getItemArtUrl();
        Glide.with(context).load(path)
                .placeholder(R.drawable.ic_default_art_grid)
                .fitCenter()
                .into(holder.defaultImg);
    }

    private void setOnClicks(final SimpleItemViewHolder holder, final int position) {

        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(position);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(context, AlbumDetailItemActivity.class);
                        i.putExtra("mediaItemCollection", itemList.get(position));
                        context.startActivity(i);
                    }
                }, 100);
//                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_MUSIC_PLAYED_FROM_ARTIST_SECTION);

            }
        });

        holder.grid_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchorView) {
                PopupMenu pm = new PopupMenu(context, anchorView);
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        try {
                            if(itemList.get(position).getParentType() == ItemType.ARTIST && itemList.get(position).getMediaElement().size() == 0)
                                itemList.get(position).setMediaElement(MediaController.getInstance(context).getArtistAlbumsList(itemList.get(position)));

                            if(((IMediaItemCollection)itemList.get(position).getMediaElement().get(0)).getMediaElement().size() == 0)
                                ((IMediaItemCollection)itemList.get(position).getMediaElement().get(0)).
                                        setMediaElement(MediaController.getInstance(activity).getArtistTrackList(itemList.get(position)));

                            switch (item.getItemId()) {
                                case R.id.popup_album_play_next:
                                    App.playbackManager().queue().addItemAsPlayNext(((IMediaItemCollection)itemList.get(position).getMediaElement().get(0)).getMediaElement());
                                    break;
                                case R.id.popup_album_add_queue:
                                    App.playbackManager().queue().addItemAsUpNext(((IMediaItemCollection)itemList.get(position).getMediaElement().get(0)).getMediaElement());
                                    break;
                                case R.id.popup_album_add_playlist:
                                    Utils.addToPlaylist(activity, ((IMediaItemCollection) itemList.get(position).getMediaElement().get(0)).getMediaElement(), null);
//                                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);
                                    FlurryAnalytics.getInstance(activity.getApplicationContext()).setEvent(FlurryEvents.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);
                                    break;
                            }
                        }catch (Exception e){ }
                        return false;
                    }
                });
                pm.inflate(R.menu.album_popup);
                pm.show();
            }
        });

    }

    private int setSize(SimpleItemViewHolder holder) {
        int size = (Utils.getWindowWidth(context) / (isPhone ? 2 : 3))
                - (int)context.getResources().getDimension(R.dimen.card_grid_img_margin);

//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (size/(isPhone?2.5:3)));
//        holder.gridBottomBg.setLayoutParams(params);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
        holder.defaultImg.setLayoutParams(layoutParams);
        holder.defaultImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return size;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public RegularTextView title, subTitle;
        public ImageView defaultImg;
        public View gridBottomBg, grid_menu, mainView;
        public TableLayout artTable;
        public FrameLayout imgPanel;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            title = (RegularTextView) itemView.findViewById(R.id.card_grid_title);
            subTitle = (RegularTextView) itemView.findViewById(R.id.card_grid_sub_title);
            defaultImg = (ImageView) itemView.findViewById(R.id.card_grid_default_img);
            artTable = (TableLayout)itemView.findViewById(R.id.card_grid_art_table);
            gridBottomBg = itemView.findViewById(R.id.card_grid_bottom);
            grid_menu = itemView.findViewById(R.id.card_grid_menu);
            imgPanel = (FrameLayout) itemView.findViewById(R.id.card_grid_img_panel);
        }
    }
}
