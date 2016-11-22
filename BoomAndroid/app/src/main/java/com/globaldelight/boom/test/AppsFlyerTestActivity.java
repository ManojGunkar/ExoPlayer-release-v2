package com.globaldelight.boom.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AppsFlyerAnalyticHelper;

import java.util.HashMap;
import java.util.Map;

public class AppsFlyerTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_flyer_test);
    }

    public void testPurchase(View v) {
        Map<String, Object> eventValue = new HashMap<>();
        eventValue.put(AFInAppEventParameterName.REVENUE, 5);
        eventValue.put(AFInAppEventParameterName.CONTENT_TYPE, "boomcombo");
        eventValue.put(AFInAppEventParameterName.CONTENT_ID, "1234567");
        eventValue.put(AFInAppEventParameterName.CURRENCY, "USD");
        AppsFlyerAnalyticHelper.startTracking(this.getApplication());
        AppsFlyerAnalyticHelper.trackEvent(this.getApplication(), AFInAppEventType.PURCHASE, eventValue);
    }
}
