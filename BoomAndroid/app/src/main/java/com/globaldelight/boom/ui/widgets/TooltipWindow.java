package com.globaldelight.boom.ui.widgets;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.globaldelight.boom.R;
import com.globaldelight.boom.utils.handlers.Preferences;

public class TooltipWindow {

    public static final int DRAW_LEFT = 1;
    public static final int DRAW_RIGHT = 2;
    public static final int DRAW_TOP_CENTER = 3;
    public static final int DRAW_BOTTOM = 4;
    public static final int DRAW_TOP_RIGHT = 11;
    public static final int DRAW_ABOVE_WITH_CLOSE = 12;

    /* Arrow position  */
    public static final int DRAW_ARROW_TOP_RIGHT = 5;
    public static final int DRAW_ARROW_DEFAULT_CENTER = 6;
    public static final int DRAW_ARROW_BOTTOM_LEFT = 7;
    private static final int MSG_DISMISS_TOOLTIP = 100;
    View contentView;
    CoachMarkTextView mInfoText;
    LinearLayout mlayout;
    ImageView mImageArrow;
    private Context ctx;
    private PopupWindow tipWindow;
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_DISMISS_TOOLTIP:
                    if (tipWindow != null && tipWindow.isShowing())
                        tipWindow.dismiss();
                    break;
            }
        }

        ;
    };
    private LayoutInflater inflater;
    private int position = 4;


    public TooltipWindow(final Context ctx, int position, String text) {
        this.ctx = ctx;
        this.position = position;
        tipWindow = new PopupWindow(ctx);

        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int layout = 0;
        switch (position) {
            case DRAW_BOTTOM:
                layout = R.layout.tooltip_bottom_layout;
                break;
            case DRAW_TOP_RIGHT:
            case DRAW_TOP_CENTER:
                layout = R.layout.tooltip_top_layout;
                break;

            case DRAW_LEFT:
                layout = R.layout.tooltip_left_layout;
                break;
            case DRAW_RIGHT:
                layout = R.layout.tooltip_right_layout;
                break;
            case DRAW_ABOVE_WITH_CLOSE:
                layout = R.layout.tooltip_layout;
                break;
        }
        contentView = inflater.inflate(layout, null);
        mInfoText = (CoachMarkTextView) contentView.findViewById(R.id.tooltip_text);
        Typeface typeFace = Typeface.createFromAsset(ctx.getAssets(), "fonts/TitilliumWeb-Light.ttf");
        mInfoText.setTypeface(typeFace);
        mImageArrow = (ImageView) contentView.findViewById(R.id.tooltip_nav_up);
        if (position == DRAW_ABOVE_WITH_CLOSE) {
            ImageView close = (ImageView) contentView.findViewById(R.id.close_button);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissTooltip();
                    Preferences.writeBoolean(ctx, Preferences.PLAYER_SCREEN_HEADSET_ENABLE, false);

                }
            });

        }


        mInfoText.setText(text);

    }

    public void showToolTip(View anchor, int arroPosition) {
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, ctx.getResources().getDisplayMetrics());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(height, height);

        switch (arroPosition) {
            case DRAW_ARROW_TOP_RIGHT:
                layoutParams.gravity = Gravity.RIGHT;
                layoutParams.setMargins(0, 0, 10, 0);

                mImageArrow.setLayoutParams(layoutParams);
                break;
            case DRAW_ARROW_BOTTOM_LEFT:
                layoutParams.gravity = Gravity.LEFT;
                layoutParams.setMargins(15, 0, 0, 0);
                mImageArrow.setLayoutParams(layoutParams);
                break;
            case DRAW_ARROW_DEFAULT_CENTER:
                layoutParams.gravity = Gravity.CENTER;
                layoutParams.setMargins(0, 0, 0, height / 2);//minus arrow height 25 dp

                mImageArrow.setLayoutParams(layoutParams);
                break;


        }
        tipWindow.setHeight(LayoutParams.WRAP_CONTENT);
        tipWindow.setWidth(LayoutParams.WRAP_CONTENT);
        /*if (position == DRAW_ABOVE_WITH_CLOSE ) {
            tipWindow.setOutsideTouchable(false);
            tipWindow.setTouchable(true);
        } else {
            tipWindow.setOutsideTouchable(true);
            tipWindow.setTouchable(false);
        }*/

        tipWindow.setFocusable(false);
        tipWindow.setBackgroundDrawable(new BitmapDrawable());

        tipWindow.setContentView(contentView);

        int screen_pos[] = new int[2];
        anchor.getLocationOnScreen(screen_pos);

        Rect anchor_rect = new Rect(screen_pos[0], screen_pos[1], screen_pos[0]
                + anchor.getWidth(), screen_pos[1] + anchor.getHeight());

        contentView.measure(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        int contentViewHeight = contentView.getMeasuredHeight();
        int contentViewWidth = contentView.getMeasuredWidth();


        int position_x = 0, position_y = 0;
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int width = display.getWidth();
        switch (position) {
            case DRAW_BOTTOM:
                position_x = anchor_rect.centerX() - (contentViewWidth - contentViewHeight / 2);
                position_y = anchor_rect.bottom - (anchor_rect.height() / 2) - 10;
                break;
            case DRAW_TOP_CENTER:
                // deprecated
                position_x = ((width - contentViewWidth) / 2) - 5;
                // position_x = anchor_rect.centerX() - (contentViewWidth - contentViewWidth / 2)-10;
                position_y = anchor_rect.top - contentViewHeight;

                break;
            case DRAW_TOP_RIGHT:
                position_x = anchor_rect.left;
                position_y = anchor_rect.top - (contentViewHeight + getArrowHeight());

                break;
            case DRAW_LEFT:
                break;
            case DRAW_RIGHT:
                break;
            case DRAW_ABOVE_WITH_CLOSE:
                // WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
                // Display display = wm.getDefaultDisplay();
                // int width = display.getWidth();  // deprecated
                position_x = (width - contentViewWidth) / 2;
                position_y = anchor_rect.top;
                break;
        }
        tipWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, position_x,
                position_y);
    }

    public boolean isTooltipShown() {
        if (tipWindow != null && tipWindow.isShowing())
            return true;
        return false;
    }

    public void dismissTooltip() {
        if (tipWindow != null && tipWindow.isShowing())
            tipWindow.dismiss();
    }

    public int getArrowHeight() {
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


}