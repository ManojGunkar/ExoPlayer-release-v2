package com.globaldelight.boom.purchase;

import android.content.Context;

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
            if (remain_days <= 5) {
                remainingdays = 5 - (int) remain_days;
            }
            return remainingdays;


        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void validateTrialPeriod(Context ctx) {
        AudioEffect audioEffectPreferenceHandler;
        audioEffectPreferenceHandler = AudioEffect.getAudioEffectInstance(ctx);
        SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy");

        String currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        String installDate = Preferences.readString(ctx, Preferences.INSTALL_DATE, currentDate);
        try {
            Date date1 = myFormat.parse(installDate);
            Date date2 = myFormat.parse(currentDate);
            long diff = date2.getTime() - date1.getTime();
            System.out.println("Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));

            int purchaseType = audioEffectPreferenceHandler.getUserPurchaseType();

            if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) > 5 && purchaseType == AudioEffect.purchase.FIVE_DAY_OFFER.ordinal()) {
                audioEffectPreferenceHandler.setUserPurchaseType(AudioEffect.purchase.NORMAL_USER);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
