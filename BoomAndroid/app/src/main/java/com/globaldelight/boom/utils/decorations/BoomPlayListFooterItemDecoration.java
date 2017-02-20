package com.globaldelight.boom.utils.decorations;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.globaldelight.boom.ui.musiclist.adapter.BoomPlayListAdapter;

/**
 * Created by Rahul Agarwal on 14-12-16.
 */

public class BoomPlayListFooterItemDecoration extends RecyclerView.ItemDecoration {
    private int space;
    private BoomPlayListAdapter adapter;

    public BoomPlayListFooterItemDecoration(int space, BoomPlayListAdapter adapter) {
        this.space = space;
        this.adapter = adapter;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        int pos = parent.getChildLayoutPosition(view);
        if (adapter.whatView(pos) == adapter.ITEM_VIEW_TYPE_ITEM_LIST) {
            if (pos % 2 == 0 && pos != 0) {
                outRect.left = space;
            }
            outRect.bottom = space;
        }
    }
}