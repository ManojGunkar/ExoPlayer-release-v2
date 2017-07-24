package com.globaldelight.boom.app.businessmodel.strategy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.globaldelight.boom.app.businessmodel.ads.adapter.AdWrapperAdapter;
import com.globaldelight.boom.app.businessmodel.ads.adspresenter.AdsPresenter;
import com.globaldelight.boom.app.businessmodel.ads.adspresenter.NoAdsPresenter;
import com.globaldelight.boom.app.businessmodel.ads.builder.AdsBuilder;

/**
 * Created by adarsh on 17/07/17.
 */

public class AdController extends BroadcastReceiver {

    private AdWrapperAdapter mAdAdapter;
    private AdsPresenter mPresenter;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mBaseAdapter;
    private boolean mIsGrid;
    private Context mContext;

    public AdController(Context context, RecyclerView recyclerView, RecyclerView.Adapter adapter, boolean isGrid ) {
        mContext = context;
        mRecyclerView = recyclerView;
        mIsGrid = isGrid;
        mBaseAdapter = adapter;

        IntentFilter filter = new IntentFilter(BusinessStrategy.ACTION_ADS_STATUS_CHANGED);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(this, filter);
    }

    public AdWrapperAdapter getAdAdapter() {
        if ( mAdAdapter == null ) {
            createAdAdapter();
        }
        return mAdAdapter;
    }

    private void createAdAdapter() {

        AdsBuilder builder = AdsBuilder.initGoogleAds(mContext, mBaseAdapter, !mIsGrid, mBaseAdapter.getItemCount())
                .setInterval(2);
        if ( mIsGrid ) {
            builder.setGridLayoutManager((GridLayoutManager)mRecyclerView.getLayoutManager());
        }
        else {
            builder.setLinearLayoutManager((LinearLayoutManager)mRecyclerView.getLayoutManager());
        }

        mAdAdapter = builder.buildGoogleAds();
        mPresenter = mAdAdapter.getAdsPresenter();
        if ( !BusinessStrategy.getInstance(mContext).isAdsEnabled() ) {
            mAdAdapter.setAdsPresenter(new NoAdsPresenter());
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ( intent.getAction().equals(BusinessStrategy.ACTION_ADS_STATUS_CHANGED) ) {
            if ( !BusinessStrategy.getInstance(mContext).isAdsEnabled() ) {
                mAdAdapter.setAdsPresenter(new NoAdsPresenter());
            }
            else {
                mAdAdapter.setAdsPresenter(mPresenter);
            }
        }
    }
}
