package com.globaldelight.boom.tidal.tidalconnector.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by adarsh on 04/05/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class TidalSubscriptionInfo {

    @SerializedName("validUntil")
    @Expose
    private String validUntil;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("subscription")
    @Expose
    private Subscription subscription;
    @SerializedName("highestSoundQuality")
    @Expose
    private String highestSoundQuality;
    @SerializedName("premiumAccess")
    @Expose
    private Boolean premiumAccess;
    @SerializedName("canGetTrial")
    @Expose
    private Boolean canGetTrial;
    @SerializedName("paymentType")
    @Expose
    private String paymentType;

    public String getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public String getHighestSoundQuality() {
        return highestSoundQuality;
    }

    public void setHighestSoundQuality(String highestSoundQuality) {
        this.highestSoundQuality = highestSoundQuality;
    }

    public Boolean getPremiumAccess() {
        return premiumAccess;
    }

    public void setPremiumAccess(Boolean premiumAccess) {
        this.premiumAccess = premiumAccess;
    }

    public Boolean getCanGetTrial() {
        return canGetTrial;
    }

    public void setCanGetTrial(Boolean canGetTrial) {
        this.canGetTrial = canGetTrial;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public class Subscription {
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("offlineGracePeriod")
        @Expose
        private Integer offlineGracePeriod;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getOfflineGracePeriod() {
            return offlineGracePeriod;
        }

        public void setOfflineGracePeriod(Integer offlineGracePeriod) {
            this.offlineGracePeriod = offlineGracePeriod;
        }
    }

}