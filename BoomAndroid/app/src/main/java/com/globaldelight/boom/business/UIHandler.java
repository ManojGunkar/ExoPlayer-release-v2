package com.globaldelight.boom.business;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by adarsh on 01/06/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public interface UIHandler {

    void handleEffectsScreen(ViewGroup root);

    RecyclerView.Adapter handleEqualizerPresets(RecyclerView.Adapter adapter);
}
