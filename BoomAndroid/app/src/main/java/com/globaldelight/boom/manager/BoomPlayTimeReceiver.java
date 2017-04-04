package com.globaldelight.boom.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;

import com.globaldelight.boom.utils.handlers.Preferences;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Rahul on 04-04-2017.
 */

public class BoomPlayTimeReceiver extends BroadcastReceiver {
    public static final String TIME_LIMIT_COMPLETE = "TIME_LIMIT_COMPLETE";
    private static int MAX_TIME_LIMIT_IN_MINUTE = 0;
    private static final int SHOW_POPUP_NONE = 0;
    private static final int SHOW_POPUP_PRIMARY = 1;
    private static final int SHOW_POPUP_SECONDRY = 2;
    private static CountDownTimer countDownTimer;
    static Context mContext;

    public static final String TIME_INTERVAL_FOR_POPUP = "TIME_INTERVAL_FOR_POPUP";
    public static final String SHOW_POPUP = "SHOW_POPUP";

    public static void setPlayingStartTime(boolean playing) {
        if(Preferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_SECONDRY){
            return;
        } else if(Preferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_PRIMARY){
            MAX_TIME_LIMIT_IN_MINUTE = 60;
        } else if(Preferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_NONE){
            MAX_TIME_LIMIT_IN_MINUTE = 20;
        }
        if(playing){
            setTimer(mContext, MAX_TIME_LIMIT_IN_MINUTE - Preferences.readInteger(mContext, TIME_INTERVAL_FOR_POPUP, 0));
        } else {
            if(null != countDownTimer){
                countDownTimer.cancel();
                countDownTimer = null;
            }
        }
    }

    public BoomPlayTimeReceiver(){}
    public BoomPlayTimeReceiver(Context context){
        this.mContext = context;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == TIME_LIMIT_COMPLETE){
            if(Preferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_SECONDRY){
                return;
            } else if(Preferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_PRIMARY){
                //                showSecondryPopup();
            } else if(Preferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_NONE){
//                showPrimaryPopup();
            }
        }
    }

    public static void setTimer(final Context mContext, int time_in_minute) {
        int hour = 0;
        int minute = time_in_minute;

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");

        String currentDateTime = sdf.format(new Date(System.currentTimeMillis()));

        Date date = null;
        try {
            date = sdf.parse(currentDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, hour);
        calendar.add(Calendar.MINUTE, minute);
        Date endDate = calendar.getTime();

        long endTime = endDate.getTime() - date.getTime();

        countDownTimer = new CountDownTimer(endTime, 1000) {

            public void onTick(long millisUntilFinished) {
                Preferences.writeInteger(mContext, TIME_INTERVAL_FOR_POPUP, Preferences.readInteger(mContext, TIME_INTERVAL_FOR_POPUP, 0) + 1);
            }

            public void onFinish() {
                if(Preferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_NONE){
                    Preferences.writeInteger(mContext, SHOW_POPUP, SHOW_POPUP_PRIMARY);
                    Preferences.writeInteger(mContext, TIME_INTERVAL_FOR_POPUP, 0);
                } else if(Preferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_PRIMARY) {
                    Preferences.writeInteger(mContext, SHOW_POPUP, SHOW_POPUP_SECONDRY);
                    Preferences.writeInteger(mContext, TIME_INTERVAL_FOR_POPUP, 0);
                }

                if(Preferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_SECONDRY){
                    return;
                } else if(Preferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_PRIMARY){
                    MAX_TIME_LIMIT_IN_MINUTE = 60;
                } else if(Preferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_NONE){
                    MAX_TIME_LIMIT_IN_MINUTE = 20;
                }
                mContext.sendBroadcast(new Intent(TIME_LIMIT_COMPLETE));
            }
        }.start();
    }
}
