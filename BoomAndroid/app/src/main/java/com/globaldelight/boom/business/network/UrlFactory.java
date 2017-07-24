package com.globaldelight.boom.business.network;


/**
 * Created by Venkata N M on 1/12/2017.
 */
public class UrlFactory {

    public static String getAddsConfigData() {
        return "https://apimboom.globaldelight.net/config/";
    }

    public static String getAccesTokenUrl() {
        return "https://apimboom.globaldelight.net/appauthentication/";
    }

    public static String postRegisterDeviceData() {
        return "https://apimboom.globaldelight.net/register/";
    }
    public static String postSaveEmailAddress() {
        return "https://apimboom.globaldelight.net/saveuseremail/";
    }


}
