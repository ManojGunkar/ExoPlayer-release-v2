package com.globaldelight.boom.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

import com.globaldelight.boom.R;

/**
 * Created by Rahul Agarwal on 05-10-16.
 */

public class NegativeSeekBar extends AppCompatSeekBar {
    private final int seekbar_height = 7;

    public NegativeSeekBar(Context context) {
        super(context);
    }


    public NegativeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public NegativeSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        // Draw track
        Context context = getContext();
        Rect rect = new Rect();
        Paint paint = new Paint();

        int height = getHeight();

        rect.set(0 + getThumbOffset()*2 ,
                (height / 2) - (seekbar_height/2),
                getWidth()- getThumbOffset()*2,
                 (height / 2) + (seekbar_height/2));

        paint.setColor(ContextCompat.getColor(context, R.color.effect_intensity_seek_background));
        canvas.drawRect(rect, paint);

        // Draw progress
        int availableWidth = getWidth() - getThumbOffset() * 4;
        int midPtX =  availableWidth / 2;
        int progressPtX = availableWidth * getProgress() / getMax();
        
        rect.set( Math.min(midPtX, progressPtX),
                (height / 2) - (seekbar_height / 2),
                Math.max(midPtX, progressPtX),
                height / 2 + (seekbar_height / 2));
        rect.offset(getThumbOffset()*2, 0);

        ColorStateList csl = ContextCompat.getColorStateList(context, R.color.intensity_thumb);
        int color = csl.getColorForState(getDrawableState(), 0);
        paint.setColor(color);

        canvas.drawRect(rect, paint);

        super.onDraw(canvas);
    }
}