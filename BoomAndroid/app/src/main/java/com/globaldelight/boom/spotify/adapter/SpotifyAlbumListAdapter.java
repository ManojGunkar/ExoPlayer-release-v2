package com.globaldelight.boom.spotify.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.spotify.pojo.AlbumPlaylist;
import com.globaldelight.boom.spotify.pojo.NewReleaseAlbums;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Manoj Kumar on 10/26/2017.
 */

public class SpotifyAlbumListAdapter extends RecyclerView.Adapter<SpotifyAlbumListAdapter.AlbumViewHolder> {

    private Context context;
    private List<AlbumPlaylist.Item> list;

    private ItemClickListener mClickListener;


    public SpotifyAlbumListAdapter(Context context, List<AlbumPlaylist.Item> albumsList) {

        this.list = albumsList;
        this.context = context;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.spotify_album_list, parent, false);
        AlbumViewHolder holder = new AlbumViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder holder, int position) {

        holder.txtTrackTitle.setText(list.get(position).getName());
        holder.txtTrackId.setText(list.get(position).getUri());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

   class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txtTrackId;
        TextView txtTrackTitle;

        public AlbumViewHolder(View itemView) {
            super(itemView);

            txtTrackTitle = (TextView) itemView.findViewById(R.id.txt_track_title_spotify);
            txtTrackId = (TextView) itemView.findViewById(R.id.txt_track_id_spotify);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if ( mClickListener != null) mClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

}
