package com.player.boom.ui.widgets;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

/**
 * Created by Rahul Agarwal on 07-10-16.
 */

public class ScrollEnableLayoutManager extends LinearLayoutManager {
    private boolean isScrollEnabled = true;

    public ScrollEnableLayoutManager(Context context) {
        super(context);
    }

    public ScrollEnableLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public void setScrollEnabled(boolean flag) {
        this.isScrollEnabled = flag;
    }

    @Override
    public boolean canScrollVertically() {
        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
        return isScrollEnabled && super.canScrollVertically();
    }
}
