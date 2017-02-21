package com.globaldelight.boom.ui.musiclist.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
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
import com.globaldelight.boom.ui.musiclist.activity.AlbumSongListActivity;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 8/8/2016.
 */
public class DefaultPlayListAdapter extends RecyclerView.Adapter<DefaultPlayListAdapter.SimpleItemViewHolder>{

    private static final String TAG = "AlbumListAdapter-TAG";
    ArrayList<MediaItemCollection> itemList;
    private Context context;
    private Activity activity;
    private  RecyclerView recyclerView;
    private boolean isPhone;

    public DefaultPlayListAdapter(Context context, Activity activity, RecyclerView recyclerView,
                                  ArrayList<? extends IMediaItemBase> itemList, boolean isPhone) {
        this.context = context;
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.itemList = (ArrayList<MediaItemCollection>) itemList;
        this.isPhone = isPhone;
    }

    @Override
    public DefaultPlayListAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_grid_item, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final DefaultPlayListAdapter.SimpleItemViewHolder holder, final int position) {

        if(itemList.get(position).getArtUrlList().isEmpty())
            itemList.get(position).setArtUrlList(MediaController.getInstance(context).getArtUrlList(itemList.get(position)));

        if(itemList.get(position).getArtUrlList().isEmpty()) {
            ArrayList list = new ArrayList();
            list.add(MediaItem.UNKNOWN_ART_URL);
            itemList.get(position).setArtUrlList(list);
        }

        int size= setSize(holder);
        if(itemList.get(position).getArtUrlList().size() >= 1 && PlayerUtils.isPathValid(itemList.get(position).getArtUrlList().get(0))){
            holder.artTable.setVisibility(View.VISIBLE);
            setSongsArtImage(holder, position, size, itemList.get(position).getArtUrlList());
        }else /*if(itemList.get(position).getArtUrlList().size() == 0 || !PlayerUtils.isPathValid(itemList.get(position).getArtUrlList().get(0)))*/{
            holder.defaultImg.setVisibility(View.VISIBLE);
            setDefaultImage(holder.defaultImg, size, size);
        }

        holder.title.setText(itemList.get(position).getItemTitle());
        int itemcount = itemList.get(position).getItemCount();
        holder.subTitle.setText((itemcount > 1 ? context.getResources().getString(R.string.songs):  context.getResources().getString(R.string.song))+" "+ itemcount);

        holder.grid_menu.setVisibility(View.VISIBLE);
        setOnClicks(holder, position);
    }

    private int setSize(SimpleItemViewHolder holder) {
        Utils utils = new Utils(context);
        int size = (utils.getWindowWidth(context) / (isPhone ? 2 : 3))
                - (int)context.getResources().getDimension(R.dimen.card_grid_img_margin);

//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (size/(isPhone?2.5:3)));
//        holder.gridBottomBg.setLayoutParams(params);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
        holder.defaultImg.setLayoutParams(layoutParams);
        holder.defaultImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

        TableRow.LayoutParams tableParams = new TableRow.LayoutParams(size, size);
        holder.imgPanel.setLayoutParams(tableParams);

        return size;
    }

    private void setSongsArtImage(final SimpleItemViewHolder holder, final int position, final int size, final ArrayList<String> Urls) {

        int count = Urls.size()>4?4:Urls.size();
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
                Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size/2, size/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                break;
            case 2:
                Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                Picasso.with(context).load(new File(Urls.get(1))).error(context.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                Picasso.with(context).load(new File(Urls.get(1))).error(context.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                break;
            case 3:
                Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                Picasso.with(context).load(new File(Urls.get(1))).error(context.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                Picasso.with(context).load(new File(Urls.get(2))).error(context.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                break;
            case 4:
                Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                Picasso.with(context).load(new File(Urls.get(1))).error(context.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                Picasso.with(context).load(new File(Urls.get(2))).error(context.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                Picasso.with(context).load(new File(Urls.get(3))).error(context.getResources().getDrawable(R.drawable.ic_default_art_grid, null))
                        /*.centerCrop().resize(size.width/2, size.height/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                break;
        }
    }

    private void setDefaultImage(ImageView img, int width, int height){
        img.setImageDrawable(context.getResources().getDrawable( R.drawable.ic_default_art_grid, null));
    }

    private void setOnClicks(final SimpleItemViewHolder holder, final int position) {

        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(position);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(context, AlbumSongListActivity.class);
                        i.putExtra("mediaItemCollection", itemList.get(position));
                        context.startActivity(i);
                    }
                }, 100);
            }
        });

        holder.grid_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu pm = new PopupMenu(context, view);
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        try {
                            if(itemList.get(position).getMediaElement().size() == 0)
                                itemList.get(position).setMediaElement(MediaController.getInstance(context).getPlayListTrackList(itemList.get(position)));

                            switch (item.getItemId()) {
                                case R.id.popup_album_add_queue:
                                    App.getPlayingQueueHandler().getUpNextList().addCollectionToUpNext(context, itemList.get(position));
                                    break;
                                case R.id.popup_album_add_playlist:
                                    Utils util = new Utils(context);
                                    util.addToPlaylist(activity, itemList.get(position).getMediaElement(), null);
                                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);
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

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public RegularTextView title, subTitle;
        public ImageView defaultImg, artImg1, artImg2, artImg3, artImg4;
        public View gridBottomBg, grid_menu, mainView;
        public TableLayout artTable;
        public FrameLayout imgPanel;

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
            grid_menu = itemView.findViewById(R.id.card_grid_menu);
            imgPanel = (FrameLayout) itemView.findViewById(R.id.card_grid_img_panel);
        }
    }

}
