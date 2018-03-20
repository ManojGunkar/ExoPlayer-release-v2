package com.globaldelight.boom.webapiconnector;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Manoj Kumar on 12-02-2018.
 */

public class RequestBody {

    @SerializedName("appid")
    @Expose
    private String appid;
    @SerializedName("deviceid")
    @Expose
    private String deviceid;
    @SerializedName("model")
    @Expose
    private String model;
    @SerializedName("build")
    @Expose
    private String build;
    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("vendor")
    @Expose
    private String vendor;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("locale")
    @Expose
    private String locale;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
