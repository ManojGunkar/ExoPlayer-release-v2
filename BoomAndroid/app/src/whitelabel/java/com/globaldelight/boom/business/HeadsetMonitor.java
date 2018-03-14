package com.globaldelight.boom.business;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.globaldelight.boom.app.receivers.ConnectivityReceiver;
import com.globaldelight.boom.bluetooth.BluetoothConnection;
import com.globaldelight.boom.webapiconnector.Headset;

import java.io.IOException;
import java.util.List;
import android.os.Handler;

/**
 * Created by adarsh on 16/02/18.
 */

public class HeadsetMonitor {

    public static final String ACTION_HEADSET_CONNECTED = "headset_connected";
    public static final String ACTION_HEADSET_SEARCHING = "headset_searching";
    public static final String ACTION_HEADSET_DISCONNECTED = "headset_disconnected";

    private Context mContext;
    private BluetoothConnection mConnection;
    private ConnectivityReceiver mReceiver;
    private Handler mHandler = new Handler();

    private BluetoothConnection.Callback mConnectionCallback = new BluetoothConnection.Callback() {
        @Override
        public void onHeadsetConnected() {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_HEADSET_CONNECTED));
        }

        @Override
        public void onHeadsetDisconnected() {
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_HEADSET_DISCONNECTED));
        }
    };


    private HeadsetConfiguration.Callback mConfigCallback = new HeadsetConfiguration.Callback() {
        @Override
        public void onSuccess(List<Headset> headphones) {
            if ( mConnection != null ) {
                mConnection.unregisterReceiver();
                mConnection = null;
            }

            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_HEADSET_SEARCHING));
            mConnection = new BluetoothConnection(mContext, headphones);
            mConnection.initBluetoothReceiver(mConnectionCallback);

        }

        @Override
        public void onFaiure() {
            if ( mConnection == null ) {
                retry();
            }
        }
    };

    private static HeadsetMonitor instance = null;

    public static HeadsetMonitor getInstance(Context context) {
        if ( instance == null ) {
            instance = new HeadsetMonitor(context.getApplicationContext());
        }

        return instance;
    }

    private HeadsetMonitor(Context context) {
        mContext = context;
    }

    public void start() {
        search();
    }


    public void Stop() {

    }


    private void search() {
        HeadsetConfiguration config = new HeadsetConfiguration(mContext);
        config.fetchHeadsets(mConfigCallback);
    }


    // Throws an IOException if doesn't have a headset list
    public Headset getConnectedHeadset() throws IOException {
        if ( mConnection == null ) throw new IOException("Network error!");

        return mConnection.getConnectedHeadset();
    }


    private void retry() {
        if ( ConnectivityReceiver.isNetworkAvailable(mContext, false) ) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if ( mConnection == null ) {
                        start();
                    }
                }
            }, 5000);
            // retry after some time.
        }
        else {
            mReceiver = new ConnectivityReceiver(mContext, new ConnectivityReceiver.ConnectivityReceiverListener() {
                @Override
                public void onNetworkConnectionChanged(boolean isConnected) {
                    if ( isConnected && mConnection == null) {
                        start();
                        mReceiver.unregister();
                    }
                }
            });
        }
    }
}
