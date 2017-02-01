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

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        PostData postData = (PostData) o;
//
//        if (appid != null ? !appid.equals(postData.appid) : postData.appid != null) return false;
//        if (apptype != null ? !apptype.equals(postData.apptype) : postData.apptype != null)
//            return false;
//        if (country != null ? !country.equals(postData.country) : postData.country != null)
//            return false;
//        if (deviceid != null ? !deviceid.equals(postData.deviceid) : postData.deviceid != null)
//            return false;
//        if (locale != null ? !locale.equals(postData.locale) : postData.locale != null)
//            return false;
//        return secretkey != null ? secretkey.equals(postData.secretkey) : postData.secretkey == null;
//
//    }
//
//    @Override
//    public int hashCode() {
//        int result = appid != null ? appid.hashCode() : 0;
//        result = 31 * result + (apptype != null ? apptype.hashCode() : 0);
//        result = 31 * result + (country != null ? country.hashCode() : 0);
//        result = 31 * result + (deviceid != null ? deviceid.hashCode() : 0);
//        result = 31 * result + (locale != null ? locale.hashCode() : 0);
//        result = 31 * result + (secretkey != null ? secretkey.hashCode() : 0);
//        return result;
//    }

    public void setSecretkey(String secretkey) {

        this.secretkey = secretkey;
    }

    private String country;
    private String deviceid;

//    @Override
//    public String toString() {
//        return "PostData{" +
//                "appid='" + appid + '\'' +
//                ", apptype='" + apptype + '\'' +
//                ", country='" + country + '\'' +
//                ", deviceid='" + deviceid + '\'' +
//                ", locale='" + locale + '\'' +
//                ", secretkey='" + secretkey + '\'' +
//                '}';
//    }

    private String locale;
    private String secretkey;


}
