package com.globaldelight.boom.ui.musiclist.adapter;

import android.app.Activity;
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
import android.widget.TableRow;
import com.globaldelight.boom.Media.MediaController;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.ui.musiclist.activity.AlbumDetailActivity;
import com.globaldelight.boom.ui.musiclist.activity.AlbumSongListActivity;
import com.globaldelight.boom.manager.PlayerServiceReceiver;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.Media.ItemType;
import com.globaldelight.boom.ui.musiclist.ListDetail;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 8/10/2016.
 */
public class DetailAlbumGridAdapter extends RecyclerView.Adapter<DetailAlbumGridAdapter.SimpleItemViewHolder> {

    private static final String TAG = "ArtistListAdapter-TAG";
    public static final int TYPE_HEADER = 111;
    private static final int ITEM_VIEW_ALBUM = 222;
    private static final int ITEM_VIEW_SONG = 333;
    private MediaItemCollection collection;
    private Activity activity;
    private  RecyclerView recyclerView;
    private ListDetail listDetail;
    private boolean isPhone;

    public boolean isHeader(int position) {
        return position == 0;
    }

    public DetailAlbumGridAdapter(Activity activity, RecyclerView recyclerView,
                                  IMediaItemCollection collection, ListDetail listDetail, boolean isPhone) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.collection = (MediaItemCollection) collection;
        this.listDetail = listDetail;
        this.isPhone = isPhone;
    }

    @Override
    public DetailAlbumGridAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if(viewType == ITEM_VIEW_ALBUM || viewType == ITEM_VIEW_SONG) {
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.card_grid_item, parent, false);
        }else if(viewType == TYPE_HEADER){
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.card_header_recycler_view, parent, false);
        }
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final DetailAlbumGridAdapter.SimpleItemViewHolder holder, final int position) {
        if(isHeader(position)){
            if(listDetail.getmSubTitle() != null) {
                holder.headerSubTitle.setText(listDetail.getmSubTitle());
            }else{
                holder.headerSubTitle.setVisibility(View.GONE);
            }
            if(listDetail.getmDetail() != null) {
                holder.headerDetail.setText(listDetail.getmDetail());
            }else{
                holder.headerDetail.setVisibility(View.GONE);
            }

            holder.mMore.setVisibility(View.VISIBLE);
            setOnMenuClickListener(holder, position);
        }else if(!isHeader(position)) {
            int pos = position - 1;
            int size = setSize(holder);

            holder.grid_menu.setVisibility(View.VISIBLE);

            switch (holder.getItemViewType()) {

                case ITEM_VIEW_ALBUM:
                    holder.title.setText(collection.getMediaElement().get(pos).getItemTitle());
                    holder.subTitle.setText(((MediaItemCollection) collection.getMediaElement().get(pos)).getItemSubTitle());
                    holder.defaultImg.setVisibility(View.VISIBLE);
                    if(null == collection.getMediaElement().get(pos).getItemArtUrl())
                        collection.getMediaElement().get(pos).setItemArtUrl(App.getPlayingQueueHandler().getUpNextList().getAlbumArtList().get(((MediaItemCollection) collection.getMediaElement().get(pos)).getItemTitle()));

                    if(null == collection.getMediaElement().get(pos).getItemArtUrl())
                        collection.getMediaElement().get(pos).setItemArtUrl(MediaItem.UNKNOWN_ART_URL);

                    setArtistImg(holder, pos, size, collection.getMediaElement().get(pos).getItemArtUrl());
                    setOnClicks(holder, pos, ITEM_VIEW_ALBUM);
                    break;
                case ITEM_VIEW_SONG:
                    if (((MediaItemCollection) collection.getMediaElement().get(pos)).getArtUrlList().isEmpty())
                        ((MediaItemCollection) collection.getMediaElement().get(pos)).setArtUrlList(MediaController.getInstance(activity).getArtUrlList(collection));

                    if (((MediaItemCollection) collection.getMediaElement().get(pos)).getArtUrlList().isEmpty()) {
                        ArrayList list = new ArrayList();
                        list.add(MediaItem.UNKNOWN_ART_URL);
                        ((MediaItemCollection) collection.getMediaElement().get(pos)).setArtUrlList(list);
                    }
                    int artCount = ((MediaItemCollection) collection.getMediaElement().get(pos)).getArtUrlList().size();
                    if (artCount >= 1 && !((MediaItemCollection) collection.getMediaElement().get(pos)).getArtUrlList().get(0).equals(MediaItem.UNKNOWN_ART_URL)) {
                        holder.artTable.setVisibility(View.VISIBLE);
                        setSongsArtImage(holder, pos, size, ((MediaItemCollection) collection.getMediaElement().get(pos)).getArtUrlList());
                    } else {
                        holder.defaultImg.setVisibility(View.VISIBLE);
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
                        holder.defaultImg.setLayoutParams(layoutParams);

                        setDefaultImage(holder.defaultImg, size, size);
                    }
                    holder.title.setText(collection.getMediaElement().get(pos).getItemTitle());

                    StringBuilder countStr = new StringBuilder();
                    countStr.append(((MediaItemCollection) collection.getMediaElement().get(pos)).getItemCount() > 1 ? activity.getResources().getString(R.string.songs) : activity.getResources().getString(R.string.song));
                    countStr.append(" ");
                    countStr.append(((MediaItemCollection) collection.getMediaElement().get(pos)).getItemCount());

                    holder.subTitle.setText(countStr);
                    setOnClicks(holder, pos, ITEM_VIEW_SONG);
                    break;
            }
        }
    }

    private void setSongsArtImage(final SimpleItemViewHolder holder, final int position, final int size, final ArrayList<String> Urls) {

        int count = Urls.size() > 4 ? 4 : Urls.size();
        TableRow.LayoutParams param = new TableRow.LayoutParams(size/2, size/2);
        holder.artImg1.setLayoutParams(param);
        holder.artImg1.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.artImg2.setLayoutParams(param);
        holder.artImg2.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.artImg3.setLayoutParams(param);
        holder.artImg3.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.artImg4.setLayoutParams(param);
        holder.artImg4.setScaleType(ImageView.ScaleType.CENTER_CROP);

        switch (count){
            case 1:
                Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                break;
            case 2:
                Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                Picasso.with(activity).load(new File(Urls.get(1))).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                Picasso.with(activity).load(new File(Urls.get(1))).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                break;
            case 3:
                Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                Picasso.with(activity).load(new File(Urls.get(1))).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                Picasso.with(activity).load(new File(Urls.get(2))).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                break;
            case 4:
                Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                Picasso.with(activity).load(new File(Urls.get(1))).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                Picasso.with(activity).load(new File(Urls.get(2))).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                Picasso.with(activity).load(new File(Urls.get(3))).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return this.collection.getMediaElement().size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if(isHeader(position)){
            return TYPE_HEADER;
        }else {
            if (collection.getMediaElement().get(position-1).getItemType() == ItemType.SONGS) {
                return ITEM_VIEW_SONG;
            }else {
                return ITEM_VIEW_ALBUM;
            }
        }
    }

    @Override
    public void onViewRecycled(DetailAlbumGridAdapter.SimpleItemViewHolder holder) {
        super.onViewRecycled(holder);
//        holder.defaultImg.setImageDrawable(null);
    }

    private void setArtistImg(final SimpleItemViewHolder holder, final int position, final int size, String path) {
        if ( path == null ) path = "";
        Picasso.with(activity)
                .load(new File(path))
                .placeholder(R.drawable.ic_default_art_grid).noFade()
                .into(holder.defaultImg);
    }

    private void setDefaultImage(ImageView img, int width, int height){
        img.setImageDrawable(activity.getResources().getDrawable( R.drawable.ic_default_art_grid, null));
    }

    private void setOnMenuClickListener(SimpleItemViewHolder holder, final int position) {
        holder.mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchorView) {
                PopupMenu pm = new PopupMenu(activity, anchorView);
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(collection.getParentType() == ItemType.ARTIST && ((IMediaItemCollection) collection.getMediaElement().get(0)).getMediaElement().size() == 0){
                            ((IMediaItemCollection) collection.getMediaElement().get(0)).setMediaElement(MediaController.getInstance(activity).getArtistTrackList(collection));
                        }else if(collection.getParentType() == ItemType.GENRE && ((IMediaItemCollection) collection.getMediaElement().get(0)).getMediaElement().size() == 0){
                            ((IMediaItemCollection) collection.getMediaElement().get(0)).setMediaElement(MediaController.getInstance(activity).getGenreTrackList(collection));
                        }
                        switch (menuItem.getItemId()) {
                            case R.id.album_header_add_play_next:
                                App.getPlayingQueueHandler().getUpNextList().addItemAsPlayNext(((IMediaItemCollection) collection.getMediaElement().get(0)).getMediaElement());
                                break;
                            case R.id.album_header_add_to_upnext:
                                App.getPlayingQueueHandler().getUpNextList().addItemAsUpNext(((IMediaItemCollection) collection.getMediaElement().get(0)).getMediaElement());
                                break;
                            case R.id.album_header_add_to_playlist:
                                Utils util = new Utils(activity);
                                util.addToPlaylist(activity, ((IMediaItemCollection) collection.getMediaElement().get(0)).getMediaElement(), null);
                                break;
                            case R.id.album_header_shuffle:
                                if (App.getPlayingQueueHandler().getUpNextList() != null) {
                                    activity.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_SHUFFLE_SONG));
                                    App.getPlayingQueueHandler().getUpNextList().addItemListToPlay(((IMediaItemCollection) collection.getMediaElement().get(0)).getMediaElement(), 0);
                                }
                                break;
                        }
                        return false;
                    }
                });
                pm.inflate(R.menu.album_header_menu);
                pm.show();
            }
        });
    }

    private void setOnClicks(final SimpleItemViewHolder holder, final int position, final int itemView) {

        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(position);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        collection.setCurrentIndex(position);
                        Intent i = null;
                        if(itemView == ITEM_VIEW_SONG){
                            i = new Intent(activity, AlbumSongListActivity.class);
                        }else if (itemView == ITEM_VIEW_ALBUM) {
                            i = new Intent(activity, AlbumDetailActivity.class);
                        }
                        i.putExtra("mediaItemCollection", collection);
                        activity.startActivity(i);
                    }
                }, 100);
            }
        });

        holder.grid_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu pm = new PopupMenu(activity, view);
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(collection.getParentType() == ItemType.ARTIST && ((IMediaItemCollection) collection.getMediaElement().get(position)).getMediaElement().size() == 0){
                            if((collection.getMediaElement().get(position)).getItemType() == ItemType.SONGS){
                                ((IMediaItemCollection) collection.getMediaElement().get(position)).setMediaElement(MediaController.getInstance(activity).getArtistTrackList(collection));
                            }else{
                                collection.setCurrentIndex(position);
                                ((IMediaItemCollection) collection.getMediaElement().get(position)).setMediaElement(MediaController.getInstance(activity).getArtistAlbumsTrackList(collection));
                            }
                        }else if(collection.getParentType() == ItemType.GENRE && ((IMediaItemCollection) collection.getMediaElement().get(position)).getMediaElement().size() == 0){
                            if((collection.getMediaElement().get(position)).getItemType() == ItemType.SONGS){
                                ((IMediaItemCollection) collection.getMediaElement().get(position)).setMediaElement(MediaController.getInstance(activity).getGenreTrackList(collection));
                            }else{
                                collection.setCurrentIndex(position);
                                ((IMediaItemCollection) collection.getMediaElement().get(position)).setMediaElement(MediaController.getInstance(activity).getGenreAlbumsTrackList(collection));
                            }
                        }
                        switch (item.getItemId()) {
                            case R.id.popup_album_play_next :
                                App.getPlayingQueueHandler().getUpNextList().addItemAsPlayNext(((IMediaItemCollection) collection.getMediaElement().get(position)).getMediaElement());
                                break;
                            case R.id.popup_album_add_queue :
                                App.getPlayingQueueHandler().getUpNextList().addItemAsUpNext(((IMediaItemCollection) collection.getMediaElement().get(position)).getMediaElement());
                                break;
                            case R.id.popup_album_add_playlist:
                                Utils util = new Utils(activity);
                                util.addToPlaylist(activity, ((IMediaItemCollection)collection.getMediaElement().get(position)).getMediaElement(), null);
                                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);
                                break;
                        }
                        return false;
                    }
                });
                pm.inflate(R.menu.album_popup);
                pm.show();
            }
        });
    }

    private int setSize(SimpleItemViewHolder holder) {
        Utils utils = new Utils(activity);
        int size = (utils.getWindowWidth(activity) / (isPhone ? 2 : 3))
                - (int)activity.getResources().getDimension(R.dimen.card_grid_img_margin);

//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (size/(isPhone?2.5:3)));
//        holder.gridBottomBg.setLayoutParams(params);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
        holder.defaultImg.setLayoutParams(layoutParams);
        holder.defaultImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

        TableRow.LayoutParams tableParams = new TableRow.LayoutParams(size, size);
        holder.imgPanel.setLayoutParams(tableParams);

        return size;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public int defaultAlbumColor;
        public RegularTextView title, subTitle;
        public ImageView defaultImg, artImg1, artImg2, artImg3, artImg4;
        public View gridBottomBg, grid_menu, mainView;
        public TableLayout artTable;
        public FrameLayout imgPanel;
        public RegularTextView headerSubTitle, headerDetail;
        ImageView mShuffle, mMore;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            title = (RegularTextView) itemView.findViewById(R.id.card_grid_title);
            subTitle = (RegularTextView) itemView.findViewById(R.id.card_grid_sub_title);
            defaultImg = (ImageView) itemView.findViewById(R.id.card_grid_default_img);
            artImg1 = (ImageView) itemView.findViewById(R.id.card_grid_art_img1);
            artImg2 = (ImageView) itemView.findViewById(R.id.card_grid_art_img2);
            artImg3 = (ImageView) itemView.findViewById(R.id.card_grid_art_img3);
            artImg4 = (ImageView) itemView.findViewById(R.id.card_grid_art_img4);
            artTable = (TableLayout)itemView.findViewById(R.id.card_grid_art_table);
            gridBottomBg = itemView.findViewById(R.id.card_grid_bottom);
            grid_menu = itemView.findViewById(R.id.card_grid_overflow_menu);
            imgPanel = (FrameLayout) itemView.findViewById(R.id.card_grid_img_panel);

            headerSubTitle = (RegularTextView) itemView.findViewById(R.id.header_sub_title);
            headerDetail = (RegularTextView) itemView.findViewById(R.id.header_detail);
            mMore = (ImageView) itemView.findViewById(R.id.recycler_header_menu);
        }

    }
}

