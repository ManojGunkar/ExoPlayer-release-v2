

package com.globaldelight.boom.app.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.app.businessmodel.inapp.InAppPurchase;
import com.globaldelight.boom.business.BusinessStrategy;
import com.globaldelight.boom.app.receivers.ConnectivityReceiver;
import com.globaldelight.boom.view.RegularButton;
import com.globaldelight.boom.view.RegularTextView;
import com.globaldelight.boom.utils.Utils;

import static com.globaldelight.boom.app.businessmodel.inapp.InAppPurchase.SKU_INAPP_ITEM;
import static com.globaldelight.boom.app.businessmodel.inapp.InAppPurchase.SKU_INAPP_ITEM_2;
import static com.globaldelight.boom.app.businessmodel.inapp.InAppPurchase.SKU_INAPP_ITEM_3;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_SONG_CHANGED;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class StoreFragment extends Fragment implements View.OnClickListener {

    ScrollView rootView;
    private static final String TAG = "In-App-Handler";
    private Context mContext;
    private Activity mActivity;
    public static final String ACTION_IN_APP_PURCHASE_SUCCESSFUL = "ACTION_INAPP_PURCHASE_SUCCESSFUL";
    private ProgressBar progressBar;
    private RegularTextView mStoreShareTxt;
    private RegularButton mStoreBuyBtn;
    private RegularButton mClearButton;
    private boolean mUserPurchased = false; // to track if the user purchased

    //    ConnectivityReceiver.isNetworkAvailable(mActivity, true)
    private BroadcastReceiver mUpdateInAppItemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case InAppPurchase.ACTION_IAP_RESTORED:
                    onSuccessRestoreAppPurchase();
                    break;

                case InAppPurchase.ACTION_IAP_SUCCESS:
                    mUserPurchased = true;
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

        mClearButton = (RegularButton) rootView.findViewById(R.id.store_clear_button);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InAppPurchase.getInstance(getActivity()).clearInAppsPurchase();
            }
        });
        mClearButton.setVisibility(View.GONE);


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
        mClearButton.setVisibility(View.VISIBLE);

    }

    private void normalStoreUI(String price){
        ((RegularTextView) rootView.findViewById(R.id.header_free_boomin)).setText(getResources().getString(R.string.store_page_header));
        ((RegularTextView) rootView.findViewById(R.id.store_buy_desription)).setText(R.string.store_page_buy_description);
        ((RegularTextView) rootView.findViewById(R.id.store_buy_desription)).setText(R.string.store_page_buy_description);

        if (null != price && price.length() > 0)
            mStoreBuyBtn.setText(getResources().getString(R.string.buy_button) + " @ " + price);
        else
            mStoreBuyBtn.setText(getResources().getString(R.string.buy_button));

        mClearButton.setVisibility(View.GONE);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.store_share_text:
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Share_Opened_from_Store);
                try {
                    Utils.shareStart(mActivity, StoreFragment.this);
                } catch (Exception e) {
                }
                break;
            case R.id.store_buyButton:
                if ( !BusinessStrategy.getInstance(getActivity()).isPurchased() ) {
                    if (ConnectivityReceiver.isNetworkAvailable(mActivity, true)) {
                        startInAppFlow();
                    }
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!InAppPurchase.getInstance(mContext).handleActivityResult(requestCode,
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

    private String getCurrentInAppItem() {
        switch (BusinessStrategy.getInstance(mContext).getPurchaseLevel()) {
            default:
            case BusinessStrategy.PRICE_FULL:
                return SKU_INAPP_ITEM;
            case BusinessStrategy.PRICE_DISCOUNT:
                return SKU_INAPP_ITEM_2;
            case BusinessStrategy.PRICE_DISCOUNT_2:
                return SKU_INAPP_ITEM_3;
        }
    }


    public void startInAppFlow() {
        InAppPurchase.getInstance(mContext).buyNow(getActivity(), getCurrentInAppItem());
    }

    @Override
    public void onDestroy() {
        mActivity.unregisterReceiver(mUpdateInAppItemReceiver);
        super.onDestroy();
        if( mUserPurchased ){
            FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Store_Closed_With_Purchase);
            MixPanelAnalyticHelper.track(mActivity, FlurryEvents.Store_Closed_With_Purchase);
        }else{
            FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Store_Closed_Without_Purchase);
        }
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

