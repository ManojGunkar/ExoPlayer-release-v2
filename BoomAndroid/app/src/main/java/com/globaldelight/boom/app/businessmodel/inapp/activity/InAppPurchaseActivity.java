package com.globaldelight.boom.app.businessmodel.inapp.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.fragments.StoreFragment;

/**
 * Created by Manoj Kumar on 7/13/2017.
 */

public class InAppPurchaseActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inapp_purchase_activity);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        StoreFragment storeFragment=new StoreFragment();
        transaction.replace(R.id.container, storeFragment,"storeFragment");
        transaction.addToBackStack(null);
        transaction.commit();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
