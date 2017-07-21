package com.globaldelight.boom.app.businessmodel.ads.adspresenter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by Manoj Kumar on 7/13/2017.
 */

public class NoAdsPresenter implements AdsPresenter {
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public int[] getPositions() {
        return new int[0];
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(ViewGroup parent) {
        return null;
    }

    @Override
    public void bind(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public void update(int count) {

    }

    @Override
    public void finish() {

    }

    @Override
    public void setCallback(Callback callback) {

    }
}
