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
import android.widget.ImageView;
import android.widget.ScrollView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.business.client.InAppHandler;
import com.globaldelight.boom.manager.ConnectivityReceiver;
import com.globaldelight.boom.ui.widgets.RegularButton;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.Utils;

import static com.globaldelight.boom.business.client.InAppHandler.ACTION_IN_APP_PURCHASE_SUCCESSFUL;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class StoreFragment extends Fragment implements View.OnClickListener{

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

        RegularTextView mStoreDescriptionFreeBoomin = (RegularTextView) rootView.findViewById(R.id.description_free_boomin);
        RegularTextView mStoreDescriptionBuy = (RegularTextView) rootView.findViewById(R.id.store_buy_desription);
        RegularTextView mStoreShareTxt = (RegularTextView) rootView.findViewById(R.id.store_share_text);

        mStoreShareTxt.setOnClickListener(this);
        RegularButton mStoreBuyBtn = (RegularButton) rootView.findViewById(R.id.store_buyButton);
        mStoreBuyBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.store_share_text:
                if(ConnectivityReceiver.isNetworkAvailable(getContext())) {
                try {
                    Utils.shareStart(getActivity());
                    (rootView.findViewById(R.id.store_discription)).setVisibility(View.GONE);
                }catch (Exception e){}
                }
                break;
            case R.id.store_buyButton:
                if(ConnectivityReceiver.isNetworkAvailable(getContext())){
                    try {
                        InAppHandler inAppHandler = new InAppHandler(getContext(), getActivity());
                        inAppHandler.startInAppFlow();
                    }catch (Exception e){}
                }
                break;
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
}
