package com.globaldelight.boom.manager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;

import com.globaldelight.boom.App;
import com.globaldelight.boom.business.BusinessPreferences;
import com.globaldelight.boom.ui.musiclist.activity.MainActivity;
import com.globaldelight.boom.ui.musiclist.activity.MasterActivity;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boomplayer.AudioEffect;

import java.util.Date;

import static com.globaldelight.boom.business.BusinessPreferences.ACTION_APP_SHARED;
import static com.globaldelight.boom.business.BusinessPreferences.ACTION_IN_APP_PURCHASE;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_ON_SWITCH_OFF_AUDIO_EFFECT;

/**
 * Created by Rahul on 04-04-2017.
 */

public class BoomPlayTimeReceiver extends BroadcastReceiver {

    public static final String TIME_LIMIT_COMPLETE = "TIME_LIMIT_COMPLETE";
    private static boolean isDisable = false;

    private static int MAX_TIME_LIMIT_IN_SECOND = 0;

    public static final int SHOW_POPUP_NONE = 0;
    public static final int SHOW_POPUP_PRIMARY = 1;
    public static final int SHOW_POPUP_SECONDARY = 2;
    private static CountDownTimer countDownTimer;

    private static final String LAST_TICK_TIME_IN_MILLI_SECOND = "LAST_TICK_TIME_IN_MILLI_SECOND";
    public static final String TIME_INTERVAL_FOR_POPUP = "TIME_INTERVAL_FOR_POPUP";
    public static final String SHOW_POPUP = "SHOW_POPUP";
    public static final String EFFECT_ON_AFTER_SECONDARY_POPUP = "EFFECT_ON_AFTER_SECONDARY_POPUP";
    public static final String REMINDER_START_TIME = "REMINDER_START_TIME";

    private static Context mContext;
    private static Activity activityForPopup;
    private static boolean mIsPlaying = false;
    private static boolean mEffectsSwitchedOn = false;

    private static final int ONE_SECOND = 1000 * 1;
    private static final int PRIMARY_TRIAL_PERIOD = 60 * 2;
    private static final int SECONDARY_TRIAL_PERIOD = 60 * 1;
    private static final int REMINDER_PERIOD = 60 * 5;

    public BoomPlayTimeReceiver(){}

    public BoomPlayTimeReceiver(Context context){
        this.mContext = context;
    }

    public static boolean isShared() {
        // get Value from shared preferences
        return BusinessPreferences.readBoolean(mContext, ACTION_APP_SHARED, false);
    }

    public static boolean isPurchased() {
        // get Value from shared preferences
        return BusinessPreferences.readBoolean(mContext, ACTION_IN_APP_PURCHASE, false);
    }

    public static boolean isNoPopupShown(){
        if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_NONE)
            return true;
        return false;
    }

    public static boolean isPrimaryPopupShown(){
        if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_PRIMARY)
            return true;
        return false;
    }

    public static boolean isSecondaryPopupShown(){
        if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_SECONDARY)
            return true;
        return false;
    }

    private static int getRemainingTimeToShowPopup(){
        long remainingTimeForSecondaryPopup;
        int timeRemainToShowPopup = MAX_TIME_LIMIT_IN_SECOND - BusinessPreferences.readInteger(mContext, TIME_INTERVAL_FOR_POPUP, 0);
        if(isPrimaryPopupShown()){
            remainingTimeForSecondaryPopup = System.currentTimeMillis() - BusinessPreferences.readLong(mContext, LAST_TICK_TIME_IN_MILLI_SECOND, 0);
            return (int) ((remainingTimeForSecondaryPopup/1000) - timeRemainToShowPopup);
        }
        return timeRemainToShowPopup;
    }

    public static boolean getConditionToSwitchOffEffect() {
        if (isShared() || isPurchased() || isNoPopupShown())
            return false;
        return true;
    }

    public static void setPlayingStartTime(boolean playing) {
        if( isDisable || isPurchased() || isShared() ){
            return;
        }

        mEffectsSwitchedOn |= AudioEffect.getAudioEffectInstance(mContext).isAudioEffectOn();
        if ( !isNoPopupShown() && !mEffectsSwitchedOn ) {
            if ( shouldRemind() ) {
                notifyUser();
            }

            return;
        }


        mIsPlaying = playing;
        if(mIsPlaying && null == countDownTimer){
            setStartTimer();
        } else if( !mIsPlaying && null != countDownTimer){
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    public static void setActivityForPopup(Activity activity) {
        activityForPopup = activity;
    }

    public static Activity getActivityForPopup() {
        return activityForPopup;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == TIME_LIMIT_COMPLETE) {
            if ( isPurchased() || isShared() ) {
                return;
            }

            switchOffEffects();
            notifyUser();
        }
    }

    private static void notifyUser() {
        if(activityForPopup != null ){
            if(isNoPopupShown()){
                startPrimaryPopup();
                resetReminder();
            }
            else {
                if ( shouldRemind() ) {
                    startSecondaryPopup();
                    resetReminder();
                }
            }
            BusinessPreferences.writeInteger(mContext, TIME_INTERVAL_FOR_POPUP, 0);
        }
        else {
            // Very crude method of notifying the user
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    notifyUser();
                }
            }, 3000);
        }
    }

    public static void setStartTimer() {
        MAX_TIME_LIMIT_IN_SECOND = (isNoPopupShown())? PRIMARY_TRIAL_PERIOD : SECONDARY_TRIAL_PERIOD;

        long endTime = getRemainingTimeToShowPopup() * ONE_SECOND;
        if ( endTime < 0 ) {
            endTime = ONE_SECOND;
        }
        countDownTimer = new CountDownTimer(endTime, ONE_SECOND) {

            public void onTick(long millisUntilFinished) {
                BusinessPreferences.writeInteger(mContext, TIME_INTERVAL_FOR_POPUP,
                        BusinessPreferences.readInteger(mContext, TIME_INTERVAL_FOR_POPUP, 0) + 1);
                BusinessPreferences.writeLong(mContext, LAST_TICK_TIME_IN_MILLI_SECOND, System.currentTimeMillis());
            }

            public void onFinish() {
                MAX_TIME_LIMIT_IN_SECOND = isNoPopupShown()? PRIMARY_TRIAL_PERIOD : SECONDARY_TRIAL_PERIOD;
                mContext.sendBroadcast(new Intent(TIME_LIMIT_COMPLETE));
                countDownTimer = null;
            }
        }.start();
    }

    public static void onEffectStateChanged(boolean isEnabled){
        if ( isEnabled && getConditionToSwitchOffEffect() ) {
            mEffectsSwitchedOn = isEnabled;
            if ( mIsPlaying && countDownTimer == null ) {
                setStartTimer();
            }
        }
    }

    private static void switchOffEffects() {
        AudioEffect.getAudioEffectInstance(mContext).setEnableAudioEffect(false);
        App.getPlayerEventHandler().setEffectEnable(false);
        mContext.sendBroadcast(new Intent(ACTION_ON_SWITCH_OFF_AUDIO_EFFECT));
        mEffectsSwitchedOn = false;
    }


    private static boolean shouldRemind() {
        long currentTime = new Date().getTime();
        long startTime = BusinessPreferences.readLong(mContext, REMINDER_START_TIME, currentTime);
        return currentTime - startTime  > (REMINDER_PERIOD * 1000);
    }

    private static void resetReminder() {
        BusinessPreferences.writeLong(mContext, REMINDER_START_TIME, new Date().getTime());
    }

    private static void startPrimaryPopup() {
        if(null != activityForPopup){
            Utils.businessPrimaryPopup(activityForPopup);
        }
    }

    private static void startSecondaryPopup() {
        if(null != activityForPopup) {
            Utils.businessSecondaryPopup(activityForPopup);
        }
    }

}
