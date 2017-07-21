package com.globaldelight.boom.app.businessmodel.ads.adspresenter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.businessmodel.ads.builder.AdsBuilder;
import com.globaldelight.boom.app.businessmodel.ads.viewholder.GoogleAdViewHolder;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeContentAd;

/**
 * Created by Manoj Kumar on 6/13/2017.
 */

public class GoogleAdsPresenter implements AdsPresenter {
    private AdsBuilder.AdsParam param;
    private AdLoader adLoader;
    private NativeContentAd mAd = null;
    private Callback callback;

    private int[] mPositions = new int[0];
    private final int AD_INTERVAL = 50;
    private final int AD_POSITION = (int)(Math.random() * 4);

    @Override
    public void update(int count) {
        int adCount = Math.max(1, count/AD_INTERVAL);
        int adPosition = Math.min(AD_POSITION, count);
        mPositions = new int[adCount];
        for ( int i = 0; i < mPositions.length; i++ ) {
            mPositions[i] = i * AD_INTERVAL + i + adPosition;
        }
    }


    public GoogleAdsPresenter(AdsBuilder.AdsParam param) {
        this.param = param;
        //Change google ad id to release build
        adLoader = new AdLoader.Builder(param.context, "ca-app-pub-3940256099942544/2247696110")
                .forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
                    @Override
                    public void onContentAdLoaded(NativeContentAd contentAd) {
                        mAd = contentAd;
                        callback.onAdsLoaded();
                    }
                })
//                .withAdListener(new AdListener() {
//                    @Override
//                    public void onAdFailedToLoad(int errorCode) {
//                    }
//
//                    @Override
//                    public void onAdLoaded() {
//                        callback.onAdsLoaded();
//                    }
//                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        .build())
                .build();


        adLoader.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public int getCount() {
        return mPositions.length;
    }

    @Override
    public int[] getPositions() {
        return mPositions;
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (param.isLinearAds)
            return new GoogleAdViewHolder(inflater.inflate(R.layout.google_native_linear_ads, parent, false));
        else
            return new GoogleAdViewHolder(inflater.inflate(R.layout.google_native_grid_ads, parent, false));
    }

    @Override
    public void bind(RecyclerView.ViewHolder holder, int position) {
        GoogleAdViewHolder viewHolder = (GoogleAdViewHolder) holder;
        if ( mAd == null ) {
            viewHolder.progressView.setVisibility(View.VISIBLE);
            viewHolder.adContentView.setVisibility(View.GONE);
            return;
        }

        NativeContentAd ad = mAd;
        viewHolder.progressView.setVisibility(View.GONE);
        viewHolder.adContentView.setVisibility(View.VISIBLE);


        if ( ad.getLogo() != null ) {
            viewHolder.logoView.setImageDrawable(ad.getLogo().getDrawable());
            viewHolder.logoView.setVisibility(View.VISIBLE);
        }
        else {
            viewHolder.logoView.setVisibility(View.GONE);
        }

        viewHolder.headerView.setText(ad.getHeadline());
        viewHolder.descriptionView.setText(ad.getBody());
        if ( viewHolder.imageView != null && ad.getImages().size() > 0 ) {
            viewHolder.imageView.setImageDrawable(ad.getImages().get(0).getDrawable());
        }
        viewHolder.advertiserView.setText(ad.getAdvertiser());
        viewHolder.adActionBtn.setText(ad.getCallToAction());
        viewHolder.adView.setNativeAd(ad);
    }


    @Override
    public void finish() {

    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
