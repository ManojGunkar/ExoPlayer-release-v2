package com.globaldelight.boom.radio.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.R;
import com.globaldelight.boom.radio.webconnector.responsepojo.LocalRadioResponse;
import com.globaldelight.boom.utils.Utils;

/**
 * Created by Manoj Kumar on 09-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class LocalRadioListAdapter extends RecyclerView.Adapter<LocalRadioListAdapter.LocalViewHolder> {

    private Context mContext;
    private LocalRadioResponse localRadioResponse;

    public LocalRadioListAdapter(Context context,LocalRadioResponse localRadioResponse){
        this.mContext=context;
        this.localRadioResponse=localRadioResponse;
    }

    @Override
    public LocalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_local_radio, null);

        LocalViewHolder viewHolder = new LocalViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(LocalViewHolder holder, int position) {
        holder.txtTitle.setText(localRadioResponse.getBody().getContent().get(position).getName());
        holder.txtSubTitle.setText(localRadioResponse.getBody().getContent().get(position).getDescription());
        final int size = Utils.largeImageSize(mContext);
        Glide.with(mContext).load(localRadioResponse.getBody().getContent().get(position).getLogo())
                .placeholder(R.drawable.ic_default_art_grid)
                .centerCrop()
                .override(size, size)
                .into(holder.imgLocalRadioLogo);
    }

    @Override
    public int getItemCount() {
        return localRadioResponse.getBody().getContent().size();
    }

    static class LocalViewHolder extends RecyclerView.ViewHolder{

        private ImageView imgLocalRadioLogo;
        private TextView txtTitle;
        private TextView txtSubTitle;

        public LocalViewHolder(View itemView) {
            super(itemView);

            imgLocalRadioLogo=itemView.findViewById(R.id.img_title_logo_local_radio);
            txtTitle=itemView.findViewById(R.id.txt_title_local_radio);
            txtSubTitle=itemView.findViewById(R.id.txt_sub_title_local_radio);
        }
    }
}
