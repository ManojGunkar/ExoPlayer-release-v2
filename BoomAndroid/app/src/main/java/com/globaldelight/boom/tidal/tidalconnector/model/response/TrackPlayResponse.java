package com.globaldelight.boom.tidal.tidalconnector.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Manoj Kumar on 30-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TrackPlayResponse {

    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("trackId")
    @Expose
    private Integer trackId;
    @SerializedName("playTimeLeftInMinutes")
    @Expose
    private Integer playTimeLeftInMinutes;
    @SerializedName("soundQuality")
    @Expose
    private String soundQuality;
    @SerializedName("encryptionKey")
    @Expose
    private String encryptionKey;
    @SerializedName("codec")
    @Expose
    private String codec;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getTrackId() {
        return trackId;
    }

    public void setTrackId(Integer trackId) {
        this.trackId = trackId;
    }

    public Integer getPlayTimeLeftInMinutes() {
        return playTimeLeftInMinutes;
    }

    public void setPlayTimeLeftInMinutes(Integer playTimeLeftInMinutes) {
        this.playTimeLeftInMinutes = playTimeLeftInMinutes;
    }

    public String getSoundQuality() {
        return soundQuality;
    }

    public void setSoundQuality(String soundQuality) {
        this.soundQuality = soundQuality;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }
}
