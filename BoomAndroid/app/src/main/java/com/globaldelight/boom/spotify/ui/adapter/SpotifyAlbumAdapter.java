package com.globaldelight.boom.spotify.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.R;
import com.globaldelight.boom.spotify.apiconnector.pojo.NewReleaseAlbums;
import com.globaldelight.boom.spotify.ui.SpotifyAlbumActivity;

import java.util.List;

import static com.globaldelight.boom.spotify.utils.Helper.ALBUM_ID;
import static com.globaldelight.boom.spotify.utils.Helper.TOKEN;

/**
 * Created by Manoj Kumar on 10/26/2017.
 */

public class SpotifyAlbumAdapter extends RecyclerView.Adapter<SpotifyAlbumAdapter.AlbumViewHolder> {

    private Context context;
    private List<NewReleaseAlbums.Item> list;
    private String token;


    public SpotifyAlbumAdapter(Context context, List<NewReleaseAlbums.Item> albumsList,String accessToken) {
        this.token=accessToken;
        this.list = albumsList;
        this.context = context;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.spotify_album_grid_item, parent, false);
        AlbumViewHolder holder = new AlbumViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder holder, int position) {
        if (list.get(position).getImages().get(0).getUrl() != null) {
            Glide.with(context)
                    .load(list.get(position).getImages().get(0).getUrl())
                    .into(holder.imgAlbumThumb);

            holder.itemView.setOnClickListener(v->{
                Intent intent=new Intent(context,SpotifyAlbumActivity.class);
                intent.putExtra(TOKEN, token);
                intent.putExtra(ALBUM_ID, list.get(position).getId());
                context.startActivity(intent);
            });

        } else {
            holder.imgAlbumThumb.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_no_music_placeholder));
        }
        holder.txtAlbumTitle.setText(list.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

   class AlbumViewHolder extends RecyclerView.ViewHolder{
        ImageView imgAlbumThumb;
        TextView txtAlbumTitle;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            imgAlbumThumb =  itemView.findViewById(R.id.img_album_thumb_spotify);
            txtAlbumTitle =  itemView.findViewById(R.id.txt_album_title_spotify);
        }

    }

}
