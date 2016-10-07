package com.player.utils.async;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.os.Handler;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

public abstract class Action<RT> extends Base {
    private final Object LOCK = new Object();
    private Handler mHandler;
    private Pool mPool;
    private int mPoolIndex;
    private boolean mExecuting;
    private boolean mCancelled;
    private Thread mThread;
    private boolean mDone;
    private RT mResult;

    public Action() {
    }

    @IntRange(
            from = 0L,
            to = 2147483647L
    )
    public final int getPoolIndex() {
        return this.mPoolIndex;
    }

    @NonNull
    public abstract String id();

    @WorkerThread
    @Nullable
    protected abstract RT run() throws InterruptedException;

    @UiThread
    protected void done(@Nullable RT result) {
    }

    @UiThread
    public final void execute() {
        Object var1 = this.LOCK;
        synchronized(this.LOCK) {
            if(this.mExecuting) {
                throw new IllegalStateException("This action has already been executed.");
            } else {
                this.mExecuting = true;
                this.mDone = false;
                this.mCancelled = false;
                if(this.mPool == null && this.mHandler == null) {
                    LOG(Action.class, "Pool is null, creating action-level handler.", new Object[0]);
                    this.mHandler = new Handler();
                }

                LOG(Action.class, "Executing action %d (%s)...", new Object[]{Integer.valueOf(this.getPoolIndex()), this.id()});
                this.mThread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            Action.this.mResult = Action.this.run();
                        } catch (InterruptedException var2) {
                            Base.LOG(Action.class, "Action %d (%s) was cancelled.", new Object[]{Integer.valueOf(Action.this.getPoolIndex()), Action.this.id()});
                            Action.this.mCancelled = true;
                        }

                        Action.this.mExecuting = false;
                        if(Action.this.isCancelled()) {
                            Base.LOG(Action.class, "Action %d (%s) was cancelled.", new Object[]{Integer.valueOf(Action.this.getPoolIndex()), Action.this.id()});
                        } else {
                            Action.this.post(new Runnable() {
                                public void run() {
                                    Base.LOG(Action.class, "Action %d (%s) finished executing!", new Object[]{Integer.valueOf(Action.this.getPoolIndex()), Action.this.id()});
                                    Action.this.mDone = true;
                                    Action.this.done(Action.this.mResult);
                                    Action.this.mThread = null;
                                    if(Action.this.mPool != null) {
                                        Action.this.mPool.pop(Action.this);
                                    }

                                }
                            });
                        }
                    }
                });
                this.mThread.start();
            }
        }
    }

    public final void waitForExecution() {
        if(!this.isExecuting()) {
            throw new IllegalStateException(String.format("Action %d (%s) is not currently executing.", new Object[]{Integer.valueOf(this.getPoolIndex()), this.id()}));
        } else {
            while(this.isExecuting() && !this.isCancelled()) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException var2) {
                    break;
                }
            }

        }
    }

    @WorkerThread
    protected final void post(Runnable runnable) {
        Object var2 = this.LOCK;
        synchronized(this.LOCK) {
            if(this.mPool == null) {
                this.mHandler.post(runnable);
            } else {
                this.mPool.post(runnable);
            }

        }
    }

    public final void cancel() {
        Object var1 = this.LOCK;
        synchronized(this.LOCK) {
            this.mCancelled = true;
            if(this.mThread != null) {
                this.mThread.interrupt();
            }

            this.mThread = null;
        }
    }

    public final boolean isExecuting() {
        Object var1 = this.LOCK;
        synchronized(this.LOCK) {
            return this.mExecuting;
        }
    }

    public final boolean isCancelled() {
        Object var1 = this.LOCK;
        synchronized(this.LOCK) {
            return this.mCancelled;
        }
    }

    public final boolean isDone() {
        return this.mDone;
    }

    public RT getResult() {
        return this.mResult;
    }

    protected final void setPool(@Nullable Pool pool, @IntRange(
            from = -1L,
            to = 2147483647L
    ) int poolIndex) {
        Object var3 = this.LOCK;
        synchronized(this.LOCK) {
            this.mPoolIndex = poolIndex;
            this.mCancelled = false;
            this.mExecuting = false;
            if(pool == null) {
                this.mPool = null;
            } else if(this.mPool != null) {
                throw new IllegalStateException(String.format("Action %s is already in use by another Pool.", new Object[]{this.id()}));
            } else {
                this.mPool = pool;
            }
        }
    }

    public String toString() {
        return String.format("%s: %s", new Object[]{this.id(), this.mResult});
    }
}

