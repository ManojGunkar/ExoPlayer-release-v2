package com.globaldelight.boom.business.client;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.globaldelight.boom.business.BusinessPreferences;
import com.globaldelight.boom.business.BusinessUtils;
import com.globaldelight.boom.business.inapp.IabHelper;
import com.globaldelight.boom.business.inapp.IabResult;
import com.globaldelight.boom.business.inapp.Inventory;
import com.globaldelight.boom.business.inapp.Purchase;

import static com.globaldelight.boom.business.BusinessPreferences.ACTION_IN_APP_PURCHASE;
import static com.globaldelight.boom.business.BusinessUtils.SKU_INAPPITEM;

/**
 * Created by Rahul Agarwal on 01-02-17.
 */

public class InAppHandler  {

//    private static final String TAG = "In-App-Handler";
//    private IabHelper mHelper;
//    private Context mContext;
//    private Activity mActivity;
//    private static IPurchaseUpdater iPurchaseUpdater;
//    private Handler uiHandler;
//    public static final String ACTION_IN_APP_PURCHASE_SUCCESSFUL = "ACTION_INAPP_PURCHASE_SUCCESSFUL";
//
//    public InAppHandler(Context context, Activity activity, final IPurchaseUpdater iPurchaseUpdater){
//        this.mContext = context;
//        this.mActivity = activity;
//        this.iPurchaseUpdater = iPurchaseUpdater;
//        this.uiHandler = new Handler();
//        mHelper = new IabHelper(mContext, BusinessUtils.base64EncodedPublicKey);
//        mHelper.enableDebugLogging(true);
//        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
//            public void onIabSetupFinished(IabResult result) {
//
//                if (!result.isSuccess()) {
//                    // Oh noes, there was a problem.
//                    complain("Problem setting up in-app billing: " + result);
//                    return;
//                }
//
//                if (mHelper == null) return;
//                // Hooray, IAB is fully set up. Now, let's get an inventory of
//                // stuff we own.
//                mHelper.queryInventoryAsync(InAppHandler.this);
//            }
//        });
//    }
//
//    public void startInAppFlow(){
//        String payload = BusinessUtils.getDeviceID(mContext);
//        mHelper.launchPurchaseFlow(mActivity, SKU_INAPPITEM, 10000,
//                this, payload);
//    }
//
//    private void disposeInAppHandler(){
//        if (mHelper != null) mHelper.dispose();
//        mHelper = null;
//    }
//
//    @Override
//    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
//        if (result.isFailure()) {
//            if(null != iPurchaseUpdater){
//                uiHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        iPurchaseUpdater.onErrorAppPurchase();
//                    }
//                });
//            }
//            return;
//        }
//        Purchase removeAdsPurchase = inventory.getPurchase(SKU_INAPPITEM);
//        boolean mIsPremium = inventory.hasPurchase(SKU_INAPPITEM);
//           /* if (mIsPremium) {
//                bt.setVisibility(View.GONE);
//                Toast.makeText(MainActivity.this, "Already you made purchase", Toast.LENGTH_SHORT);
//            }*/
//
//        if (removeAdsPurchase != null
//                && verifyDeveloperPayload(removeAdsPurchase)) {
//            mHelper.consumeAsync(inventory.getPurchase(SKU_INAPPITEM),
//                    InAppHandler.this);
//            return;
//        }
//    }
//
//    @Override
//    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
//        if (result.isFailure()) {
//            if(result.getResponse()==7){
//                BusinessPreferences.writeBoolean(mContext, ACTION_IN_APP_PURCHASE, true);
//                if(null != iPurchaseUpdater){
//                    uiHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            iPurchaseUpdater.onSuccessRestoreAppPurchase();
//                        }
//                    });
//                }
//            }else{
//                if(null != iPurchaseUpdater){
//                    uiHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            iPurchaseUpdater.onErrorAppPurchase();
//                        }
//                    });
//                }
//            }
//            disposeInAppHandler();
//            return;
//        }
//        if (!verifyDeveloperPayload(purchase)) {
//            if(null != iPurchaseUpdater){
//                uiHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        iPurchaseUpdater.onErrorAppPurchase();
//                    }
//                });
//            }
//
//            disposeInAppHandler();
//            return;
//        }
//
//        if (purchase.getSku().equals(SKU_INAPPITEM)) {
//            // bought 1/4 tank of gas. So consume it.
//            mHelper.consumeAsync(purchase, InAppHandler.this);
//        }
//    }
//
//    @Override
//    public void onConsumeFinished(Purchase purchase, IabResult result) {
//        if (result.isSuccess()) {
//            BusinessPreferences.writeBoolean(mContext, ACTION_IN_APP_PURCHASE, true);
//            if(null != iPurchaseUpdater){
//                uiHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        iPurchaseUpdater.onSuccessAppPurchase();
//                    }
//                });
//            }
//        } else {
//            if(null != iPurchaseUpdater){
//                uiHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        iPurchaseUpdater.onErrorAppPurchase();
//                    }
//                });
//            }
//        }
//        disposeInAppHandler();
//    }
//
//    void complain(String message) {
//        alert(message);
//    }
//
//    boolean verifyDeveloperPayload(Purchase purchase) {
//        String payload = purchase.getDeveloperPayload();
//
//		/*
//         * TODO: verify that the developer payload of the purchase is correct.
//		 * It will be the same one that you sent when initiating the purchase.
//		 *
//		 * WARNING: Locally generating a random string when starting a purchase
//		 * and verifying it here might seem like a good approach, but this will
//		 * fail in the case where the user purchases an item on one device and
//		 * then uses your app on a different device, because on the other device
//		 * you will not have access to the random string you originally
//		 * generated.
//		 *
//		 * So a good developer payload has these characteristics:
//		 *
//		 * 1. If two different users purchase an item, the payload is different
//		 * between them, so that one user's purchase can't be replayed to
//		 * another user.
//		 *
//		 * 2. The payload must be such that you can verify it even when the app
//		 * wasn't the one who initiated the purchase flow (so that items
//		 * purchased by the user on one device work on other devices owned by
//		 * the user).
//		 *
//		 * Using your own server to store and verify developer payloads across
//		 * app installations is recommended.
//		 */
//
//        return true;
//    }
//
//    @SuppressLint("LongLogTag")
//    void alert(String message) {
//        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
//    }
}