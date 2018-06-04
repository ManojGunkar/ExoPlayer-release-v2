package com.globaldelight.boom.radio.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.collection.base.IMediaElement;
import com.globaldelight.boom.radio.utils.FavouriteRadioManager;
import com.globaldelight.boom.radio.webconnector.model.RadioStationResponse;
import com.globaldelight.boom.utils.Utils;

import java.util.List;

/**
 * Created by Manoj Kumar on 09-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class RadioListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int DISPLAYING = 0;
    private final static int LOADING = 1;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private String errorMsg;

    private Context mContext;
    private List<RadioStationResponse.Content> mContents;
    private Callback mCallback;
    private boolean isPaginationEnabled = true;


    public RadioListAdapter(Context context, Callback callback, List<RadioStationResponse.Content> contentList) {
        this.mContext = context;
        this.mContents = contentList;
        this.mCallback = callback;
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

        App.playbackManager().queue().addItemListToPlay(mContents, position, false);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case DISPLAYING:
                RadioStationResponse.Content content = mContents.get(position);
                LocalViewHolder viewHolder = (LocalViewHolder) holder;
                viewHolder.mainView.setElevation(0);
                viewHolder.txtTitle.setText(content.getName());
                viewHolder.txtSubTitle.setText(content.getDescription());
                final int size = Utils.largeImageSize(mContext);
                Glide.with(mContext).load(content.getLogo())
                        .placeholder(R.drawable.ic_default_art_grid)
                        .centerCrop()
                        .override(size, size)
                        .into(viewHolder.imgStationThumbnail);

                viewHolder.checkFavRadio.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked){
                        FavouriteRadioManager.getInstance(mContext).addRadioStation(content);
                    }else {
                        FavouriteRadioManager.getInstance(mContext).removeRadioStation(content);
                    }
                });

                if (FavouriteRadioManager.getInstance(mContext).containsRadioStation(content)) {
                    viewHolder.checkFavRadio.setChecked(true);
                } else {
                    viewHolder.checkFavRadio.setChecked(false);
                }


                updatePlayingStation(viewHolder, mContents.get(position));
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

    private void updatePlayingStation(LocalViewHolder holder, IMediaElement item) {
        IMediaElement nowPlayingItem = App.playbackManager().queue().getPlayingItem();
        holder.overlay.setVisibility(View.GONE);
        holder.imgOverlayPlay.setVisibility(View.GONE);
        holder.progressBar.setVisibility(View.GONE);
        holder.txtTitle.setSelected(false);

        if (null != nowPlayingItem) {
            if (item.equalTo(nowPlayingItem)) {
                holder.overlay.setVisibility(View.VISIBLE);
                holder.imgOverlayPlay.setVisibility(View.VISIBLE);
                holder.txtTitle.setSelected(true);
                holder.progressBar.setVisibility(View.GONE);
                holder.imgOverlayPlay.setImageResource(R.drawable.ic_player_play);
                if (App.playbackManager().isTrackPlaying()) {
                    holder.imgOverlayPlay.setImageResource(R.drawable.ic_player_pause);
                    if (App.playbackManager().isTrackLoading()) {
                        holder.progressBar.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mContents == null ? 0 : mContents.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isPaginationEnabled) {
            return (position == mContents.size() - 1 && isLoadingAdded) ? LOADING : DISPLAYING;
        }

        return DISPLAYING;
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
        if (position>=0){
            RadioStationResponse.Content result = getItem(position);

            if (result != null) {
                mContents.remove(position);
                notifyItemRemoved(position);
            }
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

    public interface Callback {
        void retryPageLoad();
    }

    protected class LocalViewHolder extends RecyclerView.ViewHolder {

        private TextView txtTitle;
        private TextView txtSubTitle;
        private View mainView;
        private View overlay;
        private ImageView imgStationThumbnail;
        private ImageView imgOverlayPlay;
        private CheckBox checkFavRadio;
        private ProgressBar progressBar;

        public LocalViewHolder(View itemView) {
            super(itemView);

            mainView = itemView;
            imgStationThumbnail = itemView.findViewById(R.id.song_item_img);
            checkFavRadio = itemView.findViewById(R.id.check_fav_station);
            imgOverlayPlay = itemView.findViewById(R.id.song_item_img_overlay_play);
            overlay = itemView.findViewById(R.id.song_item_img_overlay);
            progressBar = itemView.findViewById(R.id.load_cloud);
            txtTitle = itemView.findViewById(R.id.txt_title_station);
            txtSubTitle = itemView.findViewById(R.id.txt_sub_title_station);

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
