package com.globaldelight.boom.business.client;

import android.widget.LinearLayout;

import com.globaldelight.boom.business.BusinessUtils;
import com.google.android.gms.ads.NativeExpressAdView;

/**
 * Created by Rahul Agarwal on 02-02-17.
 */

public interface IGoogleAddsUpdater {
    void onLoadGoogleNativeAdds(BusinessUtils.AddSource addSources, boolean libraryBannerEnable, NativeExpressAdView googleAddView);
}
