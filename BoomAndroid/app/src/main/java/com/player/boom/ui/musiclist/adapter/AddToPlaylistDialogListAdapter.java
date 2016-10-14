package com.player.boom.ui.musiclist.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.player.boom.R;
import com.player.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.player.boom.data.MediaCollection.IMediaItemBase;
import com.player.boom.data.MediaCollection.IMediaItemCollection;
import com.player.boom.data.MediaLibrary.ItemType;
import com.player.boom.data.MediaLibrary.MediaController;
import com.player.boom.data.MediaLibrary.MediaType;

import java.util.ArrayList;
import static android.app.AlertDialog.Builder;
import static android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import static android.view.LayoutInflater.from;
import static android.view.View.OnClickListener;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class AddToPlaylistDialogListAdapter extends RecyclerView.Adapter<AddToPlaylistDialogListAdapter.SimpleItemViewHolder> {

    private ArrayList<? extends IMediaItemBase> items;
    private IMediaItemBase song;
    private Context context;
    private MaterialDialog dialog;

    public AddToPlaylistDialogListAdapter(Context context, ArrayList<? extends IMediaItemBase>  items, IMediaItemBase song) {
        this.context = context;
        this.items = items;
        this.song = song;
    }

    @Override
    public SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = from(parent.getContext()).
                inflate(R.layout.playlist_dialog_item, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SimpleItemViewHolder holder, final int position) {
        holder.mainView.setBackgroundColor(0xffffffff);
        holder.name.setText(items.get(position).getItemTitle());
        holder.count.setText(context.getResources().getString(R.string.songs)
                + " " + ((MediaItemCollection)items.get(position)).getItemCount());
        holder.menu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu pm = new PopupMenu(context, view);
                pm.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.popup_playlist_rename:
                                renameDialog(position);
                                break;
                            case R.id.popup_playlist_delete:
                                MediaController.getInstance(context).deleteBoomPlaylist(((MediaItemCollection) items.get(position)).getItemId());
                                updateNewList(MediaController.getInstance(context).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB));
                                break;
                        }
                        return false;
                    }
                });
                pm.inflate(R.menu.popup_playlist);
                pm.show();
            }
        });
        holder.mainView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaController.getInstance(context).addSongToBoomPlayList(((MediaItemCollection) items.get(position)).getItemId(), song);
                items = MediaController.getInstance(context).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB);
                notifyDataSetChanged();
                dialog.dismiss();
                makeText(context, R.string.added_to_playlist, LENGTH_SHORT).show();
            }
        });
    }

    public void updateNewList(ArrayList<? extends IMediaItemBase> newList) {
        items = newList;
        notifyDataSetChanged();
    }

    public void playPlaylist(final int position) {
        if (((IMediaItemCollection)items.get(position)).getItemCount() != 0)
            new Thread(new Runnable() {
                public void run() {

//                    App.getPlayingQueueHandler().setPlayListAndPlay(items.get(position), true);

                }
            }).start();
    }

    private void renameDialog(final int position) {
        final EditText edittext = new EditText(context);
        Builder alert = new Builder(context);
        alert.setTitle("Rename");

        alert.setView(edittext);

        alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (edittext.getText().toString().matches("")) {
                    renameDialog(position);
                } else {
                    MediaController.getInstance(context).renameBoomPlaylist(edittext.getText().toString(),
                            items.get(position).getItemId());
                    updateNewList(MediaController.getInstance(context).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB));
                    notifyDataSetChanged();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    public void setDialog(MaterialDialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(ArrayList<? extends IMediaItemBase> allPlaylist) {
        this.items = allPlaylist;
        notifyDataSetChanged();
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public View mainView, menu;
        public TextView name, count;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            name = (TextView) itemView.findViewById(R.id.playlist_dialog_name);
            menu = itemView.findViewById(R.id.playlist_dialog_menu);
            count = (TextView) itemView.findViewById(R.id.playlist_dialog_song_count);
        }
    }

}
