package com.globaldelight.boom.ui.musiclist.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.ui.musiclist.ListDetail;
import com.globaldelight.boom.ui.musiclist.activity.AlbumActivity;
import com.globaldelight.boom.ui.musiclist.activity.DetailAlbumActivity;
import com.globaldelight.boom.ui.musiclist.activity.SongsDetailListActivity;
import com.globaldelight.boom.ui.widgets.CoachMarkTextView;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.reflect.Field;
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
    private PermissionChecker permissionChecker;
    private Context context;
    private  RecyclerView recyclerView;
    private ListDetail listDetail;

    public boolean isHeader(int position) {
        return position == 0;
    }

    public DetailAlbumGridAdapter(DetailAlbumActivity detailAlbumActivity, RecyclerView recyclerView,
                                  IMediaItemCollection collection, ListDetail listDetail, PermissionChecker permissionChecker) {
        this.context = detailAlbumActivity;
        this.recyclerView = recyclerView;
        this.collection = (MediaItemCollection) collection;
        this.permissionChecker = permissionChecker;
        this.listDetail = listDetail;
    }

    @Override
    public DetailAlbumGridAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if(viewType == ITEM_VIEW_ALBUM || viewType == ITEM_VIEW_SONG) {
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.card_grid_item, parent, false);
        }else if(viewType == TYPE_HEADER){
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.recycler_view_header, parent, false);
        }
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final DetailAlbumGridAdapter.SimpleItemViewHolder holder, final int position) {
        if(isHeader(position)){
            if(listDetail.getmTitle() != null) {
                holder.headerTitle.setText(listDetail.getmTitle());
            }else{
                holder.headerTitle.setVisibility(View.GONE);
            }
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
        }else if(!isHeader(position)) {
            int pos = position - 1;
            Size size = setSize(holder);
            switch (holder.getItemViewType()) {

                case ITEM_VIEW_ALBUM:
                    holder.title.setText(collection.getMediaElement().get(pos).getItemTitle());
                    holder.subTitle.setText(((MediaItemCollection) collection.getMediaElement().get(pos)).getItemSubTitle());
                    holder.defaultImg.setVisibility(View.VISIBLE);
                    setArtistImg(holder, pos, size, collection.getMediaElement().get(pos).getItemArtUrl());
                    setOnClicks(holder, pos, ITEM_VIEW_ALBUM);
                    break;
                case ITEM_VIEW_SONG:
                    if (collection.getArtUrlList().isEmpty())
                        ((MediaItemCollection) collection.getMediaElement().get(pos)).setArtUrlList(MediaController.getInstance(context).getArtUrlList(collection));
                /*ArrayList<String> artUrlList = MediaController.getInstance(context).getArtUrlList((MediaItemCollection) collection.getMediaElement().get(pos));*/
                    int artCount = ((MediaItemCollection) collection.getMediaElement().get(pos)).getArtUrlList().size();
                    if (artCount >= 1) {
                        holder.artTable.setVisibility(View.VISIBLE);
                        setSongsArtImage(holder, pos, size, ((MediaItemCollection) collection.getMediaElement().get(pos)).getArtUrlList());
                    } else if (artCount == 0) {
                        holder.defaultImg.setVisibility(View.VISIBLE);
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size.width, size.height);
                        holder.defaultImg.setLayoutParams(layoutParams);

                        setDefaultImage(holder.defaultImg, size.width, size.height);
                    } else {
                        holder.mainView.setVisibility(View.GONE);
                    }
                    holder.title.setText(collection.getMediaElement().get(pos).getItemTitle());

                    StringBuilder countStr = new StringBuilder();
                    countStr.append(((MediaItemCollection) collection.getMediaElement().get(pos)).getItemCount() > 1 ? context.getResources().getString(R.string.songs) : context.getResources().getString(R.string.song));
                    countStr.append(" ");
                    countStr.append(((MediaItemCollection) collection.getMediaElement().get(pos)).getItemCount());

                    holder.subTitle.setText(countStr);
                    setOnClicks(holder, pos, ITEM_VIEW_SONG);
                    break;
            }
        }
    }

    private void setSongsArtImage(final SimpleItemViewHolder holder, final int position, final Size size, final ArrayList<String> Urls) {

        int count = Urls.size() > 4 ? 4 : Urls.size();
        TableRow.LayoutParams param = new TableRow.LayoutParams(size.width/2, size.height/2);
        holder.artImg1.setLayoutParams(param);
        holder.artImg2.setLayoutParams(param);
        holder.artImg3.setLayoutParams(param);
        holder.artImg4.setLayoutParams(param);

        switch (count){
            case 1:
                Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                break;
            case 2:
                Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                Picasso.with(context).load(new File(Urls.get(1))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                Picasso.with(context).load(new File(Urls.get(1))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                break;
            case 3:
                Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                Picasso.with(context).load(new File(Urls.get(1))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                Picasso.with(context).load(new File(Urls.get(2))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                Picasso.with(context).load(new File(Urls.get(2))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                break;
            case 4:
                Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                Picasso.with(context).load(new File(Urls.get(1))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                Picasso.with(context).load(new File(Urls.get(2))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                Picasso.with(context).load(new File(Urls.get(3))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                        .centerCrop().resize(size.width/2, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
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

    private void setArtistImg(final SimpleItemViewHolder holder, final int position, final Size size, final String path) {

        if (isPathValid(path))
            Picasso.with(context).load(new File(path)).error(context.getResources().getDrawable(R.drawable.default_album_art_home, null)).noFade()
                    .centerCrop().resize(size.width, size.height)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.defaultImg);
        else
            setDefaultImage(holder.defaultImg, size.width, size.height);
    }

    private void setDefaultImage(ImageView img, int width, int height){
        img.setImageBitmap(Utils.getBitmapOfVector(context, R.drawable.default_album_art_home, width, height));
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
                            i = new Intent(context, SongsDetailListActivity.class);
                        }else if (itemView == ITEM_VIEW_ALBUM) {
                            i = new Intent(context, AlbumActivity.class);
                            i.putExtra("albumColor", holder.defaultAlbumColor);
                        }
                        i.putExtra("mediaItemCollection", collection);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                (Activity) context,
                                new Pair<View, String>(holder.defaultImg, "transition:imgholder")
                        );
                        ActivityCompat.startActivity((Activity) context, i, options.toBundle());
                    }
                }, 100);
            }
        });

        holder.grid_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.inflate(R.menu.album_popup);

                // Force icons to show
                Object menuHelper;
                Class[] argTypes;
                try {
                    Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
                    fMenuHelper.setAccessible(true);
                    menuHelper = fMenuHelper.get(popupMenu);
                    argTypes = new Class[] { boolean.class };
                    menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true);
                } catch (Exception e) {
                    // Possible exceptions are NoSuchMethodError and NoSuchFieldError
                    //
                    // In either case, an exception indicates something is wrong with the reflection code, or the
                    // structure of the PopupMenu class or its dependencies has changed.
                    //
                    // These exceptions should never happen since we're shipping the AppCompat library in our own apk,
                    // but in the case that they do, we simply can't force icons to display, so log the error and
                    // show the menu normally.

                    Log.w(TAG, "error forcing menu icons to show", e);
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.popup_album_add_queue :
                                if(itemView == ITEM_VIEW_SONG){
                                    ((IMediaItemCollection)collection.getMediaElement().get(collection.getCurrentIndex())).setMediaElement(MediaController.getInstance(context).getMediaCollectionItemDetails(collection));
                                    App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext(collection.getMediaElement().get(position));
                                }else if (itemView == ITEM_VIEW_ALBUM) {
                                    if(App.getPlayingQueueHandler().getUpNextList()!=null){
                                        ((IMediaItemCollection)collection.getMediaElement().get(position)).setMediaElement(MediaController.getInstance(context).getMediaCollectionItemDetails(collection));
                                        App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext(collection.getMediaElement().get(position));
                                    }
                                }

                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

    }

    private Size setSize(SimpleItemViewHolder holder) {
        Utils utils = new Utils(context);
        int size = (utils.getWindowWidth(context)
                - utils.dpToPx(context, 15)) / 2;
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(size, size);
        holder.imgPanel.setLayoutParams(layoutParams);
        return new Size(size, size);
    }

    private boolean fileExist(String albumArtPath) {
        File imgFile = new File(albumArtPath);
        return imgFile.exists();
    }

    public boolean isPathValid(String path) {
        return path != null && fileExist(path);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public class Size{
        int width;
        int height;

        public Size(int width, int height){
            this.width = width;
            this.height = height;
        }
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public int defaultAlbumColor;
        public RegularTextView title;
        public CoachMarkTextView subTitle;
        public ImageView defaultImg, artImg1, artImg2, artImg3, artImg4;
        public View gridBottomBg, grid_menu, mainView;
        public TableLayout artTable;
        public FrameLayout imgPanel;
        public RegularTextView headerTitle, headerSubTitle;
        public CoachMarkTextView headerDetail;
        ImageView mShuffle, mMore;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            title = (RegularTextView) itemView.findViewById(R.id.card_grid_title);
            subTitle = (CoachMarkTextView) itemView.findViewById(R.id.card_grid_sub_title);
            defaultImg = (ImageView) itemView.findViewById(R.id.card_grid_default_img);
            artImg1 = (ImageView) itemView.findViewById(R.id.card_grid_art_img1);
            artImg2 = (ImageView) itemView.findViewById(R.id.card_grid_art_img2);
            artImg3 = (ImageView) itemView.findViewById(R.id.card_grid_art_img3);
            artImg4 = (ImageView) itemView.findViewById(R.id.card_grid_art_img4);
            artTable = (TableLayout)itemView.findViewById(R.id.card_grid_art_table);
            gridBottomBg = itemView.findViewById(R.id.card_grid_bottom);
            grid_menu = itemView.findViewById(R.id.card_grid_menu);
            imgPanel = (FrameLayout) itemView.findViewById(R.id.card_grid_img_panel);

            headerTitle = (RegularTextView) itemView.findViewById(R.id.header_title);
            headerSubTitle = (RegularTextView) itemView.findViewById(R.id.header_sub_title);
            headerDetail = (CoachMarkTextView) itemView.findViewById(R.id.header_detail);
        }

    }
}

