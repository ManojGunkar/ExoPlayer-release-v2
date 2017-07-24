package com.globaldelight.boom.business.network;


public interface OnServerResponseListener<Success, Failure> {
    public void onSuccess(Success successParam);

    public void onFailure(Failure failureParam);
}
