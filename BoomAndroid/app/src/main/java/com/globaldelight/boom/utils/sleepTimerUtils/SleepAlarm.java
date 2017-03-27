package com.globaldelight.boom.utils.sleepTimerUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.globaldelight.boom.App;
import com.globaldelight.boom.manager.PlayerServiceReceiver;
import com.globaldelight.boom.utils.handlers.Preferences;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_ITEM_CLICKED;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */

public class SleepAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        if(Preferences.getPreferences(context).getBoolean(Preferences.SLEEP_TIMER_ENABLED, false)) {
            TimerUtils.sendMessagePlayerStop(context);
            Preferences.writeBoolean(context, Preferences.SLEEP_TIMER_ENABLED, false);
        }
        wl.release();
    }
}