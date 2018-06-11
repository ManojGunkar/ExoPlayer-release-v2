package com.globaldelight.boom.business.ads.builder;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.business.ads.adspresenter.AdsPresenter;
import com.globaldelight.boom.business.ads.adspresenter.FacebookAdsPresenter;
import com.globaldelight.boom.business.ads.adspresenter.GoogleAdsPresenter;
import com.globaldelight.boom.business.ads.adspresenter.InterstitialAdsPresenter;

/**
 * Created by Manoj Kumar on 6/2/2017.
 */

public class AdsBuilder {

    private final AdsParam param;
    private Context context;

    private AdsBuilder(AdsParam param) {
        this.param = param;
    }

    public static AdsBuilder initFacebookAds(Context context, String fBAdsId, RecyclerView.Adapter adapter, boolean isLinearAds, RecyclerView recyclerView,int listSize) {
        AdsParam param = new AdsParam();
        param.context = context;
        param.adapter = adapter;
        param.recyclerView = recyclerView;
        param.isLinearAds = isLinearAds;
        param.fBAppId = fBAdsId;
        param.adItemInterval = 10;
        param.itemContainerLayoutRes = R.layout.grid_facebook_native_ad_outline;
        param.itemGridContainerId = R.id.ad_container;
        param.forceReloadAdOnBind = true;
        param.size=listSize;
        return new AdsBuilder(param);
    }

    public static AdsBuilder initGoogleAds(Context context, RecyclerView.Adapter adapter, boolean isLinearAds,int listSize){
        AdsParam param = new AdsParam();
        param.adapter = adapter;
        param.context = context;
        param.isLinearAds = isLinearAds;
        param.size=listSize;
        return new AdsBuilder(param);
    }

    public AdsBuilder setInterval(int interval) {
        param.adItemInterval = interval;
        return this;
    }

    public AdsBuilder setLinearLayoutManager(LinearLayoutManager linearLayoutManager) {
        param.linearLayoutManager = linearLayoutManager;
        return this;
    }

    public AdsBuilder setGridLayoutManager(GridLayoutManager layoutManager) {
        param.gridLayoutManager = layoutManager;
        return this;
    }

    public AdsPresenter buildFacebookAds() {
        FlurryAnalytics.getInstance(param.context).setEvent(FlurryEvents.EVENT_FACEBOOK_ADS);
        return new FacebookAdsPresenter(param);
    }

    public AdsPresenter buildGoogleAds(){
        FlurryAnalytics.getInstance(param.context).setEvent(FlurryEvents.EVENT_GOOGLE_ADS);
        return new GoogleAdsPresenter(param);
    }

    public static InterstitialAdsPresenter buildInterstitialGoogleAds(Context context){
        return new GoogleAdsPresenter(context);
    }

    public static class AdsParam {

        public String fBAppId;
        public Context context;
        public String googleAdsId;
        public RecyclerView.Adapter adapter;
        public RecyclerView recyclerView;
        public int adItemInterval;
        public boolean isLinearAds;
        public boolean forceReloadAdOnBind;
        public int itemContainerLayoutRes;
        public int itemGridContainerId;
        public GridLayoutManager gridLayoutManager;
        public LinearLayoutManager linearLayoutManager;
        public int size;
    }

}
