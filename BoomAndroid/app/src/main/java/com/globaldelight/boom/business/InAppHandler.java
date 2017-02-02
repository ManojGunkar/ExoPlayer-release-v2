package com.globaldelight.boom.business;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.globaldelight.boom.business.inapp.IabHelper;
import com.globaldelight.boom.business.inapp.IabResult;
import com.globaldelight.boom.business.inapp.Inventory;
import com.globaldelight.boom.business.inapp.Purchase;
import com.globaldelight.boom.business.network.PostRegisterDeviceData;

import static com.globaldelight.boom.business.Utills.SKU_INAPPITEM;

/**
 * Created by Rahul Agarwal on 01-02-17.
 */

public class InAppHandler implements IabHelper.QueryInventoryFinishedListener, IabHelper.OnIabPurchaseFinishedListener, IabHelper.OnConsumeFinishedListener{

    private static final String TAG = "In-App-Handler";
    private IabHelper mHelper;
    private Context mContext;
    private Activity mActivity;

    public InAppHandler(Context context, Activity activity){
        this.mContext = context;
        this.mActivity = activity;
        mHelper = new IabHelper(mContext, Utills.base64EncodedPublicKey);
        mHelper.enableDebugLogging(true);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                if (mHelper == null) return;
                // Hooray, IAB is fully set up. Now, let's get an inventory of
                // stuff we own.
                mHelper.queryInventoryAsync(InAppHandler.this);
            }
        });
    }


    public void startInAppFlow(){
        String payload = Utills.getDeviceID(mContext);
        mHelper.launchPurchaseFlow(mActivity, SKU_INAPPITEM, 10000,
                this, payload);
    }

    @Override
    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
        if (result.isFailure()) {
            complain("Failed to query inventory: " + result);
            return;
        }
			/*
             * Check for items we own. Notice that for each purchase, we check
			 * the developer payload to see if it's correct! See
			 * verifyDeveloperPayload().
			 */

        // Check for gas delivery -- if we own gas, we should fill up the
        // tank immediately
        Purchase removeAdsPurchase = inventory.getPurchase(SKU_INAPPITEM);
        if (removeAdsPurchase != null
                && verifyDeveloperPayload(removeAdsPurchase)) {
            mHelper.consumeAsync(inventory.getPurchase(SKU_INAPPITEM),
                    this);
            return;
        }

    }

    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
        Log.d(TAG, "Purchase finished: " + result + ", purchase: "
                + purchase);
        if (result.isFailure()) {
            complain("Error purchasing: " + result);
            return;
        }
        if (!verifyDeveloperPayload(purchase)) {
            complain("Error purchasing. Authenticity verification failed.");
            return;
        }

        Log.d(TAG, "Purchase successful.");

        if (purchase.getSku().equals(SKU_INAPPITEM)) {
            // bought 1/4 tank of gas. So consume it.
            Log.d(TAG,
                    "removeAdsPurchase was succesful.. starting consumption.");
            mHelper.consumeAsync(purchase, this);
        }
    }

    @Override
    public void onConsumeFinished(Purchase purchase, IabResult result) {
        Log.d(TAG, "Consumption finished. Purchase: " + purchase
                + ", result: " + result);

        // We know this is the "gas" sku because it's the only one we
        // consume,
        // so we don't check which sku was consumed. If you have more than
        // one
        // sku, you probably should check...
        if (result.isSuccess()) {
            // successfully consumed, so we apply the effects of the item in
            // our
            // game world's logic, which in our case means filling the gas
            // tank a bit
//            Log.d(TAG, "Consumption successful. Provisioning.");
            alert("You have purchased for removing ads from your app.");
        } else {
            complain("Error while consuming: " + result);
        }
        Log.d(TAG, "End consumption flow.");
    }


    void complain(String message) {
        alert(message);
    }

    boolean verifyDeveloperPayload(Purchase purchase) {
        String payload = purchase.getDeveloperPayload();

		/*
         * TODO: verify that the developer payload of the purchase is correct.
		 * It will be the same one that you sent when initiating the purchase.
		 *
		 * WARNING: Locally generating a random string when starting a purchase
		 * and verifying it here might seem like a good approach, but this will
		 * fail in the case where the user purchases an item on one device and
		 * then uses your app on a different device, because on the other device
		 * you will not have access to the random string you originally
		 * generated.
		 *
		 * So a good developer payload has these characteristics:
		 *
		 * 1. If two different users purchase an item, the payload is different
		 * between them, so that one user's purchase can't be replayed to
		 * another user.
		 *
		 * 2. The payload must be such that you can verify it even when the app
		 * wasn't the one who initiated the purchase flow (so that items
		 * purchased by the user on one device work on other devices owned by
		 * the user).
		 *
		 * Using your own server to store and verify developer payloads across
		 * app installations is recommended.
		 */

        return true;
    }

    @SuppressLint("LongLogTag")
    void alert(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }
}