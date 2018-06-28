package com.globaldelight.boom.spotify.apiconnector.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Manoj Kumar on 28-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class FeaturedPlaylists {

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("playlists")
    @Expose
    private Playlists playlists;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Playlists getPlaylists() {
        return playlists;
    }

    public void setPlaylists(Playlists playlists) {
        this.playlists = playlists;
    }


    public class Playlists {

        @SerializedName("href")
        @Expose
        private String href;
        @SerializedName("items")
        @Expose
        private List<Item> items = null;
        @SerializedName("limit")
        @Expose
        private Integer limit;
        @SerializedName("next")
        @Expose
        private Object next;
        @SerializedName("offset")
        @Expose
        private Integer offset;
        @SerializedName("previous")
        @Expose
        private Object previous;
        @SerializedName("total")
        @Expose
        private Integer total;

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public Object getNext() {
            return next;
        }

        public void setNext(Object next) {
            this.next = next;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public Object getPrevious() {
            return previous;
        }

        public void setPrevious(Object previous) {
            this.previous = previous;
        }

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

    }

}
