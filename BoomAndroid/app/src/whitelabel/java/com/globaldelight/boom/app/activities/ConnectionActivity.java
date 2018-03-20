package com.globaldelight.boom.app.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.business.HeadsetMonitor;
import com.globaldelight.boom.view.SearchingView;

import java.io.IOException;

/**
 * Created by Manoj Kumar on 15-02-2018.
 */

public class ConnectionActivity extends AppCompatActivity {

    private View layoutBluetooth;
    private View layoutHeadphoneSearching;
    private View layoutNoNetwork;
    private View layoutHeadphoneConnection;
    private View enableBluetooth;

    private TextView txtHeadphoneName;


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case HeadsetMonitor.ACTION_HEADSET_CONNECTED:
                    try {
                        onConnected(HeadsetMonitor.getInstance(ConnectionActivity.this).getConnectedHeadset().getName());
                    } catch (IOException e) {
                        onNetworkError();
                    }
                    break;

                case HeadsetMonitor.ACTION_HEADSET_DISCONNECTED:

                    break;

                case HeadsetMonitor.ACTION_HEADSET_SEARCHING:
                    if (isBluetoothEnable()) {
                        onSearching();
                    } else {
                        bluetoothTurnOn();
                    }
                    break;
            }
        }
    };

    private final BroadcastReceiver mBluetoothTurnOnOffReceiver = new BroadcastReceiver() {
        public void onReceive (Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF){
                    bluetoothTurnOn();
                }else {
                    onSearching();
                }
            }

        }

    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_connection_container);
        layoutBluetooth = findViewById(R.id.include_bluetooth_layout);
        layoutHeadphoneConnection = findViewById(R.id.include_heaphone_connection_layout);
        layoutHeadphoneSearching = findViewById(R.id.include_searching_headphone_layout);
        layoutNoNetwork = findViewById(R.id.include_network_connection_layout);

        enableBluetooth = findViewById(R.id.ll_turn_on_bluetooth);
        enableBluetooth.setOnClickListener(this::onTurnOnBluetooth);

        txtHeadphoneName=findViewById(R.id.txt_headphone_name);

        Button bluetoothSettingsBtn = findViewById(R.id.bluetooth_settings_button);
        bluetoothSettingsBtn.setOnClickListener(this::onGotoSettings);
    }


    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(mBluetoothTurnOnOffReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        IntentFilter filter = new IntentFilter();
        filter.addAction(HeadsetMonitor.ACTION_HEADSET_CONNECTED);
        filter.addAction(HeadsetMonitor.ACTION_HEADSET_SEARCHING);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
        checkHeadsetState();

    }


    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        unregisterReceiver(mBluetoothTurnOnOffReceiver);
        super.onStop();
    }


    private void checkHeadsetState() {
        try {
            if (HeadsetMonitor.getInstance(this).getConnectedHeadset() == null) {
                if (!isBluetoothEnable()){
                    bluetoothTurnOn();
                }else {
                    onSearching();
                }
            } else {
                onConnected(HeadsetMonitor.getInstance(this).getConnectedHeadset().getName());
            }
        } catch (IOException e) {
            onNetworkError();
        }
    }

    private void bluetoothTurnOn() {
        layoutNoNetwork.setVisibility(View.GONE);
        layoutHeadphoneSearching.setVisibility(View.GONE);
        layoutHeadphoneConnection.setVisibility(View.GONE);
        layoutBluetooth.setVisibility(View.VISIBLE);
    }

    private void onNetworkError() {
        layoutNoNetwork.setVisibility(View.VISIBLE);
        layoutHeadphoneSearching.setVisibility(View.GONE);
        layoutHeadphoneConnection.setVisibility(View.GONE);
        layoutBluetooth.setVisibility(View.GONE);
    }

    private void onSearching() {
        layoutNoNetwork.setVisibility(View.GONE);
        layoutHeadphoneSearching.setVisibility(View.VISIBLE);
        layoutHeadphoneConnection.setVisibility(View.GONE);
        layoutBluetooth.setVisibility(View.GONE);

        ((SearchingView) (findViewById(R.id.searching_headphones))).startRippleAnimation();
    }

    private void onConnected(String headsetName) {
        layoutNoNetwork.setVisibility(View.GONE);
        layoutHeadphoneSearching.setVisibility(View.GONE);
        layoutHeadphoneConnection.setVisibility(View.VISIBLE);
        layoutBluetooth.setVisibility(View.GONE);
        txtHeadphoneName.setText(headsetName);
        new Handler().postDelayed(this::finish,2000);
    }

    private boolean isBluetoothEnable() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter.isEnabled();
    }

    private void onTurnOnBluetooth(View v) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(enableBtIntent);
    }

    private void onGotoSettings(View view) {
        Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
