package com.globaldelight.boom.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.globaldelight.boom.business.HeadsetConfiguration;
import com.globaldelight.boom.webapiconnector.Headset;
import com.globaldelight.boom.webapiconnector.ResponseBody;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Manoj Kumar on 16-01-2018.
 */

public class BluetoothConnection {
    private Context mContext;
    private BluetoothHeadset mHeadset;
    private List<Headset> mSupportedHeadsets;
    private Headset mConnectedHeadset = null;

    private final BluetoothProfile.ServiceListener mBluetoothServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            mHeadset = (BluetoothHeadset) proxy;
            checkHeadset();
        }

        @Override
        public void onServiceDisconnected(int profile) {
            mHeadset = null;
            mConnectedHeadset = null;
            mCallback.onHeadsetDisconnected();
        }
    };

    private Callback mCallback;
    private final BroadcastReceiver mConnectedDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                    int headsetState = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, -1);
                    if (headsetState == BluetoothHeadset.STATE_CONNECTED ) {
                        checkHeadset();
                        if ( mConnectedHeadset != null ) {
                            mCallback.onHeadsetConnected();
                        }
                    } else if (headsetState == BluetoothHeadset.STATE_DISCONNECTED) {
                        mConnectedHeadset = null;
                        mCallback.onHeadsetDisconnected();
                    }
                    break;
            }

        }
    };

    public BluetoothConnection(Context context, List<Headset> headsets) {
        this.mContext = context;
        mSupportedHeadsets = new ArrayList<Headset>();
        mSupportedHeadsets.addAll(headsets);
    }

    public Headset getConnectedHeadset() {
        return mConnectedHeadset;
    }


    public void initBluetoothReceiver(Callback callback) {
        mCallback = callback;
        registerReceiver();
    }


    public void unregisterReceiver() {
        BluetoothAdapter.getDefaultAdapter().closeProfileProxy(BluetoothProfile.HEADSET, mHeadset);
        mContext.unregisterReceiver(mConnectedDeviceReceiver);
        mHeadset = null;
    }

    private void registerReceiver() {
        BluetoothAdapter.getDefaultAdapter().getProfileProxy(mContext, mBluetoothServiceListener, BluetoothProfile.HEADSET);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        mContext.registerReceiver(mConnectedDeviceReceiver, filter);
    }


    private void checkHeadset() {
        List<BluetoothDevice> devices = mHeadset.getConnectedDevices();
        for (BluetoothDevice device : devices) {
            Headset hs = findHeadset(device.getName());
            if ( hs != null ) {
                mConnectedHeadset = hs;
                break;
            }
        }
    }

    private Headset findHeadset(String deviceName) {
        for ( Headset headset: mSupportedHeadsets ) {
            if ( deviceName.equals(headset.getName()) ) {
                return headset;
            }
        }
        return null;
    }

    public interface Callback {
        void onHeadsetConnected();
        void onHeadsetDisconnected();
    }
}
