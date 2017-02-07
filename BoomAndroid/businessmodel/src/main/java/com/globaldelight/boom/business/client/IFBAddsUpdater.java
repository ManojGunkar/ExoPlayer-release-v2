package com.globaldelight.boom.business.client;

import android.widget.LinearLayout;

import com.globaldelight.boom.business.BusinessUtils;

/**
 * Created by Rahul Agarwal on 02-02-17.
 */

public interface IFBAddsUpdater {
    void onLoadFBNativeAdds(BusinessUtils.AddSource addSources, boolean libraryBannerEnable, LinearLayout fbNativeAddContainer);
}
