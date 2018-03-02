package com.globaldelight.boom.business;

/**
 * Created by adarsh on 13/12/17.
 */
public class Result<T> {
    private @ErrorCode
    int mStatus;
    private T mObject;

    public Result(@ErrorCode int status) {
        mStatus = status;
        mObject = null;
    }

    public Result(T result) {
        mStatus = 0;
        mObject = result;
    }

    public boolean isSuccess() {
        return mStatus == ErrorCode.SUCCESS;
    }

    public @ErrorCode
    int getStatus() {
        return mStatus;
    }

    public T getObject() {
        return mObject;
    }
}
