package com.globaldelight.boom.tidal.tidalconnector.model.response;

import com.globaldelight.boom.tidal.tidalconnector.model.Item;
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
    private Artists artists;
    @SerializedName("albums")
    @Expose
    private Albums albums;
    @SerializedName("playlists")
    @Expose
    private Playlists playlists;
    @SerializedName("tracks")
    @Expose
    private Tracks tracks;
    @SerializedName("videos")
    @Expose

    private TopHit topHit;

    public Artists getArtists() {
        return artists;
    }

    public void setArtists(Artists artists) {
        this.artists = artists;
    }

    public Albums getAlbums() {
        return albums;
    }

    public void setAlbums(Albums albums) {
        this.albums = albums;
    }

    public Playlists getPlaylists() {
        return playlists;
    }

    public void setPlaylists(Playlists playlists) {
        this.playlists = playlists;
    }

    public Tracks getTracks() {
        return tracks;
    }

    public void setTracks(Tracks tracks) {
        this.tracks = tracks;
    }

    public TopHit getTopHit() {
        return topHit;
    }

    public void setTopHit(TopHit topHit) {
        this.topHit = topHit;
    }

    public class Albums {

        @SerializedName("limit")
        @Expose
        private Integer limit;
        @SerializedName("offset")
        @Expose
        private Integer offset;
        @SerializedName("totalNumberOfItems")
        @Expose
        private Integer totalNumberOfItems;
        @SerializedName("items")
        @Expose
        private List<Item> items = null;

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public Integer getTotalNumberOfItems() {
            return totalNumberOfItems;
        }

        public void setTotalNumberOfItems(Integer totalNumberOfItems) {
            this.totalNumberOfItems = totalNumberOfItems;
        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

    }

    public class Artists {

        @SerializedName("limit")
        @Expose
        private Integer limit;
        @SerializedName("offset")
        @Expose
        private Integer offset;
        @SerializedName("totalNumberOfItems")
        @Expose
        private Integer totalNumberOfItems;
        @SerializedName("items")
        @Expose
        private List<Item> items = null;

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public Integer getTotalNumberOfItems() {
            return totalNumberOfItems;
        }

        public void setTotalNumberOfItems(Integer totalNumberOfItems) {
            this.totalNumberOfItems = totalNumberOfItems;
        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

    }

    public class Playlists {

        @SerializedName("limit")
        @Expose
        private Integer limit;
        @SerializedName("offset")
        @Expose
        private Integer offset;
        @SerializedName("totalNumberOfItems")
        @Expose
        private Integer totalNumberOfItems;
        @SerializedName("items")
        @Expose
        private List<Item> items = null;

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public Integer getTotalNumberOfItems() {
            return totalNumberOfItems;
        }

        public void setTotalNumberOfItems(Integer totalNumberOfItems) {
            this.totalNumberOfItems = totalNumberOfItems;
        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

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

    public class Tracks {

        @SerializedName("limit")
        @Expose
        private Integer limit;
        @SerializedName("offset")
        @Expose
        private Integer offset;
        @SerializedName("totalNumberOfItems")
        @Expose
        private Integer totalNumberOfItems;
        @SerializedName("items")
        @Expose
        private List<Item> items = null;

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public Integer getTotalNumberOfItems() {
            return totalNumberOfItems;
        }

        public void setTotalNumberOfItems(Integer totalNumberOfItems) {
            this.totalNumberOfItems = totalNumberOfItems;
        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
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
