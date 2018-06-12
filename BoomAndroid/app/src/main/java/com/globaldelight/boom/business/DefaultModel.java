package com.globaldelight.boom.business;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by adarsh on 07/12/17.
 */

public class DefaultModel implements BusinessModel {

    class DefaultEffectsScreenPolicy implements EffectsScreenPolicy {

        @Override
        public void init(Activity activity, ViewGroup root) {

        }

        @Override
        public void finish() {

        }

    }

    private EffectsScreenPolicy mEffectsPolicy = new DefaultEffectsScreenPolicy();

    public EffectsScreenPolicy createEffectsScreenPolicy() {
        return mEffectsPolicy;
    }
}
