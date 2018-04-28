package com.globaldelight.boom.tidal.tidalconnector.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Manoj Kumar on 28-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class Item {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("duration")
    @Expose
    private Integer duration;
    @SerializedName("streamReady")
    @Expose
    private Boolean streamReady;
    @SerializedName("streamStartDate")
    @Expose
    private String streamStartDate;
    @SerializedName("allowStreaming")
    @Expose
    private Boolean allowStreaming;
    @SerializedName("premiumStreamingOnly")
    @Expose
    private Boolean premiumStreamingOnly;
    @SerializedName("numberOfTracks")
    @Expose
    private Integer numberOfTracks;
    @SerializedName("numberOfVideos")
    @Expose
    private Integer numberOfVideos;
    @SerializedName("numberOfVolumes")
    @Expose
    private Integer numberOfVolumes;
    @SerializedName("releaseDate")
    @Expose
    private String releaseDate;
    @SerializedName("copyright")
    @Expose
    private String copyright;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("version")
    @Expose
    private Object version;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("cover")
    @Expose
    private String cover;
    @SerializedName("videoCover")
    @Expose
    private Object videoCover;
    @SerializedName("explicit")
    @Expose
    private Boolean explicit;
    @SerializedName("upc")
    @Expose
    private String upc;
    @SerializedName("popularity")
    @Expose
    private Integer popularity;
    @SerializedName("audioQuality")
    @Expose
    private String audioQuality;
    @SerializedName("artist")
    @Expose
    private Artist artist;
    @SerializedName("artists")
    @Expose
    private List<Artist_> artists = null;
    @SerializedName("creator")
    @Expose
    private Creator creator;

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Boolean getStreamReady() {
        return streamReady;
    }

    public void setStreamReady(Boolean streamReady) {
        this.streamReady = streamReady;
    }

    public String getStreamStartDate() {
        return streamStartDate;
    }

    public void setStreamStartDate(String streamStartDate) {
        this.streamStartDate = streamStartDate;
    }

    public Boolean getAllowStreaming() {
        return allowStreaming;
    }

    public void setAllowStreaming(Boolean allowStreaming) {
        this.allowStreaming = allowStreaming;
    }

    public Boolean getPremiumStreamingOnly() {
        return premiumStreamingOnly;
    }

    public void setPremiumStreamingOnly(Boolean premiumStreamingOnly) {
        this.premiumStreamingOnly = premiumStreamingOnly;
    }

    public Integer getNumberOfTracks() {
        return numberOfTracks;
    }

    public void setNumberOfTracks(Integer numberOfTracks) {
        this.numberOfTracks = numberOfTracks;
    }

    public Integer getNumberOfVideos() {
        return numberOfVideos;
    }

    public void setNumberOfVideos(Integer numberOfVideos) {
        this.numberOfVideos = numberOfVideos;
    }

    public Integer getNumberOfVolumes() {
        return numberOfVolumes;
    }

    public void setNumberOfVolumes(Integer numberOfVolumes) {
        this.numberOfVolumes = numberOfVolumes;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getVersion() {
        return version;
    }

    public void setVersion(Object version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public Object getVideoCover() {
        return videoCover;
    }

    public void setVideoCover(Object videoCover) {
        this.videoCover = videoCover;
    }

    public Boolean getExplicit() {
        return explicit;
    }

    public void setExplicit(Boolean explicit) {
        this.explicit = explicit;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public Integer getPopularity() {
        return popularity;
    }

    public void setPopularity(Integer popularity) {
        this.popularity = popularity;
    }

    public String getAudioQuality() {
        return audioQuality;
    }

    public void setAudioQuality(String audioQuality) {
        this.audioQuality = audioQuality;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public List<Artist_> getArtists() {
        return artists;
    }

    public void setArtists(List<Artist_> artists) {
        this.artists = artists;
    }

    public class Artist {

        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("type")
        @Expose
        private String type;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

    public class Artist_ {

        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("type")
        @Expose
        private String type;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

    public class Creator {

        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("type")
        @Expose
        private Object type;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getType() {
            return type;
        }

        public void setType(Object type) {
            this.type = type;
        }

    }


}
