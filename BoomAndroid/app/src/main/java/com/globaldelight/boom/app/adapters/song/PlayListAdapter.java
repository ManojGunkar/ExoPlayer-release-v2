package com.globaldelight.boom.app.adapters.song;

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
import com.globaldelight.boom.app.activities.ActivityContainer;
import com.globaldelight.boom.app.activities.AlbumSongListActivity;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.view.RegularTextView;
import com.globaldelight.boom.utils.OverFlowMenuUtils;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 8/8/2016.
 */
public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.SimpleItemViewHolder>{

    private static final String TAG = "AlbumListAdapter-TAG";
    private static final int ITEM_VIEW_TYPE_RECENT = 0;
    private static final int ITEM_VIEW_TYPE_FAVOURITE = 1;
    private static final int ITEM_VIEW_TYPE_BOOM_PLAYLIST = 2;
    private static final int ITEM_VIEW_TYPE_PLAYLIST = 3;
    private int recentStartPos, favStartPos, boomPlaylisStartPos,
            playlistStartPos;
    private Activity activity;
    private  RecyclerView recyclerView;
    private ArrayList<? extends IMediaItemBase> defaultPlayList, mBoomPlayList;
    private IMediaItemBase mFavourite, mRecentPlayed;
    private boolean isPhone;

    public PlayListAdapter(Activity activity, RecyclerView recyclerView, IMediaItemBase mRecentPlayed, IMediaItemBase mFavourite, ArrayList<? extends IMediaItemBase> mBoomPlayList, ArrayList<? extends IMediaItemBase> defaultPlayList, boolean isPhone) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.mRecentPlayed = mRecentPlayed;
        this.mFavourite = mFavourite;
        this.mBoomPlayList = mBoomPlayList;
        this.defaultPlayList = defaultPlayList;
        this.isPhone = isPhone;

        initListPosition();
    }

    private void initListPosition() {
        recentStartPos = 0;
        favStartPos = 1;
        boomPlaylisStartPos = 2;
        playlistStartPos = this.mBoomPlayList.size() + 2;
    }

    public void updateNewList(IMediaItemBase mRecentPlayed, IMediaItemBase mFavourite, ArrayList<? extends IMediaItemBase> boomPlayList) {
        this.mRecentPlayed = mRecentPlayed;
        this.mFavourite = mFavourite;
        this.mBoomPlayList = boomPlayList;
        initListPosition();

        notifyDataSetChanged();
    }

    private int whatView(int position){
        if (position >= playlistStartPos) {
            return ITEM_VIEW_TYPE_PLAYLIST;
        }else if (position >= boomPlaylisStartPos && position < playlistStartPos) {
            return ITEM_VIEW_TYPE_BOOM_PLAYLIST;
        }else if (position == favStartPos) {
            return ITEM_VIEW_TYPE_FAVOURITE;
        }else{
            return ITEM_VIEW_TYPE_RECENT;
        }
    }

    private int getPosition(int position) {
        if (position >= playlistStartPos) {
            return position - playlistStartPos;
        } else if (position >= boomPlaylisStartPos && position < playlistStartPos) {
            return position - boomPlaylisStartPos;
        } else if(position == favStartPos) {
            return position - favStartPos;
        } else {
            return recentStartPos;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return whatView(position);
    }

    @Override
    public PlayListAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_grid_item, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PlayListAdapter.SimpleItemViewHolder holder, final int position) {
        int itemCount = 0;
        int size= setSize(holder);
        switch (whatView(position)){
            case ITEM_VIEW_TYPE_RECENT:
                holder.title.setText(mRecentPlayed.getItemTitle());
                itemCount = ((MediaItemCollection)mRecentPlayed).getItemCount();

                setSongsArtImage(holder, size, MediaController.getInstance(activity).
                        getArtUrlList((MediaItemCollection) mRecentPlayed));
                break;
            case ITEM_VIEW_TYPE_FAVOURITE:
                holder.title.setText(mFavourite.getItemTitle());
                itemCount = ((MediaItemCollection)mFavourite).getItemCount();

                setSongsArtImage(holder, size, MediaController.getInstance(activity).
                        getArtUrlList((MediaItemCollection) mFavourite));
                break;
            case ITEM_VIEW_TYPE_BOOM_PLAYLIST:
                holder.title.setText(mBoomPlayList.get(getPosition(position)).getItemTitle());
                itemCount = ((MediaItemCollection)mBoomPlayList.get(getPosition(position))).getItemCount();

                setSongsArtImage(holder, size, MediaController.getInstance(activity).
                        getArtUrlList((MediaItemCollection) mBoomPlayList.get(getPosition(position))));
                break;
            case ITEM_VIEW_TYPE_PLAYLIST:
                holder.title.setText(defaultPlayList.get(getPosition(position)).getItemTitle());
                itemCount = ((MediaItemCollection)defaultPlayList.get(getPosition(position))).getItemCount();

                if(((MediaItemCollection)defaultPlayList.get(getPosition(position))).getArtUrlList().isEmpty())
                    ((MediaItemCollection)defaultPlayList.get(getPosition(position))).setArtUrlList(MediaController.getInstance(activity).
                            getArtUrlList((MediaItemCollection) defaultPlayList.get(getPosition(position))));

                setSongsArtImage(holder, size, ((MediaItemCollection)defaultPlayList.get(getPosition(position))).getArtUrlList());
                break;
        }
        holder.subTitle.setText((itemCount > 1 ? activity.getResources().getString(R.string.songs):  activity.getResources().getString(R.string.song))+" "+ itemCount);

        holder.grid_menu.setVisibility(View.VISIBLE);
        setOnClicks(holder, position);
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

    private void setSongsArtImage(final SimpleItemViewHolder holder, final int size, final ArrayList<String> Urls) {
        if(Urls.size() >= 1 && PlayerUtils.isPathValid(Urls.get(0))) {
            holder.artTable.setVisibility(View.VISIBLE);
            int count = Urls.size() > 4 ? 4 : Urls.size();
            TableRow.LayoutParams param = new TableRow.LayoutParams(size / 2, size / 2);
            holder.artImg1.setLayoutParams(param);
            holder.artImg1.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.artImg2.setLayoutParams(param);
            holder.artImg2.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.artImg3.setLayoutParams(param);
            holder.artImg3.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.artImg4.setLayoutParams(param);
            holder.artImg4.setScaleType(ImageView.ScaleType.CENTER_CROP);

            switch (count) {
                case 1:
                    Glide.with(activity).load(Urls.get(0)).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null)).into(holder.artImg1);
                    Glide.with(activity).load(Urls.get(0)).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null)).into(holder.artImg2);
                    Glide.with(activity).load(Urls.get(0)).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null)).into(holder.artImg3);
                    Glide.with(activity).load(Urls.get(0)).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null)).into(holder.artImg4);
                    break;
                case 2:
                    Glide.with(activity).load(Urls.get(0)).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null)).into(holder.artImg1);
                    Glide.with(activity).load(Urls.get(1)).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null)).into(holder.artImg2);
                    Glide.with(activity).load(Urls.get(1)).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null)).into(holder.artImg3);
                    Glide.with(activity).load(Urls.get(0)).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null)).into(holder.artImg4);
                    break;
                case 3:
                    Glide.with(activity).load(Urls.get(0)).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null)).into(holder.artImg1);
                    Glide.with(activity).load(Urls.get(1)).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null)).into(holder.artImg2);
                    Glide.with(activity).load(Urls.get(2)).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null)).into(holder.artImg3);
                    Glide.with(activity).load(Urls.get(0)).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null)).into(holder.artImg4);
                    break;
                case 4:
                    Glide.with(activity).load(Urls.get(0)).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null)).into(holder.artImg1);
                    Glide.with(activity).load(Urls.get(1)).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null)).into(holder.artImg2);
                    Glide.with(activity).load(Urls.get(2)).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null)).into(holder.artImg3);
                    Glide.with(activity).load(Urls.get(3)).error(activity.getResources().getDrawable(R.drawable.ic_default_art_grid, null)).into(holder.artImg4);
                    break;
            }
        }else{
            holder.artTable.setVisibility(View.INVISIBLE);
            holder.defaultImg.setVisibility(View.VISIBLE);
            setDefaultImage(holder.defaultImg);
        }
    }

    private void setDefaultImage(ImageView img){
        img.setImageDrawable(activity.getResources().getDrawable( R.drawable.ic_default_art_grid, null));
    }

    private void setOnClicks(final SimpleItemViewHolder holder, final int position) {

        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(position);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        switch (whatView(position)){
                            case ITEM_VIEW_TYPE_RECENT:
                                Intent recentIntent = new Intent(activity, ActivityContainer.class);
                                recentIntent.putExtra("container", R.string.recently_played);
                                activity.startActivity(recentIntent);
                                break;
                            case ITEM_VIEW_TYPE_FAVOURITE:
                                Intent favIntent = new Intent(activity, ActivityContainer.class);
                                favIntent.putExtra("container", R.string.favourite_list);
                                activity.startActivity(favIntent);
                                break;
                            case ITEM_VIEW_TYPE_BOOM_PLAYLIST:
                                Intent boomIntent = new Intent(activity, AlbumSongListActivity.class);
                                boomIntent.putExtra("mediaItemCollection", (MediaItemCollection)mBoomPlayList.get(getPosition(position)));
                                activity.startActivity(boomIntent);
                                break;
                            case ITEM_VIEW_TYPE_PLAYLIST:
                                Intent listIntent = new Intent(activity, AlbumSongListActivity.class);
                                listIntent.putExtra("mediaItemCollection", (MediaItemCollection)defaultPlayList.get(getPosition(position)));
                                activity.startActivity(listIntent);
                                break;
                        }
                    }
                }, 100);
            }
        });

        holder.grid_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (whatView(position)){
                    case ITEM_VIEW_TYPE_RECENT:
                        OverFlowMenuUtils.setRecentPlayedMenu(activity, view);
                        break;
                    case ITEM_VIEW_TYPE_FAVOURITE:
                        OverFlowMenuUtils.setFavouriteMenu(activity, view);
                        break;
                    case ITEM_VIEW_TYPE_BOOM_PLAYLIST:
                        OverFlowMenuUtils.setBoomPlaylistMenu(activity, view, mBoomPlayList.get(getPosition(position)));
                        break;
                    case ITEM_VIEW_TYPE_PLAYLIST:
                        OverFlowMenuUtils.setDefaultPlaylistMenu(activity, view, defaultPlayList.get(getPosition(position)));
                        break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBoomPlayList.size() + defaultPlayList.size() + 2;
    }

    public static class SimpleItemViewHolder extends RecyclerView.ViewHolder {

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
