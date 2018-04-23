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

import com.bumptech.glide.Glide;
import com.globaldelight.boom.R;
import com.globaldelight.boom.radio.ui.CountryDetailedActivity;
import com.globaldelight.boom.radio.webconnector.model.CountryResponse;
import com.globaldelight.boom.utils.Utils;

import java.util.List;

import static com.globaldelight.boom.radio.ui.fragments.CountryFragment.KEY_COUNTRY_CODE;
import static com.globaldelight.boom.radio.ui.fragments.CountryFragment.KEY_COUNTRY_NAME;
import static com.globaldelight.boom.radio.ui.fragments.CountryFragment.KEY_COUNTRY_URL;

/**
 * Created by Manoj Kumar on 11-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class CountryListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private final static int DISPLAYING = 0;
    private final static int LOADING = 1;

    private boolean isLoadingAdded = false;


    private Context mContext;
    private List<CountryResponse.Content> mContents;

    public CountryListAdapter(Context context, List<CountryResponse.Content> contentList) {
        this.mContext = context;
        this.mContents = contentList;
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
                viewHolder.txtTitle.setText(mContents.get(position).getName());
                final int size = Utils.largeImageSize(mContext);
                Glide.with(mContext).load(mContents.get(position).getLogo())
                        .placeholder(R.drawable.ic_default_art_grid)
                        .centerCrop()
                        .override(size, size)
                        .into(viewHolder.imgLocalRadioLogo);

                viewHolder.itemView.setOnClickListener(v -> {
                    String country=mContents.get(position).getName();
                    String code=mContents.get(position).getPermalink();
                    code=code.substring(code.length()-2,code.length()).toUpperCase();
                    String url=mContents.get(position).getLogo();
                    Intent intent=new Intent(mContext, CountryDetailedActivity.class);
                    intent.putExtra(KEY_COUNTRY_NAME,country);
                    intent.putExtra(KEY_COUNTRY_URL,url);
                    intent.putExtra(KEY_COUNTRY_CODE,code);
                    mContext.startActivity(intent);
                });
                break;

            case LOADING:

                break;
        }

    }

    @Override
    public int getItemCount() {
        return mContents == null ? 0 : mContents.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == mContents.size() - 1 && isLoadingAdded) ? LOADING : DISPLAYING;
    }

    public void add(CountryResponse.Content content) {
        mContents.add(content);
        notifyItemInserted(mContents.size() - 1);
    }

    public void addAll(List<CountryResponse.Content> moveResults) {
        for (CountryResponse.Content result : moveResults) {
            add(result);
        }
    }

    public void remove(CountryResponse.Content content) {
        int position = mContents.indexOf(content);
        if (position > -1) {
            mContents.remove(position);
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


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new CountryResponse().new Content());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = mContents.size() - 1;
        CountryResponse.Content result = getItem(position);

        if (result != null) {
            mContents.remove(position);
            notifyItemRemoved(position);
        }
    }

    public CountryResponse.Content getItem(int position) {
        return mContents.get(position);
    }

    protected class LocalViewHolder extends RecyclerView.ViewHolder  {

        private ImageView imgLocalRadioLogo;
        private TextView txtTitle;

        public LocalViewHolder(View itemView) {
            super(itemView);

            imgLocalRadioLogo = itemView.findViewById(R.id.img_country_radio);
            txtTitle = itemView.findViewById(R.id.txt_country_name_radio);
        }

    }

    protected class LoadingViewHolder extends RecyclerView.ViewHolder {

        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }
}
