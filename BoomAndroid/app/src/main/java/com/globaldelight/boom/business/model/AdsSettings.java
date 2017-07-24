
package com.globaldelight.boom.business.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdsSettings {

    @SerializedName("ad_source")
    @Expose
    private String adSource;
    @SerializedName("Video_Ad_song_count")
    @Expose
    private String videoAdSongCount;
    @SerializedName("screen_list")
    @Expose
    private ScreenList screenList;

    public String getAdSource() {
        return adSource;
    }

    public void setAdSource(String adSource) {
        this.adSource = adSource;
    }

    public String getVideoAdSongCount() {
        return videoAdSongCount;
    }

    public void setVideoAdSongCount(String videoAdSongCount) {
        this.videoAdSongCount = videoAdSongCount;
    }

    public ScreenList getScreenList() {
        return screenList;
    }

    public void setScreenList(ScreenList screenList) {
        this.screenList = screenList;
    }

}
