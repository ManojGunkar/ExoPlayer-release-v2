package com.globaldelight.boom.business;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by adarsh on 01/06/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public interface EffectsScreenPolicy {
    void init(Activity activity, ViewGroup rootView);
    void finish();
}
