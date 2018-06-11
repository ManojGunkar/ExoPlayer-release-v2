package com.globaldelight.boom.radio.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.R;
import com.globaldelight.boom.radio.ui.SubCategoryActivity;
import com.globaldelight.boom.radio.ui.SubCategoryDetailedActivity;
import com.globaldelight.boom.radio.webconnector.model.CategoryResponse;
import com.globaldelight.boom.utils.Utils;

import java.util.List;

/**
 * Created by Manoj Kumar on 24-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class SubCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int DISPLAYING = 0;
    private final static int LOADING = 1;
    private boolean retryPageLoad = false;

    private String errorMsg;

    private Context mContext;
    private List<CategoryResponse.Content> mContents;
    private boolean isLoadingAdded = false;
    private RetryCallback mCallback;

    public interface RetryCallback {
        void retryPageLoad();
    }

    public void setRetryCallback(RetryCallback retryCallback){
        this.mCallback=retryCallback;
    }

    public SubCategoryAdapter(Context context, List<CategoryResponse.Content> contentList) {
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
                viewHolder = new SubCategoryAdapter.LoadingViewHolder(inflater.inflate(R.layout.item_progress, parent, false));
                break;
        }
        return viewHolder;

    }

    @NonNull
    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        return new LocalViewHolder(inflater.inflate(R.layout.item_sub_category_radio, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)) {
            case DISPLAYING:
                CategoryResponse.Content content = mContents.get(position);
                LocalViewHolder viewHolder = (LocalViewHolder) holder;
                viewHolder.txtTitle.setText(content.getName());
                final int size = Utils.largeImageSize(mContext);
                Glide.with(mContext).load(content.getLogo())
                        .placeholder(R.drawable.ic_default_art_grid)
                        .centerCrop()
                        .override(size, size)
                        .into(viewHolder.imgCatThumb);

                viewHolder.itemView.setOnClickListener(v -> {
                    if (content.getProductCount() == null || content.getProductCount() == 0) {
                        Intent intent = new Intent(mContext, SubCategoryActivity.class);
                        intent.putExtra("title", content.getName());
                        intent.putExtra("permalink", content.getPermalink());
                        mContext.startActivity(intent);
                    } else {
                        Intent intent = new Intent(mContext, SubCategoryDetailedActivity.class);
                        intent.putExtra("title", content.getName());
                        intent.putExtra("isTagDisable", true);
                        intent.putExtra("permalink", content.getPermalink());
                        intent.putExtra("url", content.getLogo());
                        mContext.startActivity(intent);
                    }

                });
                break;

            case LOADING:
                LoadingViewHolder loadingVH = (LoadingViewHolder) holder;

                if (retryPageLoad) {
                    loadingVH.llError.setVisibility(View.VISIBLE);
                    loadingVH.progressBar.setVisibility(View.GONE);

                    loadingVH.txtError.setText(
                            errorMsg != null ?
                                    errorMsg :
                                    mContext.getString(R.string.error_msg_unknown));
                } else {
                    loadingVH.llError.setVisibility(View.GONE);
                    loadingVH.progressBar.setVisibility(View.VISIBLE);
                }
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

    public void add(CategoryResponse.Content content) {
        mContents.add(content);
        notifyItemInserted(mContents.size() - 1);
    }

    public void addAll(List<CategoryResponse.Content> moveResults) {
        for (CategoryResponse.Content result : moveResults) {
            add(result);
        }
    }

    public void remove(CategoryResponse.Content content) {
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
        add(new CategoryResponse().new Content());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = mContents.size() - 1;
        if (position>=0){
            CategoryResponse.Content result = getItem(position);

            if (result != null) {
                mContents.remove(position);
                notifyItemRemoved(position);
            }
        }

    }

    public CategoryResponse.Content getItem(int position) {
        return mContents.get(position);
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(mContents.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
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

    protected class LoadingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ProgressBar progressBar;
        private ImageButton btnRetry;
        private TextView txtError;
        private LinearLayout llError;

        public LoadingViewHolder(View itemView) {
            super(itemView);

            progressBar = itemView.findViewById(R.id.loadmore_progress);
            btnRetry = itemView.findViewById(R.id.loadmore_retry);
            txtError = itemView.findViewById(R.id.loadmore_errortxt);
            llError = itemView.findViewById(R.id.loadmore_errorlayout);

            btnRetry.setOnClickListener(this);
            llError.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.loadmore_retry:
                case R.id.loadmore_errorlayout:

                    showRetry(false, null);
                    mCallback.retryPageLoad();

                    break;
            }
        }
    }

}
