
package com.globaldelight.boom.business.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ShareSettings {

    @SerializedName("Share_dialog_display_intervalinsec")
    @Expose
    private String shareDialogDisplayIntervalinsec;
    @SerializedName("Share_service_list")
    @Expose
    private List<String> shareServiceList = null;
    @SerializedName("Should_ask_email_after_expiry")
    @Expose
    private String shouldAskEmailAfterExpiry;
    @SerializedName("Free_usage_without_ads_days")
    @Expose
    private String freeUsageWithoutAdsDays;

    public String getShareDialogDisplayIntervalinsec() {
        return shareDialogDisplayIntervalinsec;
    }

    public void setShareDialogDisplayIntervalinsec(String shareDialogDisplayIntervalinsec) {
        this.shareDialogDisplayIntervalinsec = shareDialogDisplayIntervalinsec;
    }

    public List<String> getShareServiceList() {
        return shareServiceList;
    }

    public void setShareServiceList(List<String> shareServiceList) {
        this.shareServiceList = shareServiceList;
    }

    public String getShouldAskEmailAfterExpiry() {
        return shouldAskEmailAfterExpiry;
    }

    public void setShouldAskEmailAfterExpiry(String shouldAskEmailAfterExpiry) {
        this.shouldAskEmailAfterExpiry = shouldAskEmailAfterExpiry;
    }

    public String getFreeUsageWithoutAdsDays() {
        return freeUsageWithoutAdsDays;
    }

    public void setFreeUsageWithoutAdsDays(String freeUsageWithoutAdsDays) {
        this.freeUsageWithoutAdsDays = freeUsageWithoutAdsDays;
    }

}
