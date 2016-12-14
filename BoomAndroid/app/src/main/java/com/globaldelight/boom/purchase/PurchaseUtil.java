package com.globaldelight.boom.purchase;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.globaldelight.boom.purchase.api.BoomServerRequest;
import com.globaldelight.boom.utils.handlers.Preferences;
import com.globaldelight.boomplayer.AudioEffect;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by nidhin on 25/11/16.
 */

public class PurchaseUtil {
    public static final int INITIAL_OFFER_DAYS = 1;
    public static final int EXTEND_OFFER_DAYS = 0;

    public static int getRemainingDays(Context context) {
        SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy");

        String currentDate = myFormat.format(new Date());
        String installDate = Preferences.readString(context, Preferences.INSTALL_DATE, currentDate);

        try {
            Date date1 = myFormat.parse(installDate);
            Date date2 = myFormat.parse(currentDate);
            long diff = date2.getTime() - date1.getTime();
            System.out.println("Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));

            long remain_days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            int remainingdays = 0;
            if (remain_days <= INITIAL_OFFER_DAYS) {
                remainingdays = INITIAL_OFFER_DAYS - (int) remain_days;
            }
            return remainingdays;


        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static int getExtendedDays(Context context) {
        SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy");

        String currentDate = myFormat.format(new Date());
        String installDate = Preferences.readString(context, Preferences.EXTENDED_DATE, currentDate);

        try {
            Date date1 = myFormat.parse(installDate);
            Date date2 = myFormat.parse(currentDate);
            long diff = date2.getTime() - date1.getTime();
            System.out.println("Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));

            long remain_days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            int remainingdays = 0;
            if (remain_days <= EXTEND_OFFER_DAYS) {
                remainingdays = EXTEND_OFFER_DAYS - (int) remain_days;
            }
            return remainingdays;


        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean checkUserPurchase(final Context context) {
        AudioEffect audioEffectPreferenceHandler = AudioEffect.getAudioEffectInstance(context);
        int purchaseType = audioEffectPreferenceHandler.getUserPurchaseType();

        switch (AudioEffect.purchase.fromOrdinal(purchaseType)) {
            case PAID_USER:
                audioEffectPreferenceHandler.setMasterEffectControl(true);
                return true;
            case FIVE_DAY_OFFER:
                if (getRemainingDays(context) > 0) {
                    audioEffectPreferenceHandler.setMasterEffectControl(true);
                    audioEffectPreferenceHandler.setMasterEffectControl(true);

                    return true;
                } else {
                    audioEffectPreferenceHandler.setMasterEffectControl(false);
                    if (isNetworkAvailable(context)) {
                        new BoomServerRequest(context).showExtendInitialDialog();
                    }
                    return false;
                }


            case EXTENDED_FIVE_DAY_OFFER:
                if (getExtendedDays(context) > 0) {
                    audioEffectPreferenceHandler.setMasterEffectControl(true);

                    return true;
                } else {
                    audioEffectPreferenceHandler.setUserPurchaseType(AudioEffect.purchase.NORMAL_USER);
                    audioEffectPreferenceHandler.setMasterEffectControl(false);

                    return false;
                }


            case NORMAL_USER:
                audioEffectPreferenceHandler.setMasterEffectControl(false);

                return false;

        }
        return false;
    }

}
