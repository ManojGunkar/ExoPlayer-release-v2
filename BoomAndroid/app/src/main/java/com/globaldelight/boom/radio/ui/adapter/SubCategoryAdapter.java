package com.globaldelight.boom.radio.ui.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.globaldelight.boom.radio.ui.SubCategoryActivity;
import com.globaldelight.boom.radio.ui.SubCategoryDetailedActivity;
import com.globaldelight.boom.radio.webconnector.model.CategoryResponse;
import com.globaldelight.boom.radio.webconnector.model.ExploreCategory;
import com.globaldelight.boom.utils.Utils;

import java.util.List;

import retrofit2.Callback;

/**
 * Created by Manoj Kumar on 24-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class SubCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<CategoryResponse.Content> mContents;

    public SubCategoryAdapter(Context context, List<CategoryResponse.Content> contentList) {
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
        return new LocalViewHolder(inflater.inflate(R.layout.item_sub_category_radio, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        CategoryResponse.Content content=mContents.get(position);
        LocalViewHolder viewHolder = (LocalViewHolder) holder;
        viewHolder.txtTitle.setText(content.getName());
        final int size = Utils.largeImageSize(mContext);
        Glide.with(mContext).load(content.getLogo())
                .placeholder(R.drawable.ic_default_art_grid)
                .centerCrop()
                .override(size, size)
                .into(viewHolder.imgCatThumb);

        viewHolder.itemView.setOnClickListener(v -> {
            Toast.makeText(mContext, "pos " + position, Toast.LENGTH_SHORT).show();
            if (content.getProductCount()==null||content.getProductCount()==0){
                    Intent intent=new Intent(mContext, SubCategoryActivity.class);
                    intent.putExtra("title",content.getName());
                    intent.putExtra("permalink",content.getPermalink());
                    mContext.startActivity(intent);
            }
            else {
                Intent intent=new Intent(mContext, SubCategoryDetailedActivity.class);
                intent.putExtra("title",content.getName());
                intent.putExtra("isTagDisable",true);
                intent.putExtra("permalink",content.getPermalink());
                intent.putExtra("url",content.getLogo());
                mContext.startActivity(intent);
            }

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

            imgCatThumb = itemView.findViewById(R.id.img_sub_category_radio);
            txtTitle = itemView.findViewById(R.id.txt_title_sub_category_radio);
        }

    }

}
