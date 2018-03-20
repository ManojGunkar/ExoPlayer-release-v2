package com.globaldelight.boom.business;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by adarsh on 13/12/17.
 */

@IntDef({
        ErrorCode.SUCCESS,
        ErrorCode.FAILED,
        ErrorCode.INVALID_CODE,
        ErrorCode.NETWORK_ERROR,
        ErrorCode.NO_LICENSE,
        ErrorCode.LIMIT_EXCEEDED,

})
@Retention(RetentionPolicy.SOURCE)

public @interface ErrorCode {
    int     SUCCESS = 0,
            FAILED = -1,
            INVALID_CODE = -2,
            NETWORK_ERROR = -3,
            NO_LICENSE = -4,
            LIMIT_EXCEEDED = -5;
}

