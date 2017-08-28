package com.globaldelight.boom.app.share;

import android.graphics.drawable.Drawable;

/**
 * Created by Manoj Kumar on 8/24/2017.
 */

public class ShareItem {
    public final String text;
    public final Drawable icon;
    public final String pkgName;
    public ShareItem(String text, String pkgName,Drawable icon) {
        this.text = text;
        this.pkgName=pkgName;
        this.icon = icon;
    }
    @Override
    public String toString() {
        return text;
    }
}
