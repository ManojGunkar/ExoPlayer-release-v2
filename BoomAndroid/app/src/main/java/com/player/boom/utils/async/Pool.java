package com.player.boom.utils.async;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
//

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public final class Pool extends Base {
    public static final int MODE_PARALLEL = 1;
    public static final int MODE_SERIES = 2;
    private static final Object LOCK = new Object();
    private final Handler mHandler = new Handler();
    private final int mMode;
    private final ArrayList<Action> mQueue = new ArrayList();
    private Done mDone;
    private Result mResult;
    private boolean mExecuted = false;
    private int mSize;

    @UiThread
    protected Pool(@NonNull Action[] actions, int mode) {
        Collections.addAll(this.mQueue, actions);
        this.mMode = mode;
        this.prepare();
    }

    private void prepare() {
        Object var1 = LOCK;
        synchronized(LOCK) {
            for(int i = 0; i < this.mQueue.size(); ++i) {
                ((Action)this.mQueue.get(i)).setPool(this, i);
            }

            this.mSize = this.mQueue.size();
            this.LOG("Prepared %d actions for execution...", new Object[]{Integer.valueOf(this.mQueue.size())});
        }
    }

    public boolean isExecuting() {
        Object var1 = LOCK;
        synchronized(LOCK) {
            return this.mQueue != null && !this.mQueue.isEmpty();
        }
    }

    public void waitForExecution() {
        if(!this.isExecuting()) {
            throw new IllegalStateException("Pool is not currently executing.");
        } else {
            while(this.isExecuting()) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException var2) {
                    break;
                }
            }

        }
    }

    public Result getResult() {
        return this.mResult;
    }

    @UiThread
    protected Pool execute() {
        Object var1 = LOCK;
        synchronized(LOCK) {
            if(!this.mExecuted && this.mQueue.size() != 0) {
                this.mExecuted = true;
                this.mResult = new Result();
                if(this.mMode == 2) {
                    this.LOG("Executing actions in SERIES mode...", new Object[0]);
                    ((Action)this.mQueue.get(0)).execute();
                } else {
                    if(this.mMode != 1) {
                        throw new IllegalStateException("Unknown mode: " + this.mMode);
                    }

                    this.LOG("Executing actions in PARALLEL mode...", new Object[0]);
                    Iterator i$ = this.mQueue.iterator();

                    while(i$.hasNext()) {
                        Action a = (Action)i$.next();
                        a.execute();
                    }
                }

                return this;
            } else {
                throw new IllegalStateException("This Pool has already been executed or cancelled.");
            }
        }
    }

    @UiThread
    public void cancel() {
        Object var1 = LOCK;
        synchronized(LOCK) {
            this.LOG("Cancelling all actions...", new Object[0]);
            Iterator i$ = this.mQueue.iterator();

            while(i$.hasNext()) {
                Action a = (Action)i$.next();
                a.cancel();
            }

            this.mQueue.clear();
            Async.pop(this);
        }
    }

    public Pool done(Done done) {
        this.mDone = done;
        return this;
    }

    @UiThread
    protected void pop(Action action) {
        this.LOG("Removing action %d (%s) from pool.", new Object[]{Integer.valueOf(action.getPoolIndex()), action.id()});
        action.setPool((Pool)null, -1);
        this.mQueue.remove(action);
        if(this.mResult == null) {
            this.mResult = new Result();
        }

        this.mResult.put(action);
        if(this.mQueue.isEmpty()) {
            this.LOG("All actions are done executing.", new Object[0]);
            if(this.mDone != null) {
                this.mDone.result(this.mResult);
            }

            Async.pop(this);
        } else if(this.mMode == 2) {
            Action nextAction = (Action)this.mQueue.get(0);
            this.LOG("Executing next action in the series: %d", new Object[]{Integer.valueOf(nextAction.getPoolIndex())});
            nextAction.execute();
        }

    }

    @UiThread
    public void push(@NonNull Action... actions) {
        Action[] arr$ = actions;
        int len$ = actions.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Action a = arr$[i$];
            this.push(a);
        }

    }

    @UiThread
    public void push(@NonNull Action action) {
        Object var2 = LOCK;
        synchronized(LOCK) {
            ++this.mSize;
            action.setPool(this, this.mSize - 1);
            this.mQueue.add(action);
            this.LOG("Pushing action %d (%s) into the Pool.", new Object[]{Integer.valueOf(action.getPoolIndex()), action.id()});
            if(this.mExecuted) {
                if(this.mMode == 2) {
                    if(this.mQueue.isEmpty()) {
                        action.execute();
                    }
                } else {
                    if(this.mMode != 1) {
                        throw new IllegalStateException("Unknown mode: " + this.mMode);
                    }

                    action.execute();
                }
            }

        }
    }

    @WorkerThread
    protected void post(Runnable runnable) {
        this.mHandler.post(runnable);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }
}

