package com.globaldelight.boom.ui.widgets;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.globaldelight.boom.R;

/**
 * Created by mukeshkumar on 09/02/17.
 */

public class CoachMarkerWindow {

    public static final int DRAW_TOP_RIGHT = 1;
    public static final int DRAW_BOTTOM_RIGHT = 2;
    public static final int DRAW_TOP_LEFT = 3;
    public static final int DRAW_BOTTOM_LEFT = 4 ;
    public static final int DRAW_TOP_CENTER = 5;
    public static final int DRAW_BOTTOM_CENTER = 6;
    public static final int DRAW_NORMAL_LEFT = 7;
    public static final int DRAW_NORMAL_RIGHT = 8;
    public static final int DRAW_NORMAL_BOTTOM = 9;

    private View contentView;
    private RegularTextView mInfoText;
    private ImageView mImageArrow;
    private Context ctx;
    private PopupWindow tipWindow;
    private View anchor;

    private int position = 1;

    public CoachMarkerWindow(Context ctx, int position, String text) {
        this.ctx = ctx;
        this.position = position;
        tipWindow = new PopupWindow(ctx);

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int layout = 0;

        switch (position) {
            case DRAW_TOP_CENTER:
            case DRAW_TOP_LEFT:
            case DRAW_TOP_RIGHT:
            case DRAW_NORMAL_LEFT:
            case DRAW_NORMAL_RIGHT:
                layout = R.layout.tooltip_top_layout;
                break;
            case DRAW_BOTTOM_CENTER:
            case DRAW_BOTTOM_LEFT:
            case DRAW_BOTTOM_RIGHT:
                layout = R.layout.tooltip_bottom_layout;
                break;
            case DRAW_NORMAL_BOTTOM:
                layout = R.layout.tooltip_layout;
                break;
        }
        contentView = inflater.inflate(layout, null);
        mInfoText = (RegularTextView) contentView.findViewById(R.id.tooltip_text);
        mImageArrow = (ImageView) contentView.findViewById(R.id.tooltip_nav_up);
        mInfoText.setText(text);
    }

    private void showToolTip(final View anchor) {
        this.anchor = anchor;
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, ctx.getResources().getDisplayMetrics());
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(height, height);
        switch (position) {
            case DRAW_BOTTOM_RIGHT:
            case DRAW_TOP_RIGHT:
                layoutParams.gravity = Gravity.RIGHT;
                layoutParams.setMargins(0, 0, 15, 0);
                mImageArrow.setLayoutParams(layoutParams);
                break;
            case DRAW_BOTTOM_LEFT:
            case DRAW_TOP_LEFT:
                layoutParams.gravity = Gravity.LEFT;
                layoutParams.setMargins(15, 0, 0 , 0);
                mImageArrow.setLayoutParams(layoutParams);
                break;
            case DRAW_BOTTOM_CENTER:
            case DRAW_TOP_CENTER:
                layoutParams.gravity = Gravity.CENTER;
                layoutParams.setMargins(0, 0, 0 , 0);
                mImageArrow.setLayoutParams(layoutParams);
                break;
            /*case DRAW_NORMAL_BOTTOM:
                layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                layoutParams.setMargins(0, 0, 0 , 0);
                mInfoText.setLayoutParams(layoutParams);
                break;*/
            case DRAW_NORMAL_LEFT:
            case DRAW_NORMAL_RIGHT:
                mImageArrow.setVisibility(View.GONE);
                break;
        }
        tipWindow.setHeight(ActionBar.LayoutParams.WRAP_CONTENT);
        tipWindow.setWidth(ActionBar.LayoutParams.WRAP_CONTENT);
        tipWindow.setFocusable(false);
        tipWindow.setBackgroundDrawable(new BitmapDrawable());

        tipWindow.setContentView(contentView);

        int screen_pos[] = new int[2];
        anchor.getLocationOnScreen(screen_pos);

        final Rect anchor_rect = new Rect(screen_pos[0], screen_pos[1], screen_pos[0]
                + anchor.getWidth(), screen_pos[1] + anchor.getHeight());

        contentView.measure(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT);

        final int[] contentViewHeight = {contentView.getMeasuredHeight()};
        final int[] contentViewWidth = {contentView.getMeasuredWidth()};

        final int[] position_x = {0};
        final int[] position_y = { 0 };
        switch (position) {
            case DRAW_TOP_RIGHT:
                contentView.post(new Runnable() {
                    @Override
                    public void run() {
                        contentViewHeight[0] = contentView.getHeight();
                        contentViewWidth[0] = contentView.getWidth();
                        tipWindow.dismiss();
                        position_x[0] = anchor_rect.left - contentViewWidth[0] + anchor_rect.width() /2 + 40;
                        position_y[0] = (int) (anchor_rect.top - contentViewHeight[0] + getArrowHeight() * 0.5);
                        tipWindow.showAtLocation(anchor,Gravity.NO_GRAVITY,position_x[0],position_y[0]);
                    }
                });
                break;
            case DRAW_BOTTOM_RIGHT:
                contentView.post(new Runnable() {
                    @Override
                    public void run() {
                        contentViewHeight[0] = contentView.getHeight();
                        contentViewWidth[0] = contentView.getWidth();
                        tipWindow.dismiss();
                        position_x[0] = anchor_rect.left - contentViewWidth[0] + anchor_rect.width() /2 + 40;
                        position_y[0] = (int) (anchor_rect.bottom - getArrowHeight() * 0.5);
                        tipWindow.showAtLocation(anchor,Gravity.NO_GRAVITY,position_x[0],position_y[0]);
                    }
                });
                break;
            case DRAW_TOP_LEFT:
                contentView.post(new Runnable() {
                    @Override
                    public void run() {
                        contentViewHeight[0] = contentView.getHeight();
                        contentViewWidth[0] = contentView.getWidth();
                        tipWindow.dismiss();
                        position_x[0] = anchor_rect.right  - anchor_rect.width() /2 - 10;
                        position_y[0] = (int) (anchor_rect.top - contentViewHeight[0] + getArrowHeight() * 0.5);
                        tipWindow.showAtLocation(anchor,Gravity.NO_GRAVITY,position_x[0],position_y[0]);
                    }
                });
                break;
            case DRAW_BOTTOM_LEFT:
                contentView.post(new Runnable() {
                    @Override
                    public void run() {
                        contentViewHeight[0] = contentView.getHeight();
                        contentViewWidth[0] = contentView.getWidth();
                        tipWindow.dismiss();
                        position_x[0] = anchor_rect.right - anchor_rect.width() /2 - 40;
                        position_y[0] = (int) (anchor_rect.bottom - getArrowHeight() * 0.5);
                        tipWindow.showAtLocation(anchor,Gravity.NO_GRAVITY,position_x[0],position_y[0]);
                    }
                });
                break;
            case DRAW_BOTTOM_CENTER:
                contentView.post(new Runnable() {
                    @Override
                    public void run() {
                        contentViewHeight[0] = contentView.getHeight();
                        contentViewWidth[0] = contentView.getWidth();
                        tipWindow.dismiss();
                        position_x[0] = anchor_rect.centerX() - contentViewWidth[0] / 2;
                        position_y[0] = (int) (anchor_rect.bottom - getArrowHeight() * 0.5);
                        tipWindow.showAtLocation(anchor,Gravity.NO_GRAVITY,position_x[0],position_y[0]);
                    }
                });
                break;
            case DRAW_TOP_CENTER:
                contentView.post(new Runnable() {
                    @Override
                    public void run() {
                        contentViewHeight[0] = contentView.getHeight();
                        contentViewWidth[0] = contentView.getWidth();
                        tipWindow.dismiss();
                        position_x[0] = anchor_rect.centerX()  - contentViewWidth[0] / 2;
                        position_y[0] = (int) (anchor_rect.top - contentViewHeight[0] + getArrowHeight() * 0.5);
                        tipWindow.showAtLocation(anchor,Gravity.NO_GRAVITY,position_x[0],position_y[0]);
                    }
                });
                break;
            case DRAW_NORMAL_LEFT:
                contentView.post(new Runnable() {
                    @Override
                    public void run() {
                        contentViewHeight[0] = contentView.getHeight();
                        contentViewWidth[0] = contentView.getWidth();
                        tipWindow.dismiss();
                        position_x[0] = anchor_rect.left - contentViewWidth[0] - 10;
                        position_y[0] = (int) (anchor_rect.centerY() - contentViewHeight[0] * 0.5);
                        tipWindow.showAtLocation(anchor,Gravity.NO_GRAVITY,position_x[0],position_y[0]);
                    }
                });
                break;
            case DRAW_NORMAL_RIGHT:
                contentView.post(new Runnable() {
                    @Override
                    public void run() {
                        contentViewHeight[0] = contentView.getHeight();
                        contentViewWidth[0] = contentView.getWidth();
                        tipWindow.dismiss();
                        position_x[0] = anchor_rect.right + 10;
                        position_y[0] = (int) (anchor_rect.centerY() - contentViewHeight[0] * 0.5);
                        tipWindow.showAtLocation(anchor,Gravity.NO_GRAVITY,position_x[0],position_y[0]);
                    }
                });
            case DRAW_NORMAL_BOTTOM:
                contentView.post(new Runnable() {
                    @Override
                    public void run() {
                        contentViewHeight[0] = contentView.getHeight();
                        contentViewWidth[0] = contentView.getWidth();
                        tipWindow.dismiss();
                        position_x[0] = anchor_rect.centerX()  - contentViewWidth[0] / 2;
                        position_y[0] = (int) (anchor_rect.top - getArrowHeight() * 0.5);
                        tipWindow.showAtLocation(anchor,Gravity.NO_GRAVITY, position_x[0],position_y[0]);
                    }
                });
                break;
        }
        position_x[0] = 0;
        position_y[0] = 0;
        tipWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, position_x[0], position_y[0]);
    }

    private int getArrowHeight() {
        float dp = 25;
        float val = dp * ctx.getResources().getDisplayMetrics().density;
        return (int) val;
    }

    public void setAutoDismissBahaviour(boolean autodismiss) {
        if (autodismiss) {
            tipWindow.setOutsideTouchable(true);
            tipWindow.setTouchable(false);
        } else {
            tipWindow.setOutsideTouchable(false);
            tipWindow.setTouchable(true);
        }
    }

    public void dismissTooltip() {
        if (tipWindow != null && tipWindow.isShowing())
            tipWindow.dismiss();
    }


    public void showCoachMark(final View anchor) {
        anchor.post(new Runnable() {
            @Override
            public void run() {
                showToolTip(anchor);
            }
        });
    }

    public void setVisibility(int visibility) {
        if (visibility == View.GONE || visibility == View.INVISIBLE)
            dismissTooltip();
        else if (visibility == View.VISIBLE)
            showCoachMark(anchor);
    }

    public void updateText(String updatedString) {
        mInfoText.setText(updatedString);
    }
}
