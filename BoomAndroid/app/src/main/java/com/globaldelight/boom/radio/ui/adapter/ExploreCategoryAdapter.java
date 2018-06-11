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
import com.globaldelight.boom.radio.webconnector.model.ExploreCategory;
import com.globaldelight.boom.utils.Utils;

import java.util.List;

import static com.globaldelight.boom.radio.ui.adapter.RadioFragmentStateAdapter.KEY_TYPE;

/**
 * Created by Manoj Kumar on 20-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class ExploreCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<ExploreCategory.Content> mContents;

    private boolean isPodcast=false;

    public ExploreCategoryAdapter(Context context, List<ExploreCategory.Content> contentList,boolean isPodcast) {
        this.mContext = context;
        this.mContents = contentList;
        this.isPodcast=isPodcast;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return getViewHolder(parent, inflater);
    }

    @NonNull
    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        return new LocalViewHolder(inflater.inflate(R.layout.item_explore_radio, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        LocalViewHolder viewHolder = (LocalViewHolder) holder;
        ExploreCategory.Content content = mContents.get(position);
        viewHolder.txtTitle.setText(content.getName());

        switch (content.getName()){
            case "Music Genre":
                viewHolder.imgCatThumb.setImageDrawable(mContext.getDrawable(R.drawable.geners));
            break;
            case "Topic":
                viewHolder.imgCatThumb.setImageDrawable(mContext.getDrawable(R.drawable.topic));
                break;
            case "Moods":
                viewHolder.imgCatThumb.setImageDrawable(mContext.getDrawable(R.drawable.mood));
                break;
            case "Shows on Air":
                viewHolder.imgCatThumb.setImageDrawable(mContext.getDrawable(R.drawable.on_air));
                break;
            case "Special Selection":
                viewHolder.imgCatThumb.setImageDrawable(mContext.getDrawable(R.drawable.special_season));
                break;
            case "Spotlight":
                viewHolder.imgCatThumb.setImageDrawable(mContext.getDrawable(R.drawable.spotlight));
                break;
            case "Tags":
                viewHolder.imgCatThumb.setImageDrawable(mContext.getDrawable(R.drawable.tag));
                break;
        }

        viewHolder.itemView.setOnClickListener(v->onItemClicked(v, content));

    }

    private void onItemClicked(View view, ExploreCategory.Content content) {
        if ( content.getChildCount() != null && content.getChildCount() > 0) {
            Intent intent=new Intent(mContext, SubCategoryActivity.class);
            if (content.getPermalink().equals("file://tags.json")){
                intent.putExtra("isTag",true);
            }
            intent.putExtra("title",content.getName());
            intent.putExtra("permalink",content.getPermalink());
            intent.putExtra(KEY_TYPE,isPodcast);
            mContext.startActivity(intent);
        }
        else {
            Intent intent=new Intent(mContext, SubCategoryDetailedActivity.class);
            intent.putExtra("title",content.getName());
            intent.putExtra("permalink",content.getPermalink());
            intent.putExtra(KEY_TYPE,isPodcast);
            mContext.startActivity(intent);

        }

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

            imgCatThumb = itemView.findViewById(R.id.img_explore_radio);
            txtTitle = itemView.findViewById(R.id.txt_title_explore_radio);
        }

    }

}
