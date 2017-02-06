package com.globaldelight.boom.business.client;

/**
 * Created by Rahul Agarwal on 02-02-17.
 */

public interface IBusinessNetworkInit {

    void onGetAccessToken(boolean success);

    void onRegisterDevice(boolean success);

    void onGetBusinessConfiguration(boolean success);

    void onAppTrailExpired(boolean expired);

    void onEmailSubmition(boolean success);
}
