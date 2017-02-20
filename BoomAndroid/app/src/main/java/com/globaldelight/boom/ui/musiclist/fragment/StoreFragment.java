package com.globaldelight.boom.ui.musiclist.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;
import com.globaldelight.boom.R;
import com.globaldelight.boom.business.BusinessPreferences;
import com.globaldelight.boom.business.client.IPurchaseUpdater;
import com.globaldelight.boom.business.client.InAppHandler;
import com.globaldelight.boom.manager.ConnectivityReceiver;
import com.globaldelight.boom.ui.widgets.RegularButton;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.Utils;
import static android.app.Activity.RESULT_OK;
import static com.globaldelight.boom.business.BusinessPreferences.ACTION_APP_SHARED;
import static com.globaldelight.boom.business.client.InAppHandler.ACTION_IN_APP_PURCHASE_SUCCESSFUL;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class StoreFragment extends Fragment implements View.OnClickListener, IPurchaseUpdater{

    ScrollView rootView;
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
        initViews();
        return rootView;
    }

    private void initViews() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_IN_APP_PURCHASE_SUCCESSFUL);
        getActivity().registerReceiver(mUpdateInAppItemReceiver, intentFilter);

        updateStoreUI();

        updateShareContent();

        RegularTextView mStoreShareTxt = (RegularTextView) rootView.findViewById(R.id.store_share_text);

        mStoreShareTxt.setOnClickListener(this);
        RegularButton mStoreBuyBtn = (RegularButton) rootView.findViewById(R.id.store_buyButton);
        mStoreBuyBtn.setOnClickListener(this);
    }

    private void updateStoreUI() {
        if(BusinessPreferences.readBoolean(getContext(), BusinessPreferences.ACTION_IN_APP_PURCHASE, false)){
            ((RegularTextView)rootView.findViewById(R.id.header_free_boomin)).setText(getResources().getString(R.string.after_purchase_store_page_header));
            ((RegularTextView)rootView.findViewById(R.id.store_buy_desription)).setText(getResources().getString(R.string.after_purchase_store_page_buy_description));
            ((RegularButton)rootView.findViewById(R.id.store_buyButton)).setText(getResources().getString(R.string.after_purchase_buy_button));
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
                if(ConnectivityReceiver.isNetworkAvailable(getContext())) {
                    try {
                        Utils.shareStart(getActivity(), StoreFragment.this);
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
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Utils.SHARE_COMPLETE && resultCode == RESULT_OK){
            BusinessPreferences.writeBoolean(getContext(), ACTION_APP_SHARED, true);
            updateShareContent();
        }
    }

    private void updateShareContent() {
        if(BusinessPreferences.readBoolean(getContext(), ACTION_APP_SHARED, false)){
            rootView.findViewById(R.id.store_sub_discription).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.store_share_text).setVisibility(View.INVISIBLE);
        }else{
            rootView.findViewById(R.id.store_sub_discription).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.store_share_text).setVisibility(View.VISIBLE);
        }
    }

    private void startPurchaseRestore(){
        if(ConnectivityReceiver.isNetworkAvailable(getContext())){
            try {
                InAppHandler inAppHandler = new InAppHandler(getContext(), getActivity(), this);
                inAppHandler.startInAppFlow();
            }catch (Exception e){}
        }
    }

    private void updateInApp(){
        rootView.findViewById(R.id.store_buyButton).setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mUpdateInAppItemReceiver);
        super.onDestroy();
    }

    public void restorePurchase() {
        startPurchaseRestore();
    }

    @Override
    public void onErrorAppPurchase() {
        Toast.makeText(getContext(), getResources().getString(R.string.inapp_process_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccessAppPurchase() {
        updateStoreUI();
        Toast.makeText(getContext(), getResources().getString(R.string.inapp_process_success), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccessRestoreAppPurchase() {
        updateStoreUI();
        Toast.makeText(getContext(), getResources().getString(R.string.inapp_process_restore), Toast.LENGTH_SHORT).show();
    }
}
