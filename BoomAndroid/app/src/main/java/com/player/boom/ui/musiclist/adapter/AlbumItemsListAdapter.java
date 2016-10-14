package com.player.boom.ui.musiclist.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.player.boom.App;
import com.player.boom.R;
import com.player.boom.data.MediaCollection.IMediaItemCollection;
import com.player.boom.data.DeviceMediaCollection.MediaItem;
import com.player.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.player.boom.data.MediaLibrary.ItemType;
import com.player.boom.handler.PlayingQueue.QueueType;
import com.player.boom.ui.musiclist.ListDetail;
import com.player.boom.ui.musiclist.activity.AlbumActivity;
import com.player.boom.ui.widgets.IconizedMenu;
import com.player.boom.utils.PermissionChecker;
import com.player.boom.utils.Utils;

/**
 * Created by Rahul Agarwal on 8/9/2016.
 */
public class AlbumItemsListAdapter extends RecyclerView.Adapter<AlbumItemsListAdapter.SimpleItemViewHolder> {

    public static final int TYPE_HEADER = 111;
    public static final int TYPE_ITEM = 222;
    private PermissionChecker permissionChecker;
    private Context context;
    private MediaItemCollection item;
    private ListDetail listDetail;

    public AlbumItemsListAdapter(AlbumActivity context, IMediaItemCollection item, ListDetail listDetail, PermissionChecker permissionChecker) {
        this.context = context;
        this.item = (MediaItemCollection) item;
        this.permissionChecker = permissionChecker;
        this.listDetail = listDetail;
    }

    @Override
    public AlbumItemsListAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if(viewType == TYPE_ITEM) {
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.album_list_item, parent, false);
        }else{
            itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.recycler_view_header, parent, false);
        }
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AlbumItemsListAdapter.SimpleItemViewHolder holder, final int position) {

        if(position < 1){
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
        }else if(position >= 1) {
            String title;
            String duration;
            int pos = position -1;
            if (item.getItemType() == ItemType.ALBUM) {
                title = item.getMediaElement().get(pos).getItemTitle();
                duration = ((MediaItem) item.getMediaElement().get(pos)).getDuration();
            } else if (item.getItemType() == ItemType.ARTIST || item.getItemType() == ItemType.GENRE) {
                title = ((MediaItemCollection) item.getMediaElement().get(item.getCurrentIndex())).getMediaElement().get(pos).getItemTitle();
                duration = ((MediaItem) ((MediaItemCollection) item.getMediaElement().get(item.getCurrentIndex())).getMediaElement().get(pos)).getDuration();
            } else {
                title = item.getMediaElement().get(item.getCurrentIndex()).getItemTitle();
                duration = ((MediaItem) ((MediaItemCollection) item.getMediaElement().get(item.getCurrentIndex())).getMediaElement().get(pos)).getDuration();
            }
            holder.name.setText(title);
            holder.count.setText(String.valueOf(pos + 1));
            holder.duration.setText(duration);
            setOnClickListeners(holder, pos);
        }
    }

    private void setOnClickListeners(SimpleItemViewHolder holder, final int position) {
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (App.getPlayingQueueHandler().getPlayingQueue() != null) {
                    if (item.getItemType() == ItemType.ALBUM) {

                        App.getPlayingQueueHandler().getPlayingQueue().addItemToQueue(QueueType.Playing, item.getMediaElement().get(position), -1);
                        for (int i = position + 1; i < item.getMediaElement().size(); i++) {
                            App.getPlayingQueueHandler().getPlayingQueue().addItemToQueue(QueueType.Auto_UpNext, item.getMediaElement().get(i), -1);
                        }
                } else {
                        App.getPlayingQueueHandler().getPlayingQueue().addItemToQueue(QueueType.Playing, ((MediaItemCollection)item.getMediaElement().get(item.getCurrentIndex())).getMediaElement().get(position), -1);
                        for (int i = position + 1; i < item.getMediaElement().size(); i++) {
                            App.getPlayingQueueHandler().getPlayingQueue().addItemToQueue(QueueType.Auto_UpNext, ((MediaItemCollection)item.getMediaElement().get(item.getCurrentIndex())).getMediaElement().get(position), -1);
                        }
                }
            }
            }
        });
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View anchorView) {
                IconizedMenu PopupMenu = new IconizedMenu(context, anchorView);
                Menu menu = PopupMenu.getMenu();
                MenuInflater inflater = PopupMenu.getMenuInflater();
                inflater.inflate(R.menu.song_popup, PopupMenu.getMenu());
                PopupMenu.show();

                PopupMenu.setOnMenuItemClickListener(new IconizedMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.popup_song_add_queue :
                                if(App.getPlayingQueueHandler().getPlayingQueue()!=null){
                                    if (item.getItemType() == ItemType.ALBUM) {
                                        App.getPlayingQueueHandler().getPlayingQueue().addItemToQueue(QueueType.Manual_UpNext, item.getMediaElement().get(position), -1);
                                    }else{
                                        App.getPlayingQueueHandler().getPlayingQueue().addItemToQueue(QueueType.Manual_UpNext, ((MediaItemCollection)item.getMediaElement().get(item.getCurrentIndex())).getMediaElement().get(position), -1);
                                    }
                                }
                                break;
                            case R.id.popup_song_add_playlist :
                                Utils util = new Utils(context);
                                if (item.getItemType() == ItemType.ALBUM) {
                                    App.getPlayingQueueHandler().getPlayingQueue().addItemToQueue(QueueType.Manual_UpNext, item.getMediaElement().get(position), -1);
                                }else{
                                    App.getPlayingQueueHandler().getPlayingQueue().addItemToQueue(QueueType.Manual_UpNext, ((MediaItemCollection)item.getMediaElement().get(item.getCurrentIndex())).getMediaElement().get(position), -1);
                                }
                                    util.addToPlaylist((AlbumActivity)context, item.getMediaElement().get(position));
                                break;
                            case R.id.popup_song_add_fav :
                                Toast.makeText((AlbumActivity)context, "Under Development...!", Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        if(item.getItemType() == ItemType.ALBUM){
            return item.getItemCount()+1;
        }else{
            return ((MediaItemCollection)item.getMediaElement().get(item.getCurrentIndex())).getItemCount()+1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position < 1){
            return TYPE_HEADER;
        }else{
            return TYPE_ITEM;
        }
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public TextView name, count, duration;
        public View mainView, menu;

        public TextView headerTitle, headerSubTitle, headerDetail;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            name = (TextView) itemView.findViewById(R.id.album_item_name);
            duration = (TextView) itemView.findViewById(R.id.album_item_duration);
            count = (TextView) itemView.findViewById(R.id.album_item_count);
            menu = itemView.findViewById(R.id.album_item_menu);

            headerTitle = (TextView) itemView.findViewById(R.id.header_title);
            headerSubTitle = (TextView) itemView.findViewById(R.id.header_sub_title);
            headerDetail = (TextView) itemView.findViewById(R.id.header_detail);
        }
    }

}
