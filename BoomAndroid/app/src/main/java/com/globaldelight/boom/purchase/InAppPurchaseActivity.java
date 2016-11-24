package com.globaldelight.boom.purchase;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.purchase.util.IabHelper;
import com.globaldelight.boom.purchase.util.IabResult;
import com.globaldelight.boom.purchase.util.Inventory;
import com.globaldelight.boom.purchase.util.Purchase;
import com.globaldelight.boom.ui.widgets.RegularButton;
import com.globaldelight.boomplayer.AudioEffect;

public class InAppPurchaseActivity extends AppCompatActivity {
    public static final String TAG = "In-APP";
    static final String SKU_BOOM_3D_SURROUND = "android.test.purchased";
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    IabHelper mHelper;
    RegularButton buyButton;
    RegularButton restoreButton;
    private AudioEffect audioEffectPreferenceHandler;
    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase is finished: " + result.getResponse() + ", purchase: " + purchase);
/*
          Purchase data: {"packageName":"com.globaldelight.boom","orderId":"transactionId.android.test.purchased","productId":"android.test.purchased","developerPayload":"","purchaseTime":0,"purchaseState":0,"purchaseToken":"inapp:com.globaldelight.boom:android.test.purchased"}
*/

            if (result.getResponse() == IabHelper.IABHELPER_USER_CANCELLED) {
                AnalyticsHelper.purchaseCancelled();

            }




            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                //  setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                if (!verifyDeveloperPayload(purchase)) {
                    complain("Error purchasing. Authenticity verification failed.");
                    // setWaitScreen(false);
                    return;
                }
                return;
            }

            Log.d(TAG, "Purchase successful.");


            if (purchase.getSku().equals(SKU_BOOM_3D_SURROUND)) {
                // bought the premium upgrade!
                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(InAppPurchaseActivity.this, R.style.AppCompatAlertDialogStyle);
                builder.setTitle("Congratulations");
                builder.setMessage("Thank you for purchasing Boom Magical Effects Pack");
                builder.setPositiveButton("OK", null);
                builder.show();
                onSuccessPurchase();
            }

        }
    };
    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                Log.d(TAG, "Query inventory was fail." + result);
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Check for 3d delivery -- if we own 3d, we should fill up the 3d effect immediately
            Purchase effect3dPurchase = inventory.getPurchase(SKU_BOOM_3D_SURROUND);
            if (effect3dPurchase != null && verifyDeveloperPayload(effect3dPurchase)) {
                Log.d(TAG, "We have 3d. Updating it.");
                userAlreadyPurchased();

                //In Boom No need for consume it
               /* try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_BOOM_3D_SURROUND), mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error consuming gas. Another async operation in progress.");
                }*/
                return;
            }


            //setWaitScreen(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app);
        buyButton = (RegularButton) findViewById(R.id.buyButton);
        restoreButton = (RegularButton) findViewById(R.id.restore);
        audioEffectPreferenceHandler = AudioEffect.getAudioEffectInstance(this);
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPurchase();
            }
        });
        restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });
        // compute your public key and store it in base64EncodedPublicKey
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgiRZhXAbnXPjPhiuR3u6JsojGI8zmLk9YRma6j1Hc3uCXytO344tIcgHjwyNVDzMJ+U1ounor+A7ON6Uu7alb6+uuVqYgp68aA7GXg8OwHvqYJO0qzogQnPv3eyuDYtYq4EmMuc0PefCXrCdLQyUAS9bGCCianhyBknQVD8JPJZDT2mzjK73XgKT5BeWrmq1QEfWggaqXGXW+3g0DrWtC+u4BwljYrrcl3bX/KammReI/LIFKQIPb11nOrTsgG0ik2IrxaOOo0VTrDHn3Phk8Xg27/8Y7P4bAtSvQyF5U0u+vDoT6L6nKfZ4jEEwOk7XhasWL6pl7+oPzOR9NDCYEwIDAQAB";
        // Create the helper, passing it our context and the public key to verify signatures with
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);
       /* mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh no, there was a problem.
                    Log.d(TAG, "Problem setting up In-app Billing: " + result);
                }
                // Hooray, IAB is fully set up!
            }
        });*/
        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;


                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
//                try {
//                    mHelper.queryInventoryAsync(mGotInventoryListener);
//                } catch (IabHelper.IabAsyncInProgressException e) {
//                    complain("Error querying inventory. Another async operation in progress.");
//                }
            }
        });
        buyButton.setTransformationMethod(null);

    }

    // Enables or disables the "please wait" screen.
    void setWaitScreen(boolean set) {
        //  findViewById(R.id.screen_main).setVisibility(set ? View.GONE : View.VISIBLE);
        //  findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
    }

    /**
     * Verifies the developer payload of a purchase.
     */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    void complain(String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);
        // alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
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

    public void newPurchase() {

        Log.d(TAG, "Launching purchase flow for gas.");

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        try {
            mHelper.launchPurchaseFlow(this, SKU_BOOM_3D_SURROUND, RC_REQUEST,
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

    public void userAlreadyPurchased() {


        AlertDialog.Builder builder =
                new AlertDialog.Builder(InAppPurchaseActivity.this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Congratulations");
        builder.setMessage("You had already purchased Boom Magical Effects Pack.Thank you for purchasing.");
        builder.setPositiveButton("OK", null);
        builder.show();
        audioEffectPreferenceHandler.setUserPurchaseType(AudioEffect.purchase.PAID_USER);
        finish();
    }

    public void onSuccessPurchase() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(InAppPurchaseActivity.this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Congratulations");
        builder.setMessage("Thank you for purchasing Boom Magical Effects Pack");
        builder.setPositiveButton("OK", null);
        builder.show();
        audioEffectPreferenceHandler.setUserPurchaseType(AudioEffect.purchase.PAID_USER);
        finish();
    }
}
