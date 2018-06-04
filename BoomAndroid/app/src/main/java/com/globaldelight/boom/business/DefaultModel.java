package com.globaldelight.boom.business;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

/**
 * Created by adarsh on 07/12/17.
 */

public class DefaultModel implements BusinessModel {

    class DefaultUIHandler implements UIHandler {

        @Override
        public void handleEffectsScreen(ViewGroup root) {
        }

        @Override
        public RecyclerView.Adapter handleEqualizerPresets(RecyclerView.Adapter adapter) {
            return adapter;
        }
    }

    private DefaultUIHandler mUIHandler = new DefaultUIHandler();

    public UIHandler getUIHandler() {
        return mUIHandler;
    }
}
