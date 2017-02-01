package com.globaldelight.boom.business.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Venkata N M on 1/11/2017.
 */

public class PostSaveEmailObject {
    @SerializedName("appaccesstoken")
    @Expose
    private String appaccesstoken;

    public PostSaveEmailObject(String appaccesstoken, String deviceid, boolean newsletteroptin, String source, String emailid) {
        this.appaccesstoken = appaccesstoken;
        this.deviceid = deviceid;
        this.newsletteroptin = newsletteroptin;
        this.source = source;
        this.emailid = emailid;
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

    public boolean isNewsletteroptin() {
        return newsletteroptin;
    }

    public void setNewsletteroptin(boolean newsletteroptin) {
        this.newsletteroptin = newsletteroptin;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getEmailid() {
        return emailid;
    }

    public void setEmailid(String emailid) {
        this.emailid = emailid;
    }

    @SerializedName("deviceid")
    @Expose
    private String deviceid;


    @SerializedName("newsletteroptin")
    @Expose
    private boolean newsletteroptin;


    @SerializedName("source")
    @Expose
    private String source;

    @SerializedName("emailid")
    @Expose
    private String emailid;

}
