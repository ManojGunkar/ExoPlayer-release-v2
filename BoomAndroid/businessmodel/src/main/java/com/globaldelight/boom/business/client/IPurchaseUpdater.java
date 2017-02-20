package com.globaldelight.boom.business.client;

/**
 * Created by Rahul Agarwal on 02-02-17.
 */

public interface IPurchaseUpdater {

    void onErrorAppPurchase();

    void onSuccessAppPurchase();

    void onSuccessRestoreAppPurchase();
}
