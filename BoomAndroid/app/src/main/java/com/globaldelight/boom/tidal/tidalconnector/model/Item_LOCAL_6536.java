package com.globaldelight.boom.tidal.tidalconnector.model;

import android.content.res.Resources;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.collection.base.IMediaItem;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.playbackEvent.utils.MediaType;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Manoj Kumar on 28-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class Item implements IMediaItem {

    public final static String IMAGE_BASE_URL = "https://resources.tidal.com/images/";
    @SerializedName("uuid")
    @Expose
    private String uuid;
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
    @SerializedName("album")
    @Expose
    private Album album;
    @SerializedName("image")
    @Expose
    private String image;


    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("picture")
    @Expose
    private String picture;


    public String getId() {
        return String.valueOf(id);
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


    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        switch (getItemType()) {
            default:
                int count = getNumberOfTracks() != null ? getNumberOfTracks() : 0;
                Resources res = App.getApplication().getResources();
                StringBuilder countStr = new StringBuilder();
                countStr.append(count > 1 ? res.getString(R.string.songs) : res.getString(R.string.song));
                countStr.append(" ");
                countStr.append(count);

                return countStr.toString();

            case ItemType.ALBUM:
            case ItemType.SONGS:
                if (getArtist() != null) {
                    return getArtist().getName();
                }
                return "";
        }
    }

    @Override
    public String getItemArtUrl() {
        String imageId = getImage();
        if (imageId != null) {
            return IMAGE_BASE_URL + imageId.replace("-", "/") + "/320x214.jpg";
        }

        if (getCover() != null) {
            imageId = getCover();
            return IMAGE_BASE_URL + imageId.replace("-", "/") + "/320x320.jpg";
        }

        if (getAlbum() != null && getAlbum().getCover() != null) {
            imageId = getAlbum().getCover();
            return IMAGE_BASE_URL + imageId.replace("-", "/") + "/320x320.jpg";
        }

        return "";
    }

    @Override
    public void setItemArtUrl(String url) {

    }

    @Override
    public int getItemType() {
        if (getType() == null) {
            return ItemType.SONGS;
        }

        switch (getType()) {
            case "ALBUM":
                return ItemType.ALBUM;
            case "ARTIST":
                return ItemType.ARTIST;
            case "EDITORIAL":
            case "PLAYLIST":
                return ItemType.PLAYLIST;
            default:
                return ItemType.SONGS;
        }
    }

    @Override
    public String getParentId() {
        return null;
    }

    @Override
    public String getParentTitle() {
        return null;
    }

    @Override
    public int getParentType() {
        return ItemType.ALBUM;
    }

    @Override
    public int getMediaType() {
        return MediaType.TIDAL;
    }


    @Override
    public String getItemDisplayName() {
        return getTitle();
    }

    @Override
    public String getItemAlbumId() {
        return (getAlbum() != null) ? String.valueOf(getAlbum().getId()) : null;
    }

    @Override
    public String getItemAlbum() {
        return (getAlbum() != null) ? getAlbum().getTitle() : null;
    }

    @Override
    public String getItemArtistId() {
        return (getArtist() != null) ? String.valueOf(getArtist().getId()) : null;
    }

    @Override
    public String getItemArtist() {
        return (getArtist() != null) ? getArtist().getName() : null;
    }

    @Override
    public long getDurationLong() {
        return duration.longValue() * 1000;
    }

    public String getDuration() {
        try {
            Long time = duration.longValue();
            long seconds = time / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;

            if (seconds < 10) {
                return String.valueOf(minutes) + ":0" + String.valueOf(seconds);
            } else {
                return String.valueOf(minutes) + ":" + String.valueOf(seconds);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return String.valueOf(0);
        }
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    @Override
    public long getDateAdded() {
        return 0;
    }

    @Override
    public String getItemUrl() {
        return null;
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

    public class Album {

        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("title")
        @Expose
        private String title;
        @SerializedName("cover")
        @Expose
        private String cover;

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

        public String getCover() {
            return cover;
        }

        public void setCover(String cover) {
            this.cover = cover;
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
