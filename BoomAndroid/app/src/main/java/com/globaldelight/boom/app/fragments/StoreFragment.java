

package com.globaldelight.boom.app.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.globaldelight.boom.app.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.app.analytics.UtilAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.app.receivers.ConnectivityReceiver;
import com.globaldelight.boom.view.RegularButton;
import com.globaldelight.boom.view.RegularTextView;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.app.sharedPreferences.Preferences;

import java.util.ArrayList;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;
import static com.globaldelight.boom.app.sharedPreferences.Preferences.INAPP_PURCHASE_PRICE_VALUE;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class StoreFragment extends Fragment implements View.OnClickListener {

    ScrollView rootView;
    private static final String TAG = "In-App-Handler";
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

        if (ConnectivityReceiver.isNetworkAvailable(mContext, true)) {
            intiStoreStartup();
        }else{
            normalStoreUI(Preferences.readString(mActivity, INAPP_PURCHASE_PRICE_VALUE, null));
        }
    }

    private void updateStoreUiAfterStartup() {
        if(null != getActivity()) {
            normalStoreUI(Preferences.readString(mActivity, INAPP_PURCHASE_PRICE_VALUE, null));
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        FlurryAnalytics.getInstance(getActivity()).startSession();
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAnalytics.getInstance(getActivity()).endSession();

    }

    private void intiStoreStartup() {
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
               // FlurryAnalyticHelper.logEvent(UtilAnalytics.Share_Opened_from_Store);
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Share_Opened_from_Store);

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
        if (requestCode == Utils.SHARE_COMPLETE) {
            updateShareContent();
        }
    }

    private void updateShareContent() {
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
    }

    private void updateInApp() {
        mStoreBuyBtn.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        mActivity.unregisterReceiver(mUpdateInAppItemReceiver);
        super.onDestroy();
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
}

