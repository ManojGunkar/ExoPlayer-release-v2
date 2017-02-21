package com.globaldelight.boom.ui.musiclist.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.ui.widgets.RegularButton;

/**
 * Created by Rahul Agarwal on 08-02-17.
 */

public class AboutFragment extends Fragment {

    LinearLayout rootView;
    RegularButton rateButton;
    Activity mActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (LinearLayout) inflater.inflate(R.layout.fragment_about, container, false);
        mActivity = getActivity();
        initViews();
        return rootView;
    }

    private void initViews() {
        rateButton = (RegularButton) rootView.findViewById(R.id.btn_rate_app);
        rateButton.setTransformationMethod(null);
        rateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ABOUT_RATE_BUTTON_TAPPED);
            }
        });
        rateButton.setEnabled(false);
    }
}
