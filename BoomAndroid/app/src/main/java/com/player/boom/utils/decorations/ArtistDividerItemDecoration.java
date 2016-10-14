package com.player.boom.utils.decorations;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.player.boom.R;


public class ArtistDividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDivider;
    private int size;

    public ArtistDividerItemDecoration(Context context, int paddingLeft) {
        this.size = paddingLeft;
        mDivider = ContextCompat.getDrawable(context, R.drawable.old_line_divider);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft() + (size * 2);
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            if (i != 0)
                mDivider.draw(c);
        }
    }
}