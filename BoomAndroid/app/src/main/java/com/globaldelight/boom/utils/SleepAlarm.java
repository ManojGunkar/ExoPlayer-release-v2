package com.globaldelight.boom.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.globaldelight.boom.handler.PlayingQueue.PlayerEventHandler;

public class SleepAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
//        try {
//            PlayerEventHandler.getPlayerEventInstance(context).stop();
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        }
        // Put here YOUR code.
        Logger.LOGD("alarm", "fired");
        // Toast.makeText(context, "SleepAlarm !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example

        wl.release();
    }

    public void sendMessagePlayerStop(Context mContext) {
        Intent intent = new Intent();
        intent.setAction(PlayerSettings.ACTION_STOP_PLAYER);
        mContext.sendBroadcast(intent);

    }

}