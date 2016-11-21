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
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.ui.musiclist.activity.AlbumActivity;
import com.globaldelight.boom.ui.musiclist.activity.DetailAlbumActivity;
import com.globaldelight.boom.utils.PermissionChecker;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 8/8/2016.
 */
public class ArtistsGridAdapter extends RecyclerView.Adapter<ArtistsGridAdapter.SimpleItemViewHolder>{

    private static final String TAG = "AlbumListAdapter-TAG";
    ArrayList<MediaItemCollection> itemList;
    private PermissionChecker permissionChecker;
    private Context context;
    private  RecyclerView recyclerView;

    public ArtistsGridAdapter(Context context, RecyclerView recyclerView,
                              ArrayList<? extends IMediaItemBase> itemList, PermissionChecker permissionChecker) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.itemList = (ArrayList<MediaItemCollection>) itemList;
        this.permissionChecker = permissionChecker;
    }

    @Override
    public ArtistsGridAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_detail_album_item, parent, false);
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
        setArtistImg(holder, position, size);
        setOnClicks(holder, position);
    }

    private void setArtistImg(final SimpleItemViewHolder holder, final int position, final int size) {
        String path = itemList.get(position).getItemArtUrl();
        if (isPathValid(path))
            Picasso.with(context).load(new File(path)).error(context.getResources().getDrawable(R.drawable.default_album_art_home, null))
                    .centerCrop().resize(size, size)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.defaultImg);
        else {
            holder.defaultImg.setImageBitmap(Utils.getBitmapOfVector(context, R.drawable.default_album_art_home, size, size));
        }
    }

    private void setOnClicks(final SimpleItemViewHolder holder, final int position) {

        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(position);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(context, DetailAlbumActivity.class);
                        i.putExtra("mediaItemCollection", itemList.get(position));
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                (Activity) context,
                                new Pair<View, String>(holder.defaultImg, "transition:imgholder1")
                        );
                        ActivityCompat.startActivity((Activity) context, i, options.toBundle());
                    }
                }, 100);
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_MUSIC_PLAYED_FROM_ARTIST_SECTION);

            }
        });

        holder.grid_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchorView) {
                PopupMenu popupMenu = new PopupMenu(context, anchorView);
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
                                if(App.getPlayingQueueHandler().getUpNextList()!=null){
                                    itemList.get(position).setMediaElement(MediaController.getInstance(context).getMediaCollectionItemDetails(itemList.get(position)));
                                    ((IMediaItemCollection)itemList.get(position).getMediaElement().get(0)).setMediaElement(MediaController.getInstance(context).getMediaCollectionItemDetails(itemList.get(position)));

                                    App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext(((IMediaItemCollection)itemList.get(position).getMediaElement().get(itemList.get(position).getCurrentIndex())));
                                }
                                break;
                            default:
                                Toast.makeText((AlbumActivity)context, "Under Development...!", Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }
                });
                popupMenu.show();
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

        public int defaultAlbumColor;
        public TextView title, subTitle;
        public ImageView defaultImg;
        public View gridBottomBg, grid_menu, mainView;
        public TableLayout artTable;
        public FrameLayout imgPanel;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            mainView = itemView;
            title = (TextView) itemView.findViewById(R.id.card_sub_grid_title);
            subTitle = (TextView) itemView.findViewById(R.id.card_sub_grid_sub_title);
            defaultImg = (ImageView) itemView.findViewById(R.id.card_sub_grid_default_img);
            artTable = (TableLayout)itemView.findViewById(R.id.card_sub_grid_art_table);
            gridBottomBg = itemView.findViewById(R.id.card_sub_grid_bottom);
            grid_menu = itemView.findViewById(R.id.card_sub_grid_menu);
            imgPanel = (FrameLayout) itemView.findViewById(R.id.card_sub_grid_img_panel);
        }
    }
}
