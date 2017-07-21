package com.globaldelight.boom.app.businessmodel.ads.adspresenter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by Manoj Kumar on 6/7/2017.
 */

public interface AdsPresenter {
    int getCount();
    int[] getPositions();
    RecyclerView.ViewHolder createViewHolder(ViewGroup parent);
    void bind(RecyclerView.ViewHolder holder, int position);
    void update(int count);
    void finish();
    void setCallback(AdsPresenter.Callback callback);

    interface Callback{
        void onAdsLoaded();
    }
}
