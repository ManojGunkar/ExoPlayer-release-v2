package com.globaldelight.boom.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.globaldelight.boom.App;
import com.globaldelight.boom.task.PlayerService;

public class SleepAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        sendMessagePlayerStop(context);
        // Put here YOUR code.
//        Logger.LOGD("alarm", "fired");

        wl.release();
    }

    public void sendMessagePlayerStop(Context mContext) {
        if(App.getPlayerEventHandler().isPlaying())
            mContext.sendBroadcast(new Intent(PlayerService.ACTION_PLAY_PAUSE_SONG));
    }
}