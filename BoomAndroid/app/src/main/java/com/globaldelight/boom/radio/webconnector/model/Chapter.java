package com.globaldelight.boom.radio.webconnector.model;

import com.globaldelight.boom.collection.base.IMediaElement;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.playbackEvent.utils.MediaType;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * Created by adarsh on 07/06/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */

public class Chapter implements IMediaElement {

    @SerializedName("permalink")
    @Expose
    private String permalink;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("summary")
    @Expose
    private String summary;
    @SerializedName("smallLogo")
    @Expose
    private String smallLogo;
    @SerializedName("published")
    @Expose
    private String published;
    @SerializedName("duration")
    @Expose
    private Integer duration;
    @SerializedName("logo")
    @Expose
    private String logo;
    @SerializedName("podcastPermalink")
    @Expose
    private String podcastPermalink;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("podcast")
    @Expose
    private RadioStationResponse.Content podcast;

    public String getPermalink() {
        return permalink;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public String getSmallLogo() {
        return smallLogo;
    }

    public String getPublished() {
        return published;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getPodcastPermalink() {
        return podcastPermalink;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getId() {
        return getPermalink();
    }

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public String getDescription() {
        return getPublished();
    }

    @Override
    public String getItemArtUrl() {
        return getLogo();
    }

    @Override
    public void setItemArtUrl(String url) {

    }

    @Override
    public int getItemType() {
        return ItemType.CHAPTER;
    }

    @Override
    public int getMediaType() {
        return MediaType.PODCAST;
    }

    public RadioStationResponse.Content getPodcast() {
        return podcast;
    }

    public void setPodcast(RadioStationResponse.Content podcast) {
        this.podcast = podcast;
    }
}