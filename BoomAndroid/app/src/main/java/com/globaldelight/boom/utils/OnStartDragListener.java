package com.globaldelight.boom.utils;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */

public interface OnStartDragListener {
    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    void onStartDrag(RecyclerView.ViewHolder viewHolder);
}