package com.player.utils.async;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
//

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

public class Base {
    private static Base.LogRelay mLogRelay;

    public Base() {
    }

    public static void setLogRelay(Base.LogRelay relay) {
        mLogRelay = relay;
    }

    protected void LOG(@NonNull String message, @Nullable Object... args) {
        LOG(this.getClass(), message, args);
    }

    protected static void LOG(@NonNull Class<?> context, @NonNull String message, @Nullable Object... args) {
        if(args != null) {
            message = String.format(message, args);
        }

        Log.d(context.getSimpleName(), message);
        if(mLogRelay != null) {
            mLogRelay.onRelay(Html.fromHtml(String.format("<b>%s</b>: %s", new Object[]{context.getSimpleName(), message})));
        }

    }

    public interface LogRelay {
        void onRelay(Spanned var1);
    }
}

