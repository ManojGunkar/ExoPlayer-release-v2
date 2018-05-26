package com.globaldelight.boom.tidal.tidalconnector.model.response;

import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.tidal.tidalconnector.model.ItemCollection;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Manoj Kumar on 07-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class SearchResponse {

    @SerializedName("artists")
    @Expose
    private ItemCollection<Item> artists;
    @SerializedName("albums")
    @Expose
    private ItemCollection<Item> albums;
    @SerializedName("playlists")
    @Expose
    private ItemCollection<Item> playlists;
    @SerializedName("tracks")
    @Expose
    private ItemCollection<Item> tracks;
    @SerializedName("videos")
    @Expose

    private TopHit topHit;

    public ItemCollection<Item> getArtists() {
        return artists;
    }

    public void setArtists(ItemCollection<Item> artists) {
        this.artists = artists;
    }

    public ItemCollection<Item> getAlbums() {
        return albums;
    }

    public void setAlbums(ItemCollection<Item> albums) {
        this.albums = albums;
    }

    public ItemCollection<Item> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(ItemCollection<Item> playlists) {
        this.playlists = playlists;
    }

    public ItemCollection<Item> getTracks() {
        return tracks;
    }

    public void setTracks(ItemCollection<Item> tracks) {
        this.tracks = tracks;
    }

    public TopHit getTopHit() {
        return topHit;
    }

    public void setTopHit(TopHit topHit) {
        this.topHit = topHit;
    }

    public class TopHit {

        @SerializedName("value")
        @Expose
        private Value value;
        @SerializedName("type")
        @Expose
        private String type;

        public Value getValue() {
            return value;
        }

        public void setValue(Value value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

    public class Value {

        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("url")
        @Expose
        private String url;
        @SerializedName("picture")
        @Expose
        private String picture;
        @SerializedName("popularity")
        @Expose
        private Integer popularity;

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

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getPicture() {
            return picture;
        }

        public void setPicture(String picture) {
            this.picture = picture;
        }

        public Integer getPopularity() {
            return popularity;
        }

        public void setPopularity(Integer popularity) {
            this.popularity = popularity;
        }

    }
}
