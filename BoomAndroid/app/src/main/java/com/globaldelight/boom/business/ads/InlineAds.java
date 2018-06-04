package com.globaldelight.boom.business.ads;

import android.support.v7.widget.RecyclerView;

/**
 * Created by adarsh on 02/03/18.
 */

public interface InlineAds {

    RecyclerView.Adapter getAdapter();
    void register();
    void unregister();
}
