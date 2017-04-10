package com.globaldelight.boom.manager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import com.globaldelight.boom.business.BusinessPreferences;
import com.globaldelight.boom.ui.musiclist.activity.MasterActivity;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boomplayer.AudioEffect;

import static com.globaldelight.boom.business.BusinessPreferences.ACTION_APP_SHARED;
import static com.globaldelight.boom.business.BusinessPreferences.ACTION_IN_APP_PURCHASE;

/**
 * Created by Rahul on 04-04-2017.
 */

public class BoomPlayTimeReceiver extends BroadcastReceiver {

    public static final String TIME_LIMIT_COMPLETE = "TIME_LIMIT_COMPLETE";

    private static int MAX_TIME_LIMIT_IN_SECOND = 0;

    public static final int SHOW_POPUP_NONE = 0;
    public static final int SHOW_POPUP_PRIMARY = 1;
    public static final int SHOW_POPUP_SECONDARY = 2;
    private static CountDownTimer countDownTimer;

    private static String LAST_TICK_TIME_IN_MILLI_SECOND = "LAST_TICK_TIME_IN_MILLI_SECOND";
    public static final String TIME_INTERVAL_FOR_POPUP = "TIME_INTERVAL_FOR_POPUP";
    public static final String SHOW_POPUP = "SHOW_POPUP";
    public static final String EFFECT_ON_AFTER_SECONDARY_POPUP = "EFFECT_ON_AFTER_SECONDARY_POPUP";

    private static Context mContext;
    private static Activity activityForPopup;

    private static final int SIXTY_MINUTE = 60 * 6;//60
    private static final int TWENTY_MINUTE = 60 * 2;//20
    private static final int FIVE_MINUTE = 60 * 1;//5
    private static final int ONE_SECOND = 1000 * 1;

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

    private static boolean isNoPopupShown(){
        if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_NONE)
            return true;
        return false;
    }

    private static boolean isPrimaryPopupShown(){
        if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_PRIMARY)
            return true;
        return false;
    }

    public static boolean isSecondaryPopupShown(){
        if(BusinessPreferences.readInteger(mContext, SHOW_POPUP, SHOW_POPUP_NONE) == SHOW_POPUP_SECONDARY)
            return true;
        return false;
    }

    private static boolean isEffectOnAfterSecondaryPopupShown(){
        return BusinessPreferences.readBoolean(mContext, EFFECT_ON_AFTER_SECONDARY_POPUP, false);
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
        if(isPurchased() || isShared()){
            return;
        } else if(isSecondaryPopupShown() && isEffectOnAfterSecondaryPopupShown()){
            return;
        } else if(isPrimaryPopupShown() || (isSecondaryPopupShown() && !isEffectOnAfterSecondaryPopupShown())){
            MAX_TIME_LIMIT_IN_SECOND = SIXTY_MINUTE;
        } else if(isNoPopupShown()){
            MAX_TIME_LIMIT_IN_SECOND = TWENTY_MINUTE;
        }

        if(playing && null == countDownTimer){
            setStartTimer();
        } else {
            if(null != countDownTimer){
                countDownTimer.cancel();
                countDownTimer = null;
            }
        }
    }

    public static void setActivityForPopup(MasterActivity activity) {
        activityForPopup = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == TIME_LIMIT_COMPLETE && null != activityForPopup){
            if(isNoPopupShown()){
//                showPrimaryPopup
                Utils.businessPrimaryPopup(activityForPopup);
            } else {
//                  showSecondaryPopup
                Utils.businessSecondaryPopup(activityForPopup);
            }
            BusinessPreferences.writeInteger(mContext, TIME_INTERVAL_FOR_POPUP, 0);
        }
    }

    public static void setStartTimer() {
        /*int hour = 0;
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

        long endTime = endDate.getTime() - date.getTime()*/

        long endTime = getRemainingTimeToShowPopup() * ONE_SECOND;
        countDownTimer = new CountDownTimer(endTime, ONE_SECOND) {

            public void onTick(long millisUntilFinished) {
                BusinessPreferences.writeInteger(mContext, TIME_INTERVAL_FOR_POPUP,
                        BusinessPreferences.readInteger(mContext, TIME_INTERVAL_FOR_POPUP, 0) + 1);
                BusinessPreferences.writeLong(mContext, LAST_TICK_TIME_IN_MILLI_SECOND, System.currentTimeMillis());
                if(getConditionToSwitchOffEffect() && BusinessPreferences.readInteger(mContext, TIME_INTERVAL_FOR_POPUP, 0) % FIVE_MINUTE == 0){
//                    disable effects
                    AudioEffect.getAudioEffectInstance(mContext).setEnableAudioEffect(false);
                }
            }

            public void onFinish() {
                if(isNoPopupShown()){
                    MAX_TIME_LIMIT_IN_SECOND = TWENTY_MINUTE;
                } else {
                    MAX_TIME_LIMIT_IN_SECOND = SIXTY_MINUTE;
                }
                mContext.sendBroadcast(new Intent(TIME_LIMIT_COMPLETE));
            }
        }.start();
    }

    public static void showPopupIfTimeIsOver(){
        if(getRemainingTimeToShowPopup() <= 0){
            setPlayingStartTime(true);
        }
    }
}