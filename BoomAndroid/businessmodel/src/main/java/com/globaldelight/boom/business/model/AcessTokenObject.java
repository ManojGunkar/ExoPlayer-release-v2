package com.globaldelight.boom.business.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Venkata N M on 1/11/2017.
 */
public class AcessTokenObject {
    @SerializedName("appaccesstoken")
    @Expose
    private String appaccesstoken;


    public String getAppaccesstoken() {
        return appaccesstoken;
    }

    public void setAppaccesstoken(String networkStatusThresholdIndays) {
        this.appaccesstoken = networkStatusThresholdIndays;
    }
}

