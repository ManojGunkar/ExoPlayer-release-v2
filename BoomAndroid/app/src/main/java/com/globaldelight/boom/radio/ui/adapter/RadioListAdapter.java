package com.globaldelight.boom.radio.ui.adapter;

import android.content.Context;
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
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.radio.webconnector.responsepojo.LocalRadioResponse;
import com.globaldelight.boom.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manoj Kumar on 09-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class RadioListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private final static int DISPLAYING=0;
    private final static int LOADING=1;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private String errorMsg;

    private Context mContext;
    private List<LocalRadioResponse.Content> contentList;
    private Callback mCallback;

    public RadioListAdapter(Context context,Callback callback, List<LocalRadioResponse.Content> contentList){
        this.mContext=context;
        this.contentList=contentList;
        this.mCallback=callback;
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
                viewHolder = new LoadingViewHolder(inflater.inflate(R.layout.item_progress, parent, false));
                break;
        }
        return viewHolder;
    }

    @NonNull
    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        LocalViewHolder vh = new LocalViewHolder(inflater.inflate(R.layout.item_local_radio, parent, false));
        vh.itemView.setOnClickListener(v -> onClick(vh));
        return vh;
    }

    private void onClick(LocalViewHolder vh) {
        int position = vh.getAdapterPosition();
        if ( position < 0 ) {
            return;
        }

        App.playbackManager().queue().addItemToPlay(contentList.get(position));
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)){
            case DISPLAYING:
                LocalViewHolder viewHolder= (LocalViewHolder) holder;
                viewHolder.txtTitle.setText(contentList.get(position).getName());
                viewHolder.txtSubTitle.setText(contentList.get(position).getDescription());
                final int size = Utils.largeImageSize(mContext);
                Glide.with(mContext).load(contentList.get(position).getLogo())
                        .placeholder(R.drawable.ic_default_art_grid)
                        .centerCrop()
                        .override(size, size)
                        .into(viewHolder.imgLocalRadioLogo);
                break;

            case LOADING:
                LoadingViewHolder loadingVH = (LoadingViewHolder) holder;

                if (retryPageLoad) {
                    loadingVH.mErrorLayout.setVisibility(View.VISIBLE);
                    loadingVH.mProgressBar.setVisibility(View.GONE);

                    loadingVH.mErrorTxt.setText(
                            errorMsg != null ?
                                    errorMsg :
                                    mContext.getString(R.string.error_msg_unknown));

                } else {
                    loadingVH.mErrorLayout.setVisibility(View.GONE);
                    loadingVH.mProgressBar.setVisibility(View.VISIBLE);
                }
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

    public void add(LocalRadioResponse.Content content) {
        contentList.add(content);
        notifyItemInserted(contentList.size() - 1);
    }

    public void addAll(List<LocalRadioResponse.Content> moveResults) {
        for (LocalRadioResponse.Content result : moveResults) {
            add(result);
        }
    }

    public void remove(LocalRadioResponse.Content content) {
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


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new LocalRadioResponse().new Content());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = contentList.size() - 1;
        LocalRadioResponse.Content result = getItem(position);

        if (result != null) {
            contentList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(contentList.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    public LocalRadioResponse.Content getItem(int position) {
        return contentList.get(position);
    }

    protected class LocalViewHolder extends RecyclerView.ViewHolder{

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

    protected class LoadingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ProgressBar mProgressBar;
        private ImageButton mRetryBtn;
        private TextView mErrorTxt;
        private LinearLayout mErrorLayout;

        public LoadingViewHolder(View itemView) {
            super(itemView);

            mProgressBar =  itemView.findViewById(R.id.loadmore_progress);
            mRetryBtn =  itemView.findViewById(R.id.loadmore_retry);
            mErrorTxt =  itemView.findViewById(R.id.loadmore_errortxt);
            mErrorLayout =  itemView.findViewById(R.id.loadmore_errorlayout);

            mRetryBtn.setOnClickListener(this);
            mErrorLayout.setOnClickListener(this);
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

    public interface Callback{
        void retryPageLoad();
    }
}
