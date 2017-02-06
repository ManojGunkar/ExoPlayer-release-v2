package com.globaldelight.boom.ui.musiclist.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.task.PlayerEvents;
import com.globaldelight.boom.ui.widgets.RegularTextView;

import java.util.ArrayList;
import static android.view.LayoutInflater.from;
import static android.view.View.OnClickListener;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class AddToPlaylistAdapter extends RecyclerView.Adapter<AddToPlaylistAdapter.SimpleItemViewHolder> {

    private ArrayList<? extends IMediaItemBase> playList;
    private ArrayList<? extends IMediaItemBase> songList;
    private Context context;
    private MaterialDialog dialog;

    public AddToPlaylistAdapter(Context context, ArrayList<? extends IMediaItemBase>  playList, ArrayList<? extends IMediaItemBase> songList) {
        this.context = context;
        this.playList = playList;
        this.songList = songList;
    }

    @Override
    public SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = from(parent.getContext()).
                inflate(R.layout.card_playlist_dialog, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SimpleItemViewHolder holder, final int position) {
        holder.name.setText(playList.get(position).getItemTitle());
        holder.count.setText(context.getResources().getString(R.string.songs)
                + " " + ((MediaItemCollection) playList.get(position)).getItemCount());
        holder.mainView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaController.getInstance(context).addSongToBoomPlayList(playList.get(position).getItemId(), songList, false);
                playList = MediaController.getInstance(context).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB);
                notifyDataSetChanged();
                dialog.dismiss();
                makeText(context, R.string.added_to_playlist, LENGTH_SHORT).show();
                context.sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_BOOM_PLAYLIST));
            }
        });
    }

    public void setDialog(MaterialDialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public int getItemCount() {
        return playList.size();
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public View mainView;
        public RegularTextView name, count;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            name = (RegularTextView) itemView.findViewById(R.id.playlist_dialog_name);
            count = (RegularTextView) itemView.findViewById(R.id.playlist_dialog_song_count);
        }
    }

}
