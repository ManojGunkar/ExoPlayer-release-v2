package com.globaldelight.boom.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;

import com.globaldelight.boom.business.BusinessPreferences;
import com.globaldelight.boom.utils.handlers.Preferences;
import com.globaldelight.boomplayer.AudioEffect;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.globaldelight.boom.business.BusinessPreferences.ACTION_APP_SHARED;
import static com.globaldelight.boom.business.BusinessPreferences.ACTION_IN_APP_PURCHASE;

/**
 * Created by Rahul on 04-04-2017.
 */

public class BoomPlayTimeReceiver extends BroadcastReceiver {

    public static final String TIME_LIMIT_COMPLETE = "TIME_LIMIT_COMPLETE";

    private static int MAX_TIME_LIMIT_IN_MINUTE = 0;

    private static final int SHOW_POPUP_NONE = 0;
    private static final int SHOW_POPUP_PRIMARY = 1;
    private static final int SHOW_POPUP_SECONDARY = 2;
    private static CountDownTimer countDownTimer;

    private static String LAST_TICK_TIME_IN_MILI_SECOND = "LAST_TICK_TIME_IN_MILI_SECOND";
    public static final String TIME_INTERVAL_FOR_POPUP = "TIME_INTERVAL_FOR_POPUP";
    public static final String SHOW_POPUP = "SHOW_POPUP";

    static Context mContext;

    public static void setPlayingStartTime(boolean playing) {
        if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_SECONDARY){
            return;
        } else if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_PRIMARY){
            MAX_TIME_LIMIT_IN_MINUTE = 60;
        } else if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_NONE){
            MAX_TIME_LIMIT_IN_MINUTE = 20;
        }
        if(playing){
            setTimer(mContext, getRemainingTimeToShowPopup());
        } else {
            if(null != countDownTimer){
                countDownTimer.cancel();
                countDownTimer = null;
            }
        }
    }

    private static int getRemainingTimeToShowPopup(){
        long remainingTimeForSecondaryPopup;
        int timeRemainToShowPopup = MAX_TIME_LIMIT_IN_MINUTE - BusinessPreferences.readInteger(mContext, TIME_INTERVAL_FOR_POPUP, 0);
        if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_PRIMARY){
            remainingTimeForSecondaryPopup = BusinessPreferences.readLong(mContext, LAST_TICK_TIME_IN_MILI_SECOND, 0)
                                            - System.currentTimeMillis();
            return (int) ((remainingTimeForSecondaryPopup/(1000 * 60)) - timeRemainToShowPopup);
        }
        return timeRemainToShowPopup;
    }

    public BoomPlayTimeReceiver(){}

    public BoomPlayTimeReceiver(Context context){
        this.mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == TIME_LIMIT_COMPLETE){
            if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_SECONDARY){
                return;
            } else if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_PRIMARY){
                //                showSecondaryPopup();
            } else if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_NONE){
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
                BusinessPreferences.writeInteger(mContext, TIME_INTERVAL_FOR_POPUP,
                        BusinessPreferences.readInteger(mContext, TIME_INTERVAL_FOR_POPUP, 0) + 1);
                BusinessPreferences.writeLong(mContext, LAST_TICK_TIME_IN_MILI_SECOND, System.currentTimeMillis());
                if(getConditionToSwitchOffEffect() && BusinessPreferences.readInteger(mContext, TIME_INTERVAL_FOR_POPUP, 0) % 5 == 0){
//                    disable effects
                    AudioEffect.getAudioEffectInstance(mContext).setEnableAudioEffect(false);
                }
            }

            public void onFinish() {
                if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_NONE){
                    BusinessPreferences.writeInteger(mContext, SHOW_POPUP, SHOW_POPUP_PRIMARY);
                    BusinessPreferences.writeInteger(mContext, TIME_INTERVAL_FOR_POPUP, 0);
                } else if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_PRIMARY) {
                    BusinessPreferences.writeInteger(mContext, SHOW_POPUP, SHOW_POPUP_SECONDARY);
                    BusinessPreferences.writeInteger(mContext, TIME_INTERVAL_FOR_POPUP, 0);
                }

                if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_SECONDARY){
                    return;
                } else if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_PRIMARY){
                    MAX_TIME_LIMIT_IN_MINUTE = 60;
                } else if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_NONE){
                    MAX_TIME_LIMIT_IN_MINUTE = 20;
                }
                mContext.sendBroadcast(new Intent(TIME_LIMIT_COMPLETE));
            }
        }.start();
    }

    public static boolean getConditionToSwitchOffEffect() {
        if (isShared() || isPurchased() ||
                BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_NONE)
            return false;
        return true;
    }

    public static boolean isShared() {
        // get Value from shared preferences
        return BusinessPreferences.readBoolean(mContext, ACTION_APP_SHARED, false);
    }

    public static boolean isPurchased() {
        // get Value from shared preferences
        return BusinessPreferences.readBoolean(mContext, ACTION_IN_APP_PURCHASE, false);
    }
}
