package com.globaldelight.boom.view;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.utils.Utils;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

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
    private TextView mInfoText;
    private ImageView mImageArrow;
    private Context ctx;
    private PopupWindow tipWindow;
    private View anchor;

    private int position = 1;

    public interface OnDismissListener {
        void onDismiss();
    }

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
        mInfoText = contentView.findViewById(R.id.tooltip_text);
        mImageArrow = contentView.findViewById(R.id.tooltip_nav_up);
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

        int screen_pos[] = new int[2];
        anchor.getLocationOnScreen(screen_pos);

        final Rect anchor_rect = new Rect(screen_pos[0], screen_pos[1], screen_pos[0]
                + anchor.getMeasuredWidth(), screen_pos[1] + anchor.getMeasuredHeight());

        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        mInfoText.setMaxWidth(screenWidth * 8 / 10);
        contentView.measure(WRAP_CONTENT, WRAP_CONTENT);
        int contentViewHeight = contentView.getMeasuredHeight();
        int contentViewWidth = contentView.getMeasuredWidth();
        int position_x = 0;
        int position_y = 0;

        switch (position) {
            case DRAW_TOP_RIGHT:
                position_x = anchor_rect.left - contentViewWidth + anchor_rect.width() /2 + 40;
                position_y = (int) (anchor_rect.top - contentViewHeight + getArrowHeight() * 0.5);
                break;
            case DRAW_BOTTOM_RIGHT:
                position_x = anchor_rect.right - contentViewWidth;
                position_y = (int) (anchor_rect.bottom - getArrowHeight() * 0.5);
                break;
            case DRAW_TOP_LEFT:
                position_x = anchor_rect.right  - anchor_rect.width() /2 - 10;
                position_y = (int) (anchor_rect.top - contentViewHeight + getArrowHeight());
                break;
            case DRAW_BOTTOM_LEFT:
                position_x = anchor_rect.right - anchor_rect.width() /2 - Utils.dpToPx(ctx, 20);
                position_y = anchor_rect.bottom - (getArrowHeight() / 2);
                break;
            case DRAW_BOTTOM_CENTER:
                position_x = anchor_rect.centerX() - contentViewWidth / 2;
                position_y = anchor_rect.bottom - getArrowHeight() / 2;
                break;
            case DRAW_TOP_CENTER:
                position_x = anchor_rect.centerX()  - contentViewWidth / 2;
                position_y = (int) (anchor_rect.top - contentViewHeight - getArrowHeight() / 2);
                break;
            case DRAW_NORMAL_LEFT:
                position_x = anchor_rect.left - contentViewWidth - 10;
                position_y = (int) (anchor_rect.centerY() - contentViewHeight * 0.5);
                break;
            case DRAW_NORMAL_RIGHT:
                position_x = anchor_rect.right + 10;
                position_y = (int) (anchor_rect.centerY() - contentViewHeight * 0.5);
                break;

            case DRAW_NORMAL_BOTTOM:
                position_x = anchor_rect.centerX()  - contentViewWidth / 2;
                position_y = (int) anchor_rect.top;
                break;
        }

        // Fix arrow position
        if ( position == DRAW_BOTTOM_CENTER || position == DRAW_TOP_CENTER || position == DRAW_BOTTOM_LEFT ) {
            // Clips at left side
            if ( position_x < 0 ) {
                layoutParams.setMargins(0, 0, -position_x , 0);
                mImageArrow.setLayoutParams(layoutParams);
                position_x = 0;
            }
            // Clips at right side
            else if ( (position_x + contentViewWidth) > screenWidth) {
                int diff =  (position_x + contentViewWidth) - screenWidth;
                position_x = position_x - diff;
                layoutParams.setMargins(diff, 0, 0 , 0);
                mImageArrow.setLayoutParams(layoutParams);
            }
        }

        tipWindow.setHeight(WRAP_CONTENT);
        tipWindow.setWidth(contentViewWidth);
        tipWindow.setFocusable(false);
        tipWindow.setBackgroundDrawable(new BitmapDrawable());
        tipWindow.setContentView(contentView);
        tipWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, position_x, position_y);
    }


    private void fixArrowPosition() {

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

    public void setOnDismissListener(final OnDismissListener listener) {
        tipWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                listener.onDismiss();
            }
        });
    }
}
