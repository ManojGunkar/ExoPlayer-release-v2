

package com.globaldelight.boom.ui.musiclist.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.analytics.UtilAnalytics;
import com.globaldelight.boom.business.BusinessPreferences;
import com.globaldelight.boom.business.BusinessUtils;
import com.globaldelight.boom.business.inapp.IabBroadcastReceiver;
import com.globaldelight.boom.business.inapp.IabHelper;
import com.globaldelight.boom.business.inapp.IabResult;
import com.globaldelight.boom.business.inapp.Inventory;
import com.globaldelight.boom.business.inapp.Purchase;
import com.globaldelight.boom.manager.BoomPlayTimeReceiver;
import com.globaldelight.boom.manager.ConnectivityReceiver;
import com.globaldelight.boom.ui.musiclist.activity.ActivityContainer;
import com.globaldelight.boom.ui.widgets.RegularButton;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.handlers.Preferences;

import java.util.ArrayList;
import java.util.List;

import static com.globaldelight.boom.business.BusinessPreferences.ACTION_APP_SHARED;
import static com.globaldelight.boom.business.BusinessPreferences.ACTION_IN_APP_PURCHASE;
import static com.globaldelight.boom.business.BusinessPreferences.STORE_CLOSED_WITH_PURCHASE;
import static com.globaldelight.boom.business.BusinessUtils.SKU_INAPPITEM;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;
import static com.globaldelight.boom.utils.handlers.Preferences.INAPP_PURCHASE_PRICE_VALUE;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class StoreFragment extends Fragment implements View.OnClickListener {

    ScrollView rootView;
    private static final String TAG = "In-App-Handler";
    private IabHelper mHelper;
    private Context mContext;
    private Activity mActivity;
    public static final String ACTION_IN_APP_PURCHASE_SUCCESSFUL = "ACTION_INAPP_PURCHASE_SUCCESSFUL";
    boolean mIsPremium = false;
    private String boomPrice;
    private boolean intiStoreStartup;
    private ProgressBar progressBar;
    private RegularTextView mStoreShareTxt;
    private RegularButton mStoreBuyBtn;
    //    ConnectivityReceiver.isNetworkAvailable(mActivity, true)
    private BroadcastReceiver mUpdateInAppItemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY:
                    updateInApp();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ScrollView) inflater.inflate(R.layout.fragment_store, container, false);
        mActivity = getActivity();
        progressBar = new ProgressBar(mActivity);
        initViews();
        FlurryAnalyticHelper.init(mActivity);
        MixPanelAnalyticHelper.initPushNotification(mContext);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    private void initViews() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_IN_APP_PURCHASE_SUCCESSFUL);
        mActivity.registerReceiver(mUpdateInAppItemReceiver, intentFilter);

        mStoreShareTxt = (RegularTextView) rootView.findViewById(R.id.store_share_text);
        mStoreShareTxt.setOnClickListener(this);
        mStoreBuyBtn = (RegularButton) rootView.findViewById(R.id.store_buyButton);
        mStoreBuyBtn.setOnClickListener(this);

        progressBar.setVisibility(View.GONE);
        updateShareContent();

        if (!BusinessPreferences.readBoolean(mContext, ACTION_IN_APP_PURCHASE, false)) {
//            Not Purchased
            if (ConnectivityReceiver.isNetworkAvailable(mContext, true)) {
                intiStoreStartup();
            }else{
                normalStoreUI(Preferences.readString(mActivity, INAPP_PURCHASE_PRICE_VALUE, null));
            }
        }else{
//            Purchased
            purchasedStoreUI();
        }
    }

    private void updateStoreUiAfterStartup() {
        if(null != getActivity()) {
            if (!BusinessPreferences.readBoolean(mContext, ACTION_IN_APP_PURCHASE, false)) {
//            Not Purchased
                normalStoreUI(Preferences.readString(mActivity, INAPP_PURCHASE_PRICE_VALUE, null));
            } else {
//            Purchased
                purchasedStoreUI();
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        FlurryAnalyticHelper.flurryStartSession(mActivity);
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAnalyticHelper.flurryStopSession(mActivity);
    }

    private void intiStoreStartup() {
        mHelper = new IabHelper(mContext, BusinessUtils.base64EncodedPublicKey);
        mHelper.enableDebugLogging(true);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
//                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                if (mHelper == null) return;
                // Hooray, IAB is fully set up. Now, let's get an inventory of
                // stuff we own.
                ArrayList<String> skuList = new ArrayList<String>();
                skuList.add(SKU_INAPPITEM);
                try {
                    mHelper.queryInventoryAsync(true, skuList, null, mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void purchasedStoreUI(){
        ((RegularTextView) rootView.findViewById(R.id.header_free_boomin)).setText(getResources().getString(R.string.after_purchase_store_page_header));
        ((RegularTextView) rootView.findViewById(R.id.store_buy_desription)).setText(getResources().getString(R.string.after_purchase_store_page_buy_description));
        mStoreBuyBtn.setText(getResources().getString(R.string.after_purchase_buy_button));
        mStoreShareTxt.setVisibility(View.GONE);
        (rootView.findViewById(R.id.store_sub_discription)).setVisibility(View.GONE);
    }

    private void normalStoreUI(String price){
        ((RegularTextView) rootView.findViewById(R.id.header_free_boomin)).setText(getResources().getString(R.string.store_page_header));
        ((RegularTextView) rootView.findViewById(R.id.store_buy_desription)).setText(getResources().getString(R.string.store_page_buy_description));
        if (null != price)
            mStoreBuyBtn.setText(getResources().getString(R.string.buy_button) + " @ " + price);
        else
            mStoreBuyBtn.setText(getResources().getString(R.string.buy_button));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.store_share_text:
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Share_Opened_from_Store);
                try {
                    Utils.shareStart(mActivity, StoreFragment.this);
                } catch (Exception e) {
                }
                break;
            case R.id.store_buyButton:
                if (mIsPremium == false) {
                    try {
                        Log.d("installdate",String.valueOf(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).firstInstallTime));
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    startPurchaseRestore();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == Utils.SHARE_COMPLETE) {
            updateShareContent();
        }
    }

    private void updateShareContent() {
        if(BusinessPreferences.readBoolean(mContext, ACTION_IN_APP_PURCHASE, false) ||
                BusinessPreferences.readBoolean(mActivity, ACTION_APP_SHARED, false) ||
                BoomPlayTimeReceiver.isNoPopupShown() ){
            rootView.findViewById(R.id.store_sub_discription).setVisibility(View.INVISIBLE);
            mStoreShareTxt.setVisibility(View.INVISIBLE);
        }else {
            rootView.findViewById(R.id.store_sub_discription).setVisibility(View.VISIBLE);
            mStoreShareTxt.setVisibility(View.VISIBLE);
        }
    }

    private void startPurchaseRestore() {
        if (ConnectivityReceiver.isNetworkAvailable(mActivity, true)) {
            try {
                startInAppFlow();

            } catch (Exception e) {
            }
        }
    }

    public void startInAppFlow() {
        String payload = BusinessUtils.getDeviceID(mContext);
//        String payload = "test1";
        FlurryAnalyticHelper.logEvent(UtilAnalytics.Tap_on_Buy);
        try {
            mHelper.launchPurchaseFlow(mActivity, SKU_INAPPITEM, Utils.PURCHASE_FLOW_LAUNCH,
                    mPurchaseFinishedListener, payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    private void updateInApp() {
        mStoreBuyBtn.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        mActivity.unregisterReceiver(mUpdateInAppItemReceiver);
        super.onDestroy();
        if(BusinessPreferences.readBoolean(mContext, STORE_CLOSED_WITH_PURCHASE, true)){
            FlurryAnalyticHelper.logEvent(UtilAnalytics.Store_Closed_With_Purchase);
            MixPanelAnalyticHelper.track(mActivity, UtilAnalytics.Store_Closed_With_Purchase);
        }else{
            FlurryAnalyticHelper.logEvent(UtilAnalytics.Store_Closed_Without_Purchase);
        }
        MixPanelAnalyticHelper.getInstance(mContext).flush();
    }

    public void restorePurchase() {
        if (Utils.isBusinessModelEnable() && mIsPremium == false) {
            startPurchaseRestore();
        }
    }

    public void onErrorAppPurchase() {
//        Toast.makeText(mActivity, getResources().getString(R.string.inapp_process_error), Toast.LENGTH_SHORT).show();
    }

    public void onSuccessAppPurchase() {
        purchasedStoreUI();
        Toast.makeText(mActivity, getResources().getString(R.string.inapp_process_success), Toast.LENGTH_SHORT).show();
    }

    public void onSuccessRestoreAppPurchase() {
//        updateStoreUI(true, boomPriceSh);
        Toast.makeText(mActivity, getResources().getString(R.string.inapp_process_restore), Toast.LENGTH_SHORT).show();
    }


    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        @SuppressLint("LongLogTag")
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isSuccess()) {
                BusinessPreferences.writeBoolean(mContext, ACTION_IN_APP_PURCHASE, true);
                mIsPremium = true;
                onSuccessAppPurchase();
            } else {
                onErrorAppPurchase();
            }
        }
    };


    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {
            if (mHelper == null) return;
            if (result.isFailure()) {
                onErrorAppPurchase();
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Purchase_Failed);
                return;
            }
            Purchase premiumPurchase = inventory.getPurchase(SKU_INAPPITEM);

            mIsPremium = inventory.hasPurchase(SKU_INAPPITEM);
            if (inventory.hasDetails(SKU_INAPPITEM)) {
                boomPrice =
                        inventory.getSkuDetails(SKU_INAPPITEM).getPrice();
                if (null != boomPrice) {
                    Preferences.writeString(mActivity, INAPP_PURCHASE_PRICE_VALUE, boomPrice);
                }
            }

            if (mIsPremium) {
                BusinessPreferences.writeBoolean(mContext, ACTION_IN_APP_PURCHASE, true);
            }
            if (premiumPurchase != null
                    && verifyDeveloperPayload(premiumPurchase)) {
                mIsPremium = true;
            }
            updateStoreUiAfterStartup();
            return;
        }
    };
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @SuppressLint("LongLogTag")
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                if (result.getResponse() == -1003) {
                    BusinessPreferences.writeBoolean(mContext, ACTION_IN_APP_PURCHASE, true);
                    onSuccessAppPurchase();
                }
                if (result.getResponse() == 7) {
                    BusinessPreferences.writeBoolean(mContext, ACTION_IN_APP_PURCHASE, true);
                    mIsPremium = true;
                    onSuccessRestoreAppPurchase();
                } else {
                    onErrorAppPurchase();
                }
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Purchase_Failed);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                onErrorAppPurchase();
                return;
            }

            if (purchase.getSku().equals(SKU_INAPPITEM)) {
                mIsPremium = true;
                FlurryAnalyticHelper.logEvent(UtilAnalytics.PurchaseCompleted);
                BusinessPreferences.writeBoolean(mContext, ACTION_IN_APP_PURCHASE, true);
                BusinessPreferences.writeBoolean(mContext, STORE_CLOSED_WITH_PURCHASE, true);
                onSuccessAppPurchase();
            }

        }
    };


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

    public IabHelper getPurchaseHelper() {
        return mHelper;
    }
}

