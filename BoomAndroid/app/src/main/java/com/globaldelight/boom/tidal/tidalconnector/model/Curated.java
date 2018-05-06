package com.globaldelight.boom.tidal.tidalconnector.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Manoj Kumar on 06-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class Curated {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("path")
    @Expose
    private String path;
    @SerializedName("hasPlaylists")
    @Expose
    private Boolean hasPlaylists;
    @SerializedName("hasArtists")
    @Expose
    private Boolean hasArtists;
    @SerializedName("hasAlbums")
    @Expose
    private Boolean hasAlbums;
    @SerializedName("hasTracks")
    @Expose
    private Boolean hasTracks;
    @SerializedName("hasVideos")
    @Expose
    private Boolean hasVideos;
    @SerializedName("image")
    @Expose
    private String image;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getHasPlaylists() {
        return hasPlaylists;
    }

    public void setHasPlaylists(Boolean hasPlaylists) {
        this.hasPlaylists = hasPlaylists;
    }

    public Boolean getHasArtists() {
        return hasArtists;
    }

    public void setHasArtists(Boolean hasArtists) {
        this.hasArtists = hasArtists;
    }

    public Boolean getHasAlbums() {
        return hasAlbums;
    }

    public void setHasAlbums(Boolean hasAlbums) {
        this.hasAlbums = hasAlbums;
    }

    public Boolean getHasTracks() {
        return hasTracks;
    }

    public void setHasTracks(Boolean hasTracks) {
        this.hasTracks = hasTracks;
    }

    public Boolean getHasVideos() {
        return hasVideos;
    }

    public void setHasVideos(Boolean hasVideos) {
        this.hasVideos = hasVideos;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
