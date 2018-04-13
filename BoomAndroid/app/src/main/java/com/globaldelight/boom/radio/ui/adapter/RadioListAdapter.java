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
import com.globaldelight.boom.radio.utils.SaveFavouriteRadio;
import com.globaldelight.boom.radio.webconnector.responsepojo.RadioStationResponse;
import com.globaldelight.boom.utils.Utils;

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
    private List<RadioStationResponse.Content> mContents;
    private Callback mCallback;

    public RadioListAdapter(Context context,Callback callback, List<RadioStationResponse.Content> contentList){
        this.mContext=context;
        this.mContents =contentList;
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
        if (position < 0) {
            return;
        }

        App.playbackManager().queue().addItemToPlay(mContents.get(position));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)){
            case DISPLAYING:
                LocalViewHolder viewHolder= (LocalViewHolder) holder;
                viewHolder.txtTitle.setText(mContents.get(position).getName());
                viewHolder.txtSubTitle.setText(mContents.get(position).getDescription());
                final int size = Utils.largeImageSize(mContext);
                Glide.with(mContext).load(mContents.get(position).getLogo())
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
        return mContents == null ? 0 : mContents.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == mContents.size() - 1 && isLoadingAdded) ? LOADING : DISPLAYING;
    }

    public void add(RadioStationResponse.Content content) {
        mContents.add(content);
        notifyItemInserted(mContents.size() - 1);
    }

    public void addAll(List<RadioStationResponse.Content> moveResults) {
        for (RadioStationResponse.Content result : moveResults) {
            add(result);
        }
    }

    public void remove(RadioStationResponse.Content content) {
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
        add(new RadioStationResponse().new Content());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = mContents.size() - 1;
        RadioStationResponse.Content result = getItem(position);

        if (result != null) {
            mContents.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(mContents.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    public RadioStationResponse.Content getItem(int position) {
        return mContents.get(position);
    }

    protected class LocalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView imgLocalRadioLogo;
        private ImageView imgFavRadio;
        private TextView txtTitle;
        private TextView txtSubTitle;

        public LocalViewHolder(View itemView) {
            super(itemView);

            imgLocalRadioLogo=itemView.findViewById(R.id.img_title_logo_local_radio);
            imgFavRadio=itemView.findViewById(R.id.img_fav_radio_station);
            txtTitle=itemView.findViewById(R.id.txt_title_local_radio);
            txtSubTitle=itemView.findViewById(R.id.txt_sub_title_local_radio);

            imgFavRadio.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.img_fav_radio_station:
                    int position = getAdapterPosition();
                    if (position < 0) {
                        return;
                    }
                    saveRadio(imgFavRadio,position);
                    break;
            }

        }
    }

    private void saveRadio(ImageView img,int position){
        SaveFavouriteRadio.getInstance(mContext).addFavRadioStation(mContents.get(position));
        img.setImageDrawable(mContext.getDrawable(R.drawable.fav_selected));
    }
    private void removeRadio(ImageView img,int position){
        SaveFavouriteRadio.getInstance(mContext).removeFavRadioStation(mContents.get(position));
        img.setImageDrawable(mContext.getDrawable(R.drawable.fav_normal));
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
