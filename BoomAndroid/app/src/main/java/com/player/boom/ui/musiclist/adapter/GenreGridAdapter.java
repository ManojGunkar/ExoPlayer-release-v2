package com.player.boom.ui.musiclist.adapter;

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

import com.player.boom.App;
import com.player.boom.R;
import com.player.boom.data.MediaCollection.IMediaItemBase;
import com.player.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.player.boom.handler.PlayingQueue.QueueType;
import com.player.boom.ui.musiclist.activity.AlbumActivity;
import com.player.boom.ui.musiclist.activity.DetailAlbumActivity;
import com.player.boom.utils.PermissionChecker;
import com.player.boom.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 8/8/2016.
 */
public class GenreGridAdapter extends RecyclerView.Adapter<GenreGridAdapter.SimpleItemViewHolder> {

    private static final String TAG = "AlbumListAdapter-TAG";
    ArrayList<MediaItemCollection> items;
    private PermissionChecker permissionChecker;
    private Context context;
    private RecyclerView recyclerView;

    public GenreGridAdapter(Context context, RecyclerView recyclerView,
                            ArrayList<? extends IMediaItemBase> items, PermissionChecker permissionChecker) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.items = (ArrayList<MediaItemCollection>) items;
        this.permissionChecker = permissionChecker;
    }

    @Override
    public GenreGridAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_detail_album_item, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final GenreGridAdapter.SimpleItemViewHolder holder, final int position) {
        holder.defaultImg.setVisibility(View.VISIBLE);

        holder.title.setText(items.get(position).getItemTitle());
        int count = items.get(position).getItemCount();
        int albumCount = items.get(position).getItemListCount();

        StringBuilder sb = new StringBuilder();
        sb.append((count<=1 ? context.getResources().getString(R.string.song) : context.getResources().getString(R.string.songs)));
        sb.append(" ");
        sb.append(count);
        sb.append(" ");
        sb.append((albumCount<=1 ? context.getResources().getString(R.string.album) :
                context.getResources().getString(R.string.albums)));
        sb.append(" ");
        sb.append(albumCount);

        holder.subTitle.setText( sb.toString());
        int size = setSize(holder);
        setArtistImg(holder, position, size);
        setOnClicks(holder, position);
    }

    private void setArtistImg(final SimpleItemViewHolder holder, final int position, final int size) {
        String path = items.get(position).getItemArtUrl();
        if (isPathValid(path))
            Picasso.with(context).load(new File(path)).error(context.getResources().getDrawable(R.drawable.default_album_art_home, null))
                    .centerCrop().resize(size, size)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.defaultImg);
        else
            holder.defaultImg.setImageBitmap(Utils.getBitmapOfVector(context, R.drawable.default_album_art_home, size, size));
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
                        i.putExtra("mediaItemCollection", items.get(position));
                        i.putExtra("albumColor", holder.defaultAlbumColor);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                (Activity) context,
                                new Pair<View, String>(holder.defaultImg, "transition:imgholder1")
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
                                if(App.getPlayingQueueHandler().getPlayingQueue()!=null){
                                    App.getPlayingQueueHandler().getPlayingQueue().addMediaItemsToManualUpNext(items.get(position), -1);
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
        return items.size();
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
            artTable = (TableLayout) itemView.findViewById(R.id.card_sub_grid_art_table);
            gridBottomBg = itemView.findViewById(R.id.card_sub_grid_bottom);
            grid_menu = itemView.findViewById(R.id.card_sub_grid_menu);
            imgPanel = (FrameLayout) itemView.findViewById(R.id.card_sub_grid_img_panel);
        }
    }
}
