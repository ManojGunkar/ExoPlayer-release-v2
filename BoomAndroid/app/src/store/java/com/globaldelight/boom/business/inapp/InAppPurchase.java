package com.globaldelight.boom.business.inapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.android.billingclient.api.Purchase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Created by Manoj Kumar on 7/11/2017.
 * @Manually commented All method by Manoj Kumar on 12 July 2017.
 */

public class InAppPurchase implements PurchasesUpdatedListener {

    public static final String ACTION_IAP_RESTORED = "com.globaldelight.boom.IAP_RESTORED";
    public static final String ACTION_IAP_SUCCESS = "com.globaldelight.boom.IAP_SUCCESS";
    public static final String ACTION_IAP_FAILED = "com.globaldelight.boom.IAP_FAILED";

    /**
     * @InApp purchase requirement.
     */
    public static final String SKU_SUB_6MONTH = "com.globaldelight.boom_premium_6month";
    public static final String SKU_SUB_1YEAR = "com.globaldelight.boom_premium_1year";


    private static final String TAG="InApp Purchase";

    private Context context;
    private HashMap<String, String> mPrices = new HashMap<>();
    private String mPurchasedItem;

    private static InAppPurchase instance;

    /**
     * @InApp Purchase Helper class.
     */
    private BillingClient mBillingClient;

    /**
     * @before invoke this method check internet connection, it should available.
     * this method will provide only static object.
     * @param context
     * @return references of this class
     */
    public static InAppPurchase getInstance(Context context) {
        if (instance==null)instance = new InAppPurchase(context);
        return instance;
    }

    /**
     * @secure constructor for singleton pattern.
     * @param context
     */
    private InAppPurchase(Context context) {
        this.context=context;
    }

    /**
     * With the help of this method we can initialise InApp purchase.
     * @return Method chaining technique which bind other method with same context.
     */
    public  void initInAppPurchase(){
        mBillingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                    queryProductInventory();
                    queryPurchases();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }


    private void queryProductInventory() {
        List skuList = new ArrayList<> ();
        skuList.add(SKU_SUB_6MONTH);
        skuList.add(SKU_SUB_1YEAR);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        mBillingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode, List skuDetailsList) {
                        if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                            updatePrice(skuDetailsList);
                        }
                    }
                });
    }


    private void updatePrice(List<SkuDetails> skuDetailsList) {
        for (SkuDetails skuDetails : skuDetailsList) {
            String sku = skuDetails.getSku();
            String price = skuDetails.getPrice();
            mPrices.put(sku, price);
        }
    }


    public void queryPurchases() {
        boolean hadPurchase = isPurchased();
        Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.SUBS);
        if ( purchasesResult.getResponseCode() == BillingClient.BillingResponse.OK ) {
            List<Purchase> purchases = purchasesResult.getPurchasesList();
            if ( purchases.size() > 0 ) {
                mPurchasedItem = purchases.get(0).getSku();
            }
        }

        if ( !hadPurchase && isPurchased() ) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_IAP_RESTORED));
        }

    }

    private boolean mShouldClear = false;
    public void clearInAppsPurchase() {
        mShouldClear = true;
        initInAppPurchase();
    }


    /**
     *You can purchase package of InApp purchase.
     * @param activity which handled by onActivityResult.
     */
    public void buyNow(Activity activity, String inAppItem) {
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSku(inAppItem)
                .setType(BillingClient.SkuType.SUBS) // SkuType.SUB for subscription
                .build();
        mBillingClient.launchBillingFlow(activity, flowParams);
    }


    public String getItemPrice(String item) {
        return mPrices.get(item);
    }

    public boolean isPurchased() {
        return mPurchasedItem != null;
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        if (responseCode == BillingClient.BillingResponse.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                mPurchasedItem = purchase.getSku();
            }

            if ( isPurchased() ) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_IAP_SUCCESS));
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            mPurchasedItem = null;
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_IAP_FAILED));
        } else {
            // Handle any other error codes.
            mPurchasedItem = null;
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_IAP_FAILED));
        }
    }
}
