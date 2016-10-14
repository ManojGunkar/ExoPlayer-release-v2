package com.player.boom.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.view.View;

/**
 * Created by Rahul Agarwal on 05-10-16.
 */

public class Surround3DLinesView extends View {
    private int color;
    public Surround3DLinesView(Context context, int color) {
        super(context);
        this.color = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        PathEffect effects;
        effects = new DashPathEffect(new float[] { 4, 8, 4, 8 }, 2);
        Paint paintArc = new Paint();
        final RectF rect = new RectF();
        int mRadius = getWidth()/3;
        rect.set(getWidth()/2- mRadius, getHeight()/2 - mRadius, getWidth()/2 + mRadius, getHeight()/2 + mRadius);
        paintArc.setColor(Color.LTGRAY);
        paintArc.setStrokeWidth(2);
        paintArc.setAntiAlias(true);
        paintArc.setStrokeCap(Paint.Cap.SQUARE);
        paintArc.setStyle(Paint.Style.STROKE);
        paintArc.setPathEffect(effects);

        /*canvas.drawLine(getWidth()/2,
                getHeight()/2, getWidth()/2+mRadius/4+mRadius/2, getHeight()/2,paint3); //0

        canvas.drawLine(getWidth()/2,
                getHeight()/2, getWidth()/2+mRadius/2, getHeight()/2-mRadius/2,paint3); //60

        canvas.drawLine(getWidth()/2,
                getHeight()/2, getWidth()/2-mRadius/2, getHeight()/2-mRadius/2,paint3); //120

        canvas.drawLine(getWidth()/2,
                getHeight()/2, getWidth()/2-mRadius/4-mRadius/2, getHeight()/2,paint3);// 180


        canvas.drawLine(getWidth()/2,
                getHeight()/2, getWidth()/2-mRadius/2, getHeight()/2+mRadius/3,paint3); //212



        canvas.drawLine(getWidth()/2,
                getHeight()/2, getWidth()/2, getHeight()/2+mRadius/2,paint3); //270

        canvas.drawLine(getWidth()/2,
                getHeight()/2, getWidth()/2+mRadius/2+mRadius/4, getHeight()/2+mRadius/2,paint3); // 328*/


        canvas.drawArc(rect, 0, 0, true, paintArc);
        canvas.drawArc(rect, 0, -60, true, paintArc);
        canvas.drawArc(rect, 0, -120, true, paintArc);
        canvas.drawArc(rect, 0, -180, true, paintArc);
        canvas.drawArc(rect, 0, -212, true, paintArc);
        canvas.drawArc(rect, 0, -270, true, paintArc);
        canvas.drawArc(rect, 0, -328, true, paintArc);
        canvas.drawArc(rect, 0, -360, true, paintArc);

        Paint paintCircle = new Paint();
        final RectF rectCircle = new RectF();
        rectCircle.set(getWidth()/2- mRadius, getHeight()/2 - mRadius, getWidth()/2 + mRadius, getHeight()/2 + mRadius);
        paintCircle.setColor(color);
        paintCircle.setStrokeWidth(10);
        paintCircle.setAntiAlias(true);
        paintArc.setStrokeCap(Paint.Cap.ROUND);
        paintCircle.setStyle(Paint.Style.STROKE);

        canvas.drawCircle(getWidth()/2, getHeight()/2, mRadius, paintCircle);

    }
}
