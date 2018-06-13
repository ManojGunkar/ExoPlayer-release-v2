package com.globaldelight.boom.login.api.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Manoj Kumar on 13-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class SocialRequestBody {

    @SerializedName("deviceid")
    @Expose
    private String deviceid;
    @SerializedName("appaccesstoken")
    @Expose
    private String appaccesstoken;
    @SerializedName("emailid")
    @Expose
    private String emailid;
    @SerializedName("source")
    @Expose
    private String source;
    @SerializedName("screen")
    @Expose
    private String screen;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("input_token")
    @Expose
    private String inputToken;
    @SerializedName("access_token")
    @Expose
    private String accessToken;
    @SerializedName("id_token")
    @Expose
    private String idToken;

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getAppaccesstoken() {
        return appaccesstoken;
    }

    public void setAppaccesstoken(String appaccesstoken) {
        this.appaccesstoken = appaccesstoken;
    }

    public String getEmailid() {
        return emailid;
    }

    public void setEmailid(String emailid) {
        this.emailid = emailid;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getInputToken() {
        return inputToken;
    }

    public void setInputToken(String inputToken) {
        this.inputToken = inputToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

}
