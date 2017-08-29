package com.globaldelight.boom.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.globaldelight.boom.R;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Venkata N M on 2/8/2017.
 */

public class ConnectivityReceiver
        extends BroadcastReceiver {

    private ConnectivityReceiverListener connectivityReceiverListener;
    private static boolean isNWConnected = false;

    public ConnectivityReceiver(){}

    public ConnectivityReceiver(ConnectivityReceiverListener connectivityReceiverListener) {
        super();
        this.connectivityReceiverListener = connectivityReceiverListener;
    }

    @Override
    public void onReceive(Context context, Intent arg1) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null
                && activeNetwork.isConnected();

        if (connectivityReceiverListener != null && isNWConnected != isConnected) {
            connectivityReceiverListener.onNetworkConnectionChanged(isConnected);
        }
        isNWConnected = isConnected;
    }

    public static boolean isNetworkAvailable(Context context, boolean showToast) {
        if(null != context) {
            boolean isConnect;
            ConnectivityManager
                    cm = (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnect = activeNetwork != null
                    && activeNetwork.isConnected() && activeNetwork.isAvailable();
            if (!isConnect && showToast)
                Toast.makeText(context, context.getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            return isConnect;
        }else{
            return false;
        }
    }

    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged(boolean isConnected);
    }
}