package com.globaldelight.boom.business.ads.adspresenter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.BuildConfig;
import com.globaldelight.boom.R;
import com.globaldelight.boom.business.ads.builder.AdsBuilder;
import com.globaldelight.boom.business.ads.viewholder.GoogleAdViewHolder;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeContentAd;

/**
 * Created by Manoj Kumar on 6/13/2017.
 */

public class GoogleAdsPresenter implements AdsPresenter, InterstitialAdsPresenter {
    private Context mContext;
    private InterstitialAd mInterstitialAd;

    private AdsBuilder.AdsParam param;
    private AdLoader adLoader;
    private NativeContentAd mAd = null;
    private Callback callback;

    private int[] mPositions = new int[0];
    private final int AD_INTERVAL = 10;
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
        this.mContext=param.context;
        this.param = param;
        adLoader = new AdLoader.Builder(param.context, BuildConfig.GOOGLE_NATIVE_AD_ID)
                .forContentAd(contentAd -> {
                    mAd = contentAd;
                    if ( callback != null ) {
                        callback.onAdsLoaded();
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build()).build();
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
            return new GoogleAdViewHolder(inflater.inflate(R.layout.google_native_linear_ads_update, parent, false));
        else
            return new GoogleAdViewHolder(inflater.inflate(R.layout.google_native_grid_ads_update, parent, false));
    }

    @Override
    public void bind(RecyclerView.ViewHolder holder, int position) {
        GoogleAdViewHolder viewHolder = (GoogleAdViewHolder) holder;
        if ( mAd == null ) {
            viewHolder.adContentView.setVisibility(View.GONE);

            if ( adLoader != null && adLoader.isLoading() ) {
                viewHolder.errorView.setVisibility(View.GONE);
                viewHolder.progressView.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.errorView.setVisibility(View.VISIBLE);
                viewHolder.progressView.setVisibility(View.GONE);
            }

            return;
        }

        NativeContentAd ad = mAd;
        viewHolder.progressView.setVisibility(View.GONE);
        viewHolder.errorView.setVisibility(View.GONE);
        viewHolder.adContentView.setVisibility(View.VISIBLE);
        viewHolder.headerView.setText(ad.getHeadline());
        viewHolder.descriptionView.setText(ad.getBody());
        if ( viewHolder.imageView != null && ad.getImages().size() > 0 ) {
            viewHolder.imageView.setImageDrawable(ad.getImages().get(0).getDrawable());
        }
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

    @Override
    public void onComplete() {
      //  MobileAds.initialize(mContext, "ca-app-pub-3940256099942544~3347511713");

        mInterstitialAd = new InterstitialAd(mContext);
        mInterstitialAd.setAdUnitId(BuildConfig.GOOGLE_INTERSTITIAL_AD_ID);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }
}
