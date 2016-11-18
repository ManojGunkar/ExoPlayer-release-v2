package com.globaldelight.boom.utils.async;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.util.ArrayList;
import java.util.Iterator;

public final class Async extends Base {
    private static Async mInstance;
    private static final Object LOCK = new Object();
    private ArrayList<Pool> mPools = new ArrayList();

    private Async() {
    }

    @UiThread
    protected static Async instance() {
        if(mInstance == null) {
            mInstance = new Async();
        }

        return mInstance;
    }

    @UiThread
    public static Pool parallel(@NonNull Action... actions) {
        Object var1 = LOCK;
        synchronized(LOCK) {
            Pool pool = (new Pool(actions, 1)).execute();
            instance().mPools.add(pool);
            return pool;
        }
    }

    @UiThread
    public static Pool series(@NonNull Action... actions) {
        Object var1 = LOCK;
        synchronized(LOCK) {
            Pool pool = (new Pool(actions, 2)).execute();
            instance().mPools.add(pool);
            return pool;
        }
    }

    public static void cancelAll() {
        Object var0 = LOCK;
        synchronized(LOCK) {
            Iterator i$ = instance().mPools.iterator();

            while(i$.hasNext()) {
                Pool p = (Pool)i$.next();
                p.cancel();
            }

        }
    }

    protected static void pop(Pool pool) {
        LOG(Async.class, "Popping pool...", new Object[0]);
        Object var1 = LOCK;
        synchronized(LOCK) {
            instance().mPools.remove(pool);
        }
    }
}
