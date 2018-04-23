package com.globaldelight.boom.radio.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.R;
import com.globaldelight.boom.radio.webconnector.model.ExploreCategory;
import com.globaldelight.boom.utils.Utils;

import java.util.List;

/**
 * Created by Manoj Kumar on 20-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class ExploreCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<ExploreCategory.Content> mContents;

    public ExploreCategoryAdapter(Context context, List<ExploreCategory.Content> contentList) {
        this.mContext = context;
        this.mContents = contentList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return getViewHolder(parent, inflater);
    }

    @NonNull
    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        return new LocalViewHolder(inflater.inflate(R.layout.item_category_radio, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        LocalViewHolder viewHolder = (LocalViewHolder) holder;
        viewHolder.txtTitle.setText(mContents.get(position).getName());
        final int size = Utils.largeImageSize(mContext);
        Glide.with(mContext).load(mContents.get(position).getLogo())
                .placeholder(R.drawable.ic_default_art_grid)
                .centerCrop()
                .override(size, size)
                .into(viewHolder.imgCatThumb);

        viewHolder.itemView.setOnClickListener(v -> {
            Toast.makeText(mContext, "pos " + position, Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public int getItemCount() {
        return mContents == null ? 0 : mContents.size();
    }

    protected class LocalViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgCatThumb;
        private TextView txtTitle;

        public LocalViewHolder(View itemView) {
            super(itemView);

            imgCatThumb = itemView.findViewById(R.id.img_category_radio);
            txtTitle = itemView.findViewById(R.id.txt_title_category_radio);
        }

    }

}
