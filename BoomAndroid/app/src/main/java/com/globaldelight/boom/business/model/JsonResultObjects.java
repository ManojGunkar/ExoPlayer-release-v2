package com.globaldelight.boom.business.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Venkata N M on 1/11/2017.
 */
public class JsonResultObjects {
    @SerializedName("NetworkStatusThreshold_indays")
    @Expose
    private String networkStatusThresholdIndays;
    @SerializedName("buisness_model_type")
    @Expose
    private Integer buisnessModelType;
    @SerializedName("ask_for_rate")
    @Expose
    private String askForRate;
    @SerializedName("extra_trial_songs_interval_insec")
    @Expose
    private String extraTrialSongsIntervalInsec;
    @SerializedName("ads_Settings")
    @Expose
    private AdsSettings adsSettings;
    @SerializedName("submitConfigForm")
    @Expose
    private String submitConfigForm;
    @SerializedName("extra_trial_songs")
    @Expose
    private String extraTrialSongs;
    @SerializedName("servay_interval_insec")
    @Expose
    private String servayIntervalInsec;
    @SerializedName("ads_anabled")
    @Expose
    private String adsAnabled;
    @SerializedName("notify_update")
    @Expose
    private String notifyUpdate;
    @SerializedName("Share_Settings")
    @Expose
    private ShareSettings shareSettings;
    @SerializedName("should_show_servay")
    @Expose
    private String shouldShowServay;
    @SerializedName("update_inteval_in_days")
    @Expose
    private String updateIntevalInDays;
    @SerializedName("trial_days")
    @Expose
    private String trialDays;
    @SerializedName("buy_prompt_insec")
    @Expose
    private String buyPromptInsec;
    @SerializedName("extra_trial_days")
    @Expose
    private String extraTrialDays;

    public String getNetworkStatusThresholdIndays() {
        return networkStatusThresholdIndays;
    }

    public void setNetworkStatusThresholdIndays(String networkStatusThresholdIndays) {
        this.networkStatusThresholdIndays = networkStatusThresholdIndays;
    }

    public Integer getBuisnessModelType() {
        return buisnessModelType;
    }

    public void setBuisnessModelType(Integer buisnessModelType) {
        this.buisnessModelType = buisnessModelType;
    }

    public String getAskForRate() {
        return askForRate;
    }

    public void setAskForRate(String askForRate) {
        this.askForRate = askForRate;
    }

    public String getExtraTrialSongsIntervalInsec() {
        return extraTrialSongsIntervalInsec;
    }

    public void setExtraTrialSongsIntervalInsec(String extraTrialSongsIntervalInsec) {
        this.extraTrialSongsIntervalInsec = extraTrialSongsIntervalInsec;
    }

    public AdsSettings getAdsSettings() {
        return adsSettings;
    }

    public void setAdsSettings(AdsSettings adsSettings) {
        this.adsSettings = adsSettings;
    }

    public String getSubmitConfigForm() {
        return submitConfigForm;
    }

    public void setSubmitConfigForm(String submitConfigForm) {
        this.submitConfigForm = submitConfigForm;
    }

    public String getExtraTrialSongs() {
        return extraTrialSongs;
    }

    public void setExtraTrialSongs(String extraTrialSongs) {
        this.extraTrialSongs = extraTrialSongs;
    }

    public String getServayIntervalInsec() {
        return servayIntervalInsec;
    }

    public void setServayIntervalInsec(String servayIntervalInsec) {
        this.servayIntervalInsec = servayIntervalInsec;
    }

    public String getAdsAnabled() {
        return adsAnabled;
    }

    public void setAdsAnabled(String adsAnabled) {
        this.adsAnabled = adsAnabled;
    }

    public String getNotifyUpdate() {
        return notifyUpdate;
    }

    public void setNotifyUpdate(String notifyUpdate) {
        this.notifyUpdate = notifyUpdate;
    }

    public ShareSettings getShareSettings() {
        return shareSettings;
    }

    public void setShareSettings(ShareSettings shareSettings) {
        this.shareSettings = shareSettings;
    }

    public String getShouldShowServay() {
        return shouldShowServay;
    }

    public void setShouldShowServay(String shouldShowServay) {
        this.shouldShowServay = shouldShowServay;
    }

    public String getUpdateIntevalInDays() {
        return updateIntevalInDays;
    }

    public void setUpdateIntevalInDays(String updateIntevalInDays) {
        this.updateIntevalInDays = updateIntevalInDays;
    }

    public String getTrialDays() {
        return trialDays;
    }

    public void setTrialDays(String trialDays) {
        this.trialDays = trialDays;
    }

    public String getBuyPromptInsec() {
        return buyPromptInsec;
    }

    public void setBuyPromptInsec(String buyPromptInsec) {
        this.buyPromptInsec = buyPromptInsec;
    }

    public String getExtraTrialDays() {
        return extraTrialDays;
    }

    public void setExtraTrialDays(String extraTrialDays) {
        this.extraTrialDays = extraTrialDays;
    }

}

