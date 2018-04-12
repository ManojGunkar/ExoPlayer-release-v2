package com.globaldelight.boom.utils;

/**
 * Created by adarsh on 13/12/17.
 */
public class Result<T> {
    private int mStatus;
    private T mObject;

    public Result(int status) {
        mStatus = status;
        mObject = null;
    }

    public Result(T result) {
        mStatus = 0;
        mObject = result;
    }

    public boolean isSuccess() {
        return mStatus == 0;
    }

    public int getStatus() {
        return mStatus;
    }

    public T getObject() {
        return mObject;
    }
}
