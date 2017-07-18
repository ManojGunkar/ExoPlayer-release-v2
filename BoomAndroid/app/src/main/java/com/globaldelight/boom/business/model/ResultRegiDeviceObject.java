package com.globaldelight.boom.business.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Venkata N M on 1/11/2017.
 */
public class ResultRegiDeviceObject {
    @SerializedName("status")
    @Expose
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getReason() {
        return reason;
    }

    public void setReason(Integer reason) {
        this.reason = reason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @SerializedName("reason")
    @Expose
    private Integer reason;

    @SerializedName("message")
    @Expose
    private String message;
}

