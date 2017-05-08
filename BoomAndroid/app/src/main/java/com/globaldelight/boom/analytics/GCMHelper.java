package com.globaldelight.boom.analytics;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.globaldelight.boom.R;
import com.mixpanel.android.mpmetrics.GCMReceiver;

import java.security.InvalidParameterException;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Venkata N M on 4/18/2017.
 */

public class GCMHelper extends GCMReceiver {
    private static final String TAG = GCMHelper.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
        if (intent.getExtras().containsKey("mp_message")) {
            /*As per Business model Requirement need to handle the condition*/
            String mp_message = intent.getExtras().getString("mp_message");
        } else {
            showNotification(context, intent.getExtras());
        }

    }


    private void showNotification(Context context, Bundle extras) {
        String title = "BOOM";
        String content = "BOOM";
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_boom_about_icon)
                        .setContentTitle(title)
                        .setContentText(content);
        int mNotificationId = 1;
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mBuilder.setAutoCancel(true);
        mNotifyMgr.cancel(0);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }


}
