package com.globaldelight.boom.business.ads;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

/**
 * Created by adarsh on 02/03/18.
 */

public interface Advertiser {

    InlineAds   createInlineAds(Activity activity, RecyclerView recyclerView, RecyclerView.Adapter baseAdapter);
}