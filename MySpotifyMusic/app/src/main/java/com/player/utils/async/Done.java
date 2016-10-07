package com.player.utils.async;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

public interface Done {
    @UiThread
    void result(@NonNull Result var1);
}
