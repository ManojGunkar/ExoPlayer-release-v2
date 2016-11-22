package com.globaldelight.boom.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.globaldelight.boom.R;
import com.globaldelight.boom.test.util.IabHelper;
import com.globaldelight.boom.test.util.IabResult;
import com.globaldelight.boom.test.util.Purchase;

public class InAppTestActivity extends AppCompatActivity {
    public static final String TAG = "In-APP";
    static final String SKU_PREMIUM = "android.test.purchased";
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    IabHelper mHelper;
    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                //    complain("Error purchasing: " + result);
                //  setWaitScreen(false);
                return;
            }
           /* if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                setWaitScreen(false);
                return;
            }*/

            Log.d(TAG, "Purchase successful.");


            if (purchase.getSku().equals(SKU_PREMIUM)) {
                // bought the premium upgrade!
                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                /*alert("Thank you for upgrading to premium!");
                mIsPremium = true;
                updateUi();
                setWaitScreen(false);*/
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_test);
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgiRZhXAbnXPjPhiuR3u6JsojGI8zmLk9YRma6j1Hc3uCXytO344tIcgHjwyNVDzMJ+U1ounor+A7ON6Uu7alb6+uuVqYgp68aA7GXg8OwHvqYJO0qzogQnPv3eyuDYtYq4EmMuc0PefCXrCdLQyUAS9bGCCianhyBknQVD8JPJZDT2mzjK73XgKT5BeWrmq1QEfWggaqXGXW+3g0DrWtC+u4BwljYrrcl3bX/KammReI/LIFKQIPb11nOrTsgG0ik2IrxaOOo0VTrDHn3Phk8Xg27/8Y7P4bAtSvQyF5U0u+vDoT6L6nKfZ4jEEwOk7XhasWL6pl7+oPzOR9NDCYEwIDAQAB";
        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh no, there was a problem.
                    Log.d(TAG, "Problem setting up In-app Billing: " + result);
                }
                // Hooray, IAB is fully set up!
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) try {
            mHelper.dispose();
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
        mHelper = null;
    }

    public void newPurchase(View view) {

        Log.d(TAG, "Launching purchase flow for gas.");

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        try {
            mHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_REQUEST,
                    mPurchaseFinishedListener, payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            //  complain("Error launching purchase flow. Another async operation in progress.");
            // setWaitScreen(false);
        }

    }

    public void consumePurchased(View v) {
        mHelper.consumTest(this);
    }

    /* IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
         public void onConsumeFinished(Purchase purchase, IabResult result) {
             Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

             // if we were disposed of in the meantime, quit.
             if (mHelper == null) return;

             // We know this is the "gas" sku because it's the only one we consume,
             // so we don't check which sku was consumed. If you have more than one
             // sku, you probably should check...
             if (result.isSuccess()) {
                 // successfully consumed, so we apply the effects of the item in our
                 // game world's logic, which in our case means filling the gas tank a bit
                 Log.d(TAG, "Consumption successful. Provisioning.");

             }
             else {

             }

             Log.d(TAG, "End consumption flow.");
         }
     };*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }
}
