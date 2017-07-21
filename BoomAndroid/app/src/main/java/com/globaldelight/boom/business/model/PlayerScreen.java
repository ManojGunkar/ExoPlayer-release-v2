
package com.globaldelight.boom.business.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlayerScreen {

    @SerializedName("Banner_ad_display")
    @Expose
    private String bannerAdDisplay;
    @SerializedName("Video_ad_display")
    @Expose
    private String videoAdDisplay;

    public String getBannerAdDisplay() {
        return bannerAdDisplay;
    }

    public void setBannerAdDisplay(String bannerAdDisplay) {
        this.bannerAdDisplay = bannerAdDisplay;
    }

    public String getVideoAdDisplay() {
        return videoAdDisplay;
    }

    public void setVideoAdDisplay(String videoAdDisplay) {
        this.videoAdDisplay = videoAdDisplay;
    }

}
