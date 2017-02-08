package com.globaldelight.boom.business;

import android.content.Context;
import android.provider.Settings;

import com.globaldelight.boom.business.model.ScreenList;

/**
 * Created by Rahul Agarwal on 01-02-17.
 */

public class BusinessUtils {

    public static final String apptype = "android";
    public static final String appid = "com.globaldelight.boom";
    public static final String country = "IN";
    public static final String secretkey = "e286b4b87f69a58aadbb8c38ecd6fbda7df398e8b712bd542488be9fba3a1b46";

    public static final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA23Sd0dwsy4kvvyCerRAJ2ViLzJSrqxjYyVtY4D0npAxTDzzszc0Kglyc/xe3BIIsSQ7HAKE9ByKHdDYjOrJ0dx837G0dXLs8CXFRD5Rthegi0uvd5JURfXqJbx176o1WzFQ4NK1aDbtD1BAsvrAHAUdohapUaQ3rpYJtZn+9x+GX8tOtjnSmedtJiLrJ8RE6TJWJAJ4Y9UQNLCq1AuNWqOt1K2DGbxRPBNuFmAn8IVvO2RyyxbjM4BawrfdK58UXy8U64MgwK+f4+XbzqNRUmW2rA0U3xU4wCaJe7qGbpGxiBIW6t1nMzd+9HZbfd+eF3Qhk0CLJWlZnlocfYRNXjwIDAQAB";
    public static final String SKU_INAPPITEM = "com.boom_test.12";


    private static int mExpireDays = 5;

    private static AddSource mAddSource = AddSource.google;

    private static boolean isAddEnable = false, shouldAskEmailPopupExpire = false;

    private static boolean isEffectBannerEnable, isEffectVideoEnable, isPlayerBannerEnable,
            isPlayerVideoEnable, isLibraryBannerEnable, isLibraryVideoEnable;
    private static long addsDisplayIntervals = 0;


    public static String getDeviceID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static void setAppExpireDays(int days) {
        mExpireDays = days;
    }

    public static int getAppExpireDays() {
        return mExpireDays ;
    }

    public static void setAddEnable(boolean enable){
        isAddEnable = enable;
    }

    public static boolean getAddEnable(){
        return isAddEnable;
    }

    public static void setAddSources(int addSources) {
        if(addSources == 1){
            mAddSource = AddSource.google;
        }else{
            mAddSource = AddSource.facebook;
        }
    }

    public static AddSource getAddSources() {
        return mAddSource;
    }

    public static void setScreenType(ScreenList screenType) {
        if(Integer.parseInt(screenType.getEffectScreen().getBannerAdDisplay()) == 1){
            isEffectBannerEnable = true;
        }else{
            isEffectBannerEnable = false;
        }

        if(Integer.parseInt(screenType.getEffectScreen().getVideoAdDisplay()) == 1){
            isEffectVideoEnable = true;
        }else{
            isEffectVideoEnable = false;
        }

        if(Integer.parseInt(screenType.getPlayerScreen().getBannerAdDisplay()) == 1){
            isPlayerBannerEnable = true;
        }else{
            isPlayerBannerEnable = false;
        }

        if(Integer.parseInt(screenType.getPlayerScreen().getVideoAdDisplay()) == 1){
            isPlayerVideoEnable = true;
        }else{
            isPlayerVideoEnable = false;
        }

        if(Integer.parseInt(screenType.getLibraryScreen().getBannerAdDisplay()) == 1){
            isLibraryBannerEnable = true;
        }else{
            isLibraryBannerEnable = false;
        }

        if(Integer.parseInt(screenType.getLibraryScreen().getVideoAdDisplay()) == 1){
            isLibraryVideoEnable = true;
        }else{
            isLibraryVideoEnable = false;
        }
    }

    public static boolean isEffectBannerEnable() {
        return isEffectBannerEnable;
    }

    public static boolean isEffectVideoEnable() {
        return isEffectVideoEnable;
    }

    public static boolean isPlayerBannerEnable() {
        return isPlayerBannerEnable;
    }

    public static boolean isLibraryVideoEnable() {
        return isLibraryVideoEnable;
    }

    public static boolean isLibraryBannerEnable() {
        return isLibraryBannerEnable;
    }

    public static boolean isPlayerVideoEnable() {
        return isPlayerVideoEnable;
    }

    public static void setAskEmailPopupExpire(boolean askPopupExpire) {
        shouldAskEmailPopupExpire = askPopupExpire;
    }

    public static boolean getAskEmailPopupExpire() {
        return shouldAskEmailPopupExpire ;
    }

    public static void setAddsDisplayIntervals(long addsDisplayIntervals) {
        BusinessUtils.addsDisplayIntervals = addsDisplayIntervals;
    }

    public static Long getAddsDisplayIntervals() {
        return addsDisplayIntervals ;
    }

    public enum AddSource{
        google,
        facebook,
    }

    public enum PlayerScreen{
        google_banner,
        google_full_screen,
        facebook_banner,
        facebook_full_screen,
    }

    public enum LibraryScreen{
        google_banner,
        google_full_screen,
        facebook_banner,
        facebook_full_screen,
    }

    public enum EmailSource{
        dropbox,
        googledrive,
        icloud,
    }
}