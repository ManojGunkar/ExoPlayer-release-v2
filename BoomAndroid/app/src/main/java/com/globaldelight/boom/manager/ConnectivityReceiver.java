package com.globaldelight.boom.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.globaldelight.boom.R;

/**
 * Created by Venkata N M on 2/8/2017.
 */

public class ConnectivityReceiver
        extends BroadcastReceiver {

    public ConnectivityReceiverListener connectivityReceiverListener;

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
                && activeNetwork.isConnectedOrConnecting();

        if (connectivityReceiverListener != null) {
            connectivityReceiverListener.onNetworkConnectionChanged(isConnected);
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        boolean isConnect;
        ConnectivityManager
                cm = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnect = activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
        if(!isConnect){
            Toast.makeText(context, context.getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }
        return isConnect;
    }

    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged(boolean isConnected);
    }
}