package com.globaldelight.boom.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Rahul Agarwal on 08-10-16.
 */

public class CoachMarkTextView extends TextView {

    public static final String ANDROID_SCHEMA = "http://schemas.android.com/apk/res/android";


    public CoachMarkTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

//        applyCustomFont(context, attrs);
    }

    public CoachMarkTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode())
            return;
        applyCustomFont(context, attrs);
    }

    private void applyCustomFont(Context context, AttributeSet attrs) {
        int textStyle = attrs.getAttributeIntValue(ANDROID_SCHEMA, "textStyle", Typeface.NORMAL);


        Typeface customFont = selectTypeface(context, textStyle);
        setTypeface(customFont);
    }

    private Typeface selectTypeface(Context context, int textStyle) {
        /*
        * information about the TextView textStyle:
        * http://developer.android.com/reference/android/R.styleable.html#TextView_textStyle
        */
        switch (textStyle) {
            case Typeface.BOLD: // bold
                return Typeface.createFromAsset(context.getAssets(), "fonts/TitilliumWeb-SemiBold.ttf");

            case Typeface.ITALIC: // Change the usage of this font to Light
                return Typeface.createFromAsset(context.getAssets(), "fonts/TitilliumWeb-Italic.ttf");

            case Typeface.BOLD_ITALIC: // bold italic
                return Typeface.createFromAsset(context.getAssets(), "fonts/TitilliumWeb-BoldItalic.ttf");

            case Typeface.NORMAL: // regular
            default:
                return Typeface.createFromAsset(context.getAssets(), "fonts/TitilliumWeb-Light.ttf");

        }
    }
}
