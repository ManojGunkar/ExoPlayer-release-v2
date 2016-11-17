package com.player.boom.ui.musiclist.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
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
import android.widget.TableRow;
import android.widget.TextView;
import com.player.boom.App;
import com.player.boom.R;
import com.player.boom.data.DeviceMediaCollection.MediaItem;
import com.player.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.player.boom.data.MediaCollection.IMediaItemBase;
import com.player.boom.data.MediaCollection.IMediaItemCollection;
import com.player.boom.data.MediaLibrary.ItemType;
import com.player.boom.data.MediaLibrary.MediaController;
import com.player.boom.data.MediaLibrary.MediaType;
import com.player.boom.ui.musiclist.activity.SongsDetailListActivity;
import com.player.boom.utils.PermissionChecker;
import com.player.boom.utils.Utils;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rahul Agarwal on 28-09-16.
 */

public class BoomPlayListAdapter extends RecyclerView.Adapter<BoomPlayListAdapter.SimpleItemViewHolder>{

    private static final String TAG = "AlbumListAdapter-TAG";
    ArrayList<? extends IMediaItemBase> items;
    private PermissionChecker permissionChecker;
    private Context context;
    private  RecyclerView recyclerView;

    public BoomPlayListAdapter(Context context, RecyclerView recyclerView,
                                  ArrayList<? extends IMediaItemBase> items, PermissionChecker permissionChecker) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.items = (ArrayList<MediaItemCollection>) items;
        this.permissionChecker = permissionChecker;
    }

    @Override
    public BoomPlayListAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_grid_item, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final BoomPlayListAdapter.SimpleItemViewHolder holder, final int position) {

        if(((IMediaItemCollection)items.get(position)).getArtUrlList().isEmpty())
            ((IMediaItemCollection)items.get(position)).setArtUrlList(MediaController.getInstance(context).getArtUrlList((MediaItemCollection) items.get(position)));
        Size size= setSize(holder);
        if(((IMediaItemCollection)items.get(position)).getArtUrlList().size() >= 1){
            holder.artTable.setVisibility(View.VISIBLE);
            setSongsArtImage(holder, position, size, ((IMediaItemCollection)items.get(position)).getArtUrlList());
        }else if(((IMediaItemCollection)items.get(position)).getArtUrlList().size() == 0){
            holder.defaultImg.setVisibility(View.VISIBLE);
//            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(size.width, size.height);
//            holder.defaultImg.setLayoutParams(layoutParams);

            setDefaultImage(holder.defaultImg, size.width, size.height);
        }else{
            holder.mainView.setVisibility(View.GONE);
        }

        holder.title.setText(items.get(position).getItemTitle());
        int itemcount = ((IMediaItemCollection)items.get(position)).getItemCount();
        holder.subTitle.setText((itemcount > 1 ? context.getResources().getString(R.string.songs):  context.getResources().getString(R.string.song))+" "+ itemcount);

        setOnClicks(holder, position);
    }

    private Size setSize(SimpleItemViewHolder holder) {
        Utils utils = new Utils(context);
        int width = (utils.getWindowWidth(context)
                - utils.dpToPx(context, 15)) / 2;
        int height = (width*2)/3;
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(width, height);
        holder.imgPanel.setLayoutParams(layoutParams);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) height/2);
        holder.gridBottomBg.setLayoutParams(params);

        return new Size(width, height);
    }

    private void setSongsArtImage(final SimpleItemViewHolder holder, final int position, final Size size, final ArrayList<String> Urls) {

        int count = Urls.size()>6?6:Urls.size();
        TableRow.LayoutParams param = new TableRow.LayoutParams(size.width/3, size.height/2);
        holder.artImg1.setLayoutParams(param);
        holder.artImg2.setLayoutParams(param);
        holder.artImg3.setLayoutParams(param);
        holder.artImg4.setLayoutParams(param);
        holder.artImg5.setLayoutParams(param);
        holder.artImg6.setLayoutParams(param);
        Urls.trimToSize();
        try {
            switch (count){
                case 1:
                    Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                    Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                    Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                    Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                    Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg5);
                    Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg6);
                    break;
                case 2:
                    Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                    Picasso.with(context).load(new File(Urls.get(1))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                    Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                    Picasso.with(context).load(new File(Urls.get(1))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                    Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg5);
                    Picasso.with(context).load(new File(Urls.get(1))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg6);
                    break;
                case 3:
                    Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                    Picasso.with(context).load(new File(Urls.get(1))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                    Picasso.with(context).load(new File(Urls.get(2))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                    Picasso.with(context).load(new File(Urls.get(2))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                    Picasso.with(context).load(new File(Urls.get(1))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg5);
                    Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg6);
                    break;
                case 4:
                    Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                    Picasso.with(context).load(new File(Urls.get(1))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                    Picasso.with(context).load(new File(Urls.get(2))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                    Picasso.with(context).load(new File(Urls.get(3))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                    Picasso.with(context).load(new File(Urls.get(1))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg5);
                    Picasso.with(context).load(new File(Urls.get(3))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg6);
                    break;
                case 5:
                    Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                    Picasso.with(context).load(new File(Urls.get(1))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                    Picasso.with(context).load(new File(Urls.get(2))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                    Picasso.with(context).load(new File(Urls.get(3))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                    Picasso.with(context).load(new File(Urls.get(4))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg5);
                    Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg6);
                    break;
                case 6:
                    Picasso.with(context).load(new File(Urls.get(0))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg1);
                    Picasso.with(context).load(new File(Urls.get(1))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg2);
                    Picasso.with(context).load(new File(Urls.get(2))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg3);
                    Picasso.with(context).load(new File(Urls.get(3))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg4);
                    Picasso.with(context).load(new File(Urls.get(4))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg5);
                    Picasso.with(context).load(new File(Urls.get(5))).error(context.getResources().getDrawable(R.drawable.default_album_art, null))
                            .centerCrop().resize(size.width/3, size.height/2)/*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(holder.artImg6);
                    break;
            }
        }catch (NullPointerException e){}
    }

    private void setDefaultImage(ImageView img, int width, int height){
        img.setImageBitmap(Utils.getBitmapOfVector(context, R.drawable.default_album_art_home, width, height));
    }

    private void setOnClicks(final SimpleItemViewHolder holder, final int position) {

        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(position);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(context, SongsDetailListActivity.class);
                        i.putExtra("mediaItemCollection", (MediaItemCollection)items.get(position));
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                (Activity) context,
                                new Pair<View, String>(holder.gridBottomBg, "transition:imgholder")
                        );
                        context.startActivity(i/*, options.toBundle()*/);
                    }
                }, 100);
            }
        });

        holder.grid_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.inflate(R.menu.playlist_boom_menu);

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
                            case R.id.popup_add_queue :
                                if(App.getPlayingQueueHandler().getUpNextList()!=null){
                                    ((MediaItemCollection)items.get(position)).setMediaElement(MediaController.getInstance(context).getMediaCollectionItemDetails((IMediaItemCollection) items.get(position)));
                                    App.getPlayingQueueHandler().getUpNextList().addItemListToUpNext(items.get(position));
                                }
                                break;
                            case R.id.popup_playlist_delete :
                                MediaController.getInstance(context).deleteBoomPlaylist(items.get(position).getItemId());
                                updateNewList((ArrayList<? extends MediaItemCollection>) MediaController.getInstance(context).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB));
                                Snackbar.make(recyclerView, "PlayList Deleted...!", Snackbar.LENGTH_LONG).show();
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
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
        return items.size();
    }

    public void updateNewList(ArrayList<? extends MediaItemCollection> newList) {
        items = newList;
        notifyDataSetChanged();
    }

    public void onBackPressed() {

    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public TextView title, subTitle;
        public ImageView defaultImg, artImg1, artImg2, artImg3, artImg4, artImg5, artImg6;
        public View gridBottomBg, grid_menu, mainView;
        public TableLayout artTable;
        public FrameLayout imgPanel;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;

            title = (TextView) itemView.findViewById(R.id.card_grid_title);
            subTitle = (TextView) itemView.findViewById(R.id.card_grid_sub_title);
            defaultImg = (ImageView) itemView.findViewById(R.id.card_grid_default_img);
            artImg1 = (ImageView) itemView.findViewById(R.id.card_grid_art_img1);
            artImg2 = (ImageView) itemView.findViewById(R.id.card_grid_art_img2);
            artImg3 = (ImageView) itemView.findViewById(R.id.card_grid_art_img3);
            artImg4 = (ImageView) itemView.findViewById(R.id.card_grid_art_img4);
            artImg5 = (ImageView) itemView.findViewById(R.id.card_grid_art_img5);
            artImg6 = (ImageView) itemView.findViewById(R.id.card_grid_art_img6);
            artTable = (TableLayout)itemView.findViewById(R.id.card_grid_art_table);
            gridBottomBg = itemView.findViewById(R.id.card_grid_bottom);
            grid_menu = itemView.findViewById(R.id.card_grid_menu);
            imgPanel = (FrameLayout) itemView.findViewById(R.id.card_grid_img_panel);
        }
    }

    public class Size{
        int width;
        int height;

        public Size(int width, int height){
            this.width = width;
            this.height = height;
        }
    }
}