package com.globaldelight.boom.business.model;

/**
 * Created by Venkata N M on 1/11/2017.
 */

public class PostData {
    private String appid;

    public PostData(String appid, String apptype, String country, String deviceid, String locale, String secretkey) {
        this.appid = appid;
        this.apptype = apptype;
        this.country = country;
        this.deviceid = deviceid;
        this.locale = locale;
        this.secretkey = secretkey;
    }

    public PostData(String appid, String apptype, String deviceid, String secretkey) {
        this.appid = appid;
        this.apptype = apptype;
        this.deviceid = deviceid;
        this.secretkey = secretkey;
    }

    private String apptype;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getApptype() {
        return apptype;
    }

    public void setApptype(String apptype) {
        this.apptype = apptype;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getSecretkey() {
        return secretkey;
    }

    public void setSecretkey(String secretkey) {

        this.secretkey = secretkey;
    }

    private String country;
    private String deviceid;

    private String locale;
    private String secretkey;


}
