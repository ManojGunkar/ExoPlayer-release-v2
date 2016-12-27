package com.globaldelight.boom.ui.musiclist.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TextView;

import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaLibrary.DeviceMediaQuery;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.ui.musiclist.activity.AlbumActivity;
import com.globaldelight.boom.ui.widgets.CoachMarkTextView;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static com.globaldelight.boom.data.MediaLibrary.ItemType.BOOM_PLAYLIST;
import static com.globaldelight.boom.data.MediaLibrary.ItemType.PLAYLIST;

/**
 * Created by Rahul Agarwal on 8/8/2016.
 */
public class AlbumsGridAdapter extends RecyclerView.Adapter<AlbumsGridAdapter.SimpleItemViewHolder>{

    private static final String TAG = "AlbumsGridAdapter-TAG";
    ArrayList<MediaItemCollection> itemList;
    private Context context;
    private Activity activity;
    private  RecyclerView recyclerView;

    public AlbumsGridAdapter(Context context, FragmentActivity activity, RecyclerView recyclerView, ArrayList<? extends IMediaItemBase> itemList) {
        this.context = context;
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.itemList = (ArrayList<MediaItemCollection>)itemList;
    }

    @Override
    public AlbumsGridAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_grid_item, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AlbumsGridAdapter.SimpleItemViewHolder holder, final int position) {
        holder.defaultImg.setVisibility(View.VISIBLE);
        holder.title.setText(itemList.get(position).getItemTitle());
        holder.subTitle.setText(itemList.get(position).getItemSubTitle());
        int size = setSize(holder);

        setArtistImg(holder, position, size);

        if(App.getUserPreferenceHandler().isLibFromHome()) {
            holder.grid_menu.setVisibility(View.VISIBLE);
        }else{
            holder.grid_menu.setVisibility(View.INVISIBLE);
        }
        setOnClicks(holder, position);
    }

    private void setArtistImg(final SimpleItemViewHolder holder, final int position, final int size) {
        String path = itemList.get(position).getItemArtUrl();
        if (PlayerUtils.isPathValid(path))
            Picasso.with(context).load(new File(path)).error(context.getResources().getDrawable(R.drawable.ic_default_album_grid, null)).noFade()
                    .centerCrop().resize(size, size)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.defaultImg);
        else
            holder.defaultImg.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_default_album_grid));
    }


    private void setOnClicks(final SimpleItemViewHolder holder, final int position) {

        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(position);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(context, AlbumActivity.class);
                        i.putExtra("mediaItemCollection", itemList.get(position));
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
                PopupMenu pm = new PopupMenu(context, view);
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        try {
                            switch (item.getItemId()) {
                                case R.id.popup_album_play_next:
                                    if (App.getPlayingQueueHandler().getUpNextList() != null) {
                                        itemList.get(position).setMediaElement(MediaController.getInstance(context).getMediaCollectionItemDetails(itemList.get(position)));
                                        if (itemList.get(position).getMediaElement().size() > 0)
                                            App.getPlayingQueueHandler().getUpNextList().addItemListToUpNextFrom(itemList.get(position));
                                    }
                                    break;
                                case R.id.popup_album_add_queue:
                                    if (App.getPlayingQueueHandler().getUpNextList() != null) {
                                        itemList.get(position).setMediaElement(MediaController.getInstance(context).getMediaCollectionItemDetails(itemList.get(position)));
                                        if (itemList.get(position).getMediaElement().size() > 0)
                                            App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext(itemList.get(position));
                                    }
                                    break;
                                case R.id.popup_album_add_playlist:
                                    Utils util = new Utils(context);
                                    itemList.get(position).setMediaElement(MediaController.getInstance(context).getMediaCollectionItemDetails(itemList.get(position)));

                                    if (itemList.get(position).getMediaElement().size() > 0)
                                        util.addToPlaylist(activity, itemList.get(position).getMediaElement(), null);
                                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);
                                    break;
                            }
                        }catch (Exception e){

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
        Utils utils = new Utils(context);
        int size = (utils.getWindowWidth(context)
                - utils.dpToPx(context, 15)) / 2;

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size, size);
        holder.defaultImg.setLayoutParams(layoutParams);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (size/2.5));
        holder.gridBottomBg.setLayoutParams(params);
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

        public RegularTextView title;
        public CoachMarkTextView subTitle;
        public ImageView defaultImg;
        public View gridBottomBg, grid_menu, mainView;
        public TableLayout artTable;
        public FrameLayout imgPanel;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            title = (RegularTextView) itemView.findViewById(R.id.card_grid_title);
            subTitle = (CoachMarkTextView) itemView.findViewById(R.id.card_grid_sub_title);
            defaultImg = (ImageView) itemView.findViewById(R.id.card_grid_default_img);
            artTable = (TableLayout)itemView.findViewById(R.id.card_grid_art_table);
            gridBottomBg = itemView.findViewById(R.id.card_grid_bottom);
            grid_menu = itemView.findViewById(R.id.card_grid_menu);
            imgPanel = (FrameLayout) itemView.findViewById(R.id.card_grid_img_panel);
        }
    }
}

