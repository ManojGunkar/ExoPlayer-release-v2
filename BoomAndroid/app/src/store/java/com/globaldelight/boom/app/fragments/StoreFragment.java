

package com.globaldelight.boom.app.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.globaldelight.boom.BuildConfig;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.business.inapp.InAppPurchase;
import com.globaldelight.boom.app.share.ShareDialog;
import com.globaldelight.boom.business.GooglePlayStoreModel;
import com.globaldelight.boom.app.receivers.ConnectivityReceiver;
import com.globaldelight.boom.business.BusinessModelFactory;
import com.globaldelight.boom.utils.Utils;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class StoreFragment extends Fragment implements View.OnClickListener {

    ScrollView rootView;
    private static final String TAG = "StoreFragment";
    private Context mContext;
    private Activity mActivity;
    public static final String ACTION_IN_APP_PURCHASE_SUCCESSFUL = "ACTION_INAPP_PURCHASE_SUCCESSFUL";
    private ProgressBar progressBar;
    private Button mStoreBuy6MonthBtn;
    private Button mStoreBuy1YearBtn;
    private Button mClearButton;
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
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mUpdateInAppItemReceiver, intentFilter);

        mStoreBuy6MonthBtn = rootView.findViewById(R.id.store_buyButton_6month);
        mStoreBuy6MonthBtn.setOnClickListener(this);

        mStoreBuy1YearBtn = rootView.findViewById(R.id.store_buyButton_1year);
        mStoreBuy1YearBtn.setOnClickListener(this);

        mClearButton = rootView.findViewById(R.id.store_clear_button);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InAppPurchase.getInstance(getActivity()).clearInAppsPurchase();
            }
        });
        mClearButton.setVisibility(View.GONE);


        progressBar.setVisibility(View.GONE);
        updateShareContent();

        if ( !InAppPurchase.getInstance(getContext()).isPurchased() ) {
            normalStoreUI();
            InAppPurchase.getInstance(getContext()).initInAppPurchase();
        }else{
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
    }

    private void purchasedStoreUI(){
        ((TextView) rootView.findViewById(R.id.header_free_boomin)).setText(getResources().getString(R.string.after_purchase_store_page_header));
        ((TextView) rootView.findViewById(R.id.store_buy_desription)).setText(getResources().getString(R.string.after_purchase_store_page_buy_description));
        mStoreBuy6MonthBtn.setVisibility(View.GONE);
        mStoreBuy1YearBtn.setVisibility(View.GONE);
        if ( !BuildConfig.FLAVOR.equals("production") ) {
            mClearButton.setVisibility(View.VISIBLE);
        }
    }

    private void normalStoreUI(){
        ((TextView) rootView.findViewById(R.id.header_free_boomin)).setText(getResources().getString(R.string.store_page_header));
        ((TextView) rootView.findViewById(R.id.store_buy_desription)).setText(R.string.store_page_buy_description);
        ((TextView) rootView.findViewById(R.id.store_buy_desription)).setText(R.string.store_page_buy_description);

        String price6Month = InAppPurchase.getInstance(getContext()).getItemPrice(InAppPurchase.SKU_SUB_6MONTH);
        if (null != price6Month && price6Month.length() > 0)
            mStoreBuy6MonthBtn.setText(getResources().getString(R.string.buy_button) + " @ " + price6Month);
        else
            mStoreBuy6MonthBtn.setText(getResources().getString(R.string.buy_button));

        String price1Year = InAppPurchase.getInstance(getContext()).getItemPrice(InAppPurchase.SKU_SUB_1YEAR);
        if (null != price1Year && price1Year.length() > 0)
            mStoreBuy1YearBtn.setText(getResources().getString(R.string.buy_button) + " @ " + price1Year);
        else
            mStoreBuy1YearBtn.setText(getResources().getString(R.string.buy_button));

        mClearButton.setVisibility(View.GONE);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.store_buyButton_6month:
                if ( !InAppPurchase.getInstance(getContext()).isPurchased() ) {
                    if (ConnectivityReceiver.isNetworkAvailable(mActivity, true)) {
                        InAppPurchase.getInstance(mContext).buyNow(getActivity(), InAppPurchase.SKU_SUB_6MONTH);
                    }
                }
                break;

            case R.id.store_buyButton_1year:
                if ( !InAppPurchase.getInstance(getContext()).isPurchased() ) {
                    if (ConnectivityReceiver.isNetworkAvailable(mActivity, true)) {
                        InAppPurchase.getInstance(mContext).buyNow(getActivity(), InAppPurchase.SKU_SUB_1YEAR);
                    }
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
        if( InAppPurchase.getInstance(getContext()).isPurchased() ){
        }else {
            ((TextView) rootView.findViewById(R.id.store_buy_desription)).setText(R.string.store_page_buy_share_description);
        }
    }



    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mUpdateInAppItemReceiver);
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
        normalStoreUI();
    }

    public void onSuccessAppPurchase() {
        purchasedStoreUI();
        Toast.makeText(mActivity, getResources().getString(R.string.inapp_process_success), Toast.LENGTH_SHORT).show();
    }

    public void onSuccessRestoreAppPurchase() {
        purchasedStoreUI();
        Toast.makeText(mActivity, getResources().getString(R.string.inapp_process_restore), Toast.LENGTH_SHORT).show();
    }

}
