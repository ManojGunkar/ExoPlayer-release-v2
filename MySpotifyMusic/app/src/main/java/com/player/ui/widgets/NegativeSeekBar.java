package com.player.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.SeekBar;

/**
 * Created by Rahul Agarwal on 05-10-16.
 */

public class NegativeSeekBar extends SeekBar {
    private Rect rect;
    private Paint paint ;
    private int seekbar_height;
    private int plus_r=153, plus_g=204, plus_b=255;
    private int min_r=255, min_g=153, min_b=255;

    public NegativeSeekBar(Context context) {
        super(context);

    }

    public NegativeSeekBar(Context context, AttributeSet attrs) {

        super(context, attrs);
        rect = new Rect();
        paint = new Paint();
        seekbar_height = 7;
    }

    public NegativeSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {

        rect.set(0 + getThumbOffset()*2,
                (getHeight() / 2) - (seekbar_height/2),
                getWidth()- getThumbOffset()*2,
                (getHeight() / 2) + (seekbar_height/2));

        paint.setColor(Color.parseColor("#E0E4D4"));

        canvas.drawRect(rect, paint);



        if (this.getProgress() > 50) {


            rect.set(getWidth() / 2,
                    (getHeight() / 2) - (seekbar_height/2),
                    (getWidth()) / 2 + (getWidth() / 100) * (getProgress() - 50),
                    getHeight() / 2 + (seekbar_height/2));


            if(this.getProgress() > 50 && this.getProgress() <= 57){
                plus_r = 255;
                plus_g = 204;
                plus_b = 255;
            }else if(this.getProgress() > 57 && this.getProgress() <= 64){
                plus_r = 255;
                plus_g = 153;
                plus_b = 255;
            }else if(this.getProgress() > 64 && this.getProgress() <= 71){
                plus_r = 255;
                plus_g = 102;
                plus_b = 255;
            }else if(this.getProgress() > 71 && this.getProgress() <= 78){
                plus_r = 255;
                plus_g = 51;
                plus_b = 255;
            }else if(this.getProgress() > 78 && this.getProgress() <= 85){
                plus_r = 255;
                plus_g = 0;
                plus_b = 255;
            }else if(this.getProgress() > 85 && this.getProgress() <= 92){
                plus_r = 204;
                plus_g = 0;
                plus_b = 204;
            }else if(this.getProgress() > 92){
                plus_r = 153;
                plus_g = 0;
                plus_b = 153;
            }
            paint.setColor(Color.rgb(plus_r,plus_g, plus_b));

            this.getThumb().setColorFilter(Color.rgb(plus_r,plus_g, plus_b), PorterDuff.Mode.SRC_IN);

            canvas.drawRect(rect, paint);
        }

        if (this.getProgress() < 50) {

            rect.set(getWidth() / 2 - ((getWidth() / 100) * (50 - getProgress())),
                    (getHeight() / 2) - (seekbar_height/2),
                    getWidth() / 2,
                    getHeight() / 2 + (seekbar_height/2));

            if(this.getProgress() < 50 && this.getProgress() >= 43){
                plus_r = 204;
                plus_g = 229;
                plus_b = 255;
            }else if(this.getProgress() < 43 && this.getProgress() >= 36){
                plus_r = 153;
                plus_g = 204;
                plus_b = 255;
            }else if(this.getProgress() < 36 && this.getProgress() >= 29){
                plus_r = 102;
                plus_g = 178;
                plus_b = 255;
            }else if(this.getProgress() < 29 && this.getProgress() >= 22){
                plus_r = 51;
                plus_g = 153;
                plus_b = 255;
            }else if(this.getProgress() < 22 && this.getProgress() >= 15){
                plus_r = 0;
                plus_g = 128;
                plus_b = 255;
            }else if(this.getProgress() < 15 && this.getProgress() >= 8){
                plus_r = 0;
                plus_g = 102;
                plus_b = 204;
            }else if(this.getProgress() < 8){
                plus_r = 0;
                plus_g = 76;
                plus_b = 153;
            }
            paint.setColor(Color.rgb(plus_r,plus_g, plus_b));
            this.getThumb().setColorFilter(Color.rgb(plus_r,plus_g, plus_b), PorterDuff.Mode.SRC_IN);
            canvas.drawRect(rect, paint);

        }

        super.onDraw(canvas);
    }
}