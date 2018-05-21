package com.globaldelight.boom.app.adapters.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.collection.local.MediaItemCollection;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.base.IMediaElement;

import java.util.ArrayList;
import static android.view.LayoutInflater.from;
import static android.view.View.OnClickListener;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class AddToPlaylistAdapter extends RecyclerView.Adapter<AddToPlaylistAdapter.SimpleItemViewHolder> {

    private ArrayList<? extends IMediaElement> playList;
    private ArrayList<? extends IMediaElement> songList;
    private Context context;
    private MaterialDialog dialog;

    public AddToPlaylistAdapter(Context context, ArrayList<? extends IMediaElement>  playList, ArrayList<? extends IMediaElement> songList) {
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
        holder.name.setText(playList.get(position).getTitle());
        holder.count.setText(context.getResources().getString(R.string.song_count,((MediaItemCollection) playList.get(position)).getItemCount()));
        holder.mainView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if(songList.size() == 1){
                    if(MediaController.getInstance(context).isAlreadyAdded(playList.get(position), songList.get(0))){
                        makeText(context, R.string.playlist_track_already_added, LENGTH_SHORT).show();
                    }else{
                        addToPlayList(position);
                        makeText(context, R.string.added_to_playlist, LENGTH_SHORT).show();
                    }
                }else if(songList.size() > 1){
                    addToPlayList(position);
                    makeText(context, R.string.added_to_playlist, LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
    }

    private void addToPlayList(int position){
        MediaController.getInstance(context).addSongToBoomPlayList(playList.get(position), songList, false);
        playList = MediaController.getInstance(context).getBoomPlayList();
        notifyDataSetChanged();
    }

    public void setDialog(MaterialDialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public int getItemCount() {
        return playList.size();
    }

    public static class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public View mainView;
        public TextView name, count;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            name = itemView.findViewById(R.id.playlist_dialog_name);
            count = itemView.findViewById(R.id.playlist_dialog_song_count);
        }
    }

}
