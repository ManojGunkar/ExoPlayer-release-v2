

package com.globaldelight.boom.ui.musiclist.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;
import com.globaldelight.boom.R;
import com.globaldelight.boom.business.BusinessPreferences;
import com.globaldelight.boom.business.BusinessUtils;
import com.globaldelight.boom.business.client.IPurchaseUpdater;
import com.globaldelight.boom.business.inapp.IabHelper;
import com.globaldelight.boom.business.inapp.IabResult;
import com.globaldelight.boom.business.inapp.Inventory;
import com.globaldelight.boom.business.inapp.Purchase;
import com.globaldelight.boom.manager.ConnectivityReceiver;
import com.globaldelight.boom.ui.widgets.RegularButton;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.Utils;
import static android.app.Activity.RESULT_OK;
import static com.globaldelight.boom.business.BusinessPreferences.ACTION_APP_SHARED;
import static com.globaldelight.boom.business.BusinessPreferences.ACTION_IN_APP_PURCHASE;
import static com.globaldelight.boom.business.BusinessUtils.SKU_INAPPITEM;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class StoreFragment extends Fragment implements View.OnClickListener {

    private static final int SHARE_COMPLETE = 1;
    ScrollView rootView;
    private static final String TAG = "In-App-Handler";


    private IabHelper mHelper;
    private Context mContext;
    private Activity mActivity;
    private Handler uiHandler;
    public static final String ACTION_IN_APP_PURCHASE_SUCCESSFUL = "ACTION_INAPP_PURCHASE_SUCCESSFUL";
    private BroadcastReceiver mUpdateInAppItemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
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
        initViews();
        return rootView;
    }

    private void initViews() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_IN_APP_PURCHASE_SUCCESSFUL);
        mActivity.registerReceiver(mUpdateInAppItemReceiver, intentFilter);

        updateStoreUI();

        updateShareContent();

        RegularTextView mStoreShareTxt = (RegularTextView) rootView.findViewById(R.id.store_share_text);

        mStoreShareTxt.setOnClickListener(this);
        RegularButton mStoreBuyBtn = (RegularButton) rootView.findViewById(R.id.store_buyButton);
        mStoreBuyBtn.setOnClickListener(this);

        this.uiHandler = new Handler();
        this.mContext = mActivity;
        mHelper = new IabHelper(mContext, BusinessUtils.base64EncodedPublicKey);
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
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
    }

    private void updateStoreUI() {
        if(BusinessPreferences.readBoolean(mActivity, BusinessPreferences.ACTION_IN_APP_PURCHASE, false)){
            ((RegularTextView)rootView.findViewById(R.id.header_free_boomin)).setText(getResources().getString(R.string.after_purchase_store_page_header));
            ((RegularTextView)rootView.findViewById(R.id.store_buy_desription)).setText(getResources().getString(R.string.after_purchase_store_page_buy_description));
            ((RegularButton)rootView.findViewById(R.id.store_buyButton)).setText(getResources().getString(R.string.after_purchase_buy_button));
            (rootView.findViewById(R.id.store_share_text)).setVisibility(View.GONE);
            (rootView.findViewById(R.id.store_sub_discription)).setVisibility(View.GONE);
        }else{
            ((RegularTextView)rootView.findViewById(R.id.header_free_boomin)).setText(getResources().getString(R.string.store_page_header));
            ((RegularTextView)rootView.findViewById(R.id.store_buy_desription)).setText(getResources().getString(R.string.store_page_buy_description));
            ((RegularButton)rootView.findViewById(R.id.store_buyButton)).setText(getResources().getString(R.string.store_page_button_text));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.store_share_text:
                if(ConnectivityReceiver.isNetworkAvailable(mActivity)) {
                    try {
                        Utils.shareStart(mActivity, StoreFragment.this);
                    }catch (Exception e){}
                }
                break;
            case R.id.store_buyButton:
                startPurchaseRestore();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        if(requestCode == SHARE_COMPLETE){
            updateShareContent();
        }
    }


    private void updateShareContent() {
//        if(BusinessPreferences.readBoolean(mActivity, ACTION_APP_SHARED, false)){
//            rootView.findViewById(R.id.store_sub_discription).setVisibility(View.INVISIBLE);
//            rootView.findViewById(R.id.store_share_text).setVisibility(View.INVISIBLE);
//        }else{
//            rootView.findViewById(R.id.store_sub_discription).setVisibility(View.VISIBLE);
//            rootView.findViewById(R.id.store_share_text).setVisibility(View.VISIBLE);
//        }
    }

    private void startPurchaseRestore(){
        if(ConnectivityReceiver.isNetworkAvailable(mActivity)){
            try {
                startInAppFlow();
            }catch (Exception e){}
        }
    }

    public void startInAppFlow() {
        String payload = BusinessUtils.getDeviceID(mContext);
        mHelper.launchPurchaseFlow(mActivity, SKU_INAPPITEM, 10000,
                mPurchaseFinishedListener, payload);
    }

    private void updateInApp(){
        rootView.findViewById(R.id.store_buyButton).setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        mActivity.unregisterReceiver(mUpdateInAppItemReceiver);
        super.onDestroy();
    }

    public void restorePurchase() {
        startPurchaseRestore();
    }

    public void onErrorAppPurchase() {
        Toast.makeText(mActivity, getResources().getString(R.string.inapp_process_error), Toast.LENGTH_SHORT).show();
    }

    public void onSuccessAppPurchase() {
        updateStoreUI();
        Toast.makeText(mActivity, getResources().getString(R.string.inapp_process_success), Toast.LENGTH_SHORT).show();
    }

    public void onSuccessRestoreAppPurchase() {
        updateStoreUI();
        Toast.makeText(mActivity, getResources().getString(R.string.inapp_process_restore), Toast.LENGTH_SHORT).show();
    }


    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        @SuppressLint("LongLogTag")
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isSuccess()) {
                BusinessPreferences.writeBoolean(mContext, ACTION_IN_APP_PURCHASE, true);
                onSuccessAppPurchase();
            } else {
                onErrorAppPurchase();
            }
            disposeInAppHandler();
        }
    };
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {
            if (result.isFailure()) {
                onErrorAppPurchase();
                return;
            }
            Purchase removeAdsPurchase = inventory.getPurchase(SKU_INAPPITEM);
            boolean mIsPremium = inventory.hasPurchase(SKU_INAPPITEM);
           /* if (mIsPremium) {
                bt.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Already you made purchase", Toast.LENGTH_SHORT);
            }*/
            if (removeAdsPurchase != null
                    && verifyDeveloperPayload(removeAdsPurchase)) {
                mHelper.consumeAsync(inventory.getPurchase(SKU_INAPPITEM),
                        mConsumeFinishedListener);
                return;
            }

        }

    };
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @SuppressLint("LongLogTag")
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                if (result.getResponse() == 7) {
                    BusinessPreferences.writeBoolean(mContext, ACTION_IN_APP_PURCHASE, true);
                    onSuccessRestoreAppPurchase();
                } else {
                    onErrorAppPurchase();
                }
                disposeInAppHandler();
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                onErrorAppPurchase();

                disposeInAppHandler();
                return;
            }

            if (purchase.getSku().equals(SKU_INAPPITEM)) {
                // bought 1/4 tank of gas. So consume it.
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            }
        }
    };

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
    private void disposeInAppHandler() {
        /*if (mHelper != null) mHelper.dispose();
        mHelper = null;*/
    }
}

