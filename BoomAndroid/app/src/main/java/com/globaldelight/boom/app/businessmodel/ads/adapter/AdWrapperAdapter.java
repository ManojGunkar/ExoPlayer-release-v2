package com.globaldelight.boom.app.businessmodel.ads.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.globaldelight.boom.app.businessmodel.ads.adspresenter.AdsPresenter;
import com.globaldelight.boom.app.businessmodel.ads.adspresenter.NoAdsPresenter;

/**
 * Created by Manoj Kumar on 6/2/2017.
 */

public class AdWrapperAdapter extends RecyclerView.Adapter {

    public static final int TYPE_NATIVE_ADS = 900;

    private final RecyclerView.Adapter adapter;
    private AdsPresenter adsPresenter;

    public AdWrapperAdapter(final RecyclerView.Adapter adapter, AdsPresenter ads) {
        super();
        this.adapter = adapter;
        this.adsPresenter = ads != null ? ads : new NoAdsPresenter();
        this.adsPresenter.setCallback(new AdsPresenter.Callback() {
            @Override
            public void onAdsLoaded() {
                notifyDataSetChanged();
            }
        });
        adsPresenter.update(adapter.getItemCount());
        this.adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onChanged() {
                adsPresenter.update(adapter.getItemCount());
                notifyDataSetChanged();
            }

            public void onItemRangeChanged(int positionStart, int itemCount) {
                notifyItemRangeChanged(positionStart, itemCount);
            }

            public void onItemRangeInserted(int positionStart, int itemCount) {
                notifyItemRangeInserted(positionStart, itemCount);
            }

            public void onItemRangeRemoved(int positionStart, int itemCount) {
                notifyItemRangeRemoved(positionStart, itemCount);
            }

            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                notifyItemMoved(fromPosition, toPosition);
            }
        });
    }

    public void setAdsPresenter(AdsPresenter presenter) {
        adsPresenter = presenter;
        adsPresenter.setCallback(new AdsPresenter.Callback() {
            @Override
            public void onAdsLoaded() {
                notifyDataSetChanged();
            }
        });
        notifyDataSetChanged();
    }

    public AdsPresenter getAdsPresenter() {
        return adsPresenter;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if ( viewType == TYPE_NATIVE_ADS ) {
            return adsPresenter.createViewHolder(parent);
        }
        return adapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if ( getItemViewType(position) == TYPE_NATIVE_ADS ) {
            adsPresenter.bind(holder, position);
        }
        else {
            adapter.onBindViewHolder(holder, getAdapterPosition(position));
        }
    }

    @Override
    public int getItemCount() {
        return adapter.getItemCount() + adsPresenter.getCount();
    }

    @Override
    public int getItemViewType(int position) {
        if ( isAd(position) ) {
            return TYPE_NATIVE_ADS;
        }

        return adapter.getItemViewType(getAdapterPosition(position));
    }


    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
        adapter.registerAdapterDataObserver(observer);
    }

    @Override
    public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.unregisterAdapterDataObserver(observer);
        adapter.unregisterAdapterDataObserver(observer);
    }

    public RecyclerView.Adapter getWrappedAdapter() {
        return adapter;
    }


    private boolean isAd(int position) {
        int[] adPositions = adsPresenter.getPositions();
        for ( int i = 0; i < adPositions.length; i++ ) {
            if ( position == adPositions[i] ) {
                return true;
            }
        }

        return false;
    }

    private int getAdapterPosition(int position) {
        int pos = position;
        int[] adPositions = adsPresenter.getPositions();
        for ( int i = 0; i < adPositions.length && position >= adPositions[i] ; i++ ) {
            pos--;
        }

        return pos;
    }
}