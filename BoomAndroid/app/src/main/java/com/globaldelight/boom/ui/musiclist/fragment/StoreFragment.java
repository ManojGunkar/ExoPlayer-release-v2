package com.globaldelight.boom.ui.musiclist.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.ui.widgets.RegularButton;
import com.globaldelight.boom.ui.widgets.RegularTextView;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class StoreFragment extends Fragment {

    ScrollView rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ScrollView) inflater.inflate(R.layout.fragment_store, container, false);
        initViews();
        return rootView;
    }

    private void initViews() {
        ImageView mStoreBanner= (ImageView) rootView.findViewById(R.id.store_banner);
        RegularTextView mStoreDescription = (RegularTextView) rootView.findViewById(R.id.store_discription);
        RegularTextView mStoreDescriptionFreeBoomin = (RegularTextView) rootView.findViewById(R.id.description_free_boomin);
        RegularTextView mStoreDescriptionBuy = (RegularTextView) rootView.findViewById(R.id.store_buy_desription);
        RegularTextView mStoreShareTxt = (RegularTextView) rootView.findViewById(R.id.store_share_text);
        RegularButton mStoreBuyBtn = (RegularButton) rootView.findViewById(R.id.store_buyButton);
    }
}
