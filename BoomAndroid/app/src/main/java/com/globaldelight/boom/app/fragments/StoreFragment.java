

package com.globaldelight.boom.app.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.app.analytics.UtilAnalytics;
import com.globaldelight.boom.app.businessmodel.inapp.InAppPurchase;
import com.globaldelight.boom.business.BusinessStrategy;
import com.globaldelight.boom.business.inapp.IabHelper;
import com.globaldelight.boom.business.inapp.IabResult;
import com.globaldelight.boom.business.inapp.Inventory;
import com.globaldelight.boom.business.inapp.Purchase;
import com.globaldelight.boom.app.receivers.ConnectivityReceiver;
import com.globaldelight.boom.view.RegularButton;
import com.globaldelight.boom.view.RegularTextView;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.app.sharedPreferences.Preferences;

import java.util.ArrayList;

import static com.globaldelight.boom.app.businessmodel.inapp.InAppPurchase.SKU_INAPP_ITEM;
import static com.globaldelight.boom.app.businessmodel.inapp.InAppPurchase.SKU_INAPP_ITEM_2;
import static com.globaldelight.boom.app.businessmodel.inapp.InAppPurchase.SKU_INAPP_ITEM_3;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_SONG_CHANGED;
import static com.globaldelight.boom.app.sharedPreferences.Preferences.INAPP_PURCHASE_PRICE_VALUE;

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
                case ACTION_SONG_CHANGED:
                    updateInApp();
                    break;

                case InAppPurchase.ACTION_IAP_RESTORED:
                    onSuccessRestoreAppPurchase();
                    break;

                case InAppPurchase.ACTION_IAP_SUCCESS:
                    onSuccessAppPurchase();
                    break;

                case InAppPurchase.ACTION_IAP_FAILED:
                    onErrorAppPurchase();
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
    //    FlurryAnalyticHelper.init(mActivity);
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

        if (!BusinessStrategy.getInstance(getActivity()).isPurchased() ) {
//            Not Purchased
            if (ConnectivityReceiver.isNetworkAvailable(mContext, true)) {
                intiStoreStartup();
            }else{
                normalStoreUI(getCurrentPrice());
            }
        }else{
//            Purchased
            purchasedStoreUI();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(InAppPurchase.ACTION_IAP_RESTORED);
        filter.addAction(InAppPurchase.ACTION_IAP_SUCCESS);
        filter.addAction(InAppPurchase.ACTION_IAP_FAILED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateInAppItemReceiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateInAppItemReceiver);
    //    FlurryAnalyticHelper.flurryStopSession(mActivity);
    }

    private void intiStoreStartup() {
        InAppPurchase.getInstance(getActivity()).initInAppPurchase();
    }

    private void purchasedStoreUI(){
        ((RegularTextView) rootView.findViewById(R.id.header_free_boomin)).setText(getResources().getString(R.string.after_purchase_store_page_header));
        ((RegularTextView) rootView.findViewById(R.id.store_buy_desription)).setText(getResources().getString(R.string.after_purchase_store_page_buy_description));
        mStoreBuyBtn.setText(getResources().getString(R.string.after_purchase_buy_button));
        mStoreShareTxt.setVisibility(View.GONE);
    }

    private void normalStoreUI(String price){
        ((RegularTextView) rootView.findViewById(R.id.header_free_boomin)).setText(getResources().getString(R.string.store_page_header));
        ((RegularTextView) rootView.findViewById(R.id.store_buy_desription)).setText(R.string.store_page_buy_description);
        ((RegularTextView) rootView.findViewById(R.id.store_buy_desription)).setText(R.string.store_page_buy_description);

        if (null != price)
            mStoreBuyBtn.setText(getResources().getString(R.string.buy_button) + " @ " + price);
        else
            mStoreBuyBtn.setText(getResources().getString(R.string.buy_button));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.store_share_text:
            //    FlurryAnalyticHelper.logEvent(UtilAnalytics.Share_Opened_from_Store);
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
        if (!InAppPurchase.getInstance(mContext).getIabHelper().handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == Utils.SHARE_COMPLETE) {
            updateShareContent();
        }
    }

    private void updateShareContent() {
        if( BusinessStrategy.getInstance(mContext).isPurchased() ){
            mStoreShareTxt.setVisibility(View.GONE);
        }else {
            ((RegularTextView) rootView.findViewById(R.id.store_buy_desription)).setText(R.string.store_page_buy_share_description);
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

    private String getCurrentInAppItem() {
        switch (BusinessStrategy.getInstance(mContext).getPurchaseLevel()) {
            default:
            case BusinessStrategy.PRICE_FULL:
                return SKU_INAPP_ITEM;
            case BusinessStrategy.PRICE_DISCOUNT:
                return SKU_INAPP_ITEM_2;
            case BusinessStrategy.PRICE_MIN:
                return SKU_INAPP_ITEM_3;
        }
    }


    public void startInAppFlow() {
        InAppPurchase.getInstance(mContext).buyNow(getActivity(), getCurrentInAppItem());
    }

    private void updateInApp() {
        mStoreBuyBtn.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        mActivity.unregisterReceiver(mUpdateInAppItemReceiver);
        super.onDestroy();
//        if(BusinessPreferences.readBoolean(mContext, STORE_CLOSED_WITH_PURCHASE, true)){
//            FlurryAnalyticHelper.logEvent(UtilAnalytics.Store_Closed_With_Purchase);
//            MixPanelAnalyticHelper.track(mActivity, UtilAnalytics.Store_Closed_With_Purchase);
//        }else{
//            FlurryAnalyticHelper.logEvent(UtilAnalytics.Store_Closed_Without_Purchase);
//        }
        MixPanelAnalyticHelper.getInstance(mContext).flush();
    }


    public void onErrorAppPurchase() {
        normalStoreUI(getCurrentPrice());
//        Toast.makeText(mActivity, getResources().getString(R.string.inapp_process_error), Toast.LENGTH_SHORT).show();
    }

    public void onSuccessAppPurchase() {
        purchasedStoreUI();
        BusinessStrategy.getInstance(mContext).onPurchaseSuccess();
        Toast.makeText(mActivity, getResources().getString(R.string.inapp_process_success), Toast.LENGTH_SHORT).show();
    }

    public void onSuccessRestoreAppPurchase() {
        purchasedStoreUI();
        BusinessStrategy.getInstance(mContext).onPurchaseSuccess();
        Toast.makeText(mActivity, getResources().getString(R.string.inapp_process_restore), Toast.LENGTH_SHORT).show();
    }

    private String getCurrentPrice() {
        String[] prices = InAppPurchase.getInstance(mContext).getPriceList();
        if ( prices.length >= 3 ) {
            return prices[BusinessStrategy.getInstance(mContext).getPurchaseLevel()];
        }

        return "";
    }
}

