package com.globaldelight.boom.app.adapters.album;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.app.activities.AlbumDetailActivity;
import com.globaldelight.boom.app.activities.AlbumSongListActivity;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.callback.IMediaItemCollection;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.app.adapters.model.ListDetail;
import com.globaldelight.boom.utils.OverFlowMenuUtils;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.view.RegularTextView;
import com.globaldelight.boom.utils.Utils;

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
            MediaItemCollection currentItem = (MediaItemCollection)collection.getItemAt(pos);

            switch (holder.getItemViewType()) {

                case ITEM_VIEW_ALBUM:
                    holder.title.setText(currentItem.getItemTitle());
                    holder.subTitle.setText(currentItem.getItemSubTitle());
                    holder.defaultImg.setVisibility(View.VISIBLE);
                    if(null == currentItem.getItemArtUrl())
                        currentItem.setItemArtUrl(App.playbackManager().queue().getAlbumArtList().get(currentItem.getItemTitle()));

                    if(null == currentItem.getItemArtUrl())
                        currentItem.setItemArtUrl(MediaItem.UNKNOWN_ART_URL);

                    setArtistImg(holder, pos, size, currentItem.getItemArtUrl());
                    setOnClicks(holder, pos, ITEM_VIEW_ALBUM);
                    break;
                case ITEM_VIEW_SONG:
                    if (currentItem.getArtUrlList().isEmpty())
                        currentItem.setArtUrlList(MediaController.getInstance(activity).getArtUrlList(collection));

                    if (currentItem.getArtUrlList().isEmpty()) {
                        ArrayList list = new ArrayList();
                        list.add(MediaItem.UNKNOWN_ART_URL);
                        currentItem.setArtUrlList(list);
                    }
                    int artCount = currentItem.getArtUrlList().size();
                    if (artCount >= 1 && !currentItem.getArtUrlList().get(0).equals(MediaItem.UNKNOWN_ART_URL)) {
                        holder.artTable.setVisibility(View.VISIBLE);
                        setSongsArtImage(holder, pos, size, currentItem.getArtUrlList());
                    } else {
                        holder.defaultImg.setVisibility(View.VISIBLE);
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
                        holder.defaultImg.setLayoutParams(layoutParams);

                        setDefaultImage(holder.defaultImg, size, size);
                    }
                    holder.title.setText(currentItem.getItemTitle());

                    StringBuilder countStr = new StringBuilder();
                    countStr.append(currentItem.getItemCount() > 1 ? activity.getResources().getString(R.string.songs) : activity.getResources().getString(R.string.song));
                    countStr.append(" ");
                    countStr.append(currentItem.getItemCount());

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

        PlayerUtils.setSongsArtTable(activity, Urls, new ImageView[]{holder.artImg1, holder.artImg2, holder.artImg3, holder.artImg4});
    }

    @Override
    public int getItemCount() {
        return this.collection.count()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if(isHeader(position)){
            return TYPE_HEADER;
        }else {
            if (collection.getItemAt(position-1).getItemType() == ItemType.SONGS) {
                return ITEM_VIEW_SONG;
            }else {
                return ITEM_VIEW_ALBUM;
            }
        }
    }


    private void setArtistImg(final SimpleItemViewHolder holder, final int position, final int size, String path) {
        if ( path == null ) path = "";
        Glide.with(activity)
                .load(path)
                .placeholder(R.drawable.ic_default_art_grid)
                .fitCenter()
                .into(holder.defaultImg);
    }

    private void setDefaultImage(ImageView img, int width, int height){
        img.setImageDrawable(activity.getResources().getDrawable( R.drawable.ic_default_art_grid, null));
    }

    private void setOnMenuClickListener(SimpleItemViewHolder holder, final int position) {
        holder.mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchorView) {
                IMediaItemCollection theCollection = (IMediaItemCollection) collection.getItemAt(0);
                if(collection.getParentType() == ItemType.ARTIST && theCollection.count() == 0){
                    theCollection.setMediaElement(MediaController.getInstance(activity).getArtistTrackList(collection));
                }else if(collection.getParentType() == ItemType.GENRE && theCollection.count() == 0){
                    theCollection.setMediaElement(MediaController.getInstance(activity).getGenreTrackList(collection));
                }
                OverFlowMenuUtils.showCollectionMenu(activity, anchorView, R.menu.collection_header_popup, theCollection);
            }
        });
    }

    private void setOnClicks(final SimpleItemViewHolder holder, final int position, final int itemView) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {
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
                IMediaItemCollection currentItem = ((IMediaItemCollection)collection.getItemAt(position));
                if(collection.getParentType() == ItemType.ARTIST && currentItem.count() == 0){
                    if(currentItem.getItemType() == ItemType.SONGS){
                        currentItem.setMediaElement(MediaController.getInstance(activity).getArtistTrackList(collection));
                    }else{
                        collection.setCurrentIndex(position);
                        currentItem.setMediaElement(MediaController.getInstance(activity).getArtistAlbumsTrackList(collection));
                    }
                }else if(collection.getParentType() == ItemType.GENRE && currentItem.count() == 0){
                    if(currentItem.getItemType() == ItemType.SONGS){
                        currentItem.setMediaElement(MediaController.getInstance(activity).getGenreTrackList(collection));
                    }else{
                        collection.setCurrentIndex(position);
                        currentItem.setMediaElement(MediaController.getInstance(activity).getGenreAlbumsTrackList(collection));
                    }
                }

                OverFlowMenuUtils.showCollectionMenu(activity, view, R.menu.collection_popup, currentItem);
            }
        });
    }

    private int setSize(SimpleItemViewHolder holder) {
        int size = (Utils.getWindowWidth(activity) / (isPhone ? 2 : 3))
                - (int)activity.getResources().getDimension(R.dimen.card_grid_img_margin);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
        holder.defaultImg.setLayoutParams(layoutParams);
        holder.defaultImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

        TableRow.LayoutParams tableParams = new TableRow.LayoutParams(size, size);
        holder.imgPanel.setLayoutParams(tableParams);

        return size;
    }

    public static class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        public RegularTextView title, subTitle;
        public ImageView defaultImg, artImg1, artImg2, artImg3, artImg4;
        public View gridBottomBg, grid_menu;
        public TableLayout artTable;
        public FrameLayout imgPanel;
        public RegularTextView headerSubTitle, headerDetail;
        ImageView mMore;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            title = (RegularTextView) itemView.findViewById(R.id.card_grid_title);
            subTitle = (RegularTextView) itemView.findViewById(R.id.card_grid_sub_title);
            defaultImg = (ImageView) itemView.findViewById(R.id.card_grid_default_img);
            artImg1 = (ImageView) itemView.findViewById(R.id.card_grid_art_img1);
            artImg2 = (ImageView) itemView.findViewById(R.id.card_grid_art_img2);
            artImg3 = (ImageView) itemView.findViewById(R.id.card_grid_art_img3);
            artImg4 = (ImageView) itemView.findViewById(R.id.card_grid_art_img4);
            artTable = (TableLayout)itemView.findViewById(R.id.card_grid_art_table);
            gridBottomBg = itemView.findViewById(R.id.card_grid_bottom);
            grid_menu = itemView.findViewById(R.id.card_grid_menu);
            imgPanel = (FrameLayout) itemView.findViewById(R.id.card_grid_img_panel);

            headerSubTitle = (RegularTextView) itemView.findViewById(R.id.header_sub_title);
            headerDetail = (RegularTextView) itemView.findViewById(R.id.header_detail);
            mMore = (ImageView) itemView.findViewById(R.id.recycler_header_menu);
        }

    }
}

