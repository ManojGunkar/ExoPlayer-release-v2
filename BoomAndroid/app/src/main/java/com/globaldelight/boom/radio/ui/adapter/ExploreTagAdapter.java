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

import com.globaldelight.boom.R;
import com.globaldelight.boom.radio.ui.SubCategoryDetailedActivity;
import com.globaldelight.boom.radio.webconnector.model.ExploreTag;

import java.util.Collections;
import java.util.List;

/**
 * Created by Manoj Kumar on 20-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class ExploreTagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Context mContext;
    private List<ExploreTag.Tags> mTags= Collections.emptyList();

    public ExploreTagAdapter(Context context, List<ExploreTag.Tags> tags) {
        this.mContext = context;
        this.mTags = tags;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return getViewHolder(parent, inflater);
    }

    @NonNull
    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        return new TagViewHolder(inflater.inflate(R.layout.item_category_radio, parent, false));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TagViewHolder viewHolder = (TagViewHolder) holder;
        viewHolder.txtTitle.setText(mTags.get(position).getName());

        viewHolder.itemView.setOnClickListener(v -> {
            Intent intent=new Intent(mContext, SubCategoryDetailedActivity.class);
            intent.putExtra("title",mTags.get(position).getName());
            intent.putExtra("permalink",mTags.get(position).getSearchName());
            intent.putExtra("url","");
            mContext.startActivity(intent);


        });

    }

    @Override
    public int getItemCount() {
        return mTags == null ? 0 : mTags.size();
    }

    protected class TagViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgTagThumb;
        private TextView txtTitle;

        public TagViewHolder(View itemView) {
            super(itemView);

            imgTagThumb = itemView.findViewById(R.id.img_category_radio);
            txtTitle = itemView.findViewById(R.id.txt_title_category_radio);
        }

    }

}
