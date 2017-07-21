package com.globaldelight.boom.business.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Venkata N M on 1/11/2017.
 */

public class PostRegiObject {
    @SerializedName("appaccesstoken")
    @Expose
    private String appaccesstoken;
    @SerializedName("deviceid")
    @Expose
    private String deviceid;

    public PostRegiObject(String appaccesstoken, String deviceid, String country, String language, String build, String version, String model, String devicetoken, String arn, String OSVersion, String timeZoneOffset) {
        this.appaccesstoken = appaccesstoken;
        this.deviceid = deviceid;
        this.country = country;
        this.language = language;
        this.build = build;
        this.version = version;
        this.model = model;
        this.devicetoken = devicetoken;
        this.arn = arn;
        this.OSVersion = OSVersion;
        this.timeZoneOffset = timeZoneOffset;
    }

    public String getAppaccesstoken() {
        return appaccesstoken;

    }

    public void setAppaccesstoken(String appaccesstoken) {
        this.appaccesstoken = appaccesstoken;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDevicetoken() {
        return devicetoken;
    }

    public void setDevicetoken(String devicetoken) {
        this.devicetoken = devicetoken;
    }

    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    public String getOSVersion() {
        return OSVersion;
    }

    public void setOSVersion(String OSVersion) {
        this.OSVersion = OSVersion;
    }

    public String getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(String timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("language")
    @Expose
    private String language;
    @SerializedName("build")
    @Expose
    private String build;
    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("model")
    @Expose
    private String model;
    @SerializedName("devicetoken")
    @Expose
    private String devicetoken;
    @SerializedName("arn")
    @Expose
    private String arn;
    @SerializedName("OSVersion")
    @Expose
    private String OSVersion;
    @SerializedName("timeZoneOffset")
    @Expose
    private String timeZoneOffset;
}
