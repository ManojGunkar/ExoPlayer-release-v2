package com.globaldelight.boom.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import com.globaldelight.boom.R;

/**
 * Created by adarsh on 28/09/17.
 */

public class CustomCheckbox extends AppCompatCheckBox {
    public static final String ANDROID_SCHEMA = "http://schemas.android.com/apk/res/android";

    public CustomCheckbox(Context context, AttributeSet attrs) {
        super(context, attrs);

        applyCustomFont(context, attrs);
    }

    public CustomCheckbox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

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
                return ResourcesCompat.getFont(context, R.font.titilliumweb_bold);

            case Typeface.ITALIC: // italic
                return ResourcesCompat.getFont(context, R.font.titilliumweb_italic);

            case Typeface.BOLD_ITALIC: // bold italic
                return ResourcesCompat.getFont(context, R.font.titilliumweb_bolditalic);

            case Typeface.NORMAL: // regular
            default:
                return ResourcesCompat.getFont(context, R.font.titilliumweb_regular);
        }
    }
}
