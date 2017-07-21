package com.globaldelight.boom.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.globaldelight.boom.utils.Utils;

/**
 * Created by Rahul Agarwal on 02-02-17.
 */

public class BusinessRequestReceiver extends BroadcastReceiver {

    public static final String ACTION_BUSINESS_CONFIGURATION = "ACTION_BUSINESS_CONFIGURATION";

    public static final String ACTION_BUSINESS_APP_EXPIRE = "ACTION_BUSINESS_APP_EXPIRE";

    private Handler postBusinessRequest;
    private static IUpdateBusinessRequest requestReceiverListener;

    public BusinessRequestReceiver(IUpdateBusinessRequest requestReceiverListener){
        this.requestReceiverListener = requestReceiverListener;
        postBusinessRequest = new Handler();
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        switch (intent.getAction()){
            case ACTION_BUSINESS_CONFIGURATION :

                break;
            case ACTION_BUSINESS_APP_EXPIRE :
                Utils.ExpirePopup(context);
                break;
        }
    }


    public interface IUpdateBusinessRequest{
    }
}
