package com.globaldelight.boom.ui.musiclist.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.App;
import com.globaldelight.boom.ui.musiclist.activity.AlbumSongListActivity;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.ui.musiclist.fragment.BoomPlaylistFragment;
import com.globaldelight.boom.ui.widgets.CoachMarkTextView;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 28-09-16.
 */

public class BoomPlayListAdapter extends RecyclerView.Adapter<BoomPlayListAdapter.SimpleItemViewHolder>{

    private static final String TAG = "AlbumListAdapter-TAG";
    ArrayList<? extends IMediaItemBase> items;
    public static final int ITEM_VIEW_TYPE_ITEM_LIST = 0;
    public static final int ITEM_VIEW_TYPE_ITEM_LIST_FOOTER = 1;
    private Activity activity;
    private  RecyclerView recyclerView;
    private boolean isPhone;
    private BoomPlaylistFragment fragment;

    public BoomPlayListAdapter(Activity activity, BoomPlaylistFragment fragment, RecyclerView recyclerView,
                               ArrayList<? extends IMediaItemBase> items, boolean isPhone) {
        this.activity = activity;
        this.fragment = fragment;
        this.recyclerView = recyclerView;
        this.items = items;
        this.isPhone = isPhone;
    }

    public int whatView(int position){
        if(position < items.size()){
            return ITEM_VIEW_TYPE_ITEM_LIST;
        }else{
            return ITEM_VIEW_TYPE_ITEM_LIST_FOOTER;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public IMediaItemBase getItem(int position){
        return items.get(position);
    }

    @Override
    public BoomPlayListAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if(viewType == ITEM_VIEW_TYPE_ITEM_LIST){
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.card_grid_item, parent, false);
        }else {
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.card_boom_playlist_footer, parent, false);
        }
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final BoomPlayListAdapter.SimpleItemViewHolder holder, final int position) {
        if(position < items.size()) {
            int size = setSize(holder);

            holder.title.setText(getItem(position).getItemTitle());
            int itemcount = ((IMediaItemCollection) getItem(position)).getItemCount();
            holder.subTitle.setText((itemcount > 1 ? activity.getResources().getString(R.string.songs) : activity.getResources().getString(R.string.song)) + " " + itemcount);

            if (App.getUserPreferenceHandler().isLibFromHome()) {
                holder.grid_menu.setVisibility(View.VISIBLE);
            } else {
                holder.grid_menu.setVisibility(View.INVISIBLE);
            }
            if (((IMediaItemCollection) items.get(position)).getArtUrlList().isEmpty())
                ((IMediaItemCollection) items.get(position)).setArtUrlList(MediaController.getInstance(activity).getArtUrlList((MediaItemCollection) items.get(position)));

            if (((IMediaItemCollection) items.get(position)).getArtUrlList().isEmpty()) {
                ArrayList list = new ArrayList();
                list.add(MediaItem.UNKNOWN_ART_URL);
                ((IMediaItemCollection) items.get(position)).setArtUrlList(list);
            }

            setSongsArtImage(holder, position, size);
            setOnClicks(holder, position);
        }else{

        }
    }

    @Override
    public int getItemViewType(int position) {
        return whatView(position);
    }

    private int setSize(SimpleItemViewHolder holder) {
        Utils utils = new Utils(activity);
        int size = (utils.getWindowWidth(activity) / (isPhone ? 2 : 3))
                - (int)(activity.getResources().getDimension(R.dimen.twenty_four_pt)*2);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (size/(isPhone?2.5:3)));
        holder.gridBottomBg.setLayoutParams(params);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
        holder.defaultImg.setLayoutParams(layoutParams);
        holder.defaultImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

        TableRow.LayoutParams tableParams = new TableRow.LayoutParams(size, size);
        holder.imgPanel.setLayoutParams(tableParams);

        return size;
    }

    private void setSongsArtImage(final SimpleItemViewHolder holder, final int position, final int size) {

        if (((IMediaItemCollection) getItem(position)).getArtUrlList().size() > 0 && PlayerUtils.isPathValid(((IMediaItemCollection) getItem(position)).getArtUrlList().get(0))){
            holder.defaultImg.setVisibility(View.GONE);
            holder.artTable.setVisibility(View.VISIBLE);
            ArrayList<String> Urls = ((IMediaItemCollection) getItem(position)).getArtUrlList();
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
            Urls.trimToSize();
            try {
                switch (count) {
                    case 1:
                        Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_small_grid_song, null))
                                /*.centerCrop().resize(size.width / 2, size.height / 2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                        Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_small_grid_song, null))
                                /*.centerCrop().resize(size.width / 2, size.height / 2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                        Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_small_grid_song, null))
                                /*.centerCrop().resize(size.width / 2, size.height / 2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                        Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_small_grid_song, null))
                                /*.centerCrop().resize(size.width / 2, size.height / 2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                        break;
                    case 2:
                        Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_small_grid_song, null))
                                /*.centerCrop().resize(size.width / 2, size.height / 2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                        Picasso.with(activity).load(new File(Urls.get(1))).error(activity.getResources().getDrawable(R.drawable.ic_default_small_grid_song, null))
                                /*.centerCrop().resize(size.width / 2, size.height / 2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                        Picasso.with(activity).load(new File(Urls.get(1))).error(activity.getResources().getDrawable(R.drawable.ic_default_small_grid_song, null))
                                /*.centerCrop().resize(size.width / 2, size.height / 2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                        Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_small_grid_song, null))
                                /*.centerCrop().resize(size.width / 2, size.height / 2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                        break;
                    case 3:
                        Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_small_grid_song, null))
                                /*.centerCrop().resize(size.width / 2, size.height / 2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                        Picasso.with(activity).load(new File(Urls.get(1))).error(activity.getResources().getDrawable(R.drawable.ic_default_small_grid_song, null))
                                /*.centerCrop().resize(size.width / 2, size.height / 2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                        Picasso.with(activity).load(new File(Urls.get(2))).error(activity.getResources().getDrawable(R.drawable.ic_default_small_grid_song, null))
                                /*.centerCrop().resize(size.width / 2, size.height / 2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                        Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_small_grid_song, null))
                                /*.centerCrop().resize(size.width / 2, size.height / 2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                        break;
                    case 4:
                        Picasso.with(activity).load(new File(Urls.get(0))).error(activity.getResources().getDrawable(R.drawable.ic_default_small_grid_song, null))
                                /*.centerCrop().resize(size.width / 2, size.height / 2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                        Picasso.with(activity).load(new File(Urls.get(1))).error(activity.getResources().getDrawable(R.drawable.ic_default_small_grid_song, null))
                                /*.centerCrop().resize(size.width / 2, size.height / 2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                        Picasso.with(activity).load(new File(Urls.get(2))).error(activity.getResources().getDrawable(R.drawable.ic_default_small_grid_song, null))
                                /*.centerCrop().resize(size.width / 2, size.height / 2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                        Picasso.with(activity).load(new File(Urls.get(3))).error(activity.getResources().getDrawable(R.drawable.ic_default_small_grid_song, null))
                                /*.centerCrop().resize(size.width / 2, size.height / 2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                        break;
                }
            } catch (NullPointerException e) {
            }
        } else {
            holder.defaultImg.setVisibility(View.VISIBLE);
            holder.artTable.setVisibility(View.GONE);
            setDefaultImage(holder, size, size);
        }
    }

    private void setDefaultImage(SimpleItemViewHolder holder, int width, int height){
        holder.defaultImg.setImageDrawable(activity.getResources().getDrawable( R.drawable.ic_default_album_grid ));
    }

    private void setOnClicks(final SimpleItemViewHolder holder, final int position) {

        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(position);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(activity, AlbumSongListActivity.class);
                        i.putExtra("mediaItemCollection", (MediaItemCollection)getItem(position));
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
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        try {
                            switch (menuItem.getItemId()) {
                                case R.id.popup_play_next:
                                    if (App.getPlayingQueueHandler().getUpNextList() != null) {
                                        ((MediaItemCollection) items.get(position)).setMediaElement(MediaController.getInstance(activity).getMediaCollectionItemDetails((IMediaItemCollection) items.get(position)));
                                        App.getPlayingQueueHandler().getUpNextList().addItemListToUpNextFrom(items.get(position));
                                    }
                                    break;
                                case R.id.popup_add_queue:
                                    if (App.getPlayingQueueHandler().getUpNextList() != null) {
                                        ((MediaItemCollection) items.get(position)).setMediaElement(MediaController.getInstance(activity).getMediaCollectionItemDetails((IMediaItemCollection) items.get(position)));
                                        App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext(items.get(position));
                                    }
                                    break;
                                case R.id.popup_playlist_rename:
                                    renameDialog(position, items.get(position).getItemTitle());
                                    break;
                                case R.id.popup_add_playlist:
                                    ((MediaItemCollection) items.get(position)).setMediaElement(MediaController.getInstance(activity).getMediaCollectionItemDetails((IMediaItemCollection) items.get(position)));
                                    Utils util = new Utils(activity);
                                    util.addToPlaylist(activity, ((MediaItemCollection) items.get(position)).getMediaElement(), ((MediaItemCollection) items.get(position)).getItemTitle());
                                    break;
                                case R.id.popup_playlist_delete:
                                    MediaController.getInstance(activity).deleteBoomPlaylist(items.get(position).getItemId());
                                    items.clear();
                                    items = MediaController.getInstance(activity).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB);
                                    notifyItemRemoved(position);
                                    notifyDataSetChanged();
                                    if(items.size() == 0){
                                        fragment.listIsEmpty();
                                    }
                                    Toast.makeText(activity, activity.getResources().getString(R.string.playlist_deleted), Toast.LENGTH_SHORT).show();
                                    break;
                                case R.id.popup_add_song:
                                    App.getUserPreferenceHandler().setBoomPlayListId(items.get(position).getItemId());
                                    App.getUserPreferenceHandler().setLibraryStartFromHome(false);
//                                    Intent i = new Intent(activity, DeviceMusicActivity.class);
//                                    activity.startActivity(i);
                                    break;
                            }
                        }catch (Exception e){}
                        return false;
                    }
                });
                pm.inflate(R.menu.playlist_boom_menu);
                pm.show();
            }
        });

    }

    private void renameDialog(final int position, String itemTitle) {
        new MaterialDialog.Builder(activity)
                .title(R.string.dialog_txt_rename)
                .backgroundColor(ContextCompat.getColor(activity, R.color.dialog_background))
                .titleColor(ContextCompat.getColor(activity, R.color.dialog_title))
                .positiveColor(ContextCompat.getColor(activity, R.color.dialog_submit_positive))
                .negativeColor(ContextCompat.getColor(activity, R.color.dialog_submit_negative))
                .widgetColor(ContextCompat.getColor(activity, R.color.dialog_widget))
                .contentColor(ContextCompat.getColor(activity, R.color.dialog_content))
                .typeface("TitilliumWeb-SemiBold.ttf", "TitilliumWeb-Regular.ttf")
                .cancelable(true)
                .positiveText(activity.getResources().getString(R.string.dialog_txt_done))
                .negativeText(activity.getResources().getString(R.string.dialog_txt_cancel))
                .input(null, itemTitle, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (input.toString().matches("")) {
                            renameDialog(position, items.get(position).getItemTitle());
                        } else {
                            MediaController.getInstance(activity).renameBoomPlaylist(input.toString(),
                                    items.get(position).getItemId());
                            updateNewList((ArrayList<? extends MediaItemCollection>) MediaController.getInstance(activity).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB));
                            notifyDataSetChanged();
                        }
                    }
                }).show();
    }

    private boolean fileExist(String albumArtPath) {
        File imgFile = new File(albumArtPath);
        return imgFile.exists();
    }

    public boolean isPathValid(String path) {
        return path != null && fileExist(path);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public int getItemCount() {
        return items.size()+1;
    }

    public void updateNewList(ArrayList<? extends IMediaItemBase> newList) {
        items.clear();
        items = newList;
        notifyDataSetChanged();
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