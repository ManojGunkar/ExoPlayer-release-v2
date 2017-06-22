package com.globaldelight.boom.app.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.AnalyticsHelper;
import com.globaldelight.boom.app.analytics.UtilAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.view.RegularButton;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class AboutFragment extends Fragment {

    View rootView;
    RegularButton rateButton;
    Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            mActivity = (Activity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_about, container, false);
        if(null == mActivity)
            mActivity = getActivity();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
    }
    @Override
    public void onStart() {
        super.onStart();
        FlurryAnalytics.getInstance(mActivity.getApplicationContext()).startSession();
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAnalytics.getInstance(mActivity.getApplicationContext()).endSession();

    }

    private void initViews() {
        rateButton = (RegularButton) rootView.findViewById(R.id.btn_rate_app);
        rateButton.setTransformationMethod(null);
        rateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                FlurryAnalyticHelper.logEvent(UtilAnalytics.About_Rate_Button_Tapped);
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.About_Rate_Button_Tapped);
                Uri uri = Uri.parse("market://details?id=" + mActivity.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + mActivity.getPackageName())));
                }
//                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ABOUT_RATE_BUTTON_TAPPED);
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.EVENT_ABOUT_RATE_BUTTON_TAPPED);

            }
        });
        rateButton.setEnabled(true);
    }
}
