package com.globaldelight.boom.app.businessmodel.inapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.UtilAnalytics;
import com.globaldelight.boom.app.sharedPreferences.Preferences;
import com.globaldelight.boom.business.inapp.IabHelper;
import com.globaldelight.boom.business.inapp.IabResult;
import com.globaldelight.boom.business.inapp.Inventory;
import com.globaldelight.boom.business.inapp.Purchase;
import com.globaldelight.boom.playbackEvent.handler.PlaybackManager;
import com.globaldelight.boom.utils.Utils;

import java.util.ArrayList;
import java.util.Observable;

import static com.globaldelight.boom.app.sharedPreferences.Preferences.INAPP_PURCHASE_PRICE_VALUE;

/**
 * @Created by Manoj Kumar on 7/11/2017.
 * @Manually commented All method by Manoj Kumar on 12 July 2017.
 */

public class InAppPurchase {

    public static final String ACTION_IAP_RESTORED = "com.globaldelight.boom.IAP_RESTORED";
    public static final String ACTION_IAP_SUCCESS = "com.globaldelight.boom.IAP_SUCCESS";
    public static final String ACTION_IAP_FAILED = "com.globaldelight.boom.IAP_FAILED";

    /**
     * @InApp purchase requirement.
     */
    public static final String APP_TYPE = "android";
    public static final String APP_ID = "com.globaldelight.boom";
    public static final String COUNTRY = "IN";
    public static final String SECRET_KEY = "e286b4b87f69a58aadbb8c38ecd6fbda7df398e8b712bd542488be9fba3a1b46";
    public static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgiRZhXAbnXPjPhiuR3u6JsojGI8zmLk9YRma6j1Hc3uCXytO344tIcgHjwyNVDzMJ+U1ounor+A7ON6Uu7alb6+uuVqYgp68aA7GXg8OwHvqYJO0qzogQnPv3eyuDYtYq4EmMuc0PefCXrCdLQyUAS9bGCCianhyBknQVD8JPJZDT2mzjK73XgKT5BeWrmq1QEfWggaqXGXW+3g0DrWtC+u4BwljYrrcl3bX/KammReI/LIFKQIPb11nOrTsgG0ik2IrxaOOo0VTrDHn3Phk8Xg27/8Y7P4bAtSvQyF5U0u+vDoT6L6nKfZ4jEEwOk7XhasWL6pl7+oPzOR9NDCYEwIDAQAB";
    public static final String SKU_INAPP_ITEM = "com.globaldelight.boom_magicalsurroundsound1";
    public static final String SKU_INAPP_ITEM_2 = "com.globaldelight.boom_magicalsurroundsound2";
    public static final String SKU_INAPP_ITEM_3 = "com.globaldelight.boom_magicalsurroundsound3";

    private static final String IAP_ITEM1_PRICE_KEY = "com.globaldelight.boom.ITEM1_PRICE";
    private static final String IAP_ITEM2_PRICE_KEY = "com.globaldelight.boom.ITEM2_PRICE";
    private static final String IAP_ITEM3_PRICE_KEY = "com.globaldelight.boom.ITEM3_PRICE";


    public static final String TAG="InApp Purchase";

    private boolean isPremium;
    private String[] skuPrices = new String[3];
    private Context context;
    private static InAppPurchase instance;

    /**
     * @InApp Purchase Helper class.
     */
    private IabHelper iabHelper;

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

    public IabHelper getIabHelper(){
        return iabHelper;
    }
    /**
     * @secure constructor for singleton pattern.
     * @param context
     */
    private InAppPurchase(Context context) {
        this.context=context;
    }


    private void queryPurchasedItems() throws IabHelper.IabAsyncInProgressException {
        if (iabHelper.isSetupDone() && !iabHelper.isAsyncInProgress()) {
            iabHelper.queryInventoryAsync(getQueryInventory());
        }
    }

    /**
     * With the help of this method we can initialise InApp purchase.
     * @return Method chaining technique which bind other method with same context.
     */
    public  InAppPurchase initInAppPurchase(){
        iabHelper = new IabHelper(context,PUBLIC_KEY);
        iabHelper.enableDebugLogging(true);
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) return;
                if (iabHelper == null) return;

                ArrayList<String> skuList = new ArrayList<String>();
                skuList.add(SKU_INAPP_ITEM);
                skuList.add(SKU_INAPP_ITEM_2);
                skuList.add(SKU_INAPP_ITEM_3);

                try {
                    iabHelper.queryInventoryAsync(true, skuList, null, getQueryInventory());
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            queryPurchasedItems();
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * With the help of this method we can get sku details such as price, county etc.
     * @return which return QueryInventory listener.
     */
    public IabHelper.QueryInventoryFinishedListener getQueryInventory(){
        return new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result,
                                                 Inventory inventory) {
                if (iabHelper == null) return;
                if (result.isFailure()) return;

                Purchase premiumPurchase = inventory.getPurchase(SKU_INAPP_ITEM);
                isPremium = inventory.hasPurchase(SKU_INAPP_ITEM) || inventory.hasPurchase(SKU_INAPP_ITEM_2) || inventory.hasPurchase(SKU_INAPP_ITEM_3)  ;
                if (inventory.hasDetails(SKU_INAPP_ITEM)) {
                    skuPrices[0] = inventory.getSkuDetails(SKU_INAPP_ITEM).getPrice();
                    if (skuPrices[0]!=null) {
                        Preferences.writeString(context, IAP_ITEM1_PRICE_KEY, skuPrices[0]);
                    }
                }
                if (inventory.hasDetails(SKU_INAPP_ITEM_2)) {
                    skuPrices[1] = inventory.getSkuDetails(SKU_INAPP_ITEM_2).getPrice();
                    if (skuPrices!=null) {
                        Preferences.writeString(context, IAP_ITEM2_PRICE_KEY, skuPrices[1]);
                    }
                }
                if (inventory.hasDetails(SKU_INAPP_ITEM_3)) {
                    skuPrices[2] = inventory.getSkuDetails(SKU_INAPP_ITEM_3).getPrice();
                    if (skuPrices[2]!=null) {
                        Preferences.writeString(context, IAP_ITEM3_PRICE_KEY, skuPrices[2]);
                    }
                }

                if (isPremium) {
                    onPurchaseRestored();
                }
                else if (premiumPurchase != null&& verifyDeveloperPayload(premiumPurchase)) {
                    isPremium = true;
                    onPurchaseRestored();
                }

                return;
            }
        };
    }



    /**
     *You can purchase package of InApp purchase.
     * @param activity which handled by onActivityResult.
     */
    public void buyNow(Activity activity, String inAppItem) {
        String payload = getDeviceID(context);
        try {
            iabHelper.launchPurchaseFlow(activity, inAppItem, Utils.PURCHASE_FLOW_LAUNCH,
                    getPurchased(), payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    private IabHelper.OnIabPurchaseFinishedListener getPurchased(){
        return new IabHelper.OnIabPurchaseFinishedListener() {
            @SuppressLint("LongLogTag")
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                if (result.isFailure()) {
                    if (result.getResponse() == -1003) {
                        onPurchaseSuccess();
                        Toast.makeText(context, context.getResources().getString(R.string.inapp_process_success), Toast.LENGTH_SHORT).show();                    }
                    if (result.getResponse() == 7) {
                        onPurchaseRestored();
                        Toast.makeText(context, context.getResources().getString(R.string.inapp_process_restore), Toast.LENGTH_SHORT).show();
                    } else {
                        onPurchaseFailed();
                        Toast.makeText(context, context.getResources().getString(R.string.inapp_process_error), Toast.LENGTH_SHORT).show();                    return;
                    }
                //    FlurryAnalyticHelper.logEvent(UtilAnalytics.Purchase_Failed);
                    return;
                }
                if (!verifyDeveloperPayload(purchase)) {
                    onPurchaseFailed();
                    Toast.makeText(context, context.getResources().getString(R.string.inapp_process_error), Toast.LENGTH_SHORT).show();                    return;
                }
                if (purchase.getSku().equals(SKU_INAPP_ITEM) || purchase.getSku().equals(SKU_INAPP_ITEM_2) || purchase.getSku().equals(SKU_INAPP_ITEM_3)) {
                    isPremium = true;
                    onPurchaseSuccess();
                    //   FlurryAnalyticHelper.logEvent(UtilAnalytics.PurchaseCompleted);
                    Toast.makeText(context, context.getResources().getString(R.string.inapp_process_success), Toast.LENGTH_SHORT).show();
                }

            }
        };
    }

    private boolean verifyDeveloperPayload(Purchase purchase) {
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

    private void onPurchaseRestored() {
        isPremium = true;
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_IAP_RESTORED));
    }

    private void onPurchaseSuccess() {
        isPremium = true;
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_IAP_SUCCESS));
    }

    private void onPurchaseFailed() {
        Intent intent = new Intent();
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_IAP_FAILED));
    }

    public String[] getPriceList() {
        return skuPrices;
    }

    public boolean isPurchased() {
        return isPremium;
    }

    private static String getDeviceID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
 }
