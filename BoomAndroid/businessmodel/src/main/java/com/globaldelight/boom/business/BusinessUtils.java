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

    public static final String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgiRZhXAbnXPjPhiuR3u6JsojGI8zmLk9YRma6j1Hc3uCXytO344tIcgHjwyNVDzMJ+U1ounor+A7ON6Uu7alb6+uuVqYgp68aA7GXg8OwHvqYJO0qzogQnPv3eyuDYtYq4EmMuc0PefCXrCdLQyUAS9bGCCianhyBknQVD8JPJZDT2mzjK73XgKT5BeWrmq1QEfWggaqXGXW+3g0DrWtC+u4BwljYrrcl3bX/KammReI/LIFKQIPb11nOrTsgG0ik2IrxaOOo0VTrDHn3Phk8Xg27/8Y7P4bAtSvQyF5U0u+vDoT6L6nKfZ4jEEwOk7XhasWL6pl7+oPzOR9NDCYEwIDAQAB";
    public static final String SKU_INAPPITEM = "com.globaldelight.boom_magicalsurroundsound1";
//    public static final String SKU_INAPPITEM = "android.test.purchased";


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
        return 1000 * addsDisplayIntervals ;
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
        library,
        cloud,
    }
}
