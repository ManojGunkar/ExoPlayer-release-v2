package com.globaldelight.boom.app.businessmodel.ads.adspresenter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.facebook.ads.AbstractAdListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.NativeAd;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.businessmodel.ads.builder.AdsBuilder;
import com.globaldelight.boom.app.businessmodel.ads.viewholder.FbViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manoj Kumar on 6/7/2017.
 */

public class FacebookAdsPresenter implements AdsPresenter {
    private AdsBuilder.AdsParam param;
    private int[] mPositions = new int[0];
    private final static int AD_INTERVAL = 3;
    private Callback callback;

    public FacebookAdsPresenter(AdsBuilder.AdsParam param) {
        this.param = param;
    }

    @Override
    public void update(int count) {
        mPositions = new int[count/AD_INTERVAL];
        for ( int i = 0; i < mPositions.length; i++ ) {
            int position = 1;
            mPositions[i] = i * AD_INTERVAL + position;
        }
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
        View adLayoutOutline = inflater
                .inflate(param.itemContainerLayoutRes, parent, false);
        ViewGroup vg = (ViewGroup) adLayoutOutline.findViewById(param.itemGridContainerId);

        LinearLayout adLayoutContent = (LinearLayout) inflater
                .inflate(R.layout.grid_facebook_native_ad, parent, false);
        vg.addView(adLayoutContent);

        if (param.isLinearAds)
            return new FbViewHolder( inflater
                    .inflate(R.layout.item_linear_fb_ads, parent, false));
        else
            return new FbViewHolder(adLayoutOutline);
    }

    @Override
    public void bind(RecyclerView.ViewHolder holder, int position) {
        setAdsInViewHolder(holder);
    }


    @Override
    public void finish() {

    }

    public void setAdsInViewHolder(final RecyclerView.ViewHolder holder) {

        param.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                final FbViewHolder adHolder = (FbViewHolder) holder;
                if (param.forceReloadAdOnBind || !adHolder.loaded) {
                    final NativeAd nativeAd = new NativeAd(adHolder.getContext(), param.fBAppId);
                    nativeAd.setAdListener(new AbstractAdListener() {
                        @Override
                        public void onAdLoaded(Ad ad) {
                            if (ad != nativeAd) {
                                return;
                            }
                            adHolder.llAdContainer.setVisibility(View.VISIBLE);

                            // Set the Text.
                            adHolder.txtAdTitle.setText(nativeAd.getAdTitle());
                            adHolder.txtAdSocialContext.setText(nativeAd.getAdSocialContext());
                            adHolder.txtAdBody.setText(nativeAd.getAdBody());
                            adHolder.btnAdCallToAction.setText(nativeAd.getAdCallToAction());

                            // Download and display the ad icon.
                            NativeAd.Image adIcon = nativeAd.getAdIcon();
                            NativeAd.downloadAndDisplayImage(adIcon, adHolder.imgAdIcon);

                            // Download and display the cover image.
                            adHolder.mediaViewAd.setNativeAd(nativeAd);

                            // Add the AdChoices icon
                            AdChoicesView adChoicesView = new AdChoicesView(adHolder.getContext(), nativeAd, true);
                            adHolder.llAdChoicesContainer.removeAllViews();
                            adHolder.llAdChoicesContainer.addView(adChoicesView);

                            // Register the Title and CTA button to listen for clicks.
                            List<View> clickableViews = new ArrayList<>();
                            clickableViews.add(adHolder.txtAdTitle);
                            clickableViews.add(adHolder.btnAdCallToAction);
                            nativeAd.registerViewForInteraction(adHolder.llAdContainer, clickableViews);

                            adHolder.loaded = true;
                        }

                        @Override
                        public void onError(Ad ad, AdError adError) {
                            adHolder.llAdContainer.setVisibility(View.GONE);
                        }
                    });
                    nativeAd.loadAd();
                }
            }
        });

    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }


}
