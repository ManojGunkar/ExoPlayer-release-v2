package com.globaldelight.boom.purchase;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.appsflyer.AFInAppEventParameterName;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.AppsFlyerAnalyticHelper;
import com.globaldelight.boom.purchase.util.IabHelper;
import com.globaldelight.boom.purchase.util.IabResult;
import com.globaldelight.boom.purchase.util.Inventory;
import com.globaldelight.boom.purchase.util.Purchase;
import com.globaldelight.boom.ui.widgets.RegularButton;
import com.globaldelight.boom.utils.Logger;
import com.globaldelight.boomplayer.AudioEffect;

import java.util.HashMap;
import java.util.Map;

public class InAppPurchaseActivity extends AppCompatActivity {
    public static final String TAG = "In-APP";
    static final String SKU_BOOM_3D_SURROUND = "com.globaldelight.boom_magicalsurroundsound1";
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    IabHelper mHelper;
    RegularButton buyButton;
    RegularButton restoreButton;
    MaterialDialog progress;
    Context context;
    private AudioEffect audioEffectPreferenceHandler;
    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Logger.LOGD(TAG, "Purchase is finished: " + result.getResponse());
            if (result.getResponse() == IabHelper.IABHELPER_USER_CANCELLED) {
                AnalyticsHelper.trackPurchaseCancelled(InAppPurchaseActivity.this);
            }
            if (mHelper == null) return;
            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                AnalyticsHelper.purchaseFailed(InAppPurchaseActivity.this);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                if (!verifyDeveloperPayload(purchase)) {
                    complain("Error purchasing. Authenticity verification failed.");
                    return;
                }
                return;
            }
            Logger.LOGD(TAG, "Purchase successful.");
            if (purchase.getSku().equals(SKU_BOOM_3D_SURROUND)) {
                onSuccessPurchase(purchase);
            }
        }
    };

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Logger.LOGD(TAG, "Query inventory finished.");
            if (progress != null && progress.isShowing())
                progress.dismiss();
            if (mHelper == null) return;
            if (result.isFailure()) {
                Logger.LOGD(TAG, "Query inventory was fail." + result);
                complain("Failed to query inventory: " + result);
                return;
            }
            Logger.LOGD(TAG, "Query inventory was successful.");

            // Check for 3d delivery -- if we own 3d, we should fill up the 3d effect immediately
            Purchase effect3dPurchase = inventory.getPurchase(SKU_BOOM_3D_SURROUND);
            if (effect3dPurchase != null && verifyDeveloperPayload(effect3dPurchase)) {
                Logger.LOGD(TAG, "We have 3d. Updating it.");
                userAlreadyPurchased();
                return;
            }
            Logger.LOGD(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        context = this;
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
                progress = new MaterialDialog.Builder(InAppPurchaseActivity.this)
                        //.title(R.string.txt_progress_restore)
                        .content(R.string.txt_progress_restore)
                        .backgroundColor(Color.parseColor("#171921"))
                        .titleColor(Color.parseColor("#ffffff"))
                        //.positiveColor(contextgetResources().getColor(R.color.colorPrimary))
                        .widgetColor(Color.parseColor("#ffffff"))
                        .contentColor(Color.parseColor("#ffffff"))
                        .progress(true, 0)
                        .show();
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
        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Logger.LOGD(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Logger.LOGD(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;


                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Logger.LOGD(TAG, "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });
        buyButton.setTransformationMethod(null);
        restoreButton.setTransformationMethod(null);
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
        //Logger.LOGD(TAG, "**** TrivialDrive Error: " + message);
       // alert("Error: " + message);
    }

    void alert(String message) {
        new MaterialDialog.Builder(context)
                // .title(R.string.title_congratulate)
                .content(message)
                .backgroundColor(Color.parseColor("#171921"))
                .titleColor(Color.parseColor("#ffffff"))
                .positiveColor(context.getResources().getColor(R.color.colorPrimary))
                .widgetColor(Color.parseColor("#ffffff"))
                .contentColor(Color.parseColor("#ffffff"))
                .typeface("TitilliumWeb-Regular.ttf", "TitilliumWeb-Regular.ttf")
                .positiveText(R.string.btn_txt_ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                })
                .show();
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

        Logger.LOGD(TAG, "Launching purchase flow for 3d.");

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        try {
            mHelper.launchPurchaseFlow(this, SKU_BOOM_3D_SURROUND, RC_REQUEST,
                    mPurchaseFinishedListener, payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error launching purchase flow. Another async operation in progress.");
        }


    }

    public void consumePurchased(View v) {
        mHelper.consumTest(this);
    }

    /* IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
         public void onConsumeFinished(Purchase purchase, IabResult result) {
             Logger.LOGD(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

             // if we were disposed of in the meantime, quit.
             if (mHelper == null) return;

             // We know this is the "gas" sku because it's the only one we consume,
             // so we don't check which sku was consumed. If you have more than one
             // sku, you probably should check...
             if (result.isSuccess()) {
                 // successfully consumed, so we apply the effects of the item in our
                 // game world's logic, which in our case means filling the gas tank a bit
                 Logger.LOGD(TAG, "Consumption successful. Provisioning.");

             }
             else {

             }

             Logger.LOGD(TAG, "End consumption flow.");
         }
     };*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.LOGD(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Logger.LOGD(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    public void userAlreadyPurchased() {
        showRestoreCongratulateDialog();
        audioEffectPreferenceHandler.setUserPurchaseType(AudioEffect.purchase.PAID_USER);
        Map<String, Object> eventValue = new HashMap<>();
        eventValue.put(AFInAppEventParameterName.CONTENT_TYPE, SKU_BOOM_3D_SURROUND);
        AppsFlyerAnalyticHelper.startTracking(this.getApplication());
        AnalyticsHelper.purchaseSuccess(this.getApplication(), eventValue, true, SKU_BOOM_3D_SURROUND);
    }

    public void onSuccessPurchase(Purchase purchase) {
        try {
            showCongratulateDialog();
            audioEffectPreferenceHandler.setUserPurchaseType(AudioEffect.purchase.PAID_USER);
            Map<String, Object> eventValue = new HashMap<>();
            eventValue.put(AFInAppEventParameterName.CONTENT_TYPE, purchase.getItemType());
            eventValue.put(AFInAppEventParameterName.CONTENT_ID, purchase.getOrderId());
            AppsFlyerAnalyticHelper.startTracking(this.getApplication());
            AnalyticsHelper.purchaseSuccess(this.getApplication(), eventValue, false, SKU_BOOM_3D_SURROUND);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void showRestoreCongratulateDialog() {

        new MaterialDialog.Builder(context)
                .title(R.string.title_congratulate_restoreduser)
                .content(R.string.desc_congratulate_restoreduser)
                .backgroundColor(Color.parseColor("#171921"))
                .titleColor(Color.parseColor("#ffffff"))
                .typeface("TitilliumWeb-SemiBold.ttf", "TitilliumWeb-Regular.ttf")
                .positiveColor(context.getResources().getColor(R.color.colorPrimary))
                .widgetColor(Color.parseColor("#ffffff"))
                .contentColor(Color.parseColor("#ffffff"))
                .positiveText(R.string.btn_txt_ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                        finish();
                    }
                })
                .show();

    }

    public void showCongratulateDialog() {
        new MaterialDialog.Builder(context)
                .title(R.string.title_congratulate_paiduser)
                .content(R.string.desc_congratulate_paiduser)
                .backgroundColor(Color.parseColor("#171921"))
                .titleColor(Color.parseColor("#ffffff"))
                .positiveColor(context.getResources().getColor(R.color.colorPrimary))
                .typeface("TitilliumWeb-SemiBold.ttf", "TitilliumWeb-Regular.ttf")

                .widgetColor(Color.parseColor("#ffffff"))
                .contentColor(Color.parseColor("#ffffff"))
                .positiveText(R.string.btn_txt_ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                        finish();
                    }
                })
                .show();

    }

}
