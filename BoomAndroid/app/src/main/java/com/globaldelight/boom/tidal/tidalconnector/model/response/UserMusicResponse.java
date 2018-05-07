package com.globaldelight.boom.tidal.tidalconnector.model.response;

import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Manoj Kumar on 07-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class UserMusicResponse {

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
    private List<UserItem> items = null;

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

    public List<UserItem> getItems() {
        return items;
    }

    public void setItems(List<UserItem> items) {
        this.items = items;
    }

    public class UserItem {

        @SerializedName("created")
        @Expose
        private String created;
        @SerializedName("item")
        @Expose
        private Item item;

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public Item getItem() {
            return item;
        }

        public void setItem(Item item) {
            this.item = item;
        }

    }

}
