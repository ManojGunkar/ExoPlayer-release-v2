package com.globaldelight.boom.radio.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.R;
import com.globaldelight.boom.radio.webconnector.responsepojo.CountryResponse;
import com.globaldelight.boom.utils.Utils;

import java.util.List;

/**
 * Created by Manoj Kumar on 11-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class CountryListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private final static int DISPLAYING = 0;
    private final static int LOADING = 1;

    private boolean isLoadingAdded = false;

    private OnItemClickListener mOnItemClickListener;

    private Context mContext;
    private List<CountryResponse.Content> contentList;

    public CountryListAdapter(Context context, List<CountryResponse.Content> contentList) {
        this.mContext = context;
        this.contentList = contentList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case DISPLAYING:
                viewHolder = getViewHolder(parent, inflater);
                break;
            case LOADING:
                viewHolder = new LoadingViewHolder(inflater.inflate(R.layout.blank_footer, parent, false));
                break;
        }
        return viewHolder;
    }

    @NonNull
    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        return new LocalViewHolder(inflater.inflate(R.layout.item_country_radio, parent, false));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case DISPLAYING:
                LocalViewHolder viewHolder = (LocalViewHolder) holder;
                viewHolder.txtTitle.setText(contentList.get(position).getName());
                final int size = Utils.largeImageSize(mContext);
                Glide.with(mContext).load(contentList.get(position).getLogo())
                        .placeholder(R.drawable.ic_default_art_grid)
                        .centerCrop()
                        .override(size, size)
                        .into(viewHolder.imgLocalRadioLogo);

                break;

            case LOADING:

                break;
        }

    }

    @Override
    public int getItemCount() {
        return contentList == null ? 0 : contentList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == contentList.size() - 1 && isLoadingAdded) ? LOADING : DISPLAYING;
    }

    public void add(CountryResponse.Content content) {
        contentList.add(content);
        notifyItemInserted(contentList.size() - 1);
    }

    public void addAll(List<CountryResponse.Content> moveResults) {
        for (CountryResponse.Content result : moveResults) {
            add(result);
        }
    }

    public void remove(CountryResponse.Content content) {
        int position = contentList.indexOf(content);
        if (position > -1) {
            contentList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void setItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener=onItemClickListener;
    }

    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new CountryResponse().new Content());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = contentList.size() - 1;
        CountryResponse.Content result = getItem(position);

        if (result != null) {
            contentList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public CountryResponse.Content getItem(int position) {
        return contentList.get(position);
    }

    protected class LocalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imgLocalRadioLogo;
        private TextView txtTitle;

        public LocalViewHolder(View itemView) {
            super(itemView);

            imgLocalRadioLogo = itemView.findViewById(R.id.img_country_radio);
            txtTitle = itemView.findViewById(R.id.txt_coutry_name_radio);
            itemView.setOnClickListener(this::onClick);
        }

        @Override
        public void onClick(View v) {
            mOnItemClickListener.onItemClick(v,getAdapterPosition());
        }
    }

    protected class LoadingViewHolder extends RecyclerView.ViewHolder {

        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }
}
